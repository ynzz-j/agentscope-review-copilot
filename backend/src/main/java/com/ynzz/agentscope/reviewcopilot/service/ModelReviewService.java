package com.ynzz.agentscope.reviewcopilot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ynzz.agentscope.reviewcopilot.factory.AgentFactory;
import com.ynzz.agentscope.reviewcopilot.model.ReviewCategory;
import com.ynzz.agentscope.reviewcopilot.model.ReviewConfidence;
import com.ynzz.agentscope.reviewcopilot.model.ReviewFinding;
import com.ynzz.agentscope.reviewcopilot.model.ReviewJob;
import com.ynzz.agentscope.reviewcopilot.model.ReviewSeverity;
import com.ynzz.agentscope.reviewcopilot.tool.GitDiffTool;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.Msg;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ModelReviewService {

    private static final int MAX_PROMPT_CHARS = 60_000;
    private static final int MAX_CONTEXT_CHARS_PER_FILE = 12_000;
    private static final int MAX_MODEL_FINDINGS = 20;
    private static final Duration MODEL_CALL_TIMEOUT = Duration.ofSeconds(120);

    private final AgentFactory agentFactory;
    private final ObjectMapper objectMapper;

    public ModelReviewService(AgentFactory agentFactory) {
        this.agentFactory = agentFactory;
        this.objectMapper = new ObjectMapper();
    }

    public ModelReviewResult review(
            ReviewJob job,
            GitDiffTool.GitDiffResult diff,
            Map<String, String> contexts,
            List<ReviewFinding> ruleFindings) {
        String prompt = buildPrompt(job, diff, contexts, ruleFindings);
        RuntimeContext runtimeContext = RuntimeContext.builder()
                .sessionId(job.sessionId())
                .build();

        try (ReActAgent agent = agentFactory.createModelOnlyReviewer(job.sessionId())) {
            Msg response = agent.call(prompt, runtimeContext).block(MODEL_CALL_TIMEOUT);
            String rawResponse = response == null ? "" : response.getTextContent();
            return parseResponse(rawResponse);
        }
    }

    private String buildPrompt(
            ReviewJob job,
            GitDiffTool.GitDiffResult diff,
            Map<String, String> contexts,
            List<ReviewFinding> ruleFindings) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("""
                请对下面的本地 Git diff 做代码评审。

                约束：
                - 只评审提供的 diff 和源码上下文。
                - 不要调用工具，不要修改源码。
                - 不要输出 Markdown，不要输出解释性前后缀。
                - 只输出一个 JSON 对象，字段为 summary 和 findings。
                - findings 最多 20 条，只保留有明确证据的问题。
                - category 只能使用：bug-risk, maintainability, concurrency, api-contract, test-gap, agent-boundary。
                - severity 只能使用：BLOCKER, HIGH, MEDIUM, LOW。
                - confidence 只能使用：HIGH, MEDIUM, LOW。

                JSON 示例：
                {
                  "summary": "一句话总结本次模型评审结论",
                  "findings": [
                    {
                      "severity": "HIGH",
                      "category": "bug-risk",
                      "file": "src/main/java/demo/DemoService.java",
                      "line": 42,
                      "evidence": "指出 diff 中的具体证据",
                      "impact": "说明可能造成的影响",
                      "suggestion": "给出可执行修复建议",
                      "confidence": "MEDIUM"
                    }
                  ]
                }

                """);
        prompt.append("任务 ID：").append(job.id()).append(System.lineSeparator());
        prompt.append("会话 ID：").append(job.sessionId()).append(System.lineSeparator());
        prompt.append("关注类型：").append(job.focusCategories()).append(System.lineSeparator());
        prompt.append(System.lineSeparator());

        prompt.append("规则检查结果（供参考，不要重复低价值问题）：")
                .append(System.lineSeparator())
                .append(renderRuleFindings(ruleFindings))
                .append(System.lineSeparator());

        prompt.append("Git diff：").append(System.lineSeparator())
                .append("```diff").append(System.lineSeparator())
                .append(truncate(diff.diffText(), MAX_PROMPT_CHARS / 2))
                .append(System.lineSeparator()).append("```").append(System.lineSeparator());

        prompt.append("源码上下文：").append(System.lineSeparator());
        contexts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> prompt.append("文件：").append(entry.getKey()).append(System.lineSeparator())
                        .append("```").append(System.lineSeparator())
                        .append(truncate(entry.getValue(), MAX_CONTEXT_CHARS_PER_FILE))
                        .append(System.lineSeparator()).append("```").append(System.lineSeparator()));

        return truncate(prompt.toString(), MAX_PROMPT_CHARS);
    }

    private String renderRuleFindings(List<ReviewFinding> ruleFindings) {
        if (ruleFindings == null || ruleFindings.isEmpty()) {
            return "无";
        }
        StringBuilder text = new StringBuilder();
        for (ReviewFinding finding : ruleFindings) {
            text.append("- ")
                    .append(finding.severity())
                    .append(" / ")
                    .append(finding.category().getCode())
                    .append(" / ")
                    .append(finding.file())
                    .append(": ")
                    .append(finding.evidence())
                    .append(System.lineSeparator());
        }
        return text.toString();
    }

    private ModelReviewResult parseResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            return new ModelReviewResult("", List.of(), rawResponse);
        }
        try {
            JsonNode root = objectMapper.readTree(extractJson(rawResponse));
            String summary = root.isObject() ? text(root, "summary", "") : "";
            JsonNode findingsNode = root.isArray() ? root : root.path("findings");
            if (!findingsNode.isArray()) {
                return new ModelReviewResult(summary, List.of(), rawResponse);
            }

            List<ReviewFinding> findings = new ArrayList<>();
            for (JsonNode findingNode : findingsNode) {
                ReviewFinding finding = toFinding(findingNode);
                if (finding != null) {
                    findings.add(finding);
                }
                if (findings.size() >= MAX_MODEL_FINDINGS) {
                    break;
                }
            }
            return new ModelReviewResult(summary, findings, rawResponse);
        } catch (Exception e) {
            return new ModelReviewResult("", List.of(), rawResponse);
        }
    }

    private ReviewFinding toFinding(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        String evidence = text(node, "evidence", "");
        String impact = text(node, "impact", "");
        String suggestion = text(node, "suggestion", "");
        if (evidence.isBlank() || impact.isBlank() || suggestion.isBlank()) {
            return null;
        }
        return new ReviewFinding(
                severity(text(node, "severity", "MEDIUM")),
                category(text(node, "category", "maintainability")),
                text(node, "file", "模型评审"),
                line(node.path("line")),
                evidence,
                impact,
                suggestion,
                confidence(text(node, "confidence", "MEDIUM")));
    }

    private ReviewSeverity severity(String value) {
        try {
            return ReviewSeverity.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            return ReviewSeverity.MEDIUM;
        }
    }

    private ReviewCategory category(String value) {
        try {
            return ReviewCategory.from(value);
        } catch (Exception e) {
            return ReviewCategory.MAINTAINABILITY;
        }
    }

    private ReviewConfidence confidence(String value) {
        try {
            return ReviewConfidence.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            return ReviewConfidence.MEDIUM;
        }
    }

    private Integer line(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.canConvertToInt()) {
            return node.asInt();
        }
        if (node.isTextual()) {
            try {
                return Integer.parseInt(node.asText().trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private String extractJson(String rawResponse) {
        String stripped = rawResponse.strip();
        int fenceStart = stripped.indexOf("```");
        if (fenceStart >= 0) {
            int contentStart = stripped.indexOf('\n', fenceStart);
            int fenceEnd = contentStart < 0 ? -1 : stripped.indexOf("```", contentStart + 1);
            if (contentStart >= 0 && fenceEnd > contentStart) {
                return stripped.substring(contentStart + 1, fenceEnd).strip();
            }
        }

        int objectStart = stripped.indexOf('{');
        int arrayStart = stripped.indexOf('[');
        if (objectStart < 0 && arrayStart < 0) {
            return stripped;
        }
        boolean useObject = objectStart >= 0 && (arrayStart < 0 || objectStart < arrayStart);
        int start = useObject ? objectStart : arrayStart;
        int end = useObject ? stripped.lastIndexOf('}') : stripped.lastIndexOf(']');
        if (end > start) {
            return stripped.substring(start, end + 1);
        }
        return stripped.substring(start);
    }

    private String text(JsonNode node, String field, String fallback) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull()) {
            return fallback;
        }
        String text = value.asText();
        return text == null || text.isBlank() ? fallback : text.trim();
    }

    private String truncate(String text, int maxChars) {
        if (text == null || text.length() <= maxChars) {
            return text == null ? "" : text;
        }
        return text.substring(0, maxChars) + System.lineSeparator() + "...已截断...";
    }
}

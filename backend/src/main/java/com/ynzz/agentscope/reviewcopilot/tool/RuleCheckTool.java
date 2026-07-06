package com.ynzz.agentscope.reviewcopilot.tool;

import com.ynzz.agentscope.reviewcopilot.model.ReviewCategory;
import com.ynzz.agentscope.reviewcopilot.model.ReviewConfidence;
import com.ynzz.agentscope.reviewcopilot.model.ReviewFinding;
import com.ynzz.agentscope.reviewcopilot.model.ReviewSeverity;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class RuleCheckTool {

    private static final Pattern EMPTY_CATCH =
            Pattern.compile("catch\\s*\\([^)]*(Exception|Throwable)[^)]*\\)\\s*\\{\\s*\\}", Pattern.DOTALL);
    private static final Pattern PRINT_STACK_TRACE =
            Pattern.compile("catch\\s*\\([^)]*(Exception|Throwable)[^)]*\\).*?\\.printStackTrace\\s*\\(", Pattern.DOTALL);
    private static final Pattern STATIC_MUTABLE =
            Pattern.compile("static\\s+(final\\s+)?(List|Map|Set|ArrayList|HashMap|HashSet)<", Pattern.MULTILINE);

    @Tool(name = "run_rule_checks", description = "对 Git diff 执行确定性的工程质量规则检查。", readOnly = true)
    public String runRuleChecks(
            @ToolParam(name = "diffText", description = "Git diff text") String diffText) {
        return "规则提示：已检查 " + (diffText == null ? 0 : diffText.lines().count()) + " 行 diff。";
    }

    public List<ReviewFinding> check(
            GitDiffTool.GitDiffResult diffResult,
            Map<String, String> fileContexts,
            List<ReviewCategory> focusCategories) {
        Set<ReviewCategory> enabled = focusCategories == null || focusCategories.isEmpty()
                ? EnumSet.allOf(ReviewCategory.class)
                : EnumSet.copyOf(focusCategories);
        List<ReviewFinding> findings = new ArrayList<>();

        if (enabled.contains(ReviewCategory.TEST_GAP)
                && hasMainJavaChange(diffResult.changedFiles())
                && diffResult.changedFiles().stream().noneMatch(path -> path.contains("src/test/"))) {
            findings.add(new ReviewFinding(
                    ReviewSeverity.MEDIUM,
                    ReviewCategory.TEST_GAP,
                    firstMainJavaFile(diffResult.changedFiles()),
                    null,
                    "本次 diff 修改了生产 Java 代码，但没有同步修改测试文件。",
                    "缺少就近的自动化检查会提高回归风险。",
                    "为变更的分支、API 或服务行为补充聚焦测试。",
                    ReviewConfidence.MEDIUM));
        }

        for (Map.Entry<String, String> entry : fileContexts.entrySet()) {
            String file = entry.getKey();
            String text = entry.getValue();

            if (enabled.contains(ReviewCategory.BUG_RISK)
                    && (EMPTY_CATCH.matcher(text).find() || PRINT_STACK_TRACE.matcher(text).find())) {
                findings.add(new ReviewFinding(
                        ReviewSeverity.HIGH,
                        ReviewCategory.BUG_RISK,
                        file,
                        firstLineContaining(text, "catch"),
                        "异常处理看起来只吞掉异常或仅打印堆栈。",
                        "调用方和生产诊断可能无法感知真实失败。",
                        "返回明确错误、重新抛出领域异常，或记录包含上下文的日志。",
                        ReviewConfidence.HIGH));
            }

            if (enabled.contains(ReviewCategory.CONCURRENCY) && STATIC_MUTABLE.matcher(text).find()) {
                findings.add(new ReviewFinding(
                        ReviewSeverity.MEDIUM,
                        ReviewCategory.CONCURRENCY,
                        file,
                        firstLineContaining(text, "static"),
                        "文件声明了静态可变集合。",
                        "并发请求下共享可变状态可能在会话或请求之间泄漏。",
                        "优先使用不可变常量、请求级状态，或线程安全的托管组件。",
                        ReviewConfidence.MEDIUM));
            }

            if (enabled.contains(ReviewCategory.API_CONTRACT)
                    && text.contains("@RestController")
                    && text.contains("@RequestBody")
                    && !text.contains("@Valid")) {
                findings.add(new ReviewFinding(
                        ReviewSeverity.MEDIUM,
                        ReviewCategory.API_CONTRACT,
                        file,
                        firstLineContaining(text, "@RequestBody"),
                        "接口接收 request body，但没有启用校验。",
                        "无效客户端输入可能进入业务逻辑，导致错误信息不清晰。",
                        "为请求 DTO 增加 `@Valid` 和显式校验注解。",
                        ReviewConfidence.MEDIUM));
            }

            if (enabled.contains(ReviewCategory.AGENT_BOUNDARY)
                    && text.contains("@Tool")
                    && text.contains("readOnly = false")
                    && !text.contains("Permission")) {
                findings.add(new ReviewFinding(
                        ReviewSeverity.HIGH,
                        ReviewCategory.AGENT_BOUNDARY,
                        file,
                        firstLineContaining(text, "@Tool"),
                        "声明了可写 AgentScope 工具，但没有可见的权限边界。",
                        "Agent 可能在评审流程未显式说明风险的情况下产生副作用。",
                        "通过明确的权限策略保护工具，或在入门项目中保持只读。",
                        ReviewConfidence.MEDIUM));
            }
        }

        if (enabled.contains(ReviewCategory.MAINTAINABILITY)
                && diffResult.diffText() != null
                && diffResult.diffText().lines().count() > 250) {
            findings.add(new ReviewFinding(
                    ReviewSeverity.LOW,
                    ReviewCategory.MAINTAINABILITY,
                    firstChangedFile(diffResult.changedFiles()),
                    null,
                    "本次 diff 较大，难以进行聚焦评审。",
                    "多个关注点混在一次变更中会增加评审成本和回归风险。",
                    "在可行时，将无关的行为变更、测试和重构拆成更小的提交。",
                    ReviewConfidence.LOW));
        }

        return findings;
    }

    private boolean hasMainJavaChange(List<String> files) {
        return files.stream().anyMatch(path -> path.startsWith("src/main/") && path.endsWith(".java"));
    }

    private String firstMainJavaFile(List<String> files) {
        return files.stream()
                .filter(path -> path.startsWith("src/main/") && path.endsWith(".java"))
                .findFirst()
                .orElse(firstChangedFile(files));
    }

    private String firstChangedFile(List<String> files) {
        return files == null || files.isEmpty() ? "" : files.get(0);
    }

    private Integer firstLineContaining(String text, String needle) {
        String[] lines = text.split("\\R", -1);
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains(needle)) {
                return i + 1;
            }
        }
        return null;
    }
}

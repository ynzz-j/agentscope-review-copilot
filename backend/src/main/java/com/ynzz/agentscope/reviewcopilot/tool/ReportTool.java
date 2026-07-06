package com.ynzz.agentscope.reviewcopilot.tool;

import com.ynzz.agentscope.reviewcopilot.model.ReviewFinding;
import com.ynzz.agentscope.reviewcopilot.model.ReviewJob;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class ReportTool {

    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    @Tool(name = "render_review_report", description = "渲染 Markdown 评审报告。", readOnly = false)
    public String renderReviewReport(
            @ToolParam(name = "jobId", description = "评审任务 ID") String jobId,
            @ToolParam(name = "summary", description = "报告摘要") String summary) {
        return "# 评审报告" + System.lineSeparator() + System.lineSeparator()
                + "任务：`" + jobId + "`" + System.lineSeparator() + System.lineSeparator()
                + summary;
    }

    public String renderMarkdown(ReviewJob job, String modelNote) {
        StringBuilder markdown = new StringBuilder();
        markdown.append("# AgentScope 代码评审报告").append(System.lineSeparator()).append(System.lineSeparator());
        markdown.append("- 任务：`").append(job.id()).append("`").append(System.lineSeparator());
        markdown.append("- 会话：`").append(job.sessionId()).append("`").append(System.lineSeparator());
        markdown.append("- 仓库：`").append(job.repoPath()).append("`").append(System.lineSeparator());
        markdown.append("- Diff 范围：`").append(job.diffMode()).append("`").append(System.lineSeparator());
        markdown.append("- 生成时间：").append(TS.format(java.time.Instant.now())).append(System.lineSeparator());
        markdown.append(System.lineSeparator());

        markdown.append("## 摘要").append(System.lineSeparator()).append(System.lineSeparator());
        markdown.append("共发现 **").append(job.findings().size()).append("** 条工程质量发现项。")
                .append(System.lineSeparator()).append(System.lineSeparator());
        if (modelNote != null && !modelNote.isBlank()) {
            markdown.append("> ").append(modelNote).append(System.lineSeparator()).append(System.lineSeparator());
        }

        markdown.append("## 发现项").append(System.lineSeparator()).append(System.lineSeparator());
        if (job.findings().isEmpty()) {
            markdown.append("本次 diff 未生成确定性规则发现项。")
                    .append(System.lineSeparator());
        }
        for (ReviewFinding finding : job.findings()) {
            markdown.append("### ")
                    .append(finding.severity())
                    .append(" · ")
                    .append(finding.category().getCode())
                    .append(" · `")
                    .append(finding.file())
                    .append("`")
                    .append(System.lineSeparator())
                    .append(System.lineSeparator());
            if (finding.line() != null) {
                markdown.append("- 行号：").append(finding.line()).append(System.lineSeparator());
            }
            markdown.append("- 证据：").append(finding.evidence()).append(System.lineSeparator());
            markdown.append("- 影响：").append(finding.impact()).append(System.lineSeparator());
            markdown.append("- 建议：").append(finding.suggestion()).append(System.lineSeparator());
            markdown.append("- 置信度：").append(finding.confidence()).append(System.lineSeparator());
            markdown.append(System.lineSeparator());
        }
        return markdown.toString();
    }
}

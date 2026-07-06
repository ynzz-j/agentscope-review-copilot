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

    @Tool(name = "render_review_report", description = "Render a Markdown review report.", readOnly = false)
    public String renderReviewReport(
            @ToolParam(name = "jobId", description = "Review job id") String jobId,
            @ToolParam(name = "summary", description = "Report summary") String summary) {
        return "# Review Report" + System.lineSeparator() + System.lineSeparator()
                + "Job: `" + jobId + "`" + System.lineSeparator() + System.lineSeparator()
                + summary;
    }

    public String renderMarkdown(ReviewJob job, String modelNote) {
        StringBuilder markdown = new StringBuilder();
        markdown.append("# AgentScope Review Report").append(System.lineSeparator()).append(System.lineSeparator());
        markdown.append("- Job: `").append(job.id()).append("`").append(System.lineSeparator());
        markdown.append("- Session: `").append(job.sessionId()).append("`").append(System.lineSeparator());
        markdown.append("- Repository: `").append(job.repoPath()).append("`").append(System.lineSeparator());
        markdown.append("- Diff mode: `").append(job.diffMode()).append("`").append(System.lineSeparator());
        markdown.append("- Generated at: ").append(TS.format(java.time.Instant.now())).append(System.lineSeparator());
        markdown.append(System.lineSeparator());

        markdown.append("## Summary").append(System.lineSeparator()).append(System.lineSeparator());
        markdown.append("Found **").append(job.findings().size()).append("** engineering-quality finding(s).")
                .append(System.lineSeparator()).append(System.lineSeparator());
        if (modelNote != null && !modelNote.isBlank()) {
            markdown.append("> ").append(modelNote).append(System.lineSeparator()).append(System.lineSeparator());
        }

        markdown.append("## Findings").append(System.lineSeparator()).append(System.lineSeparator());
        if (job.findings().isEmpty()) {
            markdown.append("No deterministic rule findings were generated for this diff.")
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
                markdown.append("- Line: ").append(finding.line()).append(System.lineSeparator());
            }
            markdown.append("- Evidence: ").append(finding.evidence()).append(System.lineSeparator());
            markdown.append("- Impact: ").append(finding.impact()).append(System.lineSeparator());
            markdown.append("- Suggestion: ").append(finding.suggestion()).append(System.lineSeparator());
            markdown.append("- Confidence: ").append(finding.confidence()).append(System.lineSeparator());
            markdown.append(System.lineSeparator());
        }
        return markdown.toString();
    }
}

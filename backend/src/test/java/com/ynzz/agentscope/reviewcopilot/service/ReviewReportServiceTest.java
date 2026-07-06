package com.ynzz.agentscope.reviewcopilot.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.ynzz.agentscope.reviewcopilot.config.ReviewCopilotProperties;
import com.ynzz.agentscope.reviewcopilot.model.DiffMode;
import com.ynzz.agentscope.reviewcopilot.model.ReviewCategory;
import com.ynzz.agentscope.reviewcopilot.model.ReviewConfidence;
import com.ynzz.agentscope.reviewcopilot.model.ReviewFinding;
import com.ynzz.agentscope.reviewcopilot.model.ReviewJob;
import com.ynzz.agentscope.reviewcopilot.model.ReviewReport;
import com.ynzz.agentscope.reviewcopilot.model.ReviewRequest;
import com.ynzz.agentscope.reviewcopilot.model.ReviewSeverity;
import com.ynzz.agentscope.reviewcopilot.permission.ReviewPathGuard;
import com.ynzz.agentscope.reviewcopilot.permission.ReviewPermissionPolicy;
import com.ynzz.agentscope.reviewcopilot.tool.ReportTool;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReviewReportServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void writesAndReadsMarkdownReportInsideConfiguredReportDirectory() {
        ReviewCopilotProperties properties = new ReviewCopilotProperties();
        properties.getStorage().setReportDir(tempDir.resolve("reports"));
        ReviewReportService service = new ReviewReportService(
                new ReviewPermissionPolicy(new ReviewPathGuard(properties)),
                new ReportTool());
        ReviewJob job = ReviewJob.created(
                        new ReviewRequest("D:/workspace/demo", DiffMode.WORKING_TREE, null, "session-a", List.of()),
                        "D:/workspace/demo")
                .running()
                .completed(List.of(finding()), null);

        ReviewReport report = service.generateAndSave(job, "Model provider is not configured.");
        String markdown = service.readMarkdown(job.id());

        assertThat(report.markdown()).isEqualTo(markdown);
        assertThat(markdown)
                .contains("# AgentScope Review Report")
                .contains("Model provider is not configured.")
                .contains("test-gap");
        assertThat(Files.exists(tempDir.resolve("reports").resolve(job.id() + ".md"))).isTrue();
    }

    private ReviewFinding finding() {
        return new ReviewFinding(
                ReviewSeverity.MEDIUM,
                ReviewCategory.TEST_GAP,
                "src/main/java/demo/DemoService.java",
                null,
                "Production code changed without a test.",
                "Regression risk is higher.",
                "Add a focused test.",
                ReviewConfidence.MEDIUM);
    }
}

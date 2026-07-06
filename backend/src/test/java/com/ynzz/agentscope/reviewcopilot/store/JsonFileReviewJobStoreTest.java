package com.ynzz.agentscope.reviewcopilot.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ynzz.agentscope.reviewcopilot.model.DiffMode;
import com.ynzz.agentscope.reviewcopilot.model.ReviewCategory;
import com.ynzz.agentscope.reviewcopilot.model.ReviewFinding;
import com.ynzz.agentscope.reviewcopilot.model.ReviewJob;
import com.ynzz.agentscope.reviewcopilot.model.ReviewRequest;
import com.ynzz.agentscope.reviewcopilot.model.ReviewSeverity;
import com.ynzz.agentscope.reviewcopilot.model.ReviewConfidence;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JsonFileReviewJobStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void persistsAndReloadsReviewJobAsUtf8Json() {
        JsonFileReviewJobStore store = new JsonFileReviewJobStore(tempDir.resolve("jobs"));
        ReviewJob job = ReviewJob.created(
                        new ReviewRequest("D:/workspace/demo", DiffMode.WORKING_TREE, null, "session-a", List.of()),
                        "D:/workspace/demo")
                .running()
                .completed(List.of(finding()), "data/reports/report.md");

        store.save(job);

        assertThat(store.findById(job.id()))
                .isPresent()
                .get()
                .satisfies(reloaded -> {
                    assertThat(reloaded.id()).isEqualTo(job.id());
                    assertThat(reloaded.sessionId()).isEqualTo("session-a");
                    assertThat(reloaded.findings()).hasSize(1);
                    assertThat(reloaded.reportPath()).isEqualTo("data/reports/report.md");
                });
        assertThat(tempDir.resolve("jobs").resolve(job.id() + ".json")).exists();
    }

    @Test
    void rejectsUnsafeReviewIds() {
        JsonFileReviewJobStore store = new JsonFileReviewJobStore(tempDir.resolve("jobs"));

        assertThatThrownBy(() -> store.findById("../outside"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("review id must contain only");
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

package com.ynzz.agentscope.reviewcopilot.tool;

import static org.assertj.core.api.Assertions.assertThat;

import com.ynzz.agentscope.reviewcopilot.model.ReviewCategory;
import com.ynzz.agentscope.reviewcopilot.model.ReviewFinding;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RuleCheckToolTest {

    private final RuleCheckTool tool = new RuleCheckTool();

    @Test
    void createsFindingsForProductionChangeWithoutTestAndSwallowedException() {
        GitDiffTool.GitDiffResult diff = new GitDiffTool.GitDiffResult(
                Path.of("."),
                "diff --git a/src/main/java/Demo.java b/src/main/java/Demo.java\n+new code",
                List.of("src/main/java/Demo.java"),
                0,
                1,
                0);
        Map<String, String> contexts = Map.of(
                "src/main/java/Demo.java",
                """
                public class Demo {
                  void run() {
                    try {
                      risky();
                    } catch (Exception e) {
                      e.printStackTrace();
                    }
                  }
                }
                """);

        List<ReviewFinding> findings = tool.check(diff, contexts, List.of());

        assertThat(findings).extracting(ReviewFinding::category)
                .contains(ReviewCategory.TEST_GAP, ReviewCategory.BUG_RISK);
    }

    @Test
    void honorsFocusedReviewCategories() {
        GitDiffTool.GitDiffResult diff = new GitDiffTool.GitDiffResult(
                Path.of("."),
                "diff --git a/src/main/java/Demo.java b/src/main/java/Demo.java\n+new code",
                List.of("src/main/java/Demo.java"),
                0,
                1,
                0);
        Map<String, String> contexts = Map.of(
                "src/main/java/Demo.java",
                """
                public class Demo {
                  private static Map<String, String> cache;

                  void run() {
                    try {
                      risky();
                    } catch (Exception e) {
                      e.printStackTrace();
                    }
                  }
                }
                """);

        List<ReviewFinding> findings = tool.check(diff, contexts, List.of(ReviewCategory.CONCURRENCY));

        assertThat(findings).extracting(ReviewFinding::category)
                .containsExactly(ReviewCategory.CONCURRENCY);
    }
}

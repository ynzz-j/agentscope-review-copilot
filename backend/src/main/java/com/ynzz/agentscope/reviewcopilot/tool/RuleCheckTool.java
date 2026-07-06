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

    @Tool(name = "run_rule_checks", description = "Run deterministic engineering quality checks over a Git diff.", readOnly = true)
    public String runRuleChecks(
            @ToolParam(name = "diffText", description = "Git diff text") String diffText) {
        return "Rule hints: " + (diffText == null ? 0 : diffText.lines().count()) + " diff lines inspected.";
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
                    "The diff changes production Java code, but no test file changed in this review.",
                    "Regression risk is higher because the changed behavior has no nearby automated check.",
                    "Add or update focused tests for the changed branch, API, or service behavior.",
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
                        "Exception handling appears to swallow or only print the exception.",
                        "Failures may be hidden from callers and production diagnostics.",
                        "Return a clear error, rethrow a domain exception, or log with enough context.",
                        ReviewConfidence.HIGH));
            }

            if (enabled.contains(ReviewCategory.CONCURRENCY) && STATIC_MUTABLE.matcher(text).find()) {
                findings.add(new ReviewFinding(
                        ReviewSeverity.MEDIUM,
                        ReviewCategory.CONCURRENCY,
                        file,
                        firstLineContaining(text, "static"),
                        "The file declares a static mutable collection.",
                        "Shared mutable state can leak between sessions or requests under concurrent traffic.",
                        "Prefer immutable constants, request-scoped state, or a thread-safe managed component.",
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
                        "A request body is accepted without validation.",
                        "Invalid client input may reach business logic and produce unclear errors.",
                        "Add `@Valid` and explicit validation annotations to the request DTO.",
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
                        "A write-capable AgentScope tool is declared without a visible permission boundary.",
                        "The agent may perform side effects without the review flow making that risk explicit.",
                        "Guard the tool through an explicit permission policy or keep it read-only in the intro project.",
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
                    "The diff is large enough to make a focused review difficult.",
                    "Mixed concerns in one change increase review cost and regression risk.",
                    "Split unrelated behavior changes, tests, and refactors into smaller commits where practical.",
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

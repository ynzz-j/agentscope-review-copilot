package com.ynzz.agentscope.reviewcopilot.tool;

import com.ynzz.agentscope.reviewcopilot.model.DiffMode;
import com.ynzz.agentscope.reviewcopilot.permission.ReviewPathGuard;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class GitDiffTool {

    private final ReviewPathGuard pathGuard;

    public GitDiffTool(ReviewPathGuard pathGuard) {
        this.pathGuard = pathGuard;
    }

    @Tool(name = "load_git_diff", description = "从本地仓库只读加载 Git diff。", readOnly = true)
    public String loadGitDiff(
            @ToolParam(name = "repoPath", description = "本地 Git 仓库路径") String repoPath,
            @ToolParam(name = "diffMode", description = "WORKING_TREE, STAGED, or BASE_REF") String diffMode,
            @ToolParam(name = "baseRef", description = "diffMode 为 BASE_REF 时使用的基准 ref") String baseRef) {
        return loadDiff(repoPath, DiffMode.from(diffMode), baseRef).diffText();
    }

    public GitDiffResult loadDiff(String repoPath, DiffMode diffMode, String baseRef) {
        Path repoRoot = pathGuard.resolveRepositoryRoot(repoPath);
        try {
            List<String> diffArgs = diffArguments(diffMode, baseRef, false);
            List<String> nameStatusArgs = diffArguments(diffMode, baseRef, true);
            String diffText = runGit(repoRoot, diffArgs);
            String nameStatus = runGit(repoRoot, nameStatusArgs);

            List<NameStatus> statuses = parseNameStatus(nameStatus);
            List<String> files = statuses.stream()
                    .map(NameStatus::path)
                    .filter(path -> path != null && !path.isBlank())
                    .distinct()
                    .sorted(Comparator.naturalOrder())
                    .toList();

            long added = statuses.stream().filter(entry -> entry.status().startsWith("A")).count();
            long modified = statuses.stream().filter(entry -> entry.status().startsWith("M")).count();
            long deleted = statuses.stream().filter(entry -> entry.status().startsWith("D")).count();

            return new GitDiffResult(
                    repoRoot,
                    diffText,
                    files,
                    (int) added,
                    (int) modified,
                    (int) deleted);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "读取 Git diff 失败：" + repoRoot + "：" + e.getMessage(), e);
        }
    }

    private List<String> diffArguments(DiffMode diffMode, String baseRef, boolean nameStatus) {
        List<String> args = new ArrayList<>();
        args.add("diff");
        if (nameStatus) {
            args.add("--name-status");
        }
        if (diffMode == DiffMode.STAGED) {
            args.add("--cached");
        }
        if (diffMode == DiffMode.BASE_REF) {
            String ref = baseRef == null || baseRef.isBlank() ? "HEAD" : baseRef.trim();
            if (ref.startsWith("-")) {
                throw new IllegalArgumentException("baseRef 不能以 '-' 开头：" + ref);
            }
            args.add(ref);
        }
        args.add("--");
        return args;
    }

    private String runGit(Path repoRoot, List<String> args) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add("git");
        command.add("-C");
        command.add(repoRoot.toString());
        command.addAll(args);

        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        boolean completed = process.waitFor(Duration.ofSeconds(15).toMillis(), TimeUnit.MILLISECONDS);
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (!completed) {
            process.destroyForcibly();
            throw new IllegalStateException("git 命令执行超时");
        }
        if (process.exitValue() != 0) {
            throw new IllegalStateException("git 命令执行失败：" + output.strip());
        }
        return output;
    }

    private List<NameStatus> parseNameStatus(String output) {
        if (output == null || output.isBlank()) {
            return List.of();
        }
        return output.lines()
                .map(line -> line.split("\\t"))
                .filter(parts -> parts.length >= 2)
                .map(parts -> {
                    String status = parts[0];
                    String path = parts.length >= 3 ? parts[2] : parts[1];
                    return new NameStatus(status, path);
                })
                .toList();
    }

    private record NameStatus(String status, String path) {}

    public record GitDiffResult(
            Path repoRoot,
            String diffText,
            List<String> changedFiles,
            int addedFiles,
            int modifiedFiles,
            int deletedFiles) {}
}

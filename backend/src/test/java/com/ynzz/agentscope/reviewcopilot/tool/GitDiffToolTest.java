package com.ynzz.agentscope.reviewcopilot.tool;

import static org.assertj.core.api.Assertions.assertThat;

import com.ynzz.agentscope.reviewcopilot.config.ReviewCopilotProperties;
import com.ynzz.agentscope.reviewcopilot.model.DiffMode;
import com.ynzz.agentscope.reviewcopilot.permission.ReviewPathGuard;
import java.nio.file.Files;
import java.nio.file.Path;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GitDiffToolTest {

    @TempDir
    Path tempDir;

    @Test
    void loadsWorkingTreeDiff() throws Exception {
        Path repo = tempDir.resolve("repo");
        Files.createDirectories(repo.resolve("src/main/java/demo"));
        try (Git git = Git.init().setDirectory(repo.toFile()).call()) {
            Path file = repo.resolve("src/main/java/demo/DemoService.java");
            Files.writeString(file, "package demo;\npublic class DemoService {}\n");
            git.add().addFilepattern(".").call();
            git.commit().setMessage("initial").setAuthor("Review Bot", "review@example.local").call();

            Files.writeString(file, "package demo;\npublic class DemoService { void run() {} }\n");
        }

        GitDiffTool tool = new GitDiffTool(new ReviewPathGuard(new ReviewCopilotProperties()));

        GitDiffTool.GitDiffResult result = tool.loadDiff(repo.toString(), DiffMode.WORKING_TREE, null);

        assertThat(result.changedFiles()).contains("src/main/java/demo/DemoService.java");
        assertThat(result.diffText()).contains("void run()");
    }
}

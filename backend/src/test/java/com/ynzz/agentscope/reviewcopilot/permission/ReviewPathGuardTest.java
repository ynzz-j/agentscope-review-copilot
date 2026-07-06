package com.ynzz.agentscope.reviewcopilot.permission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ynzz.agentscope.reviewcopilot.config.ReviewCopilotProperties;
import java.nio.file.Files;
import java.nio.file.Path;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReviewPathGuardTest {

    @TempDir
    Path tempDir;

    @Test
    void resolvesGitRootAndRejectsSensitiveFiles() throws Exception {
        Path repo = tempDir.resolve("repo");
        Files.createDirectories(repo);
        Git.init().setDirectory(repo.toFile()).call().close();
        Files.writeString(repo.resolve("README.md"), "demo");
        Files.writeString(repo.resolve(".env"), "secret=true");

        ReviewCopilotProperties properties = new ReviewCopilotProperties();
        properties.getStorage().setReportDir(tempDir.resolve("reports"));
        ReviewPathGuard guard = new ReviewPathGuard(properties);

        assertThat(guard.resolveRepositoryRoot(repo.toString())).isEqualTo(repo.toRealPath());
        assertThat(guard.resolveReadableFile(repo, "README.md")).isEqualTo(repo.resolve("README.md").normalize());
        assertThat(guard.resolveReportPath("review-001").startsWith(tempDir.resolve("reports"))).isTrue();
        assertThatThrownBy(() -> guard.resolveReadableFile(repo, ".env"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("敏感文件路径不可读取");
        assertThatThrownBy(() -> guard.resolveReadableFile(repo, "../outside.txt"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> guard.resolveReportPath("../outside"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("jobId 只能包含");
    }
}

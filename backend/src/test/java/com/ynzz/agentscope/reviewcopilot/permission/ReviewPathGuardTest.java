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

        ReviewPathGuard guard = new ReviewPathGuard(new ReviewCopilotProperties());

        assertThat(guard.resolveRepositoryRoot(repo.toString())).isEqualTo(repo.toRealPath());
        assertThat(guard.resolveReadableFile(repo, "README.md")).isEqualTo(repo.resolve("README.md").normalize());
        assertThatThrownBy(() -> guard.resolveReadableFile(repo, ".env"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sensitive");
        assertThatThrownBy(() -> guard.resolveReadableFile(repo, "../outside.txt"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

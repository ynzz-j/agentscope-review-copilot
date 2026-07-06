package com.ynzz.agentscope.reviewcopilot.tool;

import static org.assertj.core.api.Assertions.assertThat;

import com.ynzz.agentscope.reviewcopilot.config.ReviewCopilotProperties;
import com.ynzz.agentscope.reviewcopilot.permission.ReviewPathGuard;
import com.ynzz.agentscope.reviewcopilot.permission.ReviewPermissionPolicy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileContextToolTest {

    @TempDir
    Path tempDir;

    @Test
    void readsBoundedUtf8Context() throws Exception {
        ReviewCopilotProperties properties = new ReviewCopilotProperties();
        properties.getReview().setMaxContextLinesPerFile(20);
        FileContextTool tool = newTool(properties);

        Path repo = tempDir.resolve("repo");
        Files.createDirectories(repo.resolve("src/main/java/demo"));
        Path source = repo.resolve("src/main/java/demo/DemoService.java");
        Files.write(source, IntStream.rangeClosed(1, 25)
                .mapToObj(index -> "line " + index)
                .toList());

        String context = tool.loadContexts(repo, List.of("src/main/java/demo/DemoService.java"))
                .get("src/main/java/demo/DemoService.java");

        assertThat(context)
                .contains("line 1")
                .contains("line 20")
                .contains("[truncated after 20 lines]")
                .doesNotContain("line 21");
    }

    @Test
    void skipsSensitiveFilesInsteadOfReadingSecrets() throws Exception {
        FileContextTool tool = newTool(new ReviewCopilotProperties());

        Path repo = tempDir.resolve("repo");
        Files.createDirectories(repo);
        Files.writeString(repo.resolve(".env"), "SECRET=value");

        String context = tool.loadContexts(repo, List.of(".env")).get(".env");

        assertThat(context)
                .contains("[context skipped]")
                .contains("Sensitive file path is not readable")
                .doesNotContain("SECRET=value");
    }

    private FileContextTool newTool(ReviewCopilotProperties properties) {
        ReviewPathGuard guard = new ReviewPathGuard(properties);
        return new FileContextTool(new ReviewPermissionPolicy(guard), properties);
    }
}

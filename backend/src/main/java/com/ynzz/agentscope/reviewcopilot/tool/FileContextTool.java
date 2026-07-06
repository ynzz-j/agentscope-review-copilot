package com.ynzz.agentscope.reviewcopilot.tool;

import com.ynzz.agentscope.reviewcopilot.config.ReviewCopilotProperties;
import com.ynzz.agentscope.reviewcopilot.permission.ReviewPermissionPolicy;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class FileContextTool {

    private final ReviewPermissionPolicy permissionPolicy;
    private final ReviewCopilotProperties properties;

    public FileContextTool(ReviewPermissionPolicy permissionPolicy, ReviewCopilotProperties properties) {
        this.permissionPolicy = permissionPolicy;
        this.properties = properties;
    }

    @Tool(name = "read_file_context", description = "Read a bounded text context from a changed file.", readOnly = true)
    public String readFileContext(
            @ToolParam(name = "repoPath", description = "Repository root") String repoPath,
            @ToolParam(name = "relativePath", description = "File path relative to repository root") String relativePath) {
        Path repoRoot = Path.of(repoPath);
        Path file = permissionPolicy.requireReadableFile(repoRoot, relativePath);
        return readText(file);
    }

    public Map<String, String> loadContexts(Path repoRoot, List<String> changedFiles) {
        Map<String, String> contexts = new LinkedHashMap<>();
        for (String changedFile : changedFiles) {
            try {
                Path file = permissionPolicy.requireReadableFile(repoRoot, changedFile);
                if (Files.exists(file) && Files.isRegularFile(file)) {
                    contexts.put(changedFile, readText(file));
                }
            } catch (RuntimeException ignored) {
                contexts.put(changedFile, "[context skipped] " + ignored.getMessage());
            }
        }
        return contexts;
    }

    private String readText(Path file) {
        int maxLines = Math.max(20, properties.getReview().getMaxContextLinesPerFile());
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            if (lines.size() <= maxLines) {
                return String.join(System.lineSeparator(), lines);
            }
            return String.join(System.lineSeparator(), lines.subList(0, maxLines))
                    + System.lineSeparator()
                    + "[truncated after "
                    + maxLines
                    + " lines]";
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read file context: " + file, e);
        }
    }
}

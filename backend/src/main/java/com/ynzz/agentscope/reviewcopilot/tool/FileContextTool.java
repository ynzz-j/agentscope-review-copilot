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

    @Tool(name = "read_file_context", description = "读取变更文件的受限文本上下文。", readOnly = true)
    public String readFileContext(
            @ToolParam(name = "repoPath", description = "仓库根目录") String repoPath,
            @ToolParam(name = "relativePath", description = "相对于仓库根目录的文件路径") String relativePath) {
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
                contexts.put(changedFile, "[上下文已跳过] " + ignored.getMessage());
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
                    + "[已在 "
                    + maxLines
                    + " 行后截断]";
        } catch (IOException e) {
            throw new IllegalStateException("读取文件上下文失败：" + file, e);
        }
    }
}

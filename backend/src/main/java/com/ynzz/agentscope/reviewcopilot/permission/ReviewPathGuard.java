package com.ynzz.agentscope.reviewcopilot.permission;

import com.ynzz.agentscope.reviewcopilot.config.ReviewCopilotProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class ReviewPathGuard {

    private final ReviewCopilotProperties properties;

    public ReviewPathGuard(ReviewCopilotProperties properties) {
        this.properties = properties;
    }

    public Path resolveRepositoryRoot(String repoPath) {
        try {
            Path requested = Path.of(repoPath).toRealPath();
            FileRepositoryBuilder builder = new FileRepositoryBuilder()
                    .findGitDir(requested.toFile())
                    .readEnvironment();
            if (builder.getGitDir() == null) {
                throw new IllegalArgumentException("repoPath 不在 Git 工作区内：" + repoPath);
            }
            Path gitDir = builder.getGitDir().toPath().toRealPath();
            Path workTree = gitDir.getFileName().toString().equals(".git")
                    ? gitDir.getParent()
                    : gitDir;
            if (workTree == null || !Files.exists(workTree)) {
                throw new IllegalArgumentException("无法解析 Git 工作区：" + repoPath);
            }
            return workTree.toRealPath();
        } catch (IOException e) {
            throw new IllegalArgumentException("repoPath 不存在或不可读：" + repoPath, e);
        }
    }

    public Path resolveReadableFile(Path repoRoot, String relativePath) {
        try {
            if (relativePath == null || relativePath.isBlank() || relativePath.contains("..")) {
                throw new IllegalArgumentException("无效相对路径：" + relativePath);
            }
            if (isSensitive(relativePath)) {
                throw new IllegalArgumentException("敏感文件路径不可读取：" + relativePath);
            }
            Path root = repoRoot.toRealPath();
            Path target = root.resolve(relativePath).normalize();
            if (!target.startsWith(root)) {
                throw new IllegalArgumentException("路径越过仓库根目录：" + relativePath);
            }
            return target;
        } catch (IOException e) {
            throw new IllegalArgumentException("无法解析文件路径：" + relativePath, e);
        }
    }

    public Path resolveReportPath(String jobId) {
        try {
            String safeJobId = ReviewIdValidator.requireSafe(jobId, "jobId");
            Path reportDir = properties.getStorage().getReportDir().toAbsolutePath().normalize();
            Files.createDirectories(reportDir);
            Path target = reportDir.resolve(safeJobId + ".md").normalize();
            if (!target.startsWith(reportDir)) {
                throw new IllegalArgumentException("报告路径越过报告目录：" + jobId);
            }
            return target;
        } catch (IOException e) {
            throw new IllegalStateException("无法创建报告目录", e);
        }
    }

    public boolean isSensitive(String relativePath) {
        String normalized = relativePath.replace('\\', '/').toLowerCase(Locale.ROOT);
        String fileName = Path.of(normalized).getFileName() == null
                ? normalized
                : Path.of(normalized).getFileName().toString();
        if (properties.getReview().getSensitiveFileNames().stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(fileName::equals)) {
            return true;
        }
        return properties.getReview().getSensitiveDirectories().stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(dir -> normalized.contains("/" + dir + "/") || normalized.startsWith(dir + "/"));
    }
}

package com.ynzz.agentscope.reviewcopilot.permission;

import java.nio.file.Path;

public class ReviewPermissionPolicy {

    private final ReviewPathGuard pathGuard;

    public ReviewPermissionPolicy(ReviewPathGuard pathGuard) {
        this.pathGuard = pathGuard;
    }

    public Path requireReadableFile(Path repoRoot, String relativePath) {
        return pathGuard.resolveReadableFile(repoRoot, relativePath);
    }

    public Path requireReportOutput(String jobId) {
        return pathGuard.resolveReportPath(jobId);
    }
}

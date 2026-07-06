package com.ynzz.agentscope.reviewcopilot.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public record ReviewJob(
        String id,
        String sessionId,
        String repoPath,
        DiffMode diffMode,
        String baseRef,
        List<ReviewCategory> focusCategories,
        ReviewStatus status,
        Instant createdAt,
        Instant startedAt,
        Instant finishedAt,
        List<ReviewFinding> findings,
        String reportPath,
        String errorMessage) {

    private static final DateTimeFormatter ID_TS =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneId.systemDefault());

    public static ReviewJob created(ReviewRequest request, String repoRoot) {
        String id = "review-" + ID_TS.format(Instant.now()) + "-" + UUID.randomUUID().toString().substring(0, 8);
        return new ReviewJob(
                id,
                request.normalizedSessionId(),
                repoRoot,
                request.normalizedDiffMode(),
                request.baseRef(),
                request.normalizedFocusCategories(),
                ReviewStatus.CREATED,
                Instant.now(),
                null,
                null,
                List.of(),
                null,
                null);
    }

    public ReviewJob running() {
        return new ReviewJob(
                id, sessionId, repoPath, diffMode, baseRef, focusCategories, ReviewStatus.RUNNING,
                createdAt, Instant.now(), null, findings, reportPath, null);
    }

    public ReviewJob completed(List<ReviewFinding> nextFindings, String nextReportPath) {
        return new ReviewJob(
                id, sessionId, repoPath, diffMode, baseRef, focusCategories, ReviewStatus.COMPLETED,
                createdAt, startedAt, Instant.now(), List.copyOf(nextFindings), nextReportPath, null);
    }

    public ReviewJob failed(String message) {
        return new ReviewJob(
                id, sessionId, repoPath, diffMode, baseRef, focusCategories, ReviewStatus.FAILED,
                createdAt, startedAt, Instant.now(), findings, reportPath, message);
    }
}

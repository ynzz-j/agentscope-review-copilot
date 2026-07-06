package com.ynzz.agentscope.reviewcopilot.model;

import java.time.Instant;
import java.util.List;

public record ReviewReport(
        String jobId,
        String summary,
        List<ReviewFinding> findings,
        String markdown,
        Instant generatedAt) {}

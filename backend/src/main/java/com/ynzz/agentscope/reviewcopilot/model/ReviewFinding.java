package com.ynzz.agentscope.reviewcopilot.model;

public record ReviewFinding(
        ReviewSeverity severity,
        ReviewCategory category,
        String file,
        Integer line,
        String evidence,
        String impact,
        String suggestion,
        ReviewConfidence confidence) {}

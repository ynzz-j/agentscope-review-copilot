package com.ynzz.agentscope.reviewcopilot.model;

import java.time.Instant;
import java.util.Map;

public record ReviewEvent(
        String jobId,
        ReviewEventType type,
        String message,
        Map<String, Object> payload,
        Instant timestamp) {

    public static ReviewEvent of(
            String jobId, ReviewEventType type, String message, Map<String, Object> payload) {
        return new ReviewEvent(
                jobId,
                type,
                message,
                payload == null ? Map.of() : Map.copyOf(payload),
                Instant.now());
    }
}

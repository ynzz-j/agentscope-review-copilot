package com.ynzz.agentscope.reviewcopilot.middleware;

import java.time.Instant;

public record AuditRecord(
        String type,
        String subject,
        Status status,
        int messageCount,
        int toolCount,
        long elapsedMs,
        String error,
        Instant timestamp) {

    public enum Status {
        SUCCESS,
        FAILED
    }

    public static AuditRecord modelCall(
            String agentName,
            Status status,
            int messageCount,
            int toolCount,
            long elapsedMs,
            String error) {
        return new AuditRecord(
                "agent_model_call",
                agentName,
                status,
                messageCount,
                toolCount,
                elapsedMs,
                error,
                Instant.now());
    }
}

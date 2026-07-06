package com.ynzz.agentscope.reviewcopilot.middleware;

public interface ReviewAuditSink {

    void record(AuditRecord record);
}

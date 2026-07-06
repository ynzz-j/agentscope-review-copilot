package com.ynzz.agentscope.reviewcopilot.middleware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingReviewAuditSink implements ReviewAuditSink {

    private static final Logger log = LoggerFactory.getLogger(LoggingReviewAuditSink.class);

    @Override
    public void record(AuditRecord record) {
        if (record.status() == AuditRecord.Status.SUCCESS) {
            log.info(
                    "{} subject={} messages={} tools={} elapsedMs={}",
                    record.type(),
                    record.subject(),
                    record.messageCount(),
                    record.toolCount(),
                    record.elapsedMs());
            return;
        }
        log.warn(
                "{} subject={} messages={} tools={} elapsedMs={} error={}",
                record.type(),
                record.subject(),
                record.messageCount(),
                record.toolCount(),
                record.elapsedMs(),
                record.error());
    }
}

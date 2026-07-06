package com.ynzz.agentscope.reviewcopilot.middleware;

import io.agentscope.core.agent.Agent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.middleware.MiddlewareBase;
import io.agentscope.core.middleware.ModelCallInput;
import java.util.function.Function;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class AuditingMiddleware implements MiddlewareBase {

    private final ReviewAuditSink auditSink;

    public AuditingMiddleware(ReviewAuditSink auditSink) {
        this.auditSink = auditSink;
    }

    @Override
    public Flux<AgentEvent> onModelCall(
            Agent agent,
            RuntimeContext ctx,
            ModelCallInput input,
            Function<ModelCallInput, Flux<AgentEvent>> next) {
        long started = System.nanoTime();
        int messageCount = input.messages() == null ? 0 : input.messages().size();
        int toolCount = input.tools() == null ? 0 : input.tools().size();
        return next.apply(input)
                .doOnComplete(() -> auditSink.record(AuditRecord.modelCall(
                        agent.getName(),
                        AuditRecord.Status.SUCCESS,
                        messageCount,
                        toolCount,
                        (System.nanoTime() - started) / 1_000_000,
                        null)))
                .doOnError(error -> auditSink.record(AuditRecord.modelCall(
                        agent.getName(),
                        AuditRecord.Status.FAILED,
                        messageCount,
                        toolCount,
                        (System.nanoTime() - started) / 1_000_000,
                        error.toString())));
    }
}

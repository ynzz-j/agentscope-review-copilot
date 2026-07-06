package com.ynzz.agentscope.reviewcopilot.middleware;

import io.agentscope.core.agent.Agent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.middleware.MiddlewareBase;
import io.agentscope.core.middleware.ModelCallInput;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class AuditingMiddleware implements MiddlewareBase {

    private static final Logger log = LoggerFactory.getLogger(AuditingMiddleware.class);

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
                .doOnComplete(() -> log.info(
                        "agent_model_call agent={} messages={} tools={} elapsedMs={}",
                        agent.getName(),
                        messageCount,
                        toolCount,
                        (System.nanoTime() - started) / 1_000_000))
                .doOnError(error -> log.warn(
                        "agent_model_call_failed agent={} messages={} tools={} error={}",
                        agent.getName(),
                        messageCount,
                        toolCount,
                        error.toString()));
    }
}

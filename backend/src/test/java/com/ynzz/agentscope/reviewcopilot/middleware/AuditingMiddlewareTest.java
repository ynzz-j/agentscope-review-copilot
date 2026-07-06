package com.ynzz.agentscope.reviewcopilot.middleware;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.agentscope.core.agent.Agent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.middleware.ModelCallInput;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

class AuditingMiddlewareTest {

    @Test
    void recordsSuccessfulModelCalls() {
        CapturingAuditSink sink = new CapturingAuditSink();
        AuditingMiddleware middleware = new AuditingMiddleware(sink);
        Agent agent = mockAgent();
        ModelCallInput input = new ModelCallInput(List.of(), List.of(), null, null);

        StepVerifier.create(middleware.onModelCall(agent, RuntimeContext.empty(), input, ignored -> Flux.empty()))
                .verifyComplete();

        assertThat(sink.records)
                .singleElement()
                .satisfies(record -> {
                    assertThat(record.type()).isEqualTo("agent_model_call");
                    assertThat(record.subject()).isEqualTo("Review Agent");
                    assertThat(record.status()).isEqualTo(AuditRecord.Status.SUCCESS);
                    assertThat(record.messageCount()).isZero();
                    assertThat(record.toolCount()).isZero();
                    assertThat(record.error()).isNull();
                });
    }

    @Test
    void recordsFailedModelCalls() {
        CapturingAuditSink sink = new CapturingAuditSink();
        AuditingMiddleware middleware = new AuditingMiddleware(sink);
        Agent agent = mockAgent();
        ModelCallInput input = new ModelCallInput(List.of(), List.of(), null, null);

        StepVerifier.create(middleware.onModelCall(
                        agent,
                        RuntimeContext.empty(),
                        input,
                        ignored -> Flux.error(new IllegalStateException("model failed"))))
                .expectError(IllegalStateException.class)
                .verify();

        assertThat(sink.records)
                .singleElement()
                .satisfies(record -> {
                    assertThat(record.status()).isEqualTo(AuditRecord.Status.FAILED);
                    assertThat(record.error()).contains("model failed");
                });
    }

    private Agent mockAgent() {
        Agent agent = mock(Agent.class);
        when(agent.getName()).thenReturn("Review Agent");
        return agent;
    }

    private static class CapturingAuditSink implements ReviewAuditSink {
        private final List<AuditRecord> records = new ArrayList<>();

        @Override
        public void record(AuditRecord record) {
            records.add(record);
        }
    }
}

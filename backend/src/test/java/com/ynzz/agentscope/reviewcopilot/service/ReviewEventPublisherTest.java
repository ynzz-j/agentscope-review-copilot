package com.ynzz.agentscope.reviewcopilot.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.ynzz.agentscope.reviewcopilot.model.ReviewEvent;
import com.ynzz.agentscope.reviewcopilot.model.ReviewEventType;
import java.util.Map;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class ReviewEventPublisherTest {

    @Test
    void replaysHistoryToLateSubscriberAndCompletesAfterTerminalEvent() {
        ReviewEventPublisher publisher = new ReviewEventPublisher();
        String jobId = "review-001";

        publisher.publish(ReviewEvent.of(jobId, ReviewEventType.JOB_CREATED, "created", Map.of()));
        publisher.publish(ReviewEvent.of(jobId, ReviewEventType.DIFF_LOADED, "diff loaded", Map.of("changedFiles", 1)));
        publisher.publish(ReviewEvent.of(jobId, ReviewEventType.JOB_COMPLETED, "completed", Map.of()));
        publisher.complete(jobId);

        StepVerifier.create(publisher.stream(jobId))
                .assertNext(event -> assertThat(event.type()).isEqualTo(ReviewEventType.JOB_CREATED))
                .assertNext(event -> assertThat(event.type()).isEqualTo(ReviewEventType.DIFF_LOADED))
                .assertNext(event -> assertThat(event.type()).isEqualTo(ReviewEventType.JOB_COMPLETED))
                .verifyComplete();
    }

    @Test
    void emitsLiveEventsAfterInitialHistory() {
        ReviewEventPublisher publisher = new ReviewEventPublisher();
        String jobId = "review-002";

        publisher.publish(ReviewEvent.of(jobId, ReviewEventType.JOB_CREATED, "created", Map.of()));

        StepVerifier.create(publisher.stream(jobId).take(3))
                .assertNext(event -> assertThat(event.type()).isEqualTo(ReviewEventType.JOB_CREATED))
                .then(() -> {
                    publisher.publish(ReviewEvent.of(jobId, ReviewEventType.RULE_CHECK_DONE, "rules done", Map.of()));
                    publisher.publish(ReviewEvent.of(jobId, ReviewEventType.JOB_COMPLETED, "completed", Map.of()));
                    publisher.complete(jobId);
                })
                .assertNext(event -> assertThat(event.type()).isEqualTo(ReviewEventType.RULE_CHECK_DONE))
                .assertNext(event -> assertThat(event.type()).isEqualTo(ReviewEventType.JOB_COMPLETED))
                .verifyComplete();
    }
}

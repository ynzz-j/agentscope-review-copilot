package com.ynzz.agentscope.reviewcopilot.service;

import com.ynzz.agentscope.reviewcopilot.model.ReviewEvent;
import com.ynzz.agentscope.reviewcopilot.model.ReviewEventType;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Component
public class ReviewEventPublisher {

    private final Map<String, Sinks.Many<ReviewEvent>> sinks = new ConcurrentHashMap<>();
    private final Map<String, List<ReviewEvent>> history = new ConcurrentHashMap<>();

    public Flux<ReviewEvent> stream(String jobId) {
        List<ReviewEvent> snapshot = List.copyOf(history.getOrDefault(jobId, List.of()));
        if (isTerminal(snapshot)) {
            return Flux.fromIterable(snapshot);
        }
        return Flux.fromIterable(snapshot).concatWith(sink(jobId).asFlux());
    }

    public void publish(ReviewEvent event) {
        history.computeIfAbsent(event.jobId(), ignored -> new CopyOnWriteArrayList<>()).add(event);
        sink(event.jobId()).tryEmitNext(event);
    }

    public void publish(String jobId, ReviewEventType type, String message) {
        publish(ReviewEvent.of(jobId, type, message, Map.of()));
    }

    public void complete(String jobId) {
        Sinks.Many<ReviewEvent> sink = sinks.remove(jobId);
        if (sink != null) {
            sink.tryEmitComplete();
        }
    }

    private Sinks.Many<ReviewEvent> sink(String jobId) {
        return sinks.computeIfAbsent(jobId, ignored -> Sinks.many().multicast().directBestEffort());
    }

    private boolean isTerminal(List<ReviewEvent> events) {
        return events.stream().anyMatch(event ->
                event.type() == ReviewEventType.JOB_COMPLETED || event.type() == ReviewEventType.JOB_FAILED);
    }
}

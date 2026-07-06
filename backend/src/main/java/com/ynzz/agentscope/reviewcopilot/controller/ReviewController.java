package com.ynzz.agentscope.reviewcopilot.controller;

import com.ynzz.agentscope.reviewcopilot.model.ReviewEvent;
import com.ynzz.agentscope.reviewcopilot.model.ReviewJob;
import com.ynzz.agentscope.reviewcopilot.model.ReviewRequest;
import com.ynzz.agentscope.reviewcopilot.service.ReviewReportService;
import com.ynzz.agentscope.reviewcopilot.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public Mono<ResponseEntity<ReviewJob>> createReview(@Valid @RequestBody ReviewRequest request) {
        return reviewService.createReview(request)
                .map(job -> ResponseEntity.status(HttpStatus.ACCEPTED).body(job));
    }

    @GetMapping("/{id}")
    public Mono<ReviewJob> getReview(@PathVariable String id) {
        return reviewService.getReview(id);
    }

    @GetMapping(value = "/{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<ReviewEvent>> streamEvents(@PathVariable String id) {
        return reviewService.streamEvents(id)
                .map(event -> ServerSentEvent.<ReviewEvent>builder()
                        .event(event.type().name())
                        .id(event.timestamp().toString())
                        .data(event)
                        .build());
    }

    @GetMapping(value = "/{id}/report.md", produces = "text/markdown;charset=UTF-8")
    public Mono<ResponseEntity<String>> getReport(@PathVariable String id) {
        return reviewService.readReportMarkdown(id).map(ResponseEntity::ok);
    }

    @ExceptionHandler({
            ReviewService.ReviewNotFoundException.class,
            ReviewReportService.ReportNotFoundException.class
    })
    public ResponseEntity<String> notFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> badRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}

# Chapter 02 - Streaming Review

## Goal

Build the review task API and stream progress to the Vue progress page through Server-Sent Events.

## Backend Scope

- `POST /api/reviews` creates a review job and starts the review pipeline asynchronously.
- `GET /api/reviews/{id}` returns persisted job state.
- `GET /api/reviews/{id}/events` returns `text/event-stream` events with the `ReviewEvent` DTO.
- `ReviewEventPublisher` keeps per-job event history so late subscribers can still render the full timeline.
- Terminal events (`JOB_COMPLETED`, `JOB_FAILED`) close the stream and are replayed as finite history for future subscribers.

## Event Contract

The progress page should treat the event type as the source of truth:

- `JOB_CREATED`
- `DIFF_LOADED`
- `FILE_CONTEXT_LOADED`
- `RULE_CHECK_DONE`
- `MODEL_REVIEWING`
- `FINDING_GENERATED`
- `REPORT_READY`
- `JOB_COMPLETED`
- `JOB_FAILED`

Each event contains `jobId`, `type`, `message`, `payload`, and `timestamp`.

## Frontend Scope

- `ReviewCreateView` posts `ReviewRequest`.
- `ReviewProgressView` subscribes to `/api/reviews/{id}/events`.
- `reviewStore` owns the EventSource lifecycle, appends events, fetches the final job, and navigates to the result view after completion.

## Acceptance

- A user can create a review from the UI and immediately see staged progress.
- Refreshing or opening the progress URL after a job has completed still shows the completed timeline.
- The stream does not require a model provider; missing model configuration is emitted as an explicit progress message and noted in the final report.
- Unit tests cover historical replay and live event emission in `ReviewEventPublisherTest`.

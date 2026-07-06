# Chapter 04 - Review State

## Goal

Persist review tasks, session metadata, findings, and Markdown reports so the UI can recover state after refresh.

## Backend Scope

- `ReviewJob` represents task lifecycle state.
- `JsonFileReviewJobStore` saves job JSON under `backend/data/jobs`.
- `ReviewReportService` writes Markdown reports under `backend/data/reports`.
- `GET /api/reviews/{id}` reloads persisted job state.
- `GET /api/reviews/{id}/report.md` returns the generated Markdown report.

## State Contract

The job record keeps:

- id
- sessionId
- repoPath
- diffMode
- baseRef
- focusCategories
- status
- createdAt
- startedAt
- finishedAt
- findings
- reportPath
- errorMessage

## Frontend Scope

`reviewStore` keeps active task state, progress events, findings, and report Markdown. Result pages can refetch the job and report by `id`, so a completed review remains viewable after navigation.

## Acceptance

- A completed review job can be saved and loaded from JSON.
- Markdown reports are written with UTF-8 encoding.
- Report output stays in Review Copilot's own report directory.
- The UI can fetch a completed job and copy the Markdown report without rerunning the review.

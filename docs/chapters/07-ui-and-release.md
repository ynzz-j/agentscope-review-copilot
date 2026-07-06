# Chapter 07 - UI And Release

## Goal

Finish the Vue interface, report copy workflow, README, verification scripts, branch strategy, and GitHub-ready release structure.

## Frontend Scope

- `ReviewCreateView` creates review jobs from a local repository path.
- `ReviewProgressView` renders SSE progress events.
- `ReviewResultView` renders findings, filters, Markdown report preview, and one-click report copy.
- `reviewStore` owns API calls, EventSource lifecycle, findings, report Markdown, and copy behavior.
- Vite dev proxy forwards `/api` and `/health` to the backend.

## Release Scope

- `README.md` documents local startup, model configuration, APIs, safety boundaries, and branch/tag plan.
- `scripts/verify.ps1` runs backend and frontend gates.
- `scripts/smoke-review.ps1` runs an end-to-end local diff review and checks that the reviewed demo repository is not modified by the tool.
- Completion tags exist from `base-framework` through `chapter-07-complete`.

## Acceptance

- `npm run type-check` passes.
- `npm run test:unit -- --run` passes.
- `npm run build` passes.
- `npm run lint` passes.
- The report preview has a tested empty state and rendered Markdown state.
- The project is ready to push once a GitHub remote is configured.

# Development Status

This repository is generated from the Obsidian plan:

`D:\Obsidian\KnowledgeBase\00-Inbox\AgentScope-Java 2.0.0-RC4 Review Copilot 自动开发计划.md`

## Current State

- Local path: `D:\workspace\whd\ynzz\articles\agentscope-review-copilot`
- Git branch: `main`
- Base tag: `base-framework`
- Backend package root: `com.ynzz.agentscope.reviewcopilot`
- Maven groupId: `com.ynzz`
- AgentScope-Java version: `2.0.0-RC4`
- Frontend stack: Vue 3 + TypeScript + Vite + Router + Pinia

## Implemented

- Spring Boot WebFlux backend.
- Vue 3 frontend.
- `/health`.
- `POST /api/reviews`.
- `GET /api/reviews/{id}`.
- `GET /api/reviews/{id}/events`.
- `GET /api/reviews/{id}/report.md`.
- Local Git diff loading through a fixed-argument `git diff` command.
- File context loading with repository-root checks.
- Rule-based findings for test gaps, swallowed exceptions, static mutable state, API validation gaps, and Agent tool boundaries.
- JSON job persistence under `backend/data/jobs`.
- Markdown reports under `backend/data/reports`.
- Explicit no-default-model behavior in `ModelFactory`.
- AgentScope toolkit registration and middleware extension point.
- Auditable model-call middleware with a pluggable audit sink.
- Frontend review create/progress/result pages.
- Verification and smoke-test scripts under `scripts/`.

## Verification Commands

Run the full local gate:

```powershell
.\scripts\verify.ps1 -SkipFrontendInstall
```

Run the end-to-end smoke review:

```powershell
.\scripts\smoke-review.ps1
```

## Known Gaps

- GitHub remote is not configured yet.
- GitHub CLI (`gh`) is not installed in the current environment.
- GitHub remote still needs to be configured before pushing branches and tags.
- Model-backed review is intentionally not enabled until a provider is explicitly chosen.

## Chapter Branch Targets

| Chapter | Branch | Completion tag |
|---|---|---|
| Base framework | `main` | `base-framework` |
| Chapter 01 | `chapter/01-skeleton` | `chapter-01-complete` |
| Chapter 02 | `chapter/02-streaming-review` | `chapter-02-complete` |
| Chapter 03 | `chapter/03-review-tools` | `chapter-03-complete` |
| Chapter 04 | `chapter/04-review-state` | `chapter-04-complete` |
| Chapter 05 | `chapter/05-permission-boundary` | `chapter-05-complete` |
| Chapter 06 | `chapter/06-audit-middleware` | `chapter-06-complete` |
| Chapter 07 | `chapter/07-ui-and-release` | `chapter-07-complete` |

# AgentScope Review Copilot

AgentScope Review Copilot is a project-based intro sample for **AgentScope-Java 2.0.0-RC4**. It reviews a local Git diff, generates structured engineering-quality findings, streams review progress, and writes a Markdown report.

## Tech Stack

- Backend: JDK 17+, Maven, Spring Boot WebFlux, AgentScope-Java 2.0.0-RC4, Reactor, JGit
- Frontend: Vue 3, TypeScript, Vite, Vue Router, Pinia, Vitest, ESLint, Prettier
- Java package root: `com.ynzz.agentscope.reviewcopilot`
- Maven groupId: `com.ynzz`

## Quick Start

Backend:

```bash
cd backend
mvn spring-boot:run
```

Health check:

```bash
curl http://localhost:8080/health
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`.

## Model Configuration

This project intentionally does **not** select a default model provider.

The model layer is represented by `ModelFactory` and `AgentFactory`. If model-backed review is enabled later, choose an AgentScope-Java RC4-supported provider explicitly, such as DashScope, OpenAI, Anthropic, Gemini, Ollama, or another supported extension.

When no provider is configured, the backend does not default to DashScope or any other provider. The deterministic rule checks still run and the report includes a clear model configuration note.

## API

- `GET /health`
- `POST /api/reviews`
- `GET /api/reviews/{id}`
- `GET /api/reviews/{id}/events`
- `GET /api/reviews/{id}/report.md`

Create review example:

```bash
curl -X POST http://localhost:8080/api/reviews \
  -H "Content-Type: application/json" \
  -d "{\"repoPath\":\"D:\\workspace\\demo-project\",\"diffMode\":\"WORKING_TREE\",\"sessionId\":\"demo-session\"}"
```

## Review Scope

The intro project focuses on engineering quality:

- Bug risk
- Maintainability
- Concurrency and state
- API contract
- Test gaps
- Agent boundary

Each finding contains severity, category, file, line, evidence, impact, suggestion, and confidence.

## Security Boundary

- The reviewed repository is read-only.
- Reports are written only to Review Copilot's own `backend/data/reports` directory.
- The backend does not expose an arbitrary shell execution endpoint.
- File reads are guarded by repository-root path checks.
- Sensitive paths such as `.env`, `.ssh`, and common credential files are blocked.

## Branch And Tag Plan

| Chapter | Branch | Completion tag | Description |
|---|---|---|---|
| Base framework | `main` | `base-framework` | Initial runnable framework |
| Chapter 01 | `chapter/01-skeleton` | `chapter-01-complete` | Spring Boot + Vue skeleton |
| Chapter 02 | `chapter/02-streaming-review` | `chapter-02-complete` | SSE review progress |
| Chapter 03 | `chapter/03-review-tools` | `chapter-03-complete` | Git diff and review tools |
| Chapter 04 | `chapter/04-review-state` | `chapter-04-complete` | State and report storage |
| Chapter 05 | `chapter/05-permission-boundary` | `chapter-05-complete` | Read-only boundaries |
| Chapter 06 | `chapter/06-audit-middleware` | `chapter-06-complete` | AgentScope middleware audit |
| Chapter 07 | `chapter/07-ui-and-release` | `chapter-07-complete` | UI polish and GitHub release |

Chapter flow:

```bash
git checkout main
git pull --ff-only
git checkout -b chapter/NN-topic
# implement chapter
git add .
git commit -m "feat: implement chapter NN topic"
git checkout main
git merge --no-ff chapter/NN-topic
git tag chapter-NN-complete
git push origin main
git push origin chapter/NN-topic
git push origin chapter-NN-complete
```

## Verification

Backend:

```bash
cd backend
mvn test
```

Frontend:

```bash
cd frontend
npm run type-check
npm run test:unit -- --run
npm run build
npm run lint
```

# Chapter 06 - Audit Middleware

## Goal

Add an AgentScope middleware layer that can audit model calls, tool availability, elapsed time, and errors without coupling review logic to a concrete model provider.

## Backend Scope

- `AuditingMiddleware` implements AgentScope-Java `MiddlewareBase`.
- `AuditRecord` captures model-call status, agent name, message count, tool count, elapsed time, error text, and timestamp.
- `ReviewAuditSink` isolates where audit records are written.
- `LoggingReviewAuditSink` is the default sink for the intro project.
- `AgentFactory` wires AgentScope toolkit and middlewares, but still requires explicit model configuration.

## Model Boundary

The project still does not choose a default provider. `ModelFactory` returns a clear configuration error until the actual implementation chapter selects DashScope, OpenAI, Anthropic, Gemini, Ollama, or another RC4-supported path.

## Acceptance

- Successful model-call middleware execution creates a success audit record.
- Failed model-call middleware execution creates a failed audit record with error text.
- Missing model provider remains an explicit configuration state, not a hidden default.
- Review tooling remains usable through deterministic checks when model-backed review is not configured.

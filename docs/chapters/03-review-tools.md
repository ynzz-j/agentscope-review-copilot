# Chapter 03 - Review Tools

## Goal

Implement the local review toolkit used by the AgentScope-Java intro project.

## Tool Scope

- `GitDiffTool` loads a local Git diff in read-only mode.
- `FileContextTool` reads bounded UTF-8 file context from changed files.
- `RuleCheckTool` converts deterministic engineering checks into structured `ReviewFinding` records.
- `ReportTool` renders the final Markdown report.

## Safety Shape

The tool layer intentionally avoids a general command endpoint. `GitDiffTool` builds a fixed-argument `git diff` process, rejects option-like `baseRef` values, and only returns diff text plus changed-file metadata.

File context reads go through `ReviewPermissionPolicy` and `ReviewPathGuard`, so sensitive files such as `.env` are skipped rather than copied into model context or reports.

## Finding Contract

Each `ReviewFinding` includes:

- severity
- category
- file
- line
- evidence
- impact
- suggestion
- confidence

## Acceptance

- Working-tree diffs can be loaded from a local Git repository.
- Context is truncated to the configured line budget.
- Sensitive files are not read into context.
- Focus categories narrow deterministic checks to the requested review area.

# Chapter 05 - Permission Boundary

## Goal

Make the intro project safe by default: it can read a local Git diff and write its own reports, but it must not modify the reviewed repository.

## Boundary Rules

- The reviewed repository is read-only.
- `GitDiffTool` exposes only fixed `git diff` commands.
- `FileContextTool` reads only files inside the resolved repository root.
- Sensitive files and directories are blocked from context loading.
- Job JSON is stored only under Review Copilot's configured job directory.
- Markdown reports are stored only under Review Copilot's configured report directory.
- Review and report IDs must be simple safe IDs, not paths.

## Why This Matters

This series teaches AgentScope-Java with local developer files. The project must make side effects visible and narrow: report generation is allowed, source edits are not.

## Acceptance

- `../` paths are rejected for file context reads.
- `.env` and other sensitive paths are not read.
- Unsafe review IDs cannot escape the job directory.
- Unsafe report IDs cannot escape the report directory.
- The smoke test confirms the demo repository still has only the intentional test diff after review.

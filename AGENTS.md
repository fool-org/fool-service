# fool-service Agent Guide

This file is the first-read entrypoint for agents working in this repository.
Keep it short and route detail to versioned docs instead of duplicating rules.

## Project Shape

- Java 17 / Spring Boot 2.7.4 Maven multi-module backend.
- Vue 3 / Vite frontend under `frontend/`.
- Docker Compose local stack with MySQL, Redis, backend, and frontend.
- Migration source is the legacy FoolFrame workflow; parity state lives in
  `docs/migration/foolframe-parity.md`.

## Source Of Truth

- Harness and validation matrix: `docs/validation.md`.
- Standard Engine catalog: `docs/standards/README.md` and
  `scripts/standard_engine.py`.
- Active work state: `tasks.md` unless a future external tracker is explicitly
  named in this repo.
- Delivery evidence shape: `agent_chats/README.md`.

## Validation

Run the smallest check that matches the change:

- Harness/docs/standards: `python scripts/check_repo_harness.py`.
- Backend Java changes: `mvn test` from the repository root, or a focused
  module test when the scope is isolated.
- Frontend changes: `cd frontend && npm test && npm run build`.
- Docker/runtime changes: `docker compose up -d --build`, then
  `curl http://localhost:8080/test` and inspect `docker compose ps -a`,
  including `db-migrate` at `Exited (0)`.

When validation is skipped or downgraded, record the missing prerequisite and
residual risk in the delivery evidence.

## Delivery Evidence

For meaningful code, runtime, or migration changes, add or update an
`agent_chats/YYYY/MM/DD/...md` entry with prompt, scope, changed files,
validation commands, runtime artifact paths, skipped checks, risks, and
follow-ups. Keep task-state updates in the same logical change as the code that
completes or changes the task.

## Change Discipline

- Keep changes narrowly scoped; this repository often has unrelated dirty files.
- Do not rewrite migration history or existing user edits while adding harness
  infrastructure.
- Add standards only when they have a clear owner doc, enforcement surface,
  evidence, and repair path.

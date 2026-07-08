# Vue shell split

## Prompt

Continue the FoolFrame migration while keeping Vue usable, avoiding concrete
business DTO binding, maximizing reuse, and controlling file size.

## Scope

- Moved the results panel from `App.vue` into `ResultsPanel.vue`.
- Moved the static migration map from `App.vue` into `MigrationMap.vue`.
- Kept result row rendering on the existing shared `ListDataTable`.
- Moved static migration module labels into the existing `viewShell.ts`.
- Updated the source-level renderer guard to check `ResultsPanel.vue`.
- Marked the `App.vue` size follow-up complete in `tasks.md`.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/ResultsPanel.vue`
- `frontend/src/MigrationMap.vue`
- `frontend/src/viewShell.ts`
- `frontend/src/payload.test.ts`
- `tasks.md`

## Validation

- `cd frontend && npm test`: passed, 73 tests.
- `cd frontend && npm run build`: passed.
- `python scripts/check_repo_harness.py`: passed.
- `git diff --check`: clean.
- `wc -l frontend/src/App.vue frontend/src/ResultsPanel.vue frontend/src/MigrationMap.vue frontend/src/viewShell.ts`: `App.vue` is 1997 lines.
- `docker compose up -d --build frontend`: rebuilt frontend and backend
  images, then recreated backend and frontend containers.
- `python scripts/runtime_doctor.py`: passed all compose, auth, View/data,
  detail, `inputquery`, report, message, notify, and logout checks.
- `docker compose ps`: backend, frontend, MySQL, and Redis running; MySQL and
  Redis healthy.

## Runtime Evidence

- Backend container running on `http://localhost:8080`.
- Frontend container running on `http://localhost:8081`.
- `curl http://localhost:8081/` returned the rebuilt Vue shell with
  `/assets/index-B6ZukV-E.js`.

## Risks

- This does not reduce the remaining View workflow action/state density inside
  `App.vue`; it only moves static shell panels out to keep the file below the
  current size target.

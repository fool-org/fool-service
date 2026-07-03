# Format Row Classes

## Prompt

- Continue the FoolFrame migration with Docker/Vue active.
- Keep the Vue workflow driven by View metadata and legacy data payloads.

## Scope

- Mapped migrated `ListDataItem.rowFmt` values into Vue table row classes.
- Kept the behavior intentionally close to legacy `querylistdata.js`, where
  `EditType.Format` `FmtValue` values are appended directly to `<tr class>`.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/04/2026-07-03T17-04-45Z-format-row-classes.md`

## Validation

- `cd frontend && npm test`
  - Passed: 2 test files, 40 tests.
- `cd frontend && npm run build`
  - Passed: `vue-tsc --noEmit` and Vite production build.
- `git diff --check`
  - Passed.
- `python scripts/check_repo_harness.py`
  - Passed.
- `docker compose build frontend`
  - Passed; rebuilt the Vue/Nginx frontend image.
- `docker compose up -d --no-deps frontend`
  - Passed; refreshed the running frontend container.
- `docker compose ps`
  - Passed; backend, frontend, MySQL, and Redis are running.
- `curl -fsS http://localhost:8081/`
  - Passed; returned the Vue HTML shell from the Compose frontend.
- `curl -fsS -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageIndex":1,"pageSize":1},"filter":{}}' http://localhost:8080/api/v1/data/query-list`
  - Passed; current seed returns `rowFmt=null`, so runtime confirms API shape
    and serving but not a non-empty format class.

## Skipped Checks

- Full Maven tests were not rerun; this slice changes frontend row class
  binding only.
- A live non-empty `rowFmt` sample was not available in the current Docker
  seed.

## Risks

- Lookup field-specific editors are still pending.

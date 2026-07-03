# Readonly Field Editors

## Prompt

- Continue the FoolFrame migration with Docker/Vue active.
- Keep Vue View rendering driven by metadata, not concrete business DTOs.

## Scope

- Added readonly field detection from `readOnly=true` and `editType=ReadOnly`.
- Reused a small `MetadataFieldEditor.vue` for readonly, enum, and text field
  controls instead of duplicating editor branches in `App.vue`.
- Detail and child collection readonly fields now render as disabled controls.
- Legacy `saveobj` / `savenewobj` `Propertyies` builders now skip readonly
  fields, matching the legacy `savetext.js` `data-readonly` filter.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/MetadataFieldEditor.vue`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/04/2026-07-03T16-59-00Z-readonly-field-editors.md`

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
- `curl -fsS -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
  - Passed; `OrderList` columns return `isReadOnly=true` and
    `editType=ReadOnly`.
- `curl -fsS -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageIndex":1,"pageSize":1},"filter":{}}' http://localhost:8080/api/v1/data/query-list`
  - Passed; row values return `readOnly=true` and `editType=ReadOnly`.

## Skipped Checks

- Full Maven tests were not rerun; this slice changes the Vue metadata editor
  and frontend payload builders only.

## Risks

- Lookup and formatted field-specific editors are still pending.

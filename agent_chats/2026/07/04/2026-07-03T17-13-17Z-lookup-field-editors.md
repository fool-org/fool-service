# Lookup Field Editors

## Prompt

- Continue the FoolFrame migration with Docker/Vue active.
- Keep Vue View rendering metadata-driven instead of binding to concrete
  business DTO fields.

## Scope

- Added BusinessObject lookup field detection from `prpType=BusinessObject`
  and legacy property type `16`.
- Extended `MetadataFieldEditor.vue` to search the migrated `inputquery`
  endpoint and set the field draft only after the user selects a candidate.
- Reused native inputs/buttons and existing `buildInputQueryRequest` /
  `postApi`; no typeahead dependency was added.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/MetadataFieldEditor.vue`
- `frontend/src/style.css`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/04/2026-07-03T17-13-17Z-lookup-field-editors.md`

## Validation

- `cd frontend && npm test`
  - Passed: 2 test files, 41 tests.
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
- `curl -fsS -H 'Content-Type: application/json' -d '{"viewName":"OrderList","viewItemId":"symbol","text":"BTC"}' http://localhost:8080/api/v1/data/inputquery`
  - Passed; route returned `code=0` with an empty item list because `symbol`
    is not a BusinessObject lookup field.
- `curl -fsS -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
  - Passed; current seed exposes `Long`, `String`, and `Enum` fields, but no
    BusinessObject lookup field.

## Skipped Checks

- Full Maven tests were not rerun; this slice changes the Vue metadata editor
  and frontend helper tests only.
- A live non-empty BusinessObject lookup selection was not available in the
  current Docker seed.

## Risks

- The current Docker `OrderList` seed does not include a non-empty
  BusinessObject lookup field, so live lookup selection needs a richer seeded
  view to prove end to end.

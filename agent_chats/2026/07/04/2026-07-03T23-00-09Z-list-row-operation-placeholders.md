# Prompt

Continue the Docker/FoolFrame/Vue migration. Keep the frontend View-first and
do not bind row actions to concrete business DTOs.

# Scope

- Align list row operation rendering with FoolFrame for `RequireSelect=true`
  operations whose `ViewId` is `0`.
- Keep target-View row operations clickable, but render no-target operations
  without inventing behavior.

# Legacy Reference

- `../FoolFrame/src/Web/public/javascripts/app/querylistdata.js`
  - Lines 143-151 render every `RequireSelect` operation. Positive `ViewId`
    operations call `setselect(...)`; zero-View operations render only the
    operation name.
- `../FoolFrame/src/Web/views/view.jade`
  - Lines 26-30 add an operation column if any operation requires a selected
    row.

# Changes

- `frontend/src/viewWorkflow.ts`
  - `rowOperations` now keeps all `RequireSelect=true` operations.
- `frontend/src/ListDataTable.vue`
  - No-target row operations render as disabled metadata actions.
  - Positive target View operations still emit selection with the target
    `ViewId`.
- `frontend/src/viewWorkflow.test.ts`
  - Covered row operation splitting for both positive and zero target View ids.
- `frontend/src/payload.test.ts`
  - Guarded the row operation target/disabled wiring in the table component.
- `tasks.md` and `docs/migration/foolframe-parity.md`
  - Recorded this parity increment.

# Validation

- `cd frontend && npm test -- --run`
  - Passed: 3 test files, 51 tests.
- `cd frontend && npm run build`
  - Passed: `vue-tsc --noEmit && vite build`.
- `python3 scripts/check_repo_harness.py`
  - Passed.
- `git diff --check`
  - Passed.
- `curl -sS -H 'Content-Type: application/json' -d '{"ViewId":100}' http://localhost:8080/api/v1/view/getlistview`
  - Docker seed still exposes row operations `7001` and `7002` with
    `RequireSelect=true` and `ViewId=0`.
- `wc -l frontend/src/App.vue frontend/src/ListDataTable.vue frontend/src/viewWorkflow.ts frontend/src/viewWorkflow.test.ts frontend/src/payload.test.ts`
  - `frontend/src/App.vue`: 1985 lines.
  - `frontend/src/ListDataTable.vue`: 65 lines.
  - `frontend/src/viewWorkflow.ts`: 281 lines.
  - `frontend/src/viewWorkflow.test.ts`: 231 lines.
  - `frontend/src/payload.test.ts`: 469 lines.
- `docker compose up -d --build frontend`
  - Passed; frontend and backend images rebuilt and containers restarted.
- `python3 scripts/runtime_doctor.py`
  - Passed: backend, frontend, MySQL, Redis, `/test`, `getlistview`,
    `querydata`, `querydatadetail`, `inputquery`, and `getmkqview`.

# Runtime Evidence

- `docker compose ps`
  - `fool-service-backend-1`: Up on `8080`.
  - `fool-service-frontend-1`: Up on `8081`.
  - MySQL and Redis healthy.

# Risks

- No-target row operations remain display-only because FoolFrame did not bind a
  row click target for them in `querylistdata.js`.

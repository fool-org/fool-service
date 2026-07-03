# Prompt

Continue the Docker/FoolFrame/Vue migration. Keep the frontend View-first:
render from View metadata first, then query or mutate data through that View
context. Avoid binding the Vue workflow to concrete business DTOs.

# Scope

- Align list-level create operations with FoolFrame's rendered list page.
- Reuse the existing Vue `initnew` / `savenewobj` flow.
- Keep `App.vue` under 2000 lines.

# Legacy Reference

- `../FoolFrame/src/Web/views/view.jade`
  - Lines 15-18 render `RequireSelect=false` operations as `new{ViewID}`
    links.
- `../FoolFrame/src/Web/public/javascripts/app/querylistdata.js`
  - Lines 201-207 stores operation `RequireSelect`, `ViewId`, and `Name`
    metadata for list row behavior.

# Changes

- `frontend/src/viewWorkflow.ts`
  - Added `createOperations`, a small helper that keeps only list operations
    with `requireSelect=false` and a target `viewId`.
- `frontend/src/App.vue`
  - The main toolbar now renders create operation buttons from loaded
    `getlistview` operation metadata.
  - Starting a new object accepts the operation target View id.
  - Saving a newly initialized object uses the same View id that was passed to
    `initnew`.
- `frontend/src/viewWorkflow.test.ts`
  - Covered create-operation filtering.
- `tasks.md`
  - Marked this Vue list-operation parity slice complete.
- `docs/migration/foolframe-parity.md`
  - Recorded the parity increment.

# Validation

- `cd frontend && npm test -- --run`
  - Passed: 3 test files, 50 tests.
- `cd frontend && npm run build`
  - Passed: `vue-tsc --noEmit && vite build`.
- `python3 scripts/check_repo_harness.py`
  - Passed.
- `git diff --check`
  - Passed.
- `docker compose up -d --build frontend`
  - Passed; Compose rebuilt frontend and backend images and restarted both
    containers.
- `python3 scripts/runtime_doctor.py`
  - Passed: backend, frontend, MySQL, Redis, `getlistview`, `querydata`,
    `querydatadetail`, `inputquery`, and `getmkqview`.
- `wc -l frontend/src/App.vue frontend/src/viewWorkflow.ts frontend/src/viewWorkflow.test.ts`
  - `frontend/src/App.vue`: 1998 lines.
  - `frontend/src/viewWorkflow.ts`: 277 lines.
  - `frontend/src/viewWorkflow.test.ts`: 224 lines.

# Runtime Evidence

- `docker compose ps`
  - `fool-service-backend-1`: Up on `8080`.
  - `fool-service-frontend-1`: Up on `8081`.
  - MySQL and Redis healthy.
- Docker seed `ViewId=100` currently has only `requireSelect=true`
  operations, so the live toolbar falls back to `New Row`; the
  `RequireSelect=false` create-operation branch is covered by the focused
  helper test.

# Risks

- This covers create operations with a target View id. It does not add a
  browser-navigation model for every legacy operation result view mode.

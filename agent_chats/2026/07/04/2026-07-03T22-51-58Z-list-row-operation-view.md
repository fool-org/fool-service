# Prompt

Continue the Docker/FoolFrame/Vue migration with View-first behavior. The next
slice should compare legacy FoolFrame rendering before changing Vue behavior
and should avoid concrete business DTO binding.

# Scope

- Align list row operations with FoolFrame's `RequireSelect` operation links.
- Keep the selected object's detail workflow bound to the active detail View.
- Reuse existing Vue helpers and APIs.

# Legacy Reference

- `../FoolFrame/src/Web/public/javascripts/app/querylistdata.js`
  - Lines 143-151 render `RequireSelect` operations with a positive `ViewId`
    as row-level `setselect(operation.ViewId, row.Id, rowIndex)` links.
- `../FoolFrame/src/Web/views/view.jade`
  - Lines 26-30 add an operation column when any View operation requires a
    selected row.
- `../FoolFrame/src/Web/views/detailView.jade`
  - Line 8 initializes the detail page's `viewid`, which subsequent saves and
    operations use as the active detail View context.

# Changes

- `frontend/src/viewWorkflow.ts`
  - Added `rowOperations` for `RequireSelect=true` operations with a target
    View id.
- `frontend/src/App.vue`
  - Passes metadata-driven target-View row operations into a focused list table
    renderer.
  - `selectObject` accepts the target detail View id and loads
    `querydatadetail` through that View.
  - Detail save, child collection refreshes, lookup editor context, and detail
    operation execution now reuse `detailViewId`.
- `frontend/src/ListDataTable.vue`
  - Renders list rows from view/data metadata and emits selected row operations
    with the operation target `ViewId`.
- `frontend/src/viewWorkflow.test.ts`
  - Covered create-operation and row-operation splitting.
- `frontend/src/payload.test.ts`
  - Updated the source guard to require detail View id binding for detail
    refresh/save and component-level row operation selection.
- `tasks.md`
  - Marked this row-operation View-context slice complete.
- `docs/migration/foolframe-parity.md`
  - Recorded the parity increment.

# Validation

- `cd frontend && npm test -- --run`
  - Passed: 3 test files, 51 tests.
- `cd frontend && npm run build`
  - Passed: `vue-tsc --noEmit && vite build`.
- `python3 scripts/check_repo_harness.py`
  - Passed.
- `git diff --check`
  - Passed.
- `docker compose up -d --build frontend`
  - Passed; frontend and backend images rebuilt and containers restarted.
- `python3 scripts/runtime_doctor.py`
  - Passed: backend, frontend, MySQL, Redis, `getlistview`, `querydata`,
    `querydatadetail`, `inputquery`, and `getmkqview`.
- `wc -l frontend/src/App.vue frontend/src/ListDataTable.vue frontend/src/viewWorkflow.ts frontend/src/viewWorkflow.test.ts frontend/src/payload.test.ts`
  - `frontend/src/App.vue`: 1985 lines.
  - `frontend/src/ListDataTable.vue`: 61 lines.
  - `frontend/src/viewWorkflow.ts`: 281 lines.
  - `frontend/src/viewWorkflow.test.ts`: 227 lines.
  - `frontend/src/payload.test.ts`: 468 lines.

# Runtime Evidence

- `docker compose ps`
  - `fool-service-backend-1`: Up on `8080`.
  - `fool-service-frontend-1`: Up on `8081`.
  - MySQL and Redis healthy.
- Docker seed `ViewId=100` currently has `RequireSelect=true` operations with
  `ViewId=0`, so runtime smoke proves the route/container baseline; the
  target-View row-operation branch is covered by the focused helper test.

# Risks

- This does not implement every legacy row operation presentation mode. It
  only covers the target-View selection behavior that FoolFrame renders when a
  row operation has a positive `ViewId`.

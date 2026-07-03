# Prompt

Continue the Docker/FoolFrame/Vue migration with View-first rendering and
atomic commits.

# Scope

- Compare FoolFrame list paging behavior before changing Vue.
- Add the smallest Vue paging control that uses legacy `querydata` totals.
- Keep pagination bound to the loaded `ViewId`, not a concrete business DTO.

# Legacy Reference

- `../FoolFrame/src/Web/public/javascripts/app/querylistdata.js`
  - Lines 171-175 update navbar state from `TotalItem`, `pageSize`, and
    `currentPage`.
- `../FoolFrame/src/Web/views/view.jade`
  - Lines 32-34 render the list navbar below the data table.

# Changes

- `frontend/src/viewWorkflow.ts`
  - Added small helpers for list totals and page index from direct legacy
    fields or `pageInfo`.
- `frontend/src/App.vue`
  - Added previous/next controls under the View table.
  - Page changes reuse `queryCurrentViewData()` and the current loaded View id.
- `frontend/src/viewWorkflow.test.ts`
  - Covered direct `totalItem` / `totalPage` and `pageInfo` fallback fields.
- `frontend/src/payload.test.ts`
  - Guarded that the Vue main View workflow renders paging from query totals.
- `tasks.md` and `docs/migration/foolframe-parity.md`
  - Recorded this parity increment.

# Validation

- `cd frontend && npm test -- --run`
  - Passed: 3 test files, 53 tests.
- `cd frontend && npm run build`
  - Passed: `vue-tsc --noEmit && vite build`.
- `python3 scripts/check_repo_harness.py`
  - Passed.
- `git diff --check`
  - Passed.
- `curl -sS -H 'Content-Type: application/json' -d '{"ViewId":100,"PageIndex":1,"PageSize":2}' http://localhost:8080/api/v1/data/querydata`
  - Confirmed Docker response exposes `totalItem`, `totalPage`, `pageIndex`,
    and `pageInfo`.
- `wc -l frontend/src/App.vue frontend/src/ListDataTable.vue frontend/src/viewWorkflow.ts frontend/src/viewWorkflow.test.ts frontend/src/payload.test.ts`
  - `frontend/src/App.vue`: 2013 lines.
  - `frontend/src/ListDataTable.vue`: 65 lines.
  - `frontend/src/viewWorkflow.ts`: 294 lines.
  - `frontend/src/viewWorkflow.test.ts`: 242 lines.
  - `frontend/src/payload.test.ts`: 475 lines.
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

- Paging changes only reload the list data. They do not auto-select a row on
  every page navigation.

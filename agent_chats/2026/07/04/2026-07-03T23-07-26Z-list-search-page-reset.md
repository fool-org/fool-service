# Prompt

Continue the Docker/FoolFrame/Vue migration and keep atomic commits.

# Scope

- Match FoolFrame list search behavior for the main View workflow.
- Keep the change in the existing View-first `getlistview` -> `querydata`
  flow.

# Legacy Reference

- `../FoolFrame/src/Web/public/javascripts/app/querylistdata.js`
  - Lines 180-185 set `$scope.page = 1` before running `querydata()` when the
    list search action is used.

# Changes

- `frontend/src/App.vue`
  - `loadViewWorkflow` now accepts `resetPage`.
  - The toolbar `Load View` action calls `loadViewWorkflow(true)` so search
    reloads start from page 1.
  - Mounted auto-load still uses the default path.
- `frontend/src/payload.test.ts`
  - Added a source guard for the main View search page reset.
- `tasks.md` and `docs/migration/foolframe-parity.md`
  - Recorded this parity increment.

# Validation

- `cd frontend && npm test -- --run`
  - Passed: 3 test files, 54 tests.
- `cd frontend && npm run build`
  - Passed: `vue-tsc --noEmit && vite build`.
- `python3 scripts/check_repo_harness.py`
  - Passed.
- `git diff --check`
  - Passed.
- `wc -l frontend/src/App.vue frontend/src/payload.test.ts`
  - `frontend/src/App.vue`: 2016 lines.
  - `frontend/src/payload.test.ts`: 481 lines.
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

- This does not add a separate Search button. The existing `Load View` action
  is the View workflow's search/reload action.

# Prompt

Continue the FoolFrame-to-Vue migration, but keep the flow centered on the
rendered View. Specifically, check the legacy View rendering page first, then
load data from that View context instead of binding frontend behavior to
concrete business DTOs.

# Scope

- Fix the Vue detail operation source only.
- Keep `App.vue` under the current source-size limit.
- Do not introduce business-name or DTO-specific shortcuts.

# Legacy Reference

- `../FoolFrame/src/Web/views/detailView.jade`
  - Lines 28-30 render detail action buttons from `view.Operations`.
- `../FoolFrame/src/Web/public/javascripts/app/detailview.js`
  - Lines 246-252 execute `operation.runoperation($scope.viewid, $scope.obj.Id, opid)`.

# Changes

- `frontend/src/App.vue`
  - Detail operation buttons now render from
    `detailResponse?.data?.operations`.
  - Operation parameter badges use the same detail payload.
- `frontend/src/payload.test.ts`
  - Updated the source assertion to guard that the main Vue workflow renders
    detail operations from the loaded detail payload.
- `tasks.md`
  - Marked the detail-operation payload boundary complete.
- `docs/migration/foolframe-parity.md`
  - Recorded the FoolFrame parity increment.

# Validation

- `cd frontend && npm test -- --run`
  - Passed: 3 test files, 49 tests.
- `cd frontend && npm run build`
  - Passed: `vue-tsc --noEmit && vite build`.
- `python3 scripts/check_repo_harness.py`
  - Passed.
- `git diff --check`
  - Passed.
- `docker compose up -d --build frontend`
  - Passed; frontend and backend containers recreated/up.
- `python3 scripts/runtime_doctor.py`
  - Passed: backend, frontend, MySQL, Redis, `getlistview`, `querydata`,
    `querydatadetail`, `inputquery`, and `getmkqview`.
- `wc -l frontend/src/App.vue frontend/src/payload.test.ts`
  - `frontend/src/App.vue`: 1999 lines.
  - `frontend/src/payload.test.ts`: 460 lines.

# Runtime Evidence

- `docker compose ps`
  - `fool-service-backend-1`: Up on `8080`.
  - `fool-service-frontend-1`: Up on `8081`.
  - MySQL and Redis healthy.

# Risks

- This only fixes the operation button source in the Vue detail panel. It does
  not add broader UI coverage for every legacy operation location or result
  navigation mode.

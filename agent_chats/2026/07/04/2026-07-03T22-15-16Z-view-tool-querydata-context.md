# Prompt

User called out that the migrated page should render from View first, then query
data from that View, and that binding to concrete business DTOs is wrong.

# Scope

- Keep Vue data loading tied to the rendered View metadata.
- Avoid broad backend changes because `querydata` already resolves `ViewId` to
  `View -> viewModel -> ModelDataService`.
- Keep `App.vue` under the current 2000-line control point.

# Changes

- `frontend/src/App.vue`
  - API Tools `Query Data` now copies the requested View ID into the View loader,
    runs `getlistview`, and then calls `querydata` with the loaded
    `currentViewId`.
  - This prevents shared `dataResponse` rows from being rendered under a
    different View's column metadata.
- `frontend/src/payload.test.ts`
  - Added a source-level regression check that the tools query loads View
    metadata before `/api/v1/data/querydata`.
- `tasks.md`
  - Marked the View-first tools query path complete.
- `docs/migration/foolframe-parity.md`
  - Recorded the parity increment and the reason it avoids business DTO
    shortcuts.

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
  - Passed; rebuilt frontend and, through compose dependencies, backend.
- `python3 scripts/runtime_doctor.py`
  - Passed: compose services, `/test`, `getlistview`, `querydata`,
    `inputquery`, and `getmkqview`.

# Runtime Evidence

- `docker compose ps`
  - `fool-service-backend-1`: Up, port `8080`.
  - `fool-service-frontend-1`: Up, port `8081`.
  - MySQL and Redis healthy.

# Risks

- The generic API Tools panel still allows entering arbitrary View IDs by
  design, but it now promotes that View into the rendered context before
  querying rows.
- The code path is covered by source-level frontend tests and runtime smoke,
  not a browser click test for the exact tools button.

# Follow-ups

- Continue replacing seed-specific docs/examples where they confuse parity
  evidence with business DTO coupling.

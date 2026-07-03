# Select Existing Child Items

## Prompt

- Continue FoolFrame migration progress after the Vue View-first workflow.
- Keep the frontend bound to View metadata, not concrete business DTO fields.

## Scope

- Added basic select-from-existing support for child collection groups where
  `querydatadetail.Items[]` returns `selectFromExists=true`.
- Reused existing legacy APIs:
  - `POST /api/v1/view/getlistview`
  - `POST /api/v1/data/querydata`
  - `POST /api/v1/data/saveobj`
- Added generic row-to-child-field mapping in `viewWorkflow.ts`; no new backend
  endpoint or frontend dependency was added.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- `cd frontend && npm test`
  - Passed: 2 test files, 38 tests.
- `cd frontend && npm run build`
  - Passed: `vue-tsc --noEmit` and Vite production build.
- `git diff --check`
  - Passed.
- `python scripts/check_repo_harness.py`
  - Passed.

## Runtime Evidence

- `docker compose ps`
  - Backend, frontend, MySQL, and Redis are running; MySQL is healthy.
- `POST /api/v1/data/querydatadetail` for Docker-seeded `OrderList` object
  `1001`
  - Child group `items` returned `selectedView=0`, `listViewId=0`, and
    `selectFromExists=false`.
- The current Docker-seeded `OrderList` child group has
  `selectFromExists=false`, so the new branch is covered by focused helper
  tests and TypeScript/template build validation rather than a live selectable
  child-group smoke.

## Skipped Checks

- Full Maven tests were not rerun; this slice changes only Vue/helper code and
  documentation.
- Docker rebuild was deferred until the next frontend runtime slice because the
  local frontend production build already passed.

## Risks

- Candidate search and pagination are not implemented yet; the first slice
  loads the first page of the configured candidate View.

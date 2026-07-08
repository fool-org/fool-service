# Vue View-First Render Boundary

## Prompt

The frontend should query the View to render the page first, then query data
according to that View. Binding page rendering to concrete business DTO fields
is the wrong migration shape.

## Scope

- Moved main list render-column resolution into `listRenderColumns(view, data)`.
- Made `listRenderColumns` return no columns until a `getlistview` payload is
  present, while preserving the existing View-column, `querydata.Cols`, then
  row-`Items` fallback order after the View exists.
- Added `renderedDetailFields` and `renderedDetailGroups` wrappers so Vue
  detail UI does not render fields or child groups from `querydatadetail` DTO
  payloads until `getreaditemview` metadata exists.
- Made `queryDetail` and `initNew` abort before data calls when the read-item
  View cannot be loaded.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Red: `cd frontend && npm test -- --run viewWorkflow.test.ts payload.test.ts`
  failed on missing `listRenderColumns`, `renderedDetailFields`,
  `renderedDetailGroups`, and missing read-view abort in `queryDetail`.
- Green: `cd frontend && npm test -- --run viewWorkflow.test.ts payload.test.ts`
  passed.
- Full frontend tests: `cd frontend && npm test -- --run` passed.
- Frontend build: `cd frontend && npm run build` passed.
- Repository checks: `git diff --check` passed.
- Harness: `python scripts/check_repo_harness.py` passed.

## Skipped Checks

- Docker frontend image rebuild and browser smoke were not run in this slice.

## Risks

- This intentionally keeps compatibility fallback columns from `querydata.Cols`
  and row `Items` after the View exists. It only blocks data-only page
  structure bootstrap.

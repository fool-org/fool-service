# View Metadata Workflow

## Prompt

- Continue the FoolFrame migration with Docker/Vue as the active target.
- Correct the default Vue workflow so it queries View metadata first, then uses
  that View shape to query and render data instead of binding to a concrete
  business DTO such as `OrderList` / `symbol` / `state`.

## Scope

- Replaced the default Vue first screen with a metadata-driven View workflow.
- Kept `OrderList` only as the default seeded `viewName`; rendering and save
  payload construction now use View metadata and detail data fields.
- Added generic helper coverage for list row rendering, detail save
  `Propertyies`, and child collection `Items` / `AddedItems` / `DelteItems`
  payload construction.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/payload.test.ts`
- `frontend/src/style.css`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- `cd frontend && npm test`
  - Passed: 2 test files, 37 tests.
- `cd frontend && npm run build`
  - Passed: `vue-tsc --noEmit` and Vite production build.
- `git diff --check -- frontend/src/App.vue frontend/src/style.css frontend/src/payload.test.ts frontend/src/viewWorkflow.ts frontend/src/viewWorkflow.test.ts`
  - Passed.
- `python scripts/check_repo_harness.py`
  - Passed.
- `docker compose up -d --build frontend`
  - Passed. Compose also rebuilt the backend image because of service
    dependency resolution; Maven package completed with `BUILD SUCCESS`.

## Runtime Evidence

- Docker stack after rebuild:
  - `backend` on `0.0.0.0:8080`
  - `frontend` on `0.0.0.0:8081`
  - `mysql` healthy on `127.0.0.1:3307`
  - `redis` on `127.0.0.1:6380`
- `curl http://localhost:8080/test`
  - Passed, returned 8 rows.
- `curl http://localhost:8081/`
  - Passed, returned the Vue app HTML.
- Backend View/data contracts checked directly:
  - `POST /api/v1/view/get-view` for `OrderList` returned `id=100` and
    3 `tableColumn` entries.
  - `POST /api/v1/data/query-list` returned rows with `values` and formatted
    `items`; smoke request returned `total=8`, `rows=3`.
  - `POST /api/v1/data/querydatadetail` returned `simpleData` and child
    `items[].properties`.

## Skipped Checks

- Full Maven tests were not rerun because this slice is frontend-only and does
  not change Java code or API contracts.
- Browser click-through was not run in this entry; frontend unit/build and
  Docker HTTP smoke validation covered compile, helper behavior, and served
  assets.

## Risks

- The metadata-driven editor currently uses plain inputs for all field types.
  Enum, lookup, readonly, format-specific, and select-from-existing widgets
  remain follow-up work.
- `App.vue` is still a large file; this slice moved reusable View logic into
  `viewWorkflow.ts`, but a future component split is still warranted.

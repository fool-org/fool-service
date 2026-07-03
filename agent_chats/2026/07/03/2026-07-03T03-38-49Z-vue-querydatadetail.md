# Vue querydatadetail panel

## Prompt

Continue the active migration goal:

1. Bring the environment up with Docker.
2. Complete migration against `../FoolFrame`.
3. Use Vue for the frontend.
4. Commit atomically and promptly.

## Scope

- Added a typed Vue payload builder for the legacy
  `/api/v1/data/querydatadetail` request shape.
- Added a visible Detail Data panel to the existing Vue migration console.
- Rendered returned `SimpleData` rows in a compact table.
- Updated the migration parity document.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`

## Validation

- RED: `cd frontend && npm test`
  - Failed before implementation with `buildQueryDataDetailRequest is not a function`.
- GREEN: `cd frontend && npm test`
  - Passed: 1 file, 6 tests.
- `cd frontend && npm run build`
  - Passed `vue-tsc --noEmit` and Vite production build.
- `docker compose up -d --build frontend`
  - Built frontend and backend images.
- `docker compose up -d --force-recreate frontend`
  - Recreated frontend and backend containers so the running Nginx asset used
    the new build.
- `curl http://localhost:8081/`
  - Returned `/assets/index-BUxREtY5.js`.
- `curl http://localhost:8081/assets/index-BUxREtY5.js | rg -o "Detail Data|querydatadetail|Load Detail"`
  - Found the new Vue detail UI strings in the running asset.
- `curl -H 'Content-Type: application/json' -d '{"viewId":100,"objId":"1001"}' http://localhost:8080/api/v1/data/querydatadetail`
  - Returned `OrderList` detail data with `Order ID`, `Symbol`, and `State`.
- `docker compose ps`
  - Backend, frontend, MySQL, and Redis are running; MySQL and Redis are healthy.
- `python scripts/check_repo_harness.py`
  - Passed.
- `git diff --check`
  - Passed.

## Skipped Checks

- No browser screenshot was captured; this slice uses the existing console
  layout and was verified through the built/running asset plus backend detail
  response.

## Risks

- This panel only reads detail data. It does not yet transform a detail response
  into editable save-object form state.

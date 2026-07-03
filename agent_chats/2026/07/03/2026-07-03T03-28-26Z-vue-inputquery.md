# Vue inputquery candidate lookup

## Prompt

Continue the active migration goal:

1. Bring the environment up with Docker.
2. Complete migration against `../FoolFrame`.
3. Use Vue for the frontend.
4. Commit atomically and promptly.

## Scope

- Added a typed Vue payload builder for the legacy `/api/v1/data/inputquery`
  request shape.
- Added a visible Input Query panel to the existing Vue migration console.
- Reused the current View Name field and existing `postApi` helper.
- Updated the migration parity document.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.ts`
- `frontend/src/payload.test.ts`
- `frontend/src/style.css`
- `docs/migration/foolframe-parity.md`

## Validation

- RED: `cd frontend && npm test`
  - Failed before implementation with `buildInputQueryRequest is not a function`.
- GREEN: `cd frontend && npm test`
  - Passed: 1 file, 4 tests.
- `cd frontend && npm run build`
  - Passed `vue-tsc --noEmit` and Vite production build.
- `docker compose up -d --build frontend`
  - Built frontend and backend images.
- `docker compose up -d --force-recreate frontend`
  - Recreated frontend and backend containers after the first run left the old
    frontend container running.
- `curl http://localhost:8081/`
  - Returned the new production asset names:
    `/assets/index-CEYJ71f2.js` and `/assets/index-DSUV3CQI.css`.
- `curl http://localhost:8081/assets/index-CEYJ71f2.js | rg -o "Input Query|inputquery|Query Candidates"`
  - Found the new Vue inputquery UI strings in the running asset.
- `curl http://localhost:8080/test`
  - Returned the seeded order rows.
- `curl -H 'Content-Type: application/json' -d '{"viewName":"OrderList","viewItemId":"symbol","text":"BTC"}' http://localhost:8080/api/v1/data/inputquery`
  - Returned `{"code":0,"message":"success","data":{"items":[]}}`.
- `docker compose ps`
  - Backend, frontend, MySQL, and Redis are running; MySQL and Redis are healthy.
- `python scripts/check_repo_harness.py`
  - Passed.
- `git diff --check`
  - Passed.

## Skipped Checks

- No browser screenshot was captured; this slice only adds a simple existing-page
  form and table using already validated CSS patterns.

## Risks

- The panel exposes the backend inputquery flow but does not yet wire candidate
  selection into a full `saveobj` edit workflow.

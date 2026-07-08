# Vue Table View Columns Gate

## Prompt

User noted that the migration should first inspect/render the View page, then
query data from that View; binding page rendering to concrete business DTOs is
wrong.

## Scope

- Tightened the shared Vue row table so returned `querydata` rows cannot render
  a page or row actions unless rendered View columns exist.
- Added a regression guard to the existing frontend source contract tests.
- Updated migration/task state for the view-first table boundary.

## Changed Files

- `frontend/src/ListDataTable.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- RED: `cd frontend && npm test -- src/payload.test.ts`
  - Failed at `does not render data rows before View columns exist` because the
    table still used `columns.length || rows.length`.
- GREEN: `cd frontend && npm test -- src/payload.test.ts`
  - `57 passed`.
- GREEN: `cd frontend && npm test`
  - `92 passed`.
- GREEN: `cd frontend && npm run build`
  - `vue-tsc --noEmit && vite build` succeeded.
- GREEN: `python scripts/check_repo_harness.py`
  - Repository harness validation passed.
- GREEN: `git diff --check`
- GREEN: `docker compose up -d --build frontend`
  - Frontend image built; compose also rebuilt backend image with
    `mvn -DskipTests package` successfully.
- GREEN: `docker compose up -d --no-deps --force-recreate --build frontend`
  - Recreated `fool-service-frontend-1`.
- GREEN: `docker compose ps`
  - Frontend container recreated and running; MySQL healthy; Redis healthy;
    backend running.
- GREEN: `curl -sS -o /tmp/fool-service-frontend-smoke.html -w "%{http_code} %{size_download}\n" http://localhost:8081/`
  - `200 399`
- GREEN: `curl -sS -o /tmp/fool-service-backend-smoke.txt -w "%{http_code} %{size_download}\n" http://localhost:8080/test`
  - `200 465`

## Risks

- This intentionally hides row/action rendering when a View has no columns,
  even if data rows are returned. That matches the view-first migration rule;
  a future explicit empty-column View design should add its own metadata-driven
  rendering contract instead of falling back to DTO rows.

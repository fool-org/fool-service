# Vue Report Grid Panel

## Prompt

- Continue the Docker-first FoolFrame migration.
- Keep the frontend on Vue.
- Commit small migration increments atomically.

## Scope

- Added Vue API types for legacy `makereport` request/result payloads.
- Added `buildMakeReportRequest` for the backend DTO shape.
- Added a Vue operator panel for `POST /api/v1/report/makereport`.
- Rendered the returned flat report cells as a simple grid.

## Changed Files

- `frontend/src/api.ts`
- `frontend/src/payload.ts`
- `frontend/src/payload.test.ts`
- `frontend/src/App.vue`
- `docs/migration/foolframe-parity.md`

## Validation

- RED:
  `cd frontend && npm test` failed with 2 expected failures:
  missing `Report Grid`/`/api/v1/report/makereport` in `App.vue`, and missing
  `buildMakeReportRequest`.
- GREEN:
  `cd frontend && npm test` passed with 16 tests.
- GREEN:
  `cd frontend && npm run build` passed with `vue-tsc --noEmit` and Vite
  production build.
- GREEN:
  `docker compose up -d --build frontend` built the frontend image; Compose also
  rebuilt the backend dependency image successfully.
- GREEN:
  `docker compose up -d --no-deps --force-recreate frontend` recreated the
  frontend container from the new image.
- GREEN:
  `docker compose ps` showed backend, frontend, MySQL, and Redis running, with
  MySQL and Redis healthy.
- GREEN:
  `curl http://localhost:8081/` returned the Vite-built HTML.
- GREEN:
  `curl -H 'Content-Type: application/json' -d '{"viewId":100,"currentPage":1,"pageSize":10,"queryFilter":"order_state=\"0\"","reportCols":[{"colName":"Symbol","index":1},{"colName":"State","index":2}]}' http://localhost:8081/api/v1/report/makereport`
  returned success with `Symbol`, `State`, `BTC-USDT`, and `Open` cells through
  the Compose frontend proxy.
- GREEN:
  The built frontend bundle served from `localhost:8081` contains `Report Grid`
  and `makereport`.

## Skipped Checks

- Full backend Maven tests were not rerun for this frontend-only change.
- Browser click-through was not run; source tests, production build, served
  bundle check, and HTTP proxy smoke covered this slice.

## Risks And Follow-Ups

- This exposes the existing flat report-grid path only.
- Remaining report work is still backend source adapters, saved report
  metadata, complex report BoolExp mapping, export integration, and richer Vue
  report operations after those backend surfaces exist.

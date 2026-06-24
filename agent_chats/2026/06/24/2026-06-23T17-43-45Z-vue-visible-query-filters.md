# Vue visible query filters

## Scope

- Added structured visible filters to the Vue migration console's Data Query panel.
- The Vue UI now emits Spring `QueryValue` payloads for equality and range filters while preserving the advanced JSON filter box.
- This connects the migrated `fool-query` between/composite SQL behavior to the operator-facing Vue workflow.

## Red

- `cd frontend && npm test`
  - Failed one new test because `buildQueryRequest` ignored `visibleFilters` and only returned the parsed JSON filter object.

## Green

- `cd frontend && npm test`
  - Passed: 2 tests.
- `cd frontend && npm run build`
  - Passed `vue-tsc --noEmit` and Vite production build.

## Docker runtime

- `docker compose build frontend && docker compose up -d frontend`
  - Rebuilt the Vue app with `npm run build`.
  - Restarted the frontend container after MySQL and Redis were healthy.

## Rendered QA

- Browser target: `http://localhost:8081/`.
- Flow under test: app loads -> Data Query quick range filter uses default `orderId` range `1001..1002` -> Query Data -> result table renders the two seeded rows.
- Browser checks:
  - Page identity: URL `http://localhost:8081/`, title `Fool Service`.
  - Not blank: DOM contained `Migration Console` and the Quick Filter controls.
  - Framework overlay: none observed in DOM/screenshot.
  - Console health: no `error` or `warn` logs before or after interaction.
  - Interaction proof: clicking `Query Data` rendered rows `1001 / BTC-USDT / OPEN` and `1002 / ETH-USDT / FILLED`.
  - Mobile viewport `390x844`: `Migration Console`, Quick Filter controls, and Query Data button were visible with no console errors.
- Screenshot artifacts:
  - `/tmp/fool-service-vue-filter-desktop.png`
  - `/tmp/fool-service-vue-filter-mobile.png`

## Final checks

- `docker compose ps`
  - Backend and frontend were running; MySQL and Redis were healthy.
- `cd frontend && npm test && npm run build`
  - Passed: 2 Vitest tests, `vue-tsc --noEmit`, and Vite production build.
- HTTP smoke:
  - `curl -fsS http://localhost:8081/`
    - Returned 399 bytes.
  - `curl -fsS -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":{"orderId":{"values":["1001","1002"]}}}' http://localhost:8080/api/v1/data/query-list`
    - Returned 296 bytes with `total: 2` and the two seeded rows.
- `python scripts/check_repo_harness.py`
  - Passed.
- `git diff --check`
  - Passed with no output.

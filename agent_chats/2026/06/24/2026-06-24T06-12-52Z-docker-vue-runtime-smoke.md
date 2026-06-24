# Docker Vue Runtime Smoke

## Prompt

- Continue the active goal: bring the environment up with Docker, complete migration against `../FoolFrame`, use Vue for the frontend, and keep atomic commits timely.

## Scope

- Verified the already-running Docker Compose stack.
- Captured backend, Vue frontend, view metadata, and data-query smoke outputs.
- Updated `tasks.md` to mark the runtime evidence bundle item complete.

## Changes

- Added this delivery-evidence entry for the current Docker/Vue runtime smoke.
- Updated `tasks.md` current-focus runtime evidence item from pending to complete.

## Validation

- Docker state:
  - `docker compose ps`
  - Result: backend and frontend running; MySQL and Redis running and healthy.
- Runtime smoke:
  - `curl -fsS http://localhost:8080/test > /tmp/fool-service-smoke-test.out`
  - `curl -fsS http://localhost:8081/ > /tmp/fool-service-smoke-frontend.out`
  - `curl -fsS -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view > /tmp/fool-service-smoke-view.out`
  - `curl -fsS -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":{"orderId":{"values":["1001","1002"]}}}' http://localhost:8080/api/v1/data/query-list > /tmp/fool-service-smoke-query.out`
  - Result: all commands exited 0.
- Output sizes:
  - `/tmp/fool-service-smoke-test.out`: 132 bytes.
  - `/tmp/fool-service-smoke-frontend.out`: 399 bytes.
  - `/tmp/fool-service-smoke-view.out`: 309 bytes.
  - `/tmp/fool-service-smoke-query.out`: 296 bytes.

## Runtime Evidence

- `GET /test` returned seeded order rows, including order ids `1` and `2`.
- `GET http://localhost:8081/` returned the Vue `index.html` shell with built JS/CSS assets.
- `POST /api/v1/view/get-view` returned `code: 0`, `viewName: OrderList`, and columns `Order ID`, `Symbol`, and `State`.
- `POST /api/v1/data/query-list` with `orderId` values `1001` and `1002` returned `code: 0`, `total: 2`, and seeded `BTC-USDT` / `ETH-USDT` rows.

## Risks

- This is an HTTP smoke evidence bundle, not a reusable browser automation doctor.
- The full backend goal remains incomplete because FoolFrame migration parity still has remaining module behavior.

## Follow-ups

- Add the reusable browser/runtime doctor listed in `tasks.md` backlog when the workflow is stable enough to automate.
- Commit this evidence/task-state slice separately once `.git` writes are available.

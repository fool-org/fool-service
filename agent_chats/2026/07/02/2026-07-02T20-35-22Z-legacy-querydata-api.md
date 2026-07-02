# Legacy querydata API

## Prompt

- Continue the active migration goal:
  1. run the environment with Docker;
  2. complete migration against `../FoolFrame`;
  3. use Vue for the frontend;
  4. make timely atomic commits.

## Scope

- Added legacy-compatible `POST /api/v1/data/querydata` for the old
  `QueryDataOption` payload shape.
- Reused the existing list-query path and added raw legacy `QueryFilter`
  composition after the stored view filter.
- Added a Vue API type for the legacy request payload.
- Updated migration parity notes for the new endpoint and `fool-view` file
  count.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/api/DataController.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/LegacyQueryDataRequest.java`
- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `fool-view/src/test/java/org/fool/framework/view/api/DataControllerLegacyQueryDataTest.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceOrderingTest.java`
- `frontend/src/api.ts`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/02/2026-07-02T20-35-22Z-legacy-querydata-api.md`

## Validation

- RED:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=DataControllerLegacyQueryDataTest,DataQueryServiceOrderingTest#queryLegacyViewDataAppliesLegacyQueryFilterAfterViewFilter test`
  failed because `LegacyQueryDataRequest` was missing.
- GREEN focused:
  same command passed with 2 tests.
- Backend:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am test`
  passed with 42 `fool-view` tests.
- Frontend:
  `cd frontend && npm test && npm run build` passed.
- Harness:
  `python scripts/check_repo_harness.py` passed.
- Whitespace:
  `git diff --check` passed.
- Docker:
  `docker compose up -d --build backend frontend` passed.

## Runtime Evidence

- `docker compose ps` showed backend, frontend, MySQL, and Redis running.
- `curl http://localhost:8080/test` returned seeded order rows.
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SELECT id, view_name FROM fool_sys_view ORDER BY id LIMIT 10;"` returned `100 OrderList`.
- `curl -H 'Content-Type: application/json' -d '{"viewId":100,"pageSize":10,"pageIndex":1,"queryFilter":"order_state=\"OPEN\""}' http://localhost:8080/api/v1/data/querydata`
  returned `code:0`, `totalItem:1`, and the seeded `BTC-USDT` `OPEN` row.
- `curl http://localhost:8081/` returned the Vue HTML shell.

## Skipped Checks

- No browser screenshot check; this change only adds an API type, not a visible
  Vue UI flow.

## Risks / Follow-ups

- `orderByItem` and `orderByType` are accepted for payload compatibility but
  not used, matching the current migrated default list ordering path. Add
  explicit legacy order-index parity only when the old handler behavior is
  proven to require it.
- `querydatadetail`, save/init, operation execution, and report endpoints remain
  separate migration gaps.

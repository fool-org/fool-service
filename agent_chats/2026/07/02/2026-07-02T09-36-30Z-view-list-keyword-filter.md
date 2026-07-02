# Prompt

- Continue the active goal: run the environment with Docker, migrate against
  `../FoolFrame`, keep the frontend on Vue, and commit atomically.

# Scope

- Migrated legacy `QueryDataOption.QueryFilter` / `ListViewQueryContext`
  global text filtering for list views.
- Kept the implementation scoped to simple read-only list columns supported by
  the current Java dynamic SQL path.

# Changes

- `QueryDataRequest` accepts `keyword`.
- `DataController` passes `keyword` into `DataQueryService`.
- `DataQueryService` adds a legacy-style OR `LIKE` filter over read-only
  non-collection, non-business-object list item columns.
- Vue payload building trims and sends `keyword`.
- Vue data-query panel exposes a minimal keyword input.
- `docs/migration/foolframe-parity.md` records keyword filter parity.

# Validation

- Red backend:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataQueryServiceOrderingTest test`
  failed because `DataQueryService.queryViewDataList(..., keyword)` did not
  exist.
- Red frontend:
  `cd frontend && npm test` failed because `buildQueryRequest` did not send
  `keyword`.
- Green focused backend:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataQueryServiceOrderingTest test`
  passed, 3 tests.
- Green frontend payload:
  `cd frontend && npm test` passed, 3 tests.
- Frontend Vue:
  `cd frontend && npm run build` passed.
- Harness:
  `python scripts/check_repo_harness.py` passed.
- Diff hygiene:
  `git diff --check` passed.
- Docker build/runtime:
  `docker compose up -d --build` passed, rebuilding backend and frontend
  images and starting the stack.
- Runtime smoke:
  `docker compose ps` showed MySQL and Redis healthy, backend on `8080`, and
  frontend on `8081`.
- Runtime smoke:
  `curl -sf http://localhost:8080/test` returned seeded order data.
- Runtime smoke:
  `curl -sfI http://localhost:8081/` returned `HTTP/1.1 200 OK`.
- Runtime keyword smoke:
  `curl -sf -H 'Content-Type: application/json' -d '{"viewName":"OrderList","keyword":"BTC","pageInfo":{"pageIndex":1,"pageSize":10}}' http://localhost:8080/api/v1/data/query-list`
  returned `code:0`, `total:1`, and the `BTC-USDT` row.
- Backend module:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dspring.datasource.url="jdbc:mysql://mysql:3306/car_wash?serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&allowMultiQueries=true&autoReconnect=true" -Dspring.datasource.username=root -Dspring.datasource.password=Pa88word test`
  passed, 18 tests.

# Runtime Notes

- Legacy C# source:
  `Soway.Server/Querylist/HandlerQueryData.cs` passes
  `QueryDataOption.QueryFilter` into
  `SCPB05-Soway.Model/Context/ListViewQueryContext.cs`, where it is applied as
  OR `LIKE` clauses across read-only view items.

# Risks

- Business-object keyword joins remain out of scope for this slice because the
  current Java list-query SQL generator does not build those joins.

# Follow-ups

- Complete richer query-to-view integration and business-object list query
  parity.

# Linked Commits

- Pending at evidence creation.

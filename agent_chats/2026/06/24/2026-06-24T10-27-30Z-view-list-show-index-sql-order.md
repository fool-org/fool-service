# Prompt

- Continue the active goal: run the environment with Docker, migrate against
  `../FoolFrame`, keep the frontend on Vue, and commit atomically.

# Scope

- Migrated legacy FoolFrame list-query default ordering:
  `HandlerQueryData` chooses the first `ViewItem.ShowIndex` item and
  `ListViewQueryContext` orders list rows descending by that column.
- Kept the change scoped to SQL generation, model-data paging, view query
  orchestration, tests, migration parity docs, and delivery evidence.

# Changes

- `SqlGenerator` now supports optional `ORDER BY <column> ASC|DESC` before
  paged `LIMIT/OFFSET`, while preserving the existing overloads.
- `ModelDataService.getDataListWithPageInfo` now has an overload that forwards
  optional order metadata to the SQL generator.
- `DataQueryService` now sorts view items by `showIndex`, maps the first valid
  item to a model property column, and requests descending SQL order for list
  queries.
- Added focused tests for the generated SQL and the service-level selection of
  the first legacy `ShowIndex` column.
- `docs/migration/foolframe-parity.md` records this ordering parity as
  completed.

# Validation

- Red:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=SqlGeneratorTest,DataQueryServiceOrderingTest test`
  failed because `SqlGenerator` did not yet expose the order-column overload.
- Green focused:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=SqlGeneratorTest,DataQueryServiceOrderingTest test`
  passed, 2 tests.
- Wider backend:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dspring.datasource.url="jdbc:mysql://mysql:3306/car_wash?serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&allowMultiQueries=true&autoReconnect=true" -Dspring.datasource.username=root -Dspring.datasource.password=Pa88word test`
  passed, 16 tests.
- Frontend:
  `npm test` passed in `frontend/`, 2 tests.
- Frontend build:
  `npm run build` passed in `frontend/`.
- Harness:
  `python scripts/check_repo_harness.py` passed.
- Whitespace:
  `git diff --check` passed.
- Docker:
  `docker compose up -d --build` passed; backend Docker build reported Maven
  `BUILD SUCCESS`.
- Runtime reload:
  `docker compose up -d --force-recreate backend frontend` recreated both
  runtime containers.

# Runtime Evidence

- `docker compose ps` showed backend and frontend running, MySQL and Redis
  healthy.
- `curl -sf http://localhost:8080/test` returned the seeded order JSON.
- `curl -sfI http://localhost:8081/` returned `HTTP/1.1 200 OK`.
- `curl -sf -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
  returned `OrderList` columns with `showIndex` values `1`, `2`, and `3`.
- `curl -sf -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageIndex":1,"pageSize":10}}' http://localhost:8080/api/v1/data/query-list`
  returned row values in default order `orderId=1002` then `orderId=1001`.
- `docker compose logs --tail=120 backend | grep 'ORDER BY'` showed
  `ORDER BY \`order_id\` DESC LIMIT ? OFFSET ?`.

# Risks

- Existing unpaged `getDataList` still has no default ordering surface; this
  commit only migrates the paged view-list path that mirrors the legacy
  list-query workflow.
- Full self-contained backend tests still depend on an external/Compose MySQL
  override for DAO-backed modules.

# Follow-ups

- Continue richer saved-query/report execution and query-to-view integration
  parity beyond the default view-list ordering path.

# Linked Commits

- Pending at evidence creation.

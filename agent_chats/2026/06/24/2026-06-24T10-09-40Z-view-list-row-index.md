# Prompt

- Continue the active goal: run the environment with Docker, migrate against
  `../FoolFrame`, keep the frontend on Vue, and commit atomically.

# Scope

- Migrated the legacy `Soway.Server/Querylist/ResultQuery.cs`
  `QueryKeyValueResult.RowIndex` response contract into the Spring/Vue list
  query path.
- Kept the change scoped to list-query response mapping, Vue API typing,
  migration parity docs, and delivery evidence.

# Changes

- `ListDataItem` now exposes `rowIndex`.
- `ViewDataAdapter` computes the row index from `PageNavigatorResult`
  `(pageIndex - 1) * pageSize + item offset + 1`, with an in-page fallback when
  pagination metadata is absent.
- `ViewDataAdapterTest` covers the serialized JSON contract for page 2 row
  offset.
- `frontend/src/api.ts` includes the `rowIndex` field in the Vue list-row type.
- `docs/migration/foolframe-parity.md` records legacy list-query row-index
  parity in the `Soway.Server`, `fool-view`, and Vue replacement sections.

# Validation

- Red:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=ViewDataAdapterTest test`
  failed with `ViewDataAdapterTest.listRowsExposeLegacyRowIndexFromPageOffset`
  because serialized `rowIndex` was absent.
- Green focused:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=ViewDataAdapterTest test`
  passed, 3 tests.
- Wider backend:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dspring.datasource.url="jdbc:mysql://mysql:3306/car_wash?serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&allowMultiQueries=true&autoReconnect=true" -Dspring.datasource.username=root -Dspring.datasource.password=Pa88word test`
  passed, 13 tests.
- Frontend:
  `npm test` passed in `frontend/`, 2 tests.
- Frontend build:
  `npm run build` passed in `frontend/`.
- Harness:
  `python scripts/check_repo_harness.py` passed.
- Whitespace:
  `git diff --check` passed.
- Docker:
  `docker compose up -d --build` passed; backend image Maven package reported
  `BUILD SUCCESS`.
- Runtime reload:
  `docker compose up -d --force-recreate backend frontend` recreated both
  runtime containers.

# Runtime Evidence

- `docker compose ps` showed backend and frontend running, MySQL and Redis
  healthy.
- `curl -sf http://localhost:8080/test` returned the seeded order JSON.
- `curl -sfI http://localhost:8081/` returned `HTTP/1.1 200 OK`.
- `curl -sf -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list`
  returned list rows containing `"rowIndex":1` and `"rowIndex":2`.

# Risks

- Legacy default list ordering by `View.Items.ShowIndex` remains deferred
  because the current Java `ViewItem` metadata has not yet migrated that field.
- Full self-contained backend tests still depend on an external/Compose MySQL
  override for DAO-backed modules.

# Follow-ups

- Migrate legacy `ShowIndex` metadata and default descending list-query order
  once the field is represented in `ViewItem` and the seed/schema.
- Continue query-to-view parity beyond the row response contract.

# Linked Commits

- Pending at evidence creation.

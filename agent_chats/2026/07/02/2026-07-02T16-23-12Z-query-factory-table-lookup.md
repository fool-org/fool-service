# Prompt

Continue the active migration goal: keep Docker running, migrate against
`../FoolFrame`, keep the frontend on Vue, and make timely atomic commits.

# Scope

Query factory table lookup parity with legacy `TableCollection`, which matches
table database names and display names after trimming and case normalization.

# Changes

- Added a red test proving `QueryFactory.getTable()` did not match `" ORDERS "`
  or `" 客户 "`.
- Normalized `QueryFactory.getTable()` lookup keys with `trim()` and uppercase,
  matching the legacy `TableCollection` lookup behavior.
- Updated `docs/migration/foolframe-parity.md` to list `QueryFactory` table
  lookup normalization.

# Validation

- RED:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-query -am -Dtest=QueryFactoryTest#queryFactoryFindsTablesAndKeepsEmptyColumnDefault test`
  failed with `expected:<QueryTable(showName=订单, dbName=orders)> but was:<null>`.
- GREEN:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-query -am -Dtest=QueryFactoryTest#queryFactoryFindsTablesAndKeepsEmptyColumnDefault test`
  passed.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-query -am -Dtest=QueryFactoryTest,QueryLookupCollectionTest test`
  passed.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-query -am test`
  passed.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.

# Runtime Evidence

- The validation commands ran inside the active `fool-service_default` Compose
  network.
- `docker compose ps` showed backend, frontend, MySQL, and Redis running; MySQL
  was healthy.
- `curl -fsS http://localhost:8080/test` returned seeded order JSON.
- `curl -fsS http://localhost:8081/` returned the Vue app HTML.
- `POST /api/v1/view/get-view` with `OrderList` returned `code:0`.
- `POST /api/v1/data/query-list` with `OrderList` returned `code:0` and two
  seeded rows.

# Risks

- Maven still emits the pre-existing duplicate `spring-jdbc` dependency warning
  for `fool-dao`.
- `DaoServiceTest` still logs a caught `NullPointerException` while the test
  class reports success; this was pre-existing noise and not changed here.
- This does not complete Query saved-query/report execution surfaces.

# Follow-ups

- Continue the remaining `SWDQ01-Soway.Query` saved-query/report execution and
  query-to-view parity work listed in `docs/migration/foolframe-parity.md`.

# Prompt

Continue the active migration goal: keep Docker running, migrate against
`../FoolFrame`, keep the frontend on Vue, and make timely atomic commits.

# Scope

Query SQL builder parity for legacy metadata that already stores table names,
aliases, or column names with SQL Server-style square brackets.

# Changes

- Added red tests showing `QuerySqlBuilder` double-wrapped bracketed table,
  alias, column, and join identifiers.
- Normalized one existing bracket pair before reusing the current SQL builder
  wrapping logic.
- Updated `docs/migration/foolframe-parity.md` to list bracketed identifier
  normalization under `fool-query` parity.

# Validation

- RED:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-query -am -Dtest=QuerySqlBuilderTest#sqlBuilderDoesNotDoubleWrapLegacyBracketedIdentifiers test`
  failed with `[[orders]] as [[o]]`.
- RED:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-query -am -Dtest=QuerySqlBuilderTest#tableSqlDoesNotDoubleWrapLegacyBracketedJoinIdentifiers test`
  failed with `[[o]].[[ID]]=[[i]].[[ORDER_ID]]`.
- GREEN:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-query -am -Dtest=QuerySqlBuilderTest#sqlBuilderDoesNotDoubleWrapLegacyBracketedIdentifiers+tableSqlDoesNotDoubleWrapLegacyBracketedJoinIdentifiers test`
  passed.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-query -am -Dtest=QuerySqlBuilderTest test`
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

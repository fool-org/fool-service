# Event case-insensitive object id column matching

## Prompt

- Continue the active Docker/Vue/FoolFrame migration goal after the AppManage connection-string slice.

## Scope

- Compared the legacy object-query path where model key columns are checked from a query result table and adjacent legacy DB code that compares key column names with upper-case normalization.
- Updated `JdbcEventObjectQuery` so the required object-id column is resolved by exact column label first, then by case-insensitive match.
- Kept row-value capture under the original JDBC column labels; only the object-id lookup is normalized.

## Changed Files

- `fool-event/src/main/java/org/fool/framework/event/JdbcEventObjectQuery.java`
- `fool-event/src/test/java/org/fool/framework/event/EventMigrationTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/06/24/2026-06-24T09-25-27Z-event-case-insensitive-id-column.md`

## Validation

- Red: `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-event -am -DfailIfNoTests=false -Dtest=EventMigrationTest#jdbcEventObjectQueryMatchesLegacyKeyColumnCaseInsensitively test`
  - Failed as expected with `Can't Gerneration Query Because The Id Column Isn't Included!`.
- Green: `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-event -am -DfailIfNoTests=false -Dtest=EventMigrationTest#jdbcEventObjectQueryMatchesLegacyKeyColumnCaseInsensitively test`
  - Passed.
- Broader Event: `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-event -am -DfailIfNoTests=false -Dtest=EventMigrationTest test`
  - Passed: 40 tests.
- Harness: `python scripts/check_repo_harness.py`
  - Passed.
- Patch hygiene: `git diff --check`
  - Passed.
- Runtime: `docker compose up -d --build`
  - Passed; backend Docker build ran `mvn -DskipTests package` successfully, and frontend image build reused the cached Vue build layer.
- Runtime refresh: `docker compose up -d --force-recreate backend frontend`
  - Passed; backend and frontend containers were recreated.
- Runtime status: `docker compose ps`
  - `backend`, `frontend`, `mysql`, and `redis` were running; MySQL and Redis were healthy.
- Backend smoke: `curl -sf http://localhost:8080/test`
  - Passed; returned seeded order JSON.
- Frontend smoke: `curl -sfI http://localhost:8081/`
  - Passed; returned `HTTP/1.1 200 OK`.

## Downgrades / Risks

- Direct host Maven was not used because the host Java is currently 1.8 and fails this Java 17 project with `invalid target release: 17`; backend test validation used a JDK17 Maven container instead.
- This does not broaden Event dynamic SQL generation, model loading, or scheduler behavior beyond object-id column matching.

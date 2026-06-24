# SimpleBoolExpression display string migration

## Prompt

- Continue the active Docker/Vue/FoolFrame migration goal after the query lookup string-indexer slice.

## Scope

- Compared legacy `../FoolFrame/src/Server/SWDQ01-Soway.Query/Soway.Query.BoolExp/SimpleBoolExpression.cs`.
- Migrated the legacy `SimpleBoolExpression.ToString()` display contract to Java `SimpleBoolExpression.toString()`.
- The display string now follows the legacy shape: selected table alias, column show name, compare operation display value, and formatted value.
- Updated FoolFrame parity notes for this specific behavior.

## Changed Files

- `fool-query/src/main/java/org/fool/framework/query/SimpleBoolExpression.java`
- `fool-query/src/test/java/org/fool/framework/query/SimpleBoolExpressionTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/06/24/2026-06-24T09-52-04Z-simple-bool-expression-display.md`

## Validation

- Red: `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -DfailIfNoTests=false -Dtest=SimpleBoolExpressionTest test`
  - Failed as expected because `SimpleBoolExpression.toString()` still returned the default object identity string.
- Green: `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -DfailIfNoTests=false -Dtest=SimpleBoolExpressionTest test`
  - Passed: 2 tests.
- Broader Query: `QUERY_TESTS=$(find fool-query/src/test/java -name '*Test.java' -exec basename {} .java \; | paste -sd, -); docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -DfailIfNoTests=false -Dtest="$QUERY_TESTS" test`
  - Passed: 57 tests.
- Harness: `python scripts/check_repo_harness.py`
  - Passed.
- Patch hygiene: `git diff --check`
  - Passed.
- Runtime build: `docker compose up -d --build`
  - Passed; backend Docker build ran `mvn -DskipTests package` successfully and frontend image built.
- Runtime refresh: `docker compose up -d --force-recreate backend frontend`
  - Passed; backend and frontend containers were recreated.
- Runtime status: `docker compose ps`
  - `backend`, `frontend`, `mysql`, and `redis` were running; MySQL and Redis were healthy.
- Backend smoke: `curl -sf http://localhost:8080/test`
  - Passed; returned seeded order JSON.
- Frontend smoke: `curl -sfI http://localhost:8081/`
  - Passed; returned `HTTP/1.1 200 OK`.

## Downgrades / Risks

- Direct host Maven was not used because the host Java is currently 1.8 and this Java 17 project fails there with `invalid target release: 17`; validation used a JDK17 Maven container.

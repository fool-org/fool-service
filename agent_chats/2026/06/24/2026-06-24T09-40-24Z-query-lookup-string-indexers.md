# Query lookup string indexer migration

## Prompt

- Continue the active Docker/Vue/FoolFrame migration goal after the report helper-type slice.

## Scope

- Compared legacy `../FoolFrame/src/Server/SWDQ01-Soway.Query/TableCollection.cs`.
- Compared legacy `../FoolFrame/src/Server/SWDQ01-Soway.Query/ColCollection.cs`.
- Added Java `get(String)` aliases to `QueryTableCollection` and `QueryColumnCollection` so callers can use the legacy string-indexer style in addition to the existing `find(...)` lookup.
- Updated FoolFrame parity notes and removed table/column lookup from the remaining Query gaps.

## Changed Files

- `fool-query/src/main/java/org/fool/framework/query/QueryTableCollection.java`
- `fool-query/src/main/java/org/fool/framework/query/QueryColumnCollection.java`
- `fool-query/src/test/java/org/fool/framework/query/QueryLookupCollectionTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/06/24/2026-06-24T09-40-24Z-query-lookup-string-indexers.md`

## Validation

- Red: `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -DfailIfNoTests=false -Dtest=QueryLookupCollectionTest test`
  - Failed as expected because `get(String)` resolved only to `ArrayList.get(int)`.
- Green: `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -DfailIfNoTests=false -Dtest=QueryLookupCollectionTest test`
  - Passed: 2 tests.
- Broader Query: `QUERY_TESTS=$(find fool-query/src/test/java -name '*Test.java' -exec basename {} .java \; | paste -sd, -); docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -DfailIfNoTests=false -Dtest="$QUERY_TESTS" test`
  - Passed: 56 tests.
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

- `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -DfailIfNoTests=false test` was not a valid broader signal in this environment because upstream `fool-dao` database tests tried to connect to MySQL without the Compose network and failed with `Communications link failure`.
- Direct host Maven was not used because the host Java is currently 1.8 and this Java 17 project fails there with `invalid target release: 17`; validation used a JDK17 Maven container.

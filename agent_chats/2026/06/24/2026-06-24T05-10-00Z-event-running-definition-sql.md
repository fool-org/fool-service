# Event Runtime Query Parity

## Prompt

- Continue the active goal: bring the environment up with Docker, complete migration against `../FoolFrame`, and use Vue for the frontend.

## Scope

- Compared legacy `../FoolFrame/src/Server/SCPB09-SOWAY.EVENT/EventDefFactory.cs`, where runtime definitions are loaded from `SW_EVT_DEF` with `EVTDEF_STATE = '0'`.
- Tightened the Java Event repository contract for the equivalent runtime-definition load.
- Compared legacy `../FoolFrame/src/Server/SCPB09-SOWAY.EVENT/EventCheckFactory.cs`, where `GetObjs` returns an empty list when `DefModel` is null.
- Tightened the Java Event object query path for missing model definitions.

## Changes

- Promoted `JdbcEventDefinitionRepository` running-definition SQL to `SELECT_RUNNING_DEFINITIONS_SQL`.
- Reused that same SQL in the production JDBC query path.
- Extended `EventMigrationTest.eventRuntimeLoadsOnlyRunningDefinitionsFromRepository` to assert the legacy table and running-state ordinal filter.
- Added `JdbcEventObjectQuery` early return for null/blank `modelId`, matching the old `DefModel == null` empty result branch and avoiding a bogus table lookup/query.
- Extended `EventMigrationTest` to assert missing-model object queries return empty results without touching JDBC.
- Updated `docs/migration/foolframe-parity.md` to explicitly record `SW_EVT_DEF` running-definition loading with `EVTDEF_STATE = 0` and null-model object-query parity.

## TDD

- Running-definition SQL red:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-event -am -Dtest=EventMigrationTest#eventRuntimeLoadsOnlyRunningDefinitionsFromRepository -DfailIfNoTests=false test`
  - Result: test compilation failed because `JdbcEventDefinitionRepository.SELECT_RUNNING_DEFINITIONS_SQL` did not exist.
- Running-definition SQL green:
  - Same command.
  - Result: `EventMigrationTest`: 1 test, 0 failures, 0 errors.
- Null-model object-query red:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-event -am -Dtest=EventMigrationTest#jdbcEventObjectQueryReturnsNoMatchesWhenDefinitionHasNoModel -DfailIfNoTests=false test`
  - Result: test failed because `JdbcEventObjectQuery` still called the table resolver for a definition without a model.
- Null-model object-query green:
  - Same command.
  - Result: `EventMigrationTest`: 1 test, 0 failures, 0 errors.

## Verification

- Focused Event repository contract:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-event -am -Dtest=EventMigrationTest#eventRuntimeLoadsOnlyRunningDefinitionsFromRepository -DfailIfNoTests=false test`
  - Result: `EventMigrationTest`: 1 test, 0 failures, 0 errors.
- Focused null-model object-query contract:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-event -am -Dtest=EventMigrationTest#jdbcEventObjectQueryReturnsNoMatchesWhenDefinitionHasNoModel -DfailIfNoTests=false test`
  - Result: `EventMigrationTest`: 1 test, 0 failures, 0 errors.
- Focused Event migration regression:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-event -am -Dtest=EventMigrationTest -DfailIfNoTests=false test`
  - Result: `EventMigrationTest`: 35 tests, 0 failures, 0 errors.

## Runtime Evidence

- Full backend package on the Compose network:
  - `docker run --rm --network fool-service_default -e SPRING_DATASOURCE_URL='jdbc:mysql://mysql:3306/car_wash?serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&allowMultiQueries=true&autoReconnect=true' -e SPRING_DATASOURCE_USERNAME=root -e SPRING_DATASOURCE_PASSWORD=Pa88word -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn package`
  - Result: `BUILD SUCCESS` across all 15 backend reactor modules in 24.072 s.
- Backend Compose image/runtime:
  - `docker compose build -q backend && docker compose up -d backend`: backend image rebuilt and container restarted.
  - `docker compose ps`: backend, frontend, mysql, and redis running; MySQL and Redis healthy.
- Runtime smoke after backend restart:
  - `GET http://localhost:8080/test`: response captured, 132 bytes.
  - `GET http://localhost:8081/`: Vue frontend response captured, 399 bytes.
  - `POST http://localhost:8080/api/v1/data/query-list` with `OrderList` `orderId` filter values `1001,1002`: response captured, 296 bytes.
- Repository hygiene:
  - `python scripts/check_repo_harness.py`: passed.
  - `git diff --check`: passed.

## Risks

- Running-definition SQL behavior is intentionally unchanged; the change makes the legacy filter explicit and test-covered.
- Missing-model definitions now skip object querying instead of resolving a null model name; this matches the legacy branch but may hide invalid configuration until a later diagnostics pass.

## Follow-ups

- Continue remaining Event runtime parity around deeper dynamic object-query behavior.

# Detail operation hydration

## Prompt

Continue the active goal: keep Docker running, migrate against `../FoolFrame`,
use Vue for the frontend, and commit in atomic slices.

## Scope

- Kept the existing Vue surface unchanged.
- Made legacy `querydatadetail` and `initnew` reuse `ViewDataService`, the same
  hydrated view path that now loads persisted `SW_SYS_VIEW_OPERATION` metadata.
- Accepted legacy web's `IdExp` JSON field for `querydatadetail`.
- Updated the migration parity document.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/LegacyQueryDataDetailRequest.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceDetailTest.java`
- `fool-view/src/test/java/org/fool/framework/view/api/DataControllerLegacyQueryDataDetailTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/03/2026-07-03T09-40-25Z-detail-operation-hydration.md`

## Validation

- RED backend:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataQueryServiceDetailTest test`
  failed because both legacy detail paths still read the view directly from
  `DaoService` and did not call `ViewDataService`.
- GREEN focused backend:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataQueryServiceDetailTest test`
  passed: 2 tests, 0 failures, 0 errors.
- RED DTO/controller:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataControllerLegacyQueryDataDetailTest test`
  failed because `IdExp` was not accepted by `LegacyQueryDataDetailRequest`.
- GREEN focused backend + DTO:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataControllerLegacyQueryDataDetailTest,DataQueryServiceDetailTest test`
  passed: 4 tests, 0 failures, 0 errors.
- GREEN module:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false test`
  passed: 80 tests, 0 failures, 0 errors.
- Docker runtime:
  `docker compose up -d --build backend` rebuilt and restarted the backend.
- Runtime smoke:
  `POST http://localhost:8080/api/v1/data/querydatadetail` with
  `{"viewId":100,"objId":"1001","IdExp":"#.id"}` returned operation `7001`.
  `POST http://localhost:8081/api/v1/data/initnew` with
  `{"ViewId":100,"ParentObjId":"5001"}` returned operation `7001`.
- Harness:
  `python scripts/check_repo_harness.py` passed.

## Skipped Checks

- No frontend changes were needed; the existing Vue console already renders the
  raw `querydatadetail` and `initnew` payloads.

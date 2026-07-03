# Legacy runoperation

## Prompt

Continue the active goal: keep Docker running, migrate against `../FoolFrame`,
use Vue for the frontend, and commit in atomic slices.

## Scope

- Compared `IDataService.RunOperation`, `DataService.RunOperation`,
  `HandlerRunOperation`, and the legacy web `runoperation` caller in
  `../FoolFrame`.
- Exposed `POST /api/v1/data/runoperation` for the Docker-seeded DELETE
  operation on `OrderList`.
- Hydrated persisted view-operation rows from `SW_SYS_VIEW_OPERATION`,
  `SW_SYS_OPERATION`, and `SW_SYS_OPERATIONVIEW`.
- Added a Vue operator console panel for view ID, operation ID, and object ID.
- Updated the migration parity document.

## Changed Files

- `docker/mysql/init/006-view.sql`
- `fool-view/src/main/java/org/fool/framework/view/api/DataController.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/LegacyRunOperationRequest.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/LegacyRunOperationResult.java`
- `fool-view/src/main/java/org/fool/framework/view/model/PersistedViewOperation.java`
- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `fool-view/src/main/java/org/fool/framework/view/service/ViewDataService.java`
- `fool-view/src/test/java/org/fool/framework/view/api/DataControllerRunOperationTest.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceRunOperationTest.java`
- `fool-view/src/test/java/org/fool/framework/view/service/ViewDataServiceTest.java`
- `frontend/src/App.vue`
- `frontend/src/api.ts`
- `frontend/src/payload.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/03/2026-07-03T09-33-48Z-legacy-runoperation.md`

## Validation

- RED backend:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataControllerRunOperationTest,DataQueryServiceRunOperationTest test`
  failed before implementation because `LegacyRunOperationRequest` and
  `LegacyRunOperationResult` did not exist.
- RED view-operation hydration:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=ViewDataServiceTest test`
  failed before implementation because `PersistedViewOperation` did not exist.
- RED frontend:
  `npm test -- --run payload.test.ts` failed before implementation because the
  Vue console did not expose `Run Operation` or `buildRunOperationRequest`.
- GREEN focused backend:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=ViewDataServiceTest,DataControllerRunOperationTest,DataQueryServiceRunOperationTest test`
  passed: 5 tests, 0 failures, 0 errors.
- GREEN backend module:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false test`
  passed: 79 tests, 0 failures, 0 errors.
- GREEN frontend:
  `npm test` passed: 33 tests.
- GREEN frontend build:
  `npm run build` passed.
- GREEN harness:
  `python scripts/check_repo_harness.py` passed.
- Docker rebuild:
  `docker compose up -d --build` rebuilt backend/frontend and restarted the
  Compose stack.
- Docker runtime smoke:
  `POST /api/v1/data/runoperation` passed through both
  `http://localhost:8080` and `http://localhost:8081` after creating temporary
  objects `970731` and `970732` with `savenewobj`. Both responses returned `success=true` and
  `returnMsg="操作成功"`. MySQL confirmed both temporary object IDs had count 0
  after delete.
- Docker stack:
  `docker compose ps` showed backend, frontend, MySQL, and Redis running; MySQL
  and Redis were healthy.

## Skipped Checks

- The existing Docker MySQL volume was patched manually for the new seed rows
  because init scripts do not rerun on an existing volume.
- This slice intentionally supports only the legacy DELETE base operation.
  Arbitrary command/reflection/WCF operation execution and operation-trigger
  side effects remain open migration work.

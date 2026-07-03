# Runoperation error message parity

## Prompt

Continue the active goal: keep Docker running, migrate against `../FoolFrame`,
use Vue for the frontend, and commit in atomic slices.

## Scope

- Hydrated legacy OperationView `SW_SYS_OPVIEW_ERRORMSG` into `ViewOperation`.
- Made DELETE/UPDATE `runoperation` execution failures return
  `success=false` and the legacy error-message prefix in `returnMsg`.
- Kept unsupported operation types unchanged.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/model/PersistedViewOperation.java`
- `fool-view/src/main/java/org/fool/framework/view/model/ViewOperation.java`
- `fool-view/src/main/java/org/fool/framework/view/service/ViewDataService.java`
- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `fool-view/src/test/java/org/fool/framework/view/service/ViewDataServiceTest.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceRunOperationTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/03/2026-07-03T10-05-58Z-runoperation-error-message.md`

## Validation

- RED backend:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=ViewDataServiceTest,DataQueryServiceRunOperationTest test`
  failed at test compile because `errorMsg` was not mapped.
- GREEN focused backend:
  same command passed: 6 tests, 0 failures, 0 errors.
- GREEN module:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false test`
  passed: 82 tests, 0 failures, 0 errors.
- Harness:
  `python scripts/check_repo_harness.py` passed.
- Docker runtime:
  `docker compose up -d --build backend` built and restarted backend.
- Runtime smoke:
  `POST http://localhost:8080/api/v1/data/runoperation` and
  `POST http://localhost:8081/api/v1/data/runoperation` with
  `{"ViewId":100,"ObjectId":"1001","OperationId":7002}` both returned
  `success=true` and `returnMsg="保存成功"`, proving the new operation metadata
  SQL remains runtime-compatible.
- Docker status:
  `docker compose ps` showed backend, frontend, MySQL, and Redis up.

## Skipped Checks

- No frontend source changed; the Vue console already posts and renders the
  `runoperation` response body.

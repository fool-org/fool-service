# Runoperation update parity

## Prompt

Continue the active goal: keep Docker running, migrate against `../FoolFrame`,
use Vue for the frontend, and commit in atomic slices.

## Scope

- Extended legacy `runoperation` from DELETE-only to UPDATE operations.
- Reused the existing `ModelDataService.saveData` path for
  `BaseOperationType.Update`.
- Seeded Docker metadata operation `7002` so the Vue/API console can execute an
  update-style operation against the `OrderList` smoke view.
- Left command execution, reflection, WCF, JSONPOST/JSONGET, and trigger side
  effects untouched.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceRunOperationTest.java`
- `docker/mysql/init/006-view.sql`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/03/2026-07-03T09-55-18Z-runoperation-update.md`

## Validation

- RED backend:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataQueryServiceRunOperationTest test`
  failed because UPDATE operations did not call `saveData`.
- GREEN focused backend:
  same command passed: 3 tests, 0 failures, 0 errors.
- GREEN module:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false test`
  passed: 81 tests, 0 failures, 0 errors.
- Docker DB/runtime:
  patched running MySQL metadata for operation `7002`, then ran
  `docker compose up -d --build backend`; backend build and restart passed.
- Runtime smoke:
  `POST http://localhost:8080/api/v1/data/runoperation` and
  `POST http://localhost:8081/api/v1/data/runoperation` with
  `{"ViewId":100,"ObjectId":"1001","OperationId":7002}` both returned
  `success=true` and `returnMsg="保存成功"`.
- Detail smoke:
  `POST http://localhost:8080/api/v1/data/querydatadetail` returned operation
  IDs `[7001, 7002]`.
- Harness:
  `python scripts/check_repo_harness.py` passed.
- Docker status:
  `docker compose ps` showed backend, frontend, MySQL, and Redis up.

## Skipped Checks

- No frontend source changed; the existing Vue console posts arbitrary
  `runoperation` payloads and renders the raw response.

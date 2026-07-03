# runoperation Create Migration

## Prompt

- Continue the Docker/Vue FoolFrame migration and keep commits atomic.
- Report the current overall migration completion percentage.

## Scope

- Extended legacy `runoperation` execution to `BaseOperationType.Create`.
- Reused the hydrated current object and existing command pipeline before
  delegating to `ModelDataService.createData`.
- Preserved the legacy operation success message surface.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceRunOperationTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/03/2026-07-03T11-26-50Z-runoperation-create.md`

## Validation

- Red check:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataQueryServiceRunOperationTest test`
  - Failed as expected because `modelDataService.createData(data)` was not
    invoked for `BaseOperationType.Create`.
- Green focused check:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataQueryServiceRunOperationTest test`
  - Tests run: 13, failures: 0, errors: 0.
- Green module check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false test`
  - Tests run: 93, failures: 0, errors: 0.
- Harness:
  `python scripts/check_repo_harness.py`
  - Passed: `Repository harness validation passed.`
- Docker runtime:
  `docker compose up -d --build backend`
  - Backend rebuilt and restarted.

## Runtime Evidence

Temporary Docker MySQL seed:

- Inserted operation `7004` for model `100` with
  `SW_MODEL_OPERATION_BASETYPE = 0`.
- Inserted matching view-operation and operation-view rows with success
  message `创建成功`.
- Inserted one `market_order` row with `order_id = 970733`.

Backend route:

- Request:
  `curl -H 'Content-Type: application/json' -d '{"ViewId":100,"ObjectId":"970733","OperationId":7004}' http://localhost:8080/api/v1/data/runoperation`
  - Response: `success=true`, `returnMsg=创建成功`
  - Database count for `order_id = 970733`: `1 -> 2`

Vue proxy route:

- Request:
  `curl -H 'Content-Type: application/json' -d '{"ViewId":100,"ObjectId":"970733","OperationId":7004}' http://localhost:8081/api/v1/data/runoperation`
  - Response: `success=true`, `returnMsg=创建成功`
  - Database count for `order_id = 970733`: `2 -> 3`

Cleanup verification:

- `SELECT COUNT(*) FROM market_order WHERE order_id = 970733;` returned `0`.
- `SELECT COUNT(*) FROM SW_SYS_OPERATION WHERE SysId = 7004;` returned `0`.

## Skipped Checks

- Full root `mvn test` was not rerun for this narrow `fool-view` operation
  slice; the affected module plus dependencies were tested through
  `-pl fool-view -am`.
- Frontend unit/build checks were not rerun because this slice did not change
  Vue source. The Vue proxy runtime route was smoke tested through the Compose
  frontend container.

## Remaining Risk

- FoolFrame's missing-row proxy construction behavior is not emulated here;
  this slice creates from an already hydrated object through the migrated data
  path.
- `BaseOperationType.Assebmly`, WCF/JSON/external-model execution, trigger side
  effects, and executable command types beyond current slices remain future
  work.

## Follow-Ups

- Continue with executable operation behavior only where a real dispatch target
  or seedable legacy runtime path exists.

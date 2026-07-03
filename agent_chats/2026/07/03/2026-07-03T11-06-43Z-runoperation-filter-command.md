# runoperation Filter Command Migration

## Prompt

- Continue the FoolFrame migration with Docker validation and Vue frontend in
  place.
- Report current migration completion percentage while keeping migration work
  moving.

## Scope

- Migrated the legacy `runoperation` `Filter` command slice from FoolFrame.
- Hydrated legacy command argument/property/temp columns from
  `SW_SYS_COMMANDS`.
- Preserved command execution order by legacy command index.
- Added guard behavior before DELETE/UPDATE execution: a failing filter returns
  the OperationView error prefix plus `SW_SYS_COMMAND_PROPERTY_EXP`.

## Changed Files

- `fool-model/src/main/java/org/fool/framework/model/model/OperationCommand.java`
- `fool-view/src/main/java/org/fool/framework/view/service/ViewDataService.java`
- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `fool-view/src/test/java/org/fool/framework/view/service/ViewDataServiceTest.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceRunOperationTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/03/2026-07-03T11-06-43Z-runoperation-filter-command.md`

## Validation

- Red check, command hydration:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=ViewDataServiceTest test`
  - Failed as expected before implementation because legacy argument/property
    columns were not hydrated onto `OperationCommand`.
- Red check, command execution:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataQueryServiceRunOperationTest test`
  - Failed as expected before implementation because the `Filter` command was
    ignored and `saveData` still ran.
- Green focused command hydration:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=ViewDataServiceTest test`
  - Tests run: 3, failures: 0, errors: 0.
- Green focused operation execution:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataQueryServiceRunOperationTest test`
  - Tests run: 12, failures: 0, errors: 0.
- Harness:
  `python scripts/check_repo_harness.py`
  - Passed: `Repository harness validation passed.`
- Module:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false test`
  - Tests run: 91, failures: 0, errors: 0.
- Docker runtime:
  `docker compose up -d --build backend`
  - Backend rebuilt and restarted.

## Runtime Evidence

Temporary Docker MySQL seed:

- Inserted command `7199` for operation `7002`:
  `SW_SYS_COMMAND_TYPE = 6`, expression `` `order_state` = '0' ``, and
  `SW_SYS_COMMAND_PROPERTY_EXP = '状态不允许'`.
- Reset `market_order.order_state` for `order_id = 1001` between pass/fail
  checks.
- Cleaned up command `7199` and reset `order_state = '0'` after smoke checks.

Backend route:

- Pass request:
  `curl -H 'Content-Type: application/json' -d '{"ViewId":100,"ObjectId":"1001","OperationId":7002}' http://localhost:8080/api/v1/data/runoperation`
  - Response: `success=true`, `returnMsg=保存成功`
  - Database state: `order_state=1`
- Fail request after setting `order_state=1`:
  `curl -H 'Content-Type: application/json' -d '{"ViewId":100,"ObjectId":"1001","OperationId":7002}' http://localhost:8080/api/v1/data/runoperation`
  - Response: `success=false`, `returnMsg=保存失败java.lang.IllegalStateException: 状态不允许`
  - Database state remained `order_state=1`

Vue proxy route:

- Pass request:
  `curl -H 'Content-Type: application/json' -d '{"ViewId":100,"ObjectId":"1001","OperationId":7002}' http://localhost:8081/api/v1/data/runoperation`
  - Response: `success=true`, `returnMsg=保存成功`
  - Database state: `order_state=1`
- Fail request after setting `order_state=1`:
  `curl -H 'Content-Type: application/json' -d '{"ViewId":100,"ObjectId":"1001","OperationId":7002}' http://localhost:8081/api/v1/data/runoperation`
  - Response: `success=false`, `returnMsg=保存失败java.lang.IllegalStateException: 状态不允许`
  - Database state remained `order_state=1`

Cleanup verification:

- `SELECT COUNT(*) FROM SW_SYS_COMMANDS WHERE SysId = 7199;` returned `0`.
- `SELECT order_state FROM market_order WHERE order_id = 1001;` returned `0`.

## Skipped Checks

- Full root `mvn test` was not rerun for this narrow `fool-view` command slice;
  the affected module plus dependencies were tested through `-pl fool-view -am`.
- Frontend unit/build checks were not rerun because this slice did not change
  Vue source. The Vue proxy runtime route was smoke tested through the Compose
  frontend container.

## Remaining Risk

- FoolFrame owner traversal expressions such as `#.` are still not migrated.
- Trigger side effects and executable command types beyond the covered
  `SetValue` and `Filter` slices remain future work.
- Raw legacy SQL filter bracket normalization remains unchanged; this slice
  uses the existing raw filter path.

## Follow-Ups

- Continue `runoperation` parity with the remaining legacy command types and
  trigger behavior.
- Add an automated Docker smoke script once the command matrix stabilizes.

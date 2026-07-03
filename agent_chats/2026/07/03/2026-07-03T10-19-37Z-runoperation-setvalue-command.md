# Runoperation SetValue command parity

## Prompt

Continue the active goal: keep Docker running, migrate against `../FoolFrame`,
use Vue for the frontend, and commit in atomic slices.

## Scope

- Added a runtime `OperationCommand` model for legacy `SW_SYS_COMMANDS`.
- Hydrated persisted operation commands into `ViewDataService` operations.
- Applied legacy `CommandsType.SetValue` literal commands before DELETE/UPDATE
  execution in `runoperation`.
- Seeded Docker operation `7002` with a command that writes `Order.state`.

## Changed Files

- `fool-model/src/main/java/org/fool/framework/model/model/Operation.java`
- `fool-model/src/main/java/org/fool/framework/model/model/OperationCommand.java`
- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `fool-view/src/main/java/org/fool/framework/view/service/ViewDataService.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceRunOperationTest.java`
- `fool-view/src/test/java/org/fool/framework/view/service/ViewDataServiceTest.java`
- `docker/mysql/init/006-view.sql`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/03/2026-07-03T10-19-37Z-runoperation-setvalue-command.md`

## Validation

- RED execution test:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataQueryServiceRunOperationTest test`
  failed because `OperationCommand` did not exist.
- GREEN execution test:
  same command passed: 5 tests, 0 failures, 0 errors.
- RED hydration test:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=ViewDataServiceTest test`
  failed because hydrated operations had 0 commands.
- GREEN hydration test:
  same command passed: 2 tests, 0 failures, 0 errors.
- GREEN focused combined:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataQueryServiceRunOperationTest,ViewDataServiceTest test`
  passed: 7 tests, 0 failures, 0 errors.
- GREEN module:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false test`
  passed: 83 tests, 0 failures, 0 errors.
- Harness:
  `python scripts/check_repo_harness.py` passed.
- Docker runtime:
  `docker compose up -d --build backend` built the backend image and restarted
  the backend container.

## Runtime Evidence

- Running MySQL volume patched with command `7101`: owner operation `7002`,
  type `0`, property `1003`, expression `$1`, index `1`.
- `curl http://localhost:8080/test` returned seeded order rows.
- Backend smoke:
  `POST http://localhost:8080/api/v1/data/runoperation` with
  `{"ViewId":100,"ObjectId":"1001","OperationId":7002}` returned
  `success=true` and `returnMsg="保存成功"`, and MySQL showed
  `market_order.order_state = 1` for `order_id = 1001`.
- Vue proxy smoke:
  `POST http://localhost:8081/api/v1/data/runoperation` with the same payload
  returned the same success body and produced the same DB state change.
- The smoke fixture row was reset to `order_state = 0` after each runtime check.

## Skipped Checks

- No frontend source changed; the existing Vue operator console and Nginx proxy
  path were validated through `localhost:8081`.
- Full FoolFrame expression grammar and command types other than `SetValue`
  remain out of this slice.

## Risks

- `SetValue` currently supports literal `$...` and a simple `#.field` shortcut
  only. Add the full `GetValueExpression` grammar when real command metadata
  needs math, context values, owner traversal, or business-object loading.

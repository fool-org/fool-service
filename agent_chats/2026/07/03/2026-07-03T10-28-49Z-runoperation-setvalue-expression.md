# Runoperation SetValue expression parity

## Prompt

Continue the active goal: keep Docker running, migrate against `../FoolFrame`,
use Vue for the frontend, and commit in atomic slices.

## Scope

- Extended legacy `runoperation` `SetValue` expression handling.
- Matched the FoolFrame `.property` current-object expression for simple
  target-object reads.
- Converted static `$...` command values for `Int`/`UInt` target properties
  before dynamic save.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceRunOperationTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/03/2026-07-03T10-28-49Z-runoperation-setvalue-expression.md`

## Validation

- RED current-object expression test:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataQueryServiceRunOperationTest test`
  failed with expected `BTC-USDT` but actual empty value.
- GREEN current-object expression test:
  same command passed: 6 tests, 0 failures, 0 errors.
- RED static `Int` conversion test:
  same command failed with expected `Integer<7>` but actual `String<7>`.
- GREEN focused test:
  same command passed: 7 tests, 0 failures, 0 errors.
- GREEN module:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false test`
  passed: 85 tests, 0 failures, 0 errors.
- Harness:
  `python scripts/check_repo_harness.py` passed.
- Docker backend rebuild:
  `docker compose up -d --build backend` completed and restarted the backend
  container.

## Runtime Evidence

- `curl http://localhost:8080/test` returned successfully after the backend
  rebuild.
- Backend smoke:
  `POST http://localhost:8080/api/v1/data/runoperation` with
  `{"ViewId":100,"ObjectId":"1001","OperationId":7002}` returned
  `success=true` and `returnMsg="保存成功"`, and MySQL showed
  `market_order.order_state = 1` for `order_id = 1001`.
- Vue proxy smoke:
  `POST http://localhost:8081/api/v1/data/runoperation` with the same payload
  returned the same success body and produced the same DB state change.
- The smoke fixture row was reset to `order_state = 0` after each runtime
  check.

## Skipped Checks

- Full FoolFrame `GetValueExpression` grammar remains out of this small slice:
  math expressions, context values, full owner traversal, DateTime conversion,
  and BusinessObject loading are still future migration work.
- Frontend source did not change.

## Risks

- Only `Int`/`UInt` static conversions are migrated here because that is the
  first typed scalar gap covered by tests.

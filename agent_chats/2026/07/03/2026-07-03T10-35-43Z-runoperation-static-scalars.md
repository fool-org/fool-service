# Runoperation SetValue static scalar parity

## Prompt

Continue the active goal: keep Docker running, migrate against `../FoolFrame`,
use Vue for the frontend, and commit in atomic slices.

## Scope

- Extended `runoperation` `SetValue` `$...` expression conversion to the simple
  scalar branches in FoolFrame `GetStaticVlue`.
- Covered Boolean, Byte, Char, DateTime, Int/UInt, Long/ULong, Decimal, and
  Double/Float target properties.
- Left Date/Time and string-like target properties as literal strings.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceRunOperationTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/03/2026-07-03T10-35-43Z-runoperation-static-scalars.md`

## Validation

- RED static scalar test:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataQueryServiceRunOperationTest test`
  failed with expected `Boolean<true>` but actual `String<true>`.
- GREEN focused test:
  same command passed: 8 tests, 0 failures, 0 errors.
- GREEN module:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false test`
  passed: 86 tests, 0 failures, 0 errors.
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

- No frontend source changed.
- Full FoolFrame `GetValueExpression` grammar remains out of this slice:
  math expressions, context values, owner traversal, and BusinessObject loading
  are still future work.

## Risks

- `DateTime` currently accepts Java `LocalDateTime` parseable input such as
  ISO timestamps; broader legacy date formatting should be added when real
  command metadata needs it.

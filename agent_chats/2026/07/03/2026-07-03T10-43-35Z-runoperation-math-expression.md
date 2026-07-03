# Runoperation SetValue Math Expression

## Prompt

- Continue the FoolFrame migration with Docker running, Vue frontend kept as the
  replacement UI, and commit progress atomically.
- The user asked for the current migration percentage; current repo evidence was
  checked before continuing.

## Scope

- Matched `../FoolFrame/src/Server/SCPB05-Soway.Model/Expressions/GetValueExpression.cs`
  behavior for the next narrow `runoperation` slice.
- Added composite math-expression evaluation for legacy `SetValue` command
  expressions such as `.retryCount+$2`.

## Changes

- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
  now routes composite math command expressions through
  `org.fool.framework.common.data.math.MathExpression`.
- Math operands resolve through the existing current-object and static-value
  command branches, then convert back through `staticValue` for the target
  property type.
- Static literal expressions such as legacy date/time values remain on the
  static-value path instead of being treated as subtraction expressions.
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceRunOperationTest.java`
  covers `.retryCount+$2` updating an integer target field.
- `docs/migration/foolframe-parity.md` records the increment and remaining
  gaps.

## Validation

- `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataQueryServiceRunOperationTest test`
  - Passed: 9 tests.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false test`
  - Passed: 87 tests.
- `python scripts/check_repo_harness.py`
  - Passed.

## Runtime Evidence

- `docker compose up -d --build backend`
  - Passed. Backend image rebuilt with the new slice and container restarted.
- Docker stack after rebuild:
  - Backend on `8080`, Vue frontend on `8081`, MySQL healthy on `3307`, Redis
    on `6380`.
- Runtime smoke temporarily updated Docker seed command `7101` from `$1` to
  `.state+$1`, then restored it to `$1` after verification.
- Backend route smoke:
  - Reset `market_order.order_state` for `order_id = 1001` to `2`.
  - `POST http://localhost:8080/api/v1/data/runoperation` with
    `{"ViewId":100,"ObjectId":"1001","OperationId":7002}` returned success.
  - MySQL state changed to `3`.
- Vue proxy route smoke:
  - Reset `market_order.order_state` for `order_id = 1001` to `4`.
  - `POST http://localhost:8081/api/v1/data/runoperation` with the same payload
    returned success.
  - MySQL state changed to `5`.
- Restored seed data:
  - `SW_SYS_COMMANDS.SysId = 7101` expression restored to `$1`.
  - `market_order.order_state` for `order_id = 1001` restored to `0`.

## Risks

- This slice covers composite math expressions using already migrated operand
  forms. Full FoolFrame `GetValueExpression` context values, deeper owner
  traversal, business-object loading, and non-`SetValue` command execution
  remain open.
- Bare numeric math operands without `$`/`.` are not expanded because the
  legacy resolver delegates each token through the value-expression grammar.

## Follow-ups

- Continue `runoperation` command parity with context and owner expressions.
- Add trigger side-effect execution after command value parity is stable.

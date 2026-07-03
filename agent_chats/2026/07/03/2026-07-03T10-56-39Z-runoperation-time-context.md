# Runoperation SetValue Time Context

## Prompt

- Continue the FoolFrame migration with Docker running, Vue frontend kept as the
  replacement UI, and timely atomic commits.

## Scope

- Matched the no-external-dependency time context branch from
  `../FoolFrame/src/Server/SCPB05-Soway.Model/Expressions/GetValueExpression.cs`.
- Added direct `@datetime`, `@date`, and `@time` support for legacy
  `runoperation` `SetValue` command values.

## Changes

- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
  now resolves direct `@...` command expressions before the base UPDATE save.
- `@datetime` and `@time` produce the current `LocalDateTime`; `@date` produces
  the current date at start of day.
- Unsupported context names still fall back to the existing empty-string value.
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceRunOperationTest.java`
  covers `@datetime` assignment on a legacy UPDATE operation.
- `docs/migration/foolframe-parity.md` records the increment and narrows the
  remaining context gap to auth/user/app/database context values.

## Validation

- Red test first:
  - `DataQueryServiceRunOperationTest.runLegacyUpdateOperationAppliesSetValueFromDateTimeContext`
    failed because the assigned value was still the empty-string fallback.
- `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataQueryServiceRunOperationTest test`
  - Passed after implementation: 11 tests.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false test`
  - Passed: 89 tests.
- `python scripts/check_repo_harness.py`
  - Passed.

## Runtime Evidence

- `docker compose up -d --build backend`
  - Passed. Backend image rebuilt with the new slice and container restarted.
- `curl http://localhost:8080/test`
  - Passed after rebuild.
- Existing Docker seed `runoperation` no-regression smoke:
  - Reset `market_order.order_state` for `order_id = 1001` to `0`.
  - `POST http://localhost:8080/api/v1/data/runoperation` with
    `{"ViewId":100,"ObjectId":"1001","OperationId":7002}` returned success and
    changed state to `1`.
  - Reset the same row to `0`.
  - `POST http://localhost:8081/api/v1/data/runoperation` with the same payload
    returned success through the Vue proxy and changed state to `1`.
  - Restored `market_order.order_state` for `order_id = 1001` to `0`.

## Risks

- The Docker seed data does not currently include a no-side-effect
  time-context SetValue operation, so this branch is covered by focused service
  tests rather than a direct runtime seed smoke.
- Auth/user/app/database context values, owner traversal, trigger side effects,
  and non-`SetValue` command types remain open.

## Follow-ups

- Add auth/user/app/database context values once `fool-view` has the necessary
  runtime context boundary without pulling auth dependencies into the module.
- Add a seed metadata scenario for a direct time-context SetValue if this
  becomes a repeated runtime smoke path.

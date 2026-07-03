# Runoperation SetValue BusinessObject Static Value

## Prompt

- Continue the FoolFrame migration with Docker running, Vue frontend kept as the
  replacement UI, and timely atomic commits.

## Scope

- Matched the next narrow `GetStaticVlue` branch from
  `../FoolFrame/src/Server/SCPB05-Soway.Model/Expressions/GetValueExpression.cs`.
- Added static `BusinessObject` value loading for legacy `SetValue` command
  expressions.

## Changes

- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
  now resolves `$...` expressions targeting a `BusinessObject` property through
  the target property model via `ModelDataService.getOneData`.
- If no target property model is available, the existing literal fallback is
  preserved.
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceRunOperationTest.java`
  covers loading a `Customer` dynamic object for a `$C001` command value.
- `docs/migration/foolframe-parity.md` records the increment and removes the
  stale general business-object gap from the latest SetValue expression notes.

## Validation

- Red test first:
  - `DataQueryServiceRunOperationTest.runLegacyUpdateOperationLoadsStaticBusinessObjectSetValue`
    failed with `expected:<{}> but was:<C001>`.
- `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataQueryServiceRunOperationTest test`
  - Passed after implementation: 10 tests.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false test`
  - Passed: 88 tests.
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
  `BusinessObject` SetValue operation, so this branch is covered by focused
  service tests rather than a direct runtime seed smoke.
- Context `@...`, real owner traversal for `#...`, trigger side effects, and
  non-`SetValue` command types remain open.

## Follow-ups

- Add owner/context expression support once the runtime object/context surface
  is ready.
- Add a seed metadata scenario for BusinessObject SetValue if this becomes a
  repeated runtime smoke path.

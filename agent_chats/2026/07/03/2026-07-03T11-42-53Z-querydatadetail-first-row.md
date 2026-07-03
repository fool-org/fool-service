# querydatadetail First Row Fallback Migration

## Prompt

- Continue the Docker/Vue FoolFrame migration and keep commits atomic.

## Scope

- Matched FoolFrame `HandlerQueryDataDetail` blank-object behavior for the
  simple no-`IdExp` path.
- When both `objId` and `IdExp` are blank, `DataQueryService` now queries the
  first page of the selected view model and loads the first returned object ID.
- Kept explicit `objId` and static `$...` `IdExp` behavior unchanged.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceDetailTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/03/2026-07-03T11-42-53Z-querydatadetail-first-row.md`

## Validation

- Red check:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataQueryServiceDetailTest test`
  - Failed as expected: the new blank-`objId` test returned `null` instead of
    the expected detail result.
- Green focused check:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataQueryServiceDetailTest test`
  - Tests run: 4, failures: 0, errors: 0.
- Green module check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false test`
  - Tests run: 95, failures: 0, errors: 0.
- Harness:
  `python scripts/check_repo_harness.py`
  - Passed: `Repository harness validation passed.`
- Docker runtime:
  `COMPOSE_PROGRESS=plain docker compose up -d --build backend`
  - Backend image built and backend container restarted.

## Runtime Evidence

Backend route:

- Request:
  `curl -H 'Content-Type: application/json' -d '{"viewId":100,"objId":"","IdExp":""}' http://localhost:8080/api/v1/data/querydatadetail`
  - Response: `code=0`, detail `objId=1001`, `name=OrderList`.
  - Operations included `7001` and `7002`.

Vue proxy route:

- Request:
  `curl -H 'Content-Type: application/json' -d '{"viewId":100,"objId":"","IdExp":""}' http://localhost:8081/api/v1/data/querydatadetail`
  - Response: `code=0`, detail `objId=1001`, `name=OrderList`.
  - Operations included `7001` and `7002`.

## Skipped Checks

- Frontend unit/build checks were not rerun because this slice does not change
  Vue source. The Vue proxy runtime route was smoke tested through the Compose
  frontend container.

## Remaining Risk

- Auth/context `IdExp` values such as `@userid` remain unimplemented because
  `fool-view` does not currently own auth context.

## Follow-Ups

- Add context expressions only when the backend has a migrated current-context
  source for this view path.

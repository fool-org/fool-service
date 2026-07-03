# querydatadetail Static IdExp Migration

## Prompt

- Continue the Docker/Vue FoolFrame migration and keep commits atomic.

## Scope

- Preserved legacy `IdExp` from `/api/v1/data/querydatadetail` controller to
  `DataQueryService`.
- Added blank-`objId` static `$...` ID expression resolution before loading
  detail data.
- Kept existing explicit-`objId` behavior unchanged through an overload.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/api/DataController.java`
- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `fool-view/src/test/java/org/fool/framework/view/api/DataControllerLegacyQueryDataDetailTest.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceDetailTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/03/2026-07-03T11-34-09Z-querydatadetail-idexp.md`

## Validation

- Red check:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataQueryServiceDetailTest,DataControllerLegacyQueryDataDetailTest test`
  - Failed as expected at test compilation because
    `queryLegacyViewDataDetail(String,String,String)` did not exist.
- Green focused check:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataQueryServiceDetailTest,DataControllerLegacyQueryDataDetailTest test`
  - Tests run: 5, failures: 0, errors: 0.
- Green module check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false test`
  - Tests run: 94, failures: 0, errors: 0.
- Harness:
  `python scripts/check_repo_harness.py`
  - Passed: `Repository harness validation passed.`
- Docker runtime:
  `COMPOSE_PROGRESS=plain docker compose up -d --build backend`
  - Backend image built and backend container restarted.

## Runtime Evidence

Backend route:

- Request:
  `curl -H 'Content-Type: application/json' -d '{"viewId":100,"objId":"","IdExp":"$1001"}' http://localhost:8080/api/v1/data/querydatadetail`
  - Response: `code=0`, detail `objId=1001`, `name=OrderList`
  - Operations included `7001` and `7002`.

Vue proxy route:

- Request:
  `curl -H 'Content-Type: application/json' -d '{"viewId":100,"objId":"","IdExp":"$1001"}' http://localhost:8081/api/v1/data/querydatadetail`
  - Response: `code=0`, detail `objId=1001`, `name=OrderList`
  - Operations included `7001` and `7002`.

## Skipped Checks

- Frontend unit/build checks were not rerun because this slice does not change
  Vue source. The Vue proxy runtime route was smoke tested through the Compose
  frontend container.

## Remaining Risk

- Auth/context `IdExp` values such as `@userid` are not implemented because
  `fool-view` does not currently own auth context.
- FoolFrame's blank-`objId` first-row fallback remains future work.

## Follow-Ups

- Add context expressions only when the backend has a migrated current-context
  source for this view path.

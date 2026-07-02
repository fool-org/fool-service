# Legacy inputquery

## Prompt

- Continue the active migration goal:
  1. run the environment with Docker;
  2. complete migration against `../FoolFrame`;
  3. use Vue for the frontend;
  4. make timely atomic commits.

## Scope

- Added `POST /api/v1/data/inputquery` as the Vue replacement for the main
  legacy `IDataService.inputquery` candidate lookup path.
- Added `InputQueryRequest` / `InputQueryResult` DTOs matching the legacy
  `Text`, `ViewItemId`, `ViewName`, `ModelID`, `ObjID`, `IsAdded`, `OwnerId`,
  and `Items`/`QueryItem` shape.
- Implemented business-object candidate lookup through an existing view item's
  `Property.propertyModel`, using the target model id as `id`, show property as
  `text`, show-property `LIKE`, top 5 rows, and id-desc ordering.
- Added Vue API types for the same request/result payloads.
- Updated migration parity notes.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/api/DataController.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/InputQueryRequest.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/InputQueryResult.java`
- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `fool-view/src/test/java/org/fool/framework/view/api/DataControllerInputQueryTest.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceInputQueryTest.java`
- `frontend/src/api.ts`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/02/2026-07-02T21-32-02Z-legacy-inputquery.md`

## Validation

- RED:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=DataControllerInputQueryTest,DataQueryServiceInputQueryTest test`
  failed because `InputQueryRequest` and `InputQueryResult` were missing.
- GREEN:
  same focused command passed with 2 tests.
- Backend:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am test`
  passed with 50 `fool-view` tests.
- Frontend:
  `cd frontend && npm test && npm run build` passed.
- Harness:
  `python scripts/check_repo_harness.py` passed.
- Whitespace:
  `git diff --check` passed.
- Docker:
  `docker compose up -d --build backend frontend` passed.

## Runtime Evidence

- `docker compose ps` showed backend, frontend, MySQL, and Redis running.
- `curl http://localhost:8080/test` returned the seeded two-row smoke payload.
- `curl -H 'Content-Type: application/json' -d '{"viewName":"OrderList","viewItemId":"symbol","text":"BTC"}' http://localhost:8080/api/v1/data/inputquery`
  returned `code:0` with an empty `items` list because the seeded `symbol`
  field has no `propertyModel`.
- `curl http://localhost:8081/` returned the Vue HTML shell.

## Skipped Checks

- No browser interaction check; this slice adds an API route and Vue payload
  types, not a visible input widget.

## Risks / Follow-ups

- Legacy source-list and owner-context branches from `HandlerInputQuery` remain
  open for a later collection-editing slice.

# InputQuery ViewId Context

## Prompt

Keep the migration focused on the View-first flow: render the page from View
metadata first, then query data from that View context instead of binding the
frontend or backend lookup path to concrete business DTO names.

## Scope

- Added legacy `ViewId` support to the `inputquery` request DTO.
- Changed `DataQueryService.inputQuery` to resolve the View by `ViewId` first,
  with `ViewName` retained as a compatibility fallback.
- Passed the current loaded View id from the Vue View workflow into metadata
  lookup editors and the manual input-query tool.
- Kept the payload builder generic; no new `Order`, trading pair, or concrete
  business DTO defaults were added.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/dto/InputQueryRequest.java`
- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `fool-view/src/test/java/org/fool/framework/view/api/DataControllerInputQueryTest.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceInputQueryTest.java`
- `frontend/src/api.ts`
- `frontend/src/payload.ts`
- `frontend/src/payload.test.ts`
- `frontend/src/MetadataFieldEditor.vue`
- `frontend/src/App.vue`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/04/2026-07-03T20-59-55Z-inputquery-viewid-context.md`

## Validation

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -DfailIfNoTests=false -Dtest=DataQueryServiceInputQueryTest,DataControllerInputQueryTest test`
- `cd frontend && npm test -- payload.test.ts`
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -DfailIfNoTests=false test`
- `cd frontend && npm test && npm run build`
- `python3 scripts/check_repo_harness.py`
- `git diff --check`
- `docker compose up -d --build --force-recreate backend frontend`

## Runtime Evidence

- `docker compose ps` showed backend, frontend, MySQL, and Redis running.
- `curl -fsS http://localhost:8080/test` returned 8 backend rows.
- `POST http://localhost:8080/api/v1/data/inputquery` with
  `{"ViewId":100,"ViewItemId":"Customer","Text":"Ada","IsAdded":false}`
  returned `code=0` with candidate `3001 / Ada Capital`.
- The same `ViewId` inputquery payload through the frontend proxy
  `http://localhost:8081/api/v1/data/inputquery` also returned
  `3001 / Ada Capital`.

## Risks

- `ViewName` remains in the DTO and payload as legacy compatibility. Future
  cleanup can remove visible `ViewName` usage only after no migrated clients
  depend on it.

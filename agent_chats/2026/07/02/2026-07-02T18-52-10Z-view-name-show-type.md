# View Name And ShowType Parity

## Prompt

- Continue the active migration goal: Docker runtime, FoolFrame parity, Vue frontend, and timely atomic commits.

## Scope

- Added legacy `Soway.Server/ListView/HandlerGetListView.cs` top-level `ViewData.Name` and `ViewData.ShowType` aliases to the modern view-definition DTO.
- Reused existing `viewName` and `viewType`; no new model or schema surface was added.

## Changed Files

- `fool-view/src/test/java/org/fool/framework/view/adapter/ViewAdapterTest.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/ListViewInfo.java`
- `fool-view/src/main/java/org/fool/framework/view/adapter/ViewAdapter.java`
- `frontend/src/api.ts`
- `docs/migration/foolframe-parity.md`

## Validation

- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=ViewAdapterTest#viewInfoIncludesLegacyNameAndShowType test`
  - Failed as expected with `ListViewInfo should expose legacy name metadata`.
- GREEN: same focused command passed.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am test`
  - Passed: `Tests run: 28, Failures: 0, Errors: 0, Skipped: 0`.
- `cd frontend && npm test`
  - Passed: 1 file, 3 tests.
- `cd frontend && npm run build`
  - Passed.

## Runtime Evidence

- `docker compose up -d --build backend frontend`
  - Passed.
- `docker compose ps`
  - `backend` and `frontend` are up.
  - `mysql` and `redis` are up and healthy.
- `curl --retry 20 --retry-connrefused --retry-delay 1 -fsS http://localhost:8080/test`
  - Returned seeded order rows.
- `curl --retry 20 --retry-connrefused --retry-delay 1 -fsS http://localhost:8081/`
  - Returned the Vue shell HTML.
- `curl --retry 20 --retry-connrefused --retry-delay 1 -fsS -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
  - Returned `data.name = "OrderList"` and `data.showType = "ListView"`.

## Risks

- Legacy `TempFile` still needs a view-file source before parity can be claimed.

## Follow-Ups

- Continue with remaining `HandlerGetListView.cs` fields only when the Java model has a real source for them.

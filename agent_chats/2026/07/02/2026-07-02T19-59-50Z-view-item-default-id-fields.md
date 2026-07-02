# Delivery Evidence: legacy view-item default ID fields

## Prompt

- Keep migrating `fool-service` against `../FoolFrame`, keep Docker running, keep the frontend on Vue, and report the overall migration percentage.

## Scope

- Add the legacy `ViewItem` default ID metadata fields exposed by `Soway.Service.ViewItem`:
  - `PropertyId`
  - `EditViewId`
  - `EditExp`
- Preserve the current legacy runtime behavior for this slice: `HandlerGetListView.cs` does not assign these fields, so the observed JSON/default value is `0`.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/dto/TableColumnInfo.java`
- `fool-view/src/main/java/org/fool/framework/view/adapter/ViewAdapter.java`
- `fool-view/src/test/java/org/fool/framework/view/adapter/ViewAdapterTest.java`
- `frontend/src/api.ts`
- `docs/migration/foolframe-parity.md`

## Validation

- RED:
  - `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=ViewAdapterTest#viewInfoIncludesLegacyColumnDefaultPropertyAndEditIds test`
  - Failed as expected because `TableColumnInfo` did not expose the legacy ID metadata getters.
- GREEN focused:
  - `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=ViewAdapterTest#viewInfoIncludesLegacyColumnDefaultPropertyAndEditIds test`
- Adapter suite:
  - `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=ViewAdapterTest test`
  - Result: `Tests run: 17, Failures: 0, Errors: 0, Skipped: 0`
- Module suite:
  - `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am test`
  - Result: `BUILD SUCCESS`; `Tests run: 36, Failures: 0, Errors: 0, Skipped: 0`
- Frontend:
  - `cd frontend && npm test && npm run build`
  - Result: Vitest 3 tests passed; `vue-tsc && vite build` passed.
- Runtime:
  - `docker compose up -d --build backend`
  - `docker compose ps`
  - `curl -fsS http://localhost:8080/test`
  - `curl -fsS -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
  - Result: backend/frontend/MySQL/Redis running; `OrderList` columns include `propertyId:0`, `editViewId:0`, and `editExp:0`.

## Skipped Checks

- Full root `mvn test` was not rerun in this slice because the change is isolated to `fool-view` DTO/adapter and Vue API typing; the focused module suite was run instead.

## Risks / Follow-ups

- This slice only exposes the legacy default fields. It does not introduce a non-zero source for `PropertyId`, `EditViewId`, or `EditExp`, because the checked legacy server path also leaves them at their DTO defaults.

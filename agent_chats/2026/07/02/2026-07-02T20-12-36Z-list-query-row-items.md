# Delivery Evidence: legacy list-query row Items

## Prompt

- Continue migrating `fool-service` against `../FoolFrame`, keep Docker running, keep the frontend on Vue, and commit atomically.

## Scope

- Add current-JSON row `items` metadata matching legacy `QueryKeyValueResult.Items` / `ObjValuePair` for list-query rows.
- Preserve the existing Vue `values` map.
- Fields added per cell:
  - `objId`
  - `prpId`
  - `fmtValue`
  - `prpShowName`
  - `prpType`
  - `prpModelId`
  - `readOnly`
  - `editType`

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/dto/ListDataItem.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/ListDataValue.java`
- `fool-view/src/main/java/org/fool/framework/view/adapter/ViewDataAdapter.java`
- `fool-view/src/test/java/org/fool/framework/view/adapter/ViewDataAdapterTest.java`
- `frontend/src/api.ts`
- `docs/migration/foolframe-parity.md`

## Validation

- RED:
  - `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=ViewDataAdapterTest#listRowsExposeLegacyValueItems test`
  - Failed as expected: `ListDataItem should expose legacy Items`.
- GREEN focused:
  - Same command passed.
- Module:
  - `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am test`
  - Result: `BUILD SUCCESS`; `Tests run: 38, Failures: 0, Errors: 0, Skipped: 0`.
- Frontend:
  - `cd frontend && npm test && npm run build`
  - Result: Vitest 3 tests passed; `vue-tsc && vite build` passed.
- Runtime:
  - `docker compose up -d --build backend`
  - `docker compose ps`
  - `curl -fsS --retry 10 --retry-delay 2 --retry-connrefused http://localhost:8080/test`
  - `curl -fsS --retry 10 --retry-delay 2 --retry-connrefused -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list`
  - Result: backend/frontend/MySQL/Redis running; each `query-list` row includes `items` with `objId`, `prpId`, `fmtValue`, `prpShowName`, `prpType`, `prpModelId`, `readOnly`, and `editType`.

## Skipped Checks

- Full root `mvn test` was not rerun; this slice is isolated to `fool-view` list-query response DTO/adapter and Vue typing.

## Risks / Follow-ups

- This slice covers the simple value metadata path. Legacy date/time enum and BusinessObject-specific `ObjValuePair` formatting can be tightened in a later focused slice if needed.

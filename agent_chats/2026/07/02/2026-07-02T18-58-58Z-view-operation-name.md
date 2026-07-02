# View Operation Name Parity

## Prompt

- Continue the active migration goal: Docker runtime, FoolFrame parity, Vue frontend, and timely atomic commits.

## Scope

- Added legacy `Soway.Server/ListView/ViewOperation.cs` `Name` alias to the modern operation DTO.
- Reused the existing operation display text source; no model or schema changes were added.

## Changed Files

- `fool-view/src/test/java/org/fool/framework/view/adapter/ViewAdapterTest.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/OperationInfo.java`
- `fool-view/src/main/java/org/fool/framework/view/adapter/ViewAdapter.java`
- `frontend/src/api.ts`
- `docs/migration/foolframe-parity.md`

## Validation

- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=ViewAdapterTest#viewOperationsAreMappedToLegacyOperationInfo test`
  - Failed as expected with `OperationInfo should expose legacy name metadata`.
- GREEN: same focused command passed.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am test`
  - Passed: `Tests run: 28, Failures: 0, Errors: 0, Skipped: 0`.
- `npm test` from `frontend/`
  - Passed: 1 file, 3 tests.
- `npm run build` from `frontend/`
  - Passed.

## Runtime Evidence

- `docker compose up -d --build backend frontend`
  - Passed; backend and frontend images built.
- `docker compose ps`
  - `backend` and `frontend` are up.
  - `mysql` and `redis` are up and healthy.
- `curl --retry 20 --retry-connrefused --retry-delay 1 -fsS http://localhost:8080/test`
  - Returned seeded order rows.
- `curl --retry 20 --retry-connrefused --retry-delay 1 -fsS http://localhost:8081/`
  - Returned the Vue shell HTML.
- `curl --retry 20 --retry-connrefused --retry-delay 1 -fsS -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
  - Returned `data.name = "OrderList"` and `data.showType = "ListView"`.
  - The seeded `OrderList` currently has no operations, so operation-name JSON is covered by the focused adapter test.

## Risks

- Legacy `TempFile` and item-level view-file metadata still need runtime view-file hydration before parity can be claimed.

## Follow-Ups

- Continue with remaining `Soway.Server` fields only when the Java model has a real runtime source.

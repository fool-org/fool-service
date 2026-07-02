# View Type Alias Parity

## Prompt

- Continue the active migration goal: Docker runtime, FoolFrame parity, Vue frontend, and timely atomic commits.

## Scope

- Added the legacy `Soway.Server/ListView/ViewData.cs` top-level `Type` DTO field to the modern list-view response.
- Reused the existing `View.viewType` value, matching the already migrated `ShowType` alias.
- Synchronized the Vue API type and migration parity notes.

## Changed Files

- `fool-view/src/test/java/org/fool/framework/view/adapter/ViewAdapterTest.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/ListViewInfo.java`
- `fool-view/src/main/java/org/fool/framework/view/adapter/ViewAdapter.java`
- `frontend/src/api.ts`
- `docs/migration/foolframe-parity.md`

## Validation

- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=ViewAdapterTest#viewInfoIncludesLegacyNameAndShowType test`
  - Failed as expected with `ListViewInfo should expose legacy type metadata`.
- GREEN: same focused command passed.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am test`
  - Passed: `Tests run: 29, Failures: 0, Errors: 0, Skipped: 0`.
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
  - Returned `data.type = ListView`, `data.viewType = ListView`, and `data.showType = ListView`.

## Risks

- Legacy `TempFile` view-file runtime hydration remains open.

## Follow-Ups

- Continue migrating view-file/runtime hydration fields after the existing DTO aliases are covered.

# View Column Name Parity

## Prompt

- Continue the active migration goal: Docker runtime, FoolFrame parity, Vue frontend, and timely atomic commits.

## Scope

- Added legacy `Soway.Server/ListView/HandlerGetListView.cs` `ViewItem.Name` metadata to the modern view-column DTO.
- Kept the change to existing `ViewItem.itemName`; no new view-file/list-view model surface was added.

## Changed Files

- `fool-view/src/test/java/org/fool/framework/view/adapter/ViewAdapterTest.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/TableColumnInfo.java`
- `fool-view/src/main/java/org/fool/framework/view/adapter/ViewAdapter.java`
- `frontend/src/api.ts`
- `docs/migration/foolframe-parity.md`

## Validation

- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=ViewAdapterTest#viewInfoIncludesLegacyColumnName test`
  - Failed as expected with `TableColumnInfo should expose legacy name metadata`.
- GREEN: same focused command passed.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am test`
  - Passed: `Tests run: 26, Failures: 0, Errors: 0, Skipped: 0`.
- `cd frontend && npm test`
  - Passed: 1 file, 3 tests.
- `cd frontend && npm run build`
  - Passed.

## Runtime Evidence

- `docker compose up -d --build backend frontend`
  - Passed; backend and frontend images rebuilt, backend restarted.
- `docker compose ps`
  - backend/frontend/mysql/redis up; mysql/redis healthy.
- `curl http://localhost:8080/test`
  - Returned seeded order data.
- `curl http://localhost:8081/`
  - Returned the Vue HTML shell.
- `curl -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
  - Returned `tableColumn` entries with `name`: `Order ID`, `Symbol`, and `State`.

## Risks

- Legacy `ListViewId`, `ListViewType`, `PropertyModel`, and template-file fields still need model/schema decisions before parity can be claimed.

## Follow-Ups

- Continue migrating the remaining concrete `HandlerGetListView.cs` fields only where current Java models already expose a source.

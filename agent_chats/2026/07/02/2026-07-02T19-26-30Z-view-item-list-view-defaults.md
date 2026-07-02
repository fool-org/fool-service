# View Item ListView Defaults Parity

## Prompt

- Continue the active migration goal: Docker runtime, FoolFrame parity, Vue frontend, and timely atomic commits.

## Scope

- Added legacy `Soway.Server/ListView/ViewItem.cs` `ListViewId` and `ListViewType` DTO fields to list-column metadata.
- Matched the legacy null-list-view branch by returning `0` and `0` when no linked list view is hydrated.
- Synchronized the Vue API type and migration parity notes.

## Changed Files

- `fool-view/src/test/java/org/fool/framework/view/adapter/ViewAdapterTest.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/TableColumnInfo.java`
- `fool-view/src/main/java/org/fool/framework/view/adapter/ViewAdapter.java`
- `frontend/src/api.ts`
- `docs/migration/foolframe-parity.md`

## Validation

- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=ViewAdapterTest#viewInfoIncludesLegacyLinkedListViewDefaults test`
  - Failed as expected with `TableColumnInfo should expose legacy list-view ID metadata`.
- GREEN: same focused command passed.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am test`
  - Passed: `Tests run: 31, Failures: 0, Errors: 0, Skipped: 0`.
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
  - Returned `data.tableColumn[].listViewId = [0, 0, 0]` and `data.tableColumn[].listViewType = [0, 0, 0]`.

## Risks

- Non-zero `ListViewId`/`ListViewType` still need runtime hydration from a linked legacy list view source.

## Follow-Ups

- Add linked-list-view lookup when the modern view-item load path can resolve `selectViewName` to a view id and type.

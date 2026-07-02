# View Column Behavior Metadata Parity

## Prompt

- Continue the Docker-backed migration from `../FoolFrame`.
- Keep the frontend on Vue and commit atomic slices.

## Scope

- Compared legacy list view metadata from:
  - `../FoolFrame/src/Server/Soway.Server/ListView/HandlerGetListView.cs`
  - `../FoolFrame/src/Server/Soway.Server/ListView/ViewItem.cs`
  - `../FoolFrame/src/Server/SCPB05-Soway.Model/View/ViewItem.cs`
- Migrated the remaining lightweight list column metadata already present in modern storage: `Format`, `IsReadOnly`, and `EditType`.

## Changes

- `fool-view/src/main/java/org/fool/framework/view/dto/TableColumnInfo.java`
- `fool-view/src/main/java/org/fool/framework/view/adapter/ViewAdapter.java`
- `fool-view/src/test/java/org/fool/framework/view/adapter/ViewAdapterTest.java`
- `frontend/src/api.ts`
- `docs/migration/foolframe-parity.md`

## Validation

- RED first:
  - `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=ViewAdapterTest#viewInfoIncludesLegacyColumnBehaviorMetadata test`
  - Failed as expected with `TableColumnInfo should expose legacy format metadata`.
- Focused GREEN:
  - Same command passed after adding DTO fields and adapter mapping.
- Backend module:
  - `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am test`
  - Passed: `Tests run: 24, Failures: 0, Errors: 0, Skipped: 0`.
- Frontend:
  - `cd frontend && npm test`
  - `cd frontend && npm run build`
  - Both passed.
- Docker runtime:
  - `docker compose up -d --build backend frontend`
  - Rebuilt and restarted backend and frontend.

## Runtime Evidence

- `docker compose ps` showed backend, frontend, mysql, and redis up; mysql and redis healthy.
- `curl -fsS http://localhost:8080/test` returned seeded order data.
- `curl -fsS http://localhost:8081/` returned the Vue app shell with the rebuilt asset.
- `curl -fsS -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view` returned `tableColumn` entries with `format`, `isReadOnly`, and `editType`.

## Risks

- This does not implement legacy detail/group/chart views; it only exposes list column behavior metadata already represented by current `ViewItem`.

## Follow-ups

- Continue comparing legacy list/detail view response contracts before adding heavier UI behavior.

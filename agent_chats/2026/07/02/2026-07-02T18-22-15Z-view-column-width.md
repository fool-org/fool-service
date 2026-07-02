# View Column Width Parity

## Prompt

- Continue Docker-backed FoolFrame migration.
- Report overall migration percentage and keep moving on concrete parity gaps.
- Frontend remains Vue.

## Scope

- Source parity checked against legacy FoolFrame list view metadata:
  - `../FoolFrame/src/Server/SCPB05-Soway.Model/View/ViewItem.cs`
  - `../FoolFrame/src/Server/Soway.Server/ListView/HandlerGetListView.cs`
- Migrated the legacy `ViewItem.Width` / `VIEW_ITEM_WIDTH` column metadata into backend DTOs, SQL seed schema, and Vue table rendering.

## Changes

- `fool-view/src/main/java/org/fool/framework/view/model/ViewItem.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/TableColumnInfo.java`
- `fool-view/src/main/java/org/fool/framework/view/adapter/ViewAdapter.java`
- `fool-view/src/test/java/org/fool/framework/view/adapter/ViewAdapterTest.java`
- `docker/mysql/init/006-view.sql`
- `frontend/src/api.ts`
- `frontend/src/App.vue`
- `docs/migration/foolframe-parity.md`

## Validation

- RED first:
  - `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=ViewAdapterTest#viewInfoIncludesLegacyColumnWidth test`
  - Failed as expected with `ViewItem should expose legacy width metadata`.
- Focused GREEN:
  - Same command passed after implementation.
- Backend module:
  - `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am test`
  - Passed: `Tests run: 23, Failures: 0, Errors: 0, Skipped: 0`.
- Frontend:
  - `cd frontend && npm run build`
  - `cd frontend && npm test`
  - Both passed after typing the computed result columns as `TableColumnInfo[]`.
- Docker runtime:
  - `docker compose up -d --build backend frontend`
  - Backend and frontend images rebuilt; backend container restarted successfully.

## Runtime Evidence

- Existing live MySQL volume was updated once:
  - `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "ALTER TABLE fool_sys_view_item ADD COLUMN width int NOT NULL DEFAULT 0 AFTER show_index;"`
- `docker compose ps` showed backend, frontend, mysql, and redis up; mysql and redis healthy.
- `curl -fsS http://localhost:8080/test` returned seeded order data.
- `curl -fsS http://localhost:8081/` returned the Vue app shell with built assets.
- `curl -fsS -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view` returned `tableColumn` entries with `"width":0`.

## Risks

- Existing database volumes need the idempotent schema block or a one-time live migration; fresh Compose initialization has the column in `006-view.sql`.
- This slice only covers list column width metadata. Other view layout semantics remain governed by the parity backlog.

## Follow-ups

- Continue comparing `Soway.Server` list/detail/group/chart handlers against the Vue replacement workflow.
- Keep percent estimates tied to `docs/migration/foolframe-parity.md` and live Docker/API evidence.

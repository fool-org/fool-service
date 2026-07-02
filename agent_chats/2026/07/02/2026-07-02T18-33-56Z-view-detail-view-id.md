# View DetailViewId Parity

## Prompt

- Continue the Docker-backed FoolFrame migration.
- Keep frontend on Vue and commit atomic slices.

## Scope

- Compared legacy `Soway.Server/ListView/HandlerGetListView.cs`, which maps `view.DefaultDetailView.ID` into `ViewData.DetailViewId`.
- Exposed the existing modern `View.defaultDetailView` ID through `ListViewInfo.detailViewId`.

## Changes

- `fool-view/src/main/java/org/fool/framework/view/dto/ListViewInfo.java`
- `fool-view/src/main/java/org/fool/framework/view/adapter/ViewAdapter.java`
- `fool-view/src/test/java/org/fool/framework/view/adapter/ViewAdapterTest.java`
- `frontend/src/api.ts`
- `docs/migration/foolframe-parity.md`

## Validation

- RED first:
  - `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=ViewAdapterTest#viewInfoIncludesLegacyDetailViewId test`
  - Failed as expected with `ListViewInfo should expose legacy detail view ID metadata`.
- Focused GREEN:
  - Same command passed after adding `detailViewId` to `ListViewInfo` and `ViewAdapter`.
- Backend module:
  - `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am test`
  - Passed: `Tests run: 25, Failures: 0, Errors: 0, Skipped: 0`.
- Frontend:
  - `cd frontend && npm test`
  - `cd frontend && npm run build`
  - Both passed.
- Docker runtime:
  - `docker compose up -d --build backend frontend`
  - Rebuilt backend/frontend and restarted backend.

## Runtime Evidence

- `docker compose ps` showed backend, frontend, mysql, and redis up; mysql and redis healthy.
- `curl -fsS http://localhost:8080/test` returned seeded order data.
- `curl -fsS http://localhost:8081/` returned the Vue app shell.
- `curl -fsS -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view` returned `detailViewId: 0`.

## Risks

- This only exposes default detail view IDs already present on `View`; it does not implement legacy `TempFile` because the modern model has no view-file field.

## Follow-ups

- Continue list/detail response parity before implementing heavier legacy page behavior.

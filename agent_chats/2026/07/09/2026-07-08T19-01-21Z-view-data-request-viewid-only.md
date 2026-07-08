# View Data Request ViewId Only

## Prompt

Keep the migration on the view-first path: render a View page first, then query
data from that View, without binding generic requests to business DTO names.

## Scope

- Removed `viewName` from generic `ViewDataRequest` and `QueryDataRequest`.
- Added unknown-field tolerance so stale `ViewName` / `viewName` JSON is ignored
  instead of becoming a supported shortcut.
- Kept `InputQueryRequest.viewName` untouched because it is a legacy protocol
  compatibility boundary.
- Removed the unused `viewName` field from the frontend `ViewDataRequest` type.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/dto/ViewDataRequest.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/QueryDataRequest.java`
- `fool-view/src/test/java/org/fool/framework/view/api/ViewControllerLegacyGetListViewTest.java`
- `fool-view/src/test/java/org/fool/framework/view/api/ViewControllerLegacyGetReadItemViewTest.java`
- `fool-view/src/test/java/org/fool/framework/view/api/DataControllerLegacyQueryDataTest.java`
- `frontend/src/api.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=ViewControllerLegacyGetListViewTest,DataControllerLegacyQueryDataTest test`
  - Failed because both generic request DTOs still declared `viewName`.
- GREEN: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=ViewControllerLegacyGetListViewTest,ViewControllerLegacyGetReadItemViewTest,DataControllerLegacyQueryDataTest test`
  - `13` focused controller tests passed.
- GREEN: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am test`
  - `149` fool-view reactor tests passed.
- GREEN: `cd frontend && npm test -- src/payload.test.ts`
  - `57` passed.
- GREEN: `cd frontend && npm run build`
  - `vue-tsc --noEmit && vite build` succeeded.
- GREEN: `python scripts/check_repo_harness.py`
  - Repository harness validation passed.
- GREEN: `git diff --check`
- GREEN: `docker compose up -d --build backend`
  - Backend image rebuilt and `fool-service-backend-1` restarted.
- GREEN: `docker compose ps`
  - Backend and frontend running; MySQL and Redis healthy.
- GREEN: `curl -sS -o /tmp/fool-service-backend-smoke.txt -w "%{http_code} %{size_download}\n" http://localhost:8080/test`
  - `200 465`.
- GREEN: `curl -sS -o /tmp/fool-service-frontend-smoke.html -w "%{http_code} %{size_download}\n" http://localhost:8081/`
  - `200 399`.

## Risks

- Clients that still send only `ViewName` to generic View/data endpoints now
  continue to receive the existing `ViewId is required` error. That is
  intentional; `ViewName` is not a rendering/data lookup entrypoint there.

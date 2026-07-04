# getlistview Pascal Metadata

## Prompt

User emphasized that the page must render from View metadata first, then query
data through that View context, and must not bind to concrete business DTOs.

## Scope

- Added FoolFrame Pascal response aliases to `getlistview` View metadata DTOs:
  top-level View info, list item/column metadata, and operation metadata.
- Added Vue helper support for Pascal `Items`, `Operations`, `DetailViewId`,
  and operation target fields while keeping that compatibility centralized in
  `viewWorkflow.ts`.
- Kept `App.vue` below 2000 lines after the helper change.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/dto/ListViewInfo.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/TableColumnInfo.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/OperationInfo.java`
- `fool-view/src/test/java/org/fool/framework/view/adapter/ViewAdapterTest.java`
- `frontend/src/api.ts`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/ListDataTable.vue`
- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- RED: focused backend test failed before DTO aliases because serialized
  `getlistview` metadata did not contain Pascal `ID`.
- GREEN: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=ViewAdapterTest,ViewDataAdapterTest test`
- GREEN: `cd frontend && npm test`
- GREEN: `cd frontend && npm run build`
- GREEN: `python3 scripts/check_repo_harness.py`
- GREEN: `git diff --check`
- GREEN: `docker compose up -d --build backend frontend`
- GREEN: `docker compose up -d --build frontend` after the final frontend
  fallback fix; Compose rebuilt and restarted backend and frontend.
- GREEN: `python3 scripts/runtime_doctor.py`

## Runtime Evidence

- `GET http://localhost:8080/test` returned the seeded backend smoke payload.
- Backend `POST http://localhost:8080/api/v1/view/getlistview` with
  `{"ViewId":100}` returned Pascal aliases including `ID=100`, `Items[0].ID`,
  `Items[0].PropertyName`, `Operations[0].ID`, `Operations[0].RequireSelect`,
  and `DetailViewId=102`.
- Frontend proxy `POST http://localhost:8081/api/v1/view/getlistview` with
  `{"ViewId":100}` returned `ID=100`, `Items=4`, `Operations=2`, and
  `DetailViewId=102`.
- Frontend HTML referenced the rebuilt asset `assets/index-DIVIMqJD.js`.
- `docker compose ps` showed backend, frontend, MySQL, and Redis running, with
  MySQL and Redis healthy.

## Risks

- `OperationParamInfo` remains a migrated camel-case extension; FoolFrame's
  `ViewOperation` shape for list metadata does not include params.
- Existing unrelated untracked `docs/superpowers/` was left untouched.

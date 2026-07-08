# Runoperation result aliases and View-first refresh

## Prompt

Continue the FoolFrame migration, but keep the Vue page flow View-first:
render from loaded View metadata, query data from that View, and avoid binding
page logic to concrete business DTOs.

## Scope

- Exposed FoolFrame Pascal aliases on `LegacyRunOperationResult`: `Value`,
  `IsSuccess`, `ReturnObjId`, `ReturnViewId`, and `ReturnMsg`.
- Added a shared Vue helper, `legacyRunOperationSuccess`, so operation refresh
  logic does not read result DTO fields directly in the page.
- Made `queryCurrentViewData()` verify that the rendered `getlistview(viewId)`
  metadata is loaded before calling `querydata(viewId)`.
- Updated detail field display to use the existing field display helper instead
  of direct `fmtValue` reads.
- Added a source-level regression check for the current data-query path.
- Recorded a follow-up to split `App.vue`; it is currently 2054 lines and
  should trend back below the 2000-line target before more panels are added.
- Updated `tasks.md` and `docs/migration/foolframe-parity.md`.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/dto/LegacyRunOperationResult.java`
- `fool-view/src/test/java/org/fool/framework/view/api/DataControllerRunOperationTest.java`
- `frontend/src/App.vue`
- `frontend/src/api.ts`
- `frontend/src/payload.test.ts`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`

## Validation

- `cd frontend && npm test`: passed, 73 tests.
- `cd frontend && npm run build`: passed.
- `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=DataControllerRunOperationTest -DfailIfNoTests=false test`: passed, 2 focused tests.
- `python scripts/check_repo_harness.py`: passed.
- `git diff --check`: clean.
- `docker compose up -d --build`: backend and frontend images built, backend
  and frontend containers recreated and started.
- `python scripts/runtime_doctor.py`: passed all compose, auth, View/data,
  detail, `inputquery`, report, message, notify, and logout checks.
- `docker compose ps`: backend, frontend, MySQL, and Redis running; MySQL and
  Redis healthy.
- `curl http://localhost:8080/test`: returned Docker seed rows.
- `curl http://localhost:8081/`: returned the rebuilt Vue shell.

## Runtime Evidence

- Backend container running on `http://localhost:8080`.
- Frontend container running on `http://localhost:8081`.
- Runtime doctor passed `view:getlistview` before `data:querydata`, then
  detail, inputquery, and report checks against the loaded View context.

## Risks

- `runtime_doctor.py` does not execute `runoperation` because the currently
  seeded operations mutate data. The Pascal result alias surface is covered by
  `DataControllerRunOperationTest`, and Vue success handling is covered by
  `viewWorkflow.test.ts`.

## Follow-ups

- Split `App.vue` View workflow state/actions before adding more panels; the
  file is still under the 2100-line harness limit but above the 2000-line
  target.

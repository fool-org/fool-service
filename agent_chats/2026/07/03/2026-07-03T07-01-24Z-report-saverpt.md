# Report Save Definition Parity

## Prompt

- Continue the FoolFrame migration.
- Keep the environment Docker-first.
- Keep the frontend in Vue.
- Commit atomically.

## Scope

- Compared the legacy Web `/report/saverpt` route and server
  `HandlerSaveReport`.
- Added `/api/v1/report/saverpt` to the Spring report controller.
- Reused `MakeReportRequest` for legacy `ViewId`, `ReportCols`, `FilterExp`,
  and `ReportName` payloads.
- Added a Vue save-report-definition panel.
- Documented that this slice matches the legacy no-op success surface, not
  persisted saved-report metadata.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/api/ReportController.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/MakeReportRequest.java`
- `fool-view/src/test/java/org/fool/framework/view/api/ReportControllerTest.java`
- `frontend/src/api.ts`
- `frontend/src/payload.ts`
- `frontend/src/payload.test.ts`
- `frontend/src/App.vue`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/03/2026-07-03T07-01-24Z-report-saverpt.md`

## Validation

- RED: `cd frontend && npm test`
  - Failed because App.vue did not contain `Save Report Definition`.
- RED: `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=ReportControllerTest test`
  - Failed test compilation on missing `MakeReportRequest.setReportName` and
    missing `ReportController.saveReport`.
- PASS: `cd frontend && npm test`
  - 18 tests passed.
- PASS: `cd frontend && npm run build`
  - `vue-tsc --noEmit && vite build`.
- PASS: `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=ReportControllerTest test`
  - `ReportControllerTest`: 9 tests, 0 failures.
- PASS: `docker compose up -d --build backend frontend`
  - Backend and frontend images built and restarted.
- PASS: `curl -sS http://localhost:8080/test`
  - Returned Docker seed order rows.
- PASS: `curl -sS -H 'Content-Type: application/json' -d '{"ViewId":100,"ReportName":"Order Daily","ReportCols":[{"ColName":"Symbol","Index":1}],"FilterExp":{"Col":{"Name":"order_state"},"CompareOp":{"ID":"1","Name":"等于"},"ValueExp":"0","ValueFmt":"Open"}}' http://localhost:8080/api/v1/report/saverpt`
  - Returned `{"code":0,"message":"success","data":null}`.
- PASS: `curl -sS -H 'Content-Type: application/json' -d '{"ViewId":100,"ReportName":"Order Daily","ReportCols":[{"ColName":"Symbol","Index":1}],"FilterExp":{"Col":{"Name":"order_state"},"CompareOp":{"ID":"1","Name":"等于"},"ValueExp":"0","ValueFmt":"Open"}}' http://localhost:8081/api/v1/report/saverpt`
  - Returned `{"code":0,"message":"success","data":null}` through the Vue
    frontend proxy.
- PASS: running frontend bundle grep found `/api/v1/report/saverpt` and
  `Save Report Definition`.

## Skipped Checks

- Full persisted saved-report behavior was not implemented because legacy
  `HandlerSaveReport.ImplementBusinessLogic` is empty.

## Risks

- This endpoint intentionally does not persist saved report definitions. Add a
  storage-backed report-definition slice only when a real legacy persistence
  target is identified.

## Follow-Ups

- Continue report migration with saved report metadata/execution/export once
  the legacy persistence surface is mapped.

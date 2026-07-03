# Vue Report Column Candidates

## Prompt

- Continue the FoolFrame migration using Docker.
- Keep the frontend in Vue.
- Report the overall migration percentage when asked.

## Scope

- Added the migrated report model candidate-column lookup to the Vue operator
  console.
- Reused the existing legacy list-view request shape to call
  `/api/v1/report/getmkqview` by view ID.
- Rendered candidate columns with compare/select catalogs and enum states.
- Updated migration parity evidence for the Vue replacement surface.

## Changed Files

- `frontend/src/api.ts`
- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/03/2026-07-03T06-54-07Z-vue-report-columns.md`

## Validation

- RED: `cd frontend && npm test`
  - Expected failure before implementation:
    `expected appSource to contain "/api/v1/report/getmkqview"`.
- PASS: `cd frontend && npm test`
  - `src/payload.test.ts` passed.
  - 17 tests passed.
- PASS: `cd frontend && npm run build`
  - `vue-tsc --noEmit && vite build`.
- PASS: `docker compose up -d --build frontend`
  - Backend and frontend images built.
  - MySQL and Redis were healthy.
  - Backend restarted successfully.
- PASS: `curl -sS http://localhost:8080/test`
  - Returned Docker seed order rows.
- PASS: `curl -sS -H 'Content-Type: application/json' -d '{"ViewId":100}' http://localhost:8081/api/v1/report/getmkqview`
  - Returned `code:0` with 3 report candidate columns.
- PASS: `curl -sS http://localhost:8081/assets/index-RK8vEffM.js | rg -o "Report Columns|/api/v1/report/getmkqview|reportModelResponse"`
  - Confirmed the running Compose frontend bundle includes `Report Columns`.

## Skipped Checks

- No full `mvn test` in this slice because backend Java code was unchanged.
  The Compose frontend rebuild still rebuilt the backend image and Maven reactor
  with skipped tests as part of the Docker build.

## Risks

- The long-lived Docker MySQL volume still contains double-encoded Chinese
  display text for the seeded compare/select catalogs. The Vue panel renders
  the API response as returned; fixing the seed/data encoding should be a
  separate data migration slice.

## Follow-Ups

- Add a browser/runtime doctor once the frontend workflow stabilizes enough for
  repeatable UI smoke automation.

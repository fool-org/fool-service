# Vue Report Columns Autofill

## Prompt

Continue the Docker/Vue FoolFrame migration and keep the frontend usable
without growing `App.vue` past the size guard.

## Scope

- Compared the current Vue report tool with FoolFrame `mkreport.js`, where a
  selected report column stores `ColName`, `ColId`, `SelectedTypeId`, `Index`,
  and `OrderType=2`.
- Added a tested `buildReportColsFromModel` helper in `viewWorkflow.ts`.
- Wired `loadReportColumns()` to populate the existing `Report Columns JSON`
  field from `getmkqview` metadata.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/04/2026-07-03T20-37-36Z-vue-report-cols-autofill.md`

## Validation

- RED:
  `cd frontend && npm test -- viewWorkflow.test.ts payload.test.ts`
  failed because `buildReportColsFromModel` was missing and `App.vue` did not
  update `reportColsJson`.
- GREEN:
  `cd frontend && npm test -- viewWorkflow.test.ts payload.test.ts`
- `cd frontend && npm test && npm run build`
- `wc -l frontend/src/App.vue frontend/src/viewWorkflow.ts frontend/src/viewWorkflow.test.ts frontend/src/payload.test.ts`
  confirmed `frontend/src/App.vue` is 1996 lines.
- `python3 scripts/check_repo_harness.py`
- `git diff --check`
- `docker compose up -d --build --force-recreate frontend`
- `docker compose ps`
- `curl -fsS http://localhost:8081/`
- `curl -fsS http://localhost:8080/test`
- `curl -fsS -H 'Content-Type: application/json' -d '{"ViewId":100}' http://localhost:8081/api/v1/report/getmkqview`
  returned `code=0`, 4 columns, and names
  `Order ID`, `Symbol`, `Customer`, `State`.

## Skipped

- Did not add a full visual report-column picker. The existing textarea remains
  the editable escape hatch until report execution semantics are complete.

## Risks

- `SelectedTypeId` is now present in the Vue payload, but backend report
  execution still needs a separate slice to execute selected-type aggregates
  through the shared query path.

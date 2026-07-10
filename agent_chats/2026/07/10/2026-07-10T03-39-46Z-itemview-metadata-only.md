# Item View Metadata-Only Rendering

## Prompt
- Continue the Docker/FoolFrame/Vue migration, inspecting View rendering before
  data binding and avoiding concrete business DTO coupling.

## Scope
- Compared the legacy `/itemview:id` Express route and `item.jade` page with
  the current Vue startup path.
- Fixed Vue so `/itemview:id` loads `getreaditemview` metadata without calling
  `querydatadetail` with an empty object id.
- Kept read-item View metadata under both the requested View id and the
  resolved View id, reusing the shared View cache.
- Rendered declared fields and child View columns in a full-width metadata-only
  detail panel on desktop and mobile.

## Changed Files
- `frontend/src/App.vue`
- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/style.css`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/useViewDataWorkflow.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/10/2026-07-10T03-39-46Z-itemview-metadata-only.md`

## Validation
- `cd frontend && npm test` passed: 7 files, 130 tests.
- `cd frontend && npm run build` passed.
- `docker compose up -d --build frontend` completed successfully.
- `docker compose up -d --no-deps --force-recreate frontend` replaced the
  running frontend container.
- `python scripts/runtime_doctor.py` passed.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.

## Runtime Evidence
- Before the fix, browser validation of `/itemview100` showed order `1001`,
  `BTC-USDT`, and `Ada Capital`; backend logs showed
  `querydatadetail` with `objId=""`.
- After the fix, the page showed only `OrderList` field definitions and the
  `Items` child columns. A 390px viewport had no horizontal overflow.
- Backend logs since `2026-07-10T03:38:00Z` showed
  `/api/v1/view/getreaditemview` and no `/api/v1/data/querydatadetail` for the
  browser reload.
- Screenshot:
  `artifacts/runs/20260710-itemview-metadata-only/itemview100-desktop.png`.

## Risks
- `App.vue` is 1,992 lines after this slice, still below the current 2,000-line
  guard but with little room for more inline workflow code.

## Follow-ups
- Move the legacy list-page report workflow out of API Tools and into the
  View-driven page using the existing report state and helpers.

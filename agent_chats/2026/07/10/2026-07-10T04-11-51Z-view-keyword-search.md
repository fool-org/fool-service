# View Keyword Search

## Prompt
- Continue the Docker/FoolFrame/Vue migration toward a usable frontend while
  keeping View metadata ahead of data and avoiding concrete business DTOs.

## Scope
- Compared the old `view.jade` / `querylistdata.js` list search with the
  current Vue toolbar.
- Removed editable View ID, `Load View`, and raw `QueryFilter` controls from
  the main View page.
- Added a normal Search input that sends the generic `keyword` only after
  `getlistview` metadata is loaded.
- Kept the API Tools `QueryFilter` as a separate compatibility state so raw
  filtering cannot leak back into the main View workflow.
- Reset keyword state when navigating to another menu or legacy View route.
- Kept paging, auto-refresh, detail selection, and no-result detail clearing
  on the shared View-first query path.

## Changed Files
- `frontend/src/App.vue`
- `frontend/src/style.css`
- `frontend/src/useViewDataWorkflow.ts`
- `frontend/src/useViewDataWorkflow.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/10/2026-07-10T04-11-51Z-view-keyword-search.md`

## Validation
- `cd frontend && npm test` passed: 7 files, 132 tests.
- `cd frontend && npm run build` passed.
- `python scripts/check_repo_harness.py` passed.
- `docker compose build frontend` passed.
- `docker compose up -d --force-recreate --no-deps frontend` replaced the
  running frontend container.
- `python scripts/runtime_doctor.py` passed.
- `git diff --check` passed.

## Runtime Evidence
- `/view100` no longer rendered editable View ID, `Load View`, or
  `QueryFilter` controls in the main View panel.
- Searching `BTC` returned only order `1001` / `BTC-USDT` and selected detail
  object `1001`; clearing and submitting restored all eight rows.
- Pressing Enter with `ETH` returned only order `1002` / `ETH-USDT`.
- At 390px, document and viewport widths both remained 390px and the toolbar
  rendered as a 328px single-column grid.
- Browser console warnings/errors: none.
- Screenshots:
  `artifacts/runs/20260710-view-keyword-search/view-search-desktop.png` and
  `artifacts/runs/20260710-view-keyword-search/view-search-mobile.png`.

## Risks
- Generic keyword matching intentionally follows rendered View metadata and
  searchable scalar/business-object display columns; it is not an arbitrary
  SQL-expression editor.

## Follow-ups
- Continue the frontend parity audit for legacy shell actions that still only
  appear in API Tools rather than their user-facing View or shell context.

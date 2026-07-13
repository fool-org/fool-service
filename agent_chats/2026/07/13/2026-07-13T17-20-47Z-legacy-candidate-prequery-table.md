# Legacy Candidate Prequery Table Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Inspect the old select-existing child flow before
changing the candidate table's prequery state.

## Scope

- Show candidate View headings before the first data query.
- Keep prequery data/filler rows absent and queried pages padded to ten rows.
- Preserve View-first projection and local-only candidate staging.

## Changed Files

- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/ListDataTable.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T17-20-47Z-legacy-candidate-prequery-table.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/detailView.jade` mounts the candidate table before
  a query runs.
- `../FoolFrame/src/Web/public/javascripts/app/detailview.js` `initQueryView`
  loads the candidate View, clears the table, and appends its headings before
  opening the selection dialog.
- `../FoolFrame/src/Web/public/javascripts/app/querylistdata.js` appends query
  rows, pads to the page size, and passes the selected View-projected row to the
  detail controller without saving immediately.

## Implementation

- Kept `ListDataTable` mounted before `candidateState.queried` becomes true.
- Set its minimum rows to zero before query and to the existing page size after
  query.
- Reused shared table state to hide PrimeVue's automatic empty-message row when
  there are no rendered rows.
- Added no DTO, API, dependency, component, or duplicated candidate state.

## Validation

- Full frontend suite: 155 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Docker frontend production build and forced no-dependency recreation passed.
  Deployed image id:
  `sha256:6bed5cfef8b6a0c2a320a99fe399fffbd93f6625d08d515ec077c7c0ae130064`.
- Authenticated `/view100/1001` candidate dialog rendered one table, three
  View-derived headings, one visible heading row, and `记录数未知,请查询` before
  query. A zero-result query retained three headings and produced 11 rows total.
- Querying `Item` returned two selectable rows. Selecting the first closed the
  dialog and increased the detail table from four to five rows; reloading
  restored four rows, proving the selection was not persisted.
- Focused screenshots at 1440x1000 and 390x844 show the prequery heading-only
  table without clipping or overlap.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-candidate-prequery-table/candidate-prequery-desktop.png`
- `artifacts/runs/20260714-legacy-candidate-prequery-table/candidate-prequery-mobile.png`

## Risks And Follow-Ups

- Parent Save persistence was intentionally not exercised in this slice; the
  existing staged-child save contract and tests remain unchanged.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.

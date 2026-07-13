# Main List Pagination Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically.

## Scope

- Compare old `view.jade`, `viewWithChart.jade`, and `navbar.js` against the
  shared Vue list panel.
- Restore the old Previous / direct page links / Next interaction and separate
  record-total placement.
- Remove main-list controls and update-time text absent from the old templates.
- Prove a direct page-2 request in the authenticated Docker runtime.

## Changed Files

- `frontend/src/ViewListPanel.vue`
- `frontend/src/useViewDataWorkflow.ts`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T11-27-53Z-list-pagination-parity.md`

## Legacy Evidence

- `views/view.jade` and `views/viewWithChart.jade` place `#info` above
  `ul.pagination` and do not render `querytime`.
- `navbar.js` renders Previous, at most seven direct page links, and Next.
- `querylistdata.js` updates the record text to `共{total}条记录`.
- `FreshTime` is visible only in Sudoku List/Map/Chart include templates.

## Validation

- Focused `payload.test.ts`: 82 tests passed.
- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Compose frontend build passed with image manifest
  `sha256:659b472d9f229c366ce1e5fe3c88826f7de5f26581084fbef1c91f21369a1ed4`.
- Compose frontend/backend smoke requests passed; `db-migrate` remained
  `Exited (0)`.
- Authenticated browser at 390x844 had `scrollWidth=390`, record text
  `共8条记录`, Previous / Page 1 / Next, and no main-list update time.
- Three exact temporary rows (`999991001` through `999991003`) expanded the
  list to 11 records. Page 2 appeared, clicking it selected page 2 and queried
  the single remaining row (`1001`) without changing the `/` route.
- All three temporary rows were deleted; their database count returned to zero,
  and the browser returned to 8 records on page 1.
- Browser console logs: none.

## Runtime Evidence

- `artifacts/runs/20260713-list-pagination-parity/desktop.png`
- `artifacts/runs/20260713-list-pagination-parity/mobile.png`

## Risks And Follow-Ups

- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.

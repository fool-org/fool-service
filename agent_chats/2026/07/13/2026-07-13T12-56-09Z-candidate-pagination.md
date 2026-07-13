# Candidate Pagination Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Keep files controlled and reuse code.

## Scope

- Compare candidate-dialog paging with the old shared navbar behavior.
- Reuse one presentation-only paginator for main and candidate lists.

## Changed Files

- `frontend/src/LegacyPagination.vue`
- `frontend/src/ViewListPanel.vue`
- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T12-56-09Z-candidate-pagination.md`

## Legacy Evidence

- `../FoolFrame/src/Web/public/javascripts/app/querylistdata.js` lines 171-175
  send every query result's total, page size, and current page to
  `navbar.updateNavbar`, including the candidate dialog.
- `navbar.js` renders Previous, up to seven direct page links, and Next.
- Vue used that contract only on the main list; the candidate dialog exposed
  Previous, an invented `第 x / y 页` label, and Next with no direct jump.

## Implementation

- Extracted the existing PrimeVue paginator contract into the 39-line
  `LegacyPagination.vue` presentation component.
- Main and candidate lists now pass their own generic totals and page state to
  that component; candidate querying remains in the existing App workflow.
- Candidate pagination remains absent before its first query, matching the old
  dialog's cleared `#pagenav` state.

## Validation

- Focused `payload.test.ts`: 82 tests passed.
- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Compose frontend build passed with image manifest
  `sha256:e387966128915f0e9f7b9b0d81980744ed9a76ef7662f9c787f4d7042915303a`.
- Compose frontend/backend smoke requests returned HTTP 200; MySQL and Redis
  remained healthy and `db-migrate` remained `Exited (0)`.
- Authenticated `/view100/1001`: the candidate paginator was absent before
  Find, then showed `共1条记录` and direct page `1` after querying `Legacy`.
- Main `/view100` retained `共8条记录` and direct page `1` after extraction.
- At 390px, the candidate dialog measured 358px wide, retained its result and
  page link, and page `scrollWidth` equaled 390.

## Runtime Evidence

- `artifacts/runs/20260713-candidate-pagination/desktop.jpg`
- `artifacts/runs/20260713-candidate-pagination/mobile.jpg`

## Risks And Follow-Ups

- Seed data has one candidate page, so runtime proves rendering and state
  wiring; the existing page event maps PrimeVue's zero-based index to the
  backend's one-based page and remains source-contract tested.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.

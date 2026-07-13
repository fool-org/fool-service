# Candidate Enter Query Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically.

## Scope

- Compare the old select-existing candidate form's Enter behavior against the
  deployed detail page.
- Route Enter through the existing metadata-driven candidate query event.

## Changed Files

- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T12-48-59Z-candidate-enter-query.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/detailView.jade` lines 132-135 place the
  candidate input and `query()` Find button in one form.
- The deployed Vue input accepted Enter without running a query, leaving
  `记录数未知,请查询` and no candidate rows.
- The Vue input now emits the same `queryExistingDetailItems` event used by
  the existing Find button; no query or paging state was duplicated.

## Validation

- Focused `payload.test.ts`: 82 tests passed.
- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Compose frontend build passed with image manifest
  `sha256:19a8519f1e1ff6de5f235241f78eb850acaadcce5aa3bc2a8a9be21011273cca`.
- Compose frontend/backend smoke requests returned HTTP 200; MySQL and Redis
  remained healthy and `db-migrate` remained `Exited (0)`.
- Authenticated desktop `/view100/1001`: entering `Legacy` and pressing Enter
  returned the `Legacy item` candidate and changed the feedback to `共1条记录`.
- At 390px, the dialog measured 358px wide, retained one result row, and page
  `scrollWidth` equaled 390.

## Runtime Evidence

- `artifacts/runs/20260713-candidate-enter-search/desktop.jpg`
- `artifacts/runs/20260713-candidate-enter-search/mobile.jpg`

## Risks And Follow-Ups

- This change only adds an input event binding to the existing query path; the
  shared candidate state and pagination behavior are unchanged.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.

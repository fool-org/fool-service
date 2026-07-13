# Legacy Table Hover Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Keep the implementation small and reusable.

## Scope

- Restore the old shared list-row hover feedback.
- Preserve contextual `RowFmt` hover colors and the existing View-first table.

## Changed Files

- `frontend/src/style.css`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T13-28-23Z-legacy-table-hover.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/view.jade`, `viewWithChart.jade`,
  `detailView.jade`, and `views/includes/List.jade` all apply `.table-hover`.
- `../FoolFrame/src/Web/public/stylesheets/bootstrap.css` lines 3136-3139 set
  the hovered row background to `#f5f5f5`.
- The deployed PrimeVue table had no equivalent row-hover class or rule.

## Implementation

- Added one shared metadata-table hover rule using Bootstrap's exact color.
- Kept the contextual hover rule after the generic rule so `active`, `success`,
  `info`, `warning`, and `danger` retain their old specific colors.

## Validation

- Focused `payload.test.ts`: 82 tests passed.
- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Compose frontend build passed with image id
  `sha256:d48615854ccaa53625189a3dd8134332adad1f56c231e093c8e3b46e90a55a3e`.
- Compose frontend/backend smoke requests returned HTTP 200; MySQL and Redis
  remained healthy and `db-migrate` remained `Exited (0)`.
- Authenticated `/view100` CSSOM exposed the deployed selector
  `.metadata-data-table .p-datatable-tbody > tr:hover > td` with computed rule
  value `rgb(245, 245, 245)` at 1280x720.

## Runtime Evidence

- `artifacts/runs/20260713-legacy-table-hover/deployed-css-rule.json`

## Risks And Follow-Ups

- The browser automation surface did not retain a synthetic `:hover` state,
  so the runtime artifact records the deployed CSSOM rule rather than claiming
  a misleading hover screenshot.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.

# Legacy Table Cell Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Keep files small and reuse shared code.

## Scope

- Restore Bootstrap table header/body typography, alignment, and borders.
- Keep the existing shared View-first table and responsive behavior.

## Changed Files

- `frontend/src/style.css`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T13-37-46Z-legacy-table-cells.md`

## Legacy Evidence

- Bootstrap 3 table cells inherit 14px type at `1.42857143` line height,
  align body cells to the top, and use a 1px `#ddd` top border.
- Table headers remain bold, align to the bottom, and use a 2px `#ddd` bottom
  border.
- The deployed PrimeVue table instead used centered 15px/21.75px body cells,
  normal-weight headers, and 1px `#cbd5e1` bottom borders.

## Implementation

- Extended the existing shared header/body selectors with Bootstrap's exact
  font size, line height, weight, alignment, and border values.
- Added no component, prop, state, or request code; the stylesheet grew by
  nine declarations and remains 944 lines.

## Validation

- Focused `payload.test.ts`: 82 tests passed.
- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Compose frontend build passed with image id
  `sha256:df9632d3438426fd8c356fca936ca6103f9d67b4f8b384b19bdc0471a6c33f6e`.
- Compose frontend/backend smoke requests returned HTTP 200; MySQL and Redis
  remained healthy and `db-migrate` remained `Exited (0)`.
- Authenticated `/view100` computed header styles were
  `14px/20px/700/bottom/2px #ddd`; body styles were
  `14px/20px/400/top/1px #ddd` with no bottom border.
- At 390px the page `scrollWidth` remained 390 and headers remained 32px high.

## Runtime Evidence

- `artifacts/runs/20260713-legacy-table-cells/desktop.jpg`
- `artifacts/runs/20260713-legacy-table-cells/mobile.jpg`

## Risks And Follow-Ups

- Long wrapped values can still increase row height, as they did in the old
  table; the fixed page-row count and responsive wrapping remain unchanged.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.

# Legacy Row Format Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Resolve Views before data and avoid business DTO
binding.

## Scope

- Restore the visible Bootstrap contextual styles used by legacy list
  `EditType=10` row-format metadata.
- Preserve the existing View-first row class flow and responsive table layout.

## Changed Files

- `frontend/src/style.css`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T13-23-46Z-legacy-row-format.md`

## Legacy Evidence

- `../FoolFrame/src/Web/public/javascripts/app/querylistdata.js` lines 129-136
  append every `EditType == 10` item's `FmtValue` to the generated table-row
  class instead of rendering that item as a cell.
- The old application loaded Bootstrap 3, whose `active`, `success`, `info`,
  `warning`, and `danger` table classes supplied contextual background and
  hover colors.
- Vue already derives and applies each row's `RowFmt`; the missing stylesheet
  rules were the only gap in this path.

## Implementation

- Added the five Bootstrap 3 contextual row color pairs under the shared
  metadata table scope.
- Reused two CSS rules and custom properties for normal and hover backgrounds;
  no new component, request, row state, or business DTO binding was added.

## Validation

- Focused `payload.test.ts`: 82 tests passed.
- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Compose frontend build passed with image id
  `sha256:74206c73fb0440ca75bea60d844616810362565665ec6554b5352826cb55b786`.
- Compose frontend/backend smoke requests returned HTTP 200; MySQL and Redis
  remained healthy and `db-migrate` remained `Exited (0)`.
- Authenticated `/view100`: a temporary local `EditType=10` Symbol item and
  `warning` value removed the Symbol cell, applied row class
  `p-row-odd warning`, and produced `rgb(252, 248, 227)` on desktop.
- At 390px, the warning background remained and page `scrollWidth` stayed 390.
- Both metadata tables and the order sample were restored after the probe;
  final DB evidence was `0/0/BTC-USDT`, and the reloaded row exposed Symbol
  again with no warning class or contextual background.

## Runtime Evidence

- `artifacts/runs/20260713-legacy-row-format/desktop-warning.jpg`
- `artifacts/runs/20260713-legacy-row-format/mobile-warning.jpg`
- `artifacts/runs/20260713-legacy-row-format/restored-desktop.jpg`

## Risks And Follow-Ups

- Standard Bootstrap contextual classes are restored. Application-specific
  custom classes still require their own stylesheet rules, as in FoolFrame.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.

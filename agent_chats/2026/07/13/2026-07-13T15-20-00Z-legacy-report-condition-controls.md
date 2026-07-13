# Legacy Report Condition Controls Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Keep files small and reuse shared code.

## Scope

- Restore the condensed condition editor's control density.
- Preserve metadata-driven field, comparison, value, and grouping behavior.

## Changed Files

- `frontend/src/style.css`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T15-20-00Z-legacy-report-condition-controls.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/view.jade` renders report conditions in
  `.table.table-condensed` with native joins, fields, comparisons, and values.
- The Vue condition grid preserved the old columns and state flow but left
  PrimeVue Select/Input controls at their larger default height.

## Implementation

- Scoped Select and Input geometry to `.report-condition-row`.
- Reused the migrated form contract: 34px height, 4px radius, 6x12px padding,
  14px text, 20px line height, and a stable 34px Select dropdown target.
- Added no component, state, event, DTO, or serializer changes.
- A CSS raw-source assertion was attempted and removed because this Vite test
  setup returns an empty string for CSS `?raw`; runtime computed styles provide
  the direct evidence instead.

## Validation

- Focused frontend suite: 82 tests passed after removing the invalid CSS raw
  assertion.
- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Docker frontend production build and forced Compose recreation passed.
  Deployed image id:
  `sha256:581337f4e6265584100b4744442a441d8ac83f958de4482c2cd9a8c526367494`.
- Authenticated `/view101` report setup added two conditions. The field Select
  root computed to 34px height, 4px radius, and 14px text; its label computed
  6x12px padding and 20px line height.
- Choosing Item ID still generated the default Equal comparison and a value
  input. That input computed to 34px, 6x12px, 14/20px, and 4px radius.
- Desktop and 390x844 screenshots show compact rows; mobile retains horizontal
  scrolling instead of collapsing condition columns.
- Repository harness validation passed; frontend `/` and frontend-proxied
  `/test` returned HTTP 200. MySQL and Redis remained healthy and `db-migrate`
  remained `Exited (0)`.

## Runtime Evidence

- `artifacts/runs/20260713-legacy-report-condition-controls/condition-controls-desktop.png`
- `artifacts/runs/20260713-legacy-report-condition-controls/condition-controls-mobile.png`

## Risks And Follow-Ups

- The mobile condition table intentionally scrolls horizontally like the old
  fixed-column editor; responsive column rewriting would change its workflow.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.

# Legacy Column Width Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Resolve Views before data and avoid business DTO
binding.

## Scope

- Restore metadata-defined list column widths from old Jade templates.
- Keep zero-width configuration on automatic table layout.

## Changed Files

- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/ListDataTable.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T13-16-38Z-legacy-column-width.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/view.jade` line 23 and
  `viewWithChart.jade` line 31 apply each ViewItem's `Width` as a pixel header
  width.
- Vue already loaded camel/Pascal width metadata in `TableColumnInfo` but the
  shared table ignored it.
- Current Docker View rows configure width zero, which preserves browser
  automatic table layout.

## Implementation

- Added one pure `columnWidth` metadata helper that normalizes camel/Pascal
  positive widths and returns zero for missing, zero, negative, or invalid
  values.
- `ListDataTable` applies the resulting pixel width to its shared PrimeVue
  columns; list rows remain unrelated to this presentation decision.

## Validation

- Focused `viewWorkflow.test.ts`: 48 tests passed.
- Focused `payload.test.ts`: 82 tests passed.
- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Compose frontend build passed with image manifest
  `sha256:8b52bf18a60d696d456ec4d77cb748e6d00b6ad4f864842dcdd36875280753b8`.
- Compose frontend/backend smoke requests returned HTTP 200; MySQL and Redis
  remained healthy and `db-migrate` remained `Exited (0)`.
- Authenticated `/view100`: default zero-width `Symbol` had no inline style
  and measured 208px. A temporary local metadata width of 480 produced exact
  `width: 480px` and a 480px desktop column through the deployed frontend.
- At 390px, the same metadata style remained present while the table adapted
  within the viewport and page `scrollWidth` stayed 390.
- Both local metadata tables were restored after the probe; final DB evidence
  for modern/legacy Symbol widths was `0/0`, and the browser style was absent.

## Runtime Evidence

- `artifacts/runs/20260713-legacy-column-width/desktop-override.jpg`
- `artifacts/runs/20260713-legacy-column-width/mobile-override.jpg`

## Risks And Follow-Ups

- Width remains a table-layout hint, matching Bootstrap's automatic table
  behavior; narrow viewports may compress a configured width to avoid page
  overflow.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.

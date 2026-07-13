# Legacy Inert Row Operations

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically and retaining View-first rendering and code reuse.

## Scope

- Restore row-operation presentation when metadata has no target View id.
- Preserve target-View row navigation through the shared table.
- Correct the earlier migration record that described inert names as disabled.

## Changed Files

- `frontend/src/ListDataTable.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T20-58-22Z-legacy-inert-row-operations.md`

## Legacy Evidence

- `view.jade` registers every View operation with `querylistdata.js`.
- For a selected-row operation whose `ViewId` is greater than zero, old Web
  attaches `setselect(ViewId, row.Id, rowIndex)` to its anchor.
- For `ViewId <= 0`, old Web still renders the metadata name but creates an
  anchor with no click handler. It does not render a disabled form control.

## Implementation

- The shared table now chooses a PrimeVue navigation button only for positive
  target View ids. Non-target operations render as inert text with no event or
  button role and retain the old Bootstrap link blue.
- Filler rows, operation headings, target View navigation, and View-derived
  operation labels remain unchanged. `ListDataTable.vue` is 127 lines.

## Validation

- Full frontend suite: 164 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Frontend Docker build and forced recreation passed. Deployed image id:
  `sha256:e7633d44c7c49027453d0c5438e0bfa201d8e3397584acf765370ef003ed8111`.
- Authenticated `/view100` retained eight data rows and all 16 seeded `删除` /
  `保存` metadata names. The operation columns contained zero buttons or
  button roles, and the inert text computed to `rgb(51, 122, 183)`.
- At 390x844 the same 16 names remained visible, the table pane was 328px wide,
  and document scroll width remained exactly 390px.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-inert-row-operations/inert-row-operations-desktop.png`
- `artifacts/runs/20260714-legacy-inert-row-operations/inert-row-operations-mobile.png`

## Risks And Follow-Ups

- Seeded row operations intentionally have no target View id, so they remain
  non-executable exactly like old Web. Positive target ids retain the existing
  tested navigation branch.
- Complete page parity remains subject to further View-by-View comparison.
- The existing authenticated browser session was reused, so no fresh CAPTCHA
  was generated or read for this frontend-only slice.

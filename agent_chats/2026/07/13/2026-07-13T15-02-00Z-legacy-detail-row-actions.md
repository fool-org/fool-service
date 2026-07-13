# Legacy Detail Row Actions Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Keep files small and reuse shared code.

## Scope

- Restore the old child-row action weight without changing staging behavior.
- Preserve inline editing, deletion, and parent-save ownership.

## Changed Files

- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T15-02-00Z-legacy-detail-row-actions.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/detailView.jade` renders child Edit and Delete as
  plain links instead of default or danger buttons.
- `detailview.js` toggles Edit to Save, stages field changes, and hides deleted
  rows locally; persistence remains owned by the parent Save command.
- The Vue event chain already matches that behavior but used bordered primary
  Edit/Save and outlined-danger Delete buttons that inflated each row.

## Implementation

- Changed inline Edit/Save to a secondary text command.
- Changed Delete from outlined danger to text danger.
- Retained existing PrimeVue icons for recognition and kept all event handlers,
  labels, disabled states, and mutation helpers unchanged.
- Added source-level assertions for both action presentations; no CSS or helper
  abstraction was needed.

## Validation

- Focused frontend suite: 82 tests passed.
- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Docker frontend production build and forced Compose recreation passed.
  Deployed image id:
  `sha256:635a1d78805079b784936bd1374cacb61af6e7c7b12d9a81880db3ee892f5ab8`.
- Authenticated `/view102/1001` exposed three text-danger Delete commands.
- Clicking Delete on item `2004` removed only that rendered row and left two
  Delete commands. Reloading without parent Save restored item `2004` and all
  three commands, proving no persisted mutation.
- Desktop and 390x844 screenshots show lighter action columns without overlap;
  a horizontally scrolled mobile screenshot covers Delete and Detail commands.
- Repository harness validation passed; frontend `/` and frontend-proxied
  `/test` returned HTTP 200. MySQL and Redis remained healthy and `db-migrate`
  remained `Exited (0)`.

## Runtime Evidence

- `artifacts/runs/20260713-legacy-detail-row-actions/row-actions-desktop.png`
- `artifacts/runs/20260713-legacy-detail-row-actions/row-actions-mobile.png`
- `artifacts/runs/20260713-legacy-detail-row-actions/row-actions-mobile-operations.png`

## Risks And Follow-Ups

- Danger color and familiar icons are retained as a usability improvement over
  the old unstyled links.
- The seeded Docker View uses select-existing children, so inline Edit/Save is
  covered by source assertions and existing staging tests rather than this
  runtime fixture.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.

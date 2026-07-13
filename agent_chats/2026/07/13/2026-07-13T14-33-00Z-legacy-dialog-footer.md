# Legacy Dialog Footer Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Keep files small and reuse shared code.

## Scope

- Restore old modal-footer button hierarchy and compact Bootstrap geometry.
- Preserve existing View-first events, disabled states, and dialog ownership.

## Changed Files

- `frontend/src/ShellActions.vue`
- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/ViewReportPanel.vue`
- `frontend/src/payload.test.ts`
- `frontend/src/style.css`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T14-33-00Z-legacy-dialog-footer.md`

## Legacy Evidence

- `detailView.jade` renders candidate Cancel with `.btn.btn-default`; its inert
  primary Confirm placeholder remains intentionally omitted from Vue.
- `view.jade` renders report Cancel as `.btn-default`, Confirm and result Return
  as `.btn-primary`, and Save as `.btn-info`.
- `default.jade` renders system-message, info, and error footer commands with
  `.btn-default`.
- Bootstrap 3 buttons use 34px height, 6x12px padding, 14/20px typography, 4px
  radius, and adjacent modal-footer buttons use 5px spacing.

## Implementation

- Changed default candidate/report dismiss commands from text to outlined
  secondary buttons.
- Changed system-message and operation feedback commands from filled secondary
  to outlined secondary buttons.
- Kept report Confirm and Return primary and retained every existing click,
  disabled, and close path.
- Added one shared dialog-footer geometry rule instead of per-dialog CSS.
- Added source-level assertions for the hierarchy that previously regressed.

## Validation

- Focused frontend suite: 82 tests passed.
- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Direct frontend image build and forced Compose recreation passed. Deployed
  image id:
  `sha256:c4b61aa91a567b13727fa97cbbe0ada3a89160b4acbccaa8c374358900f46d95`.
- Authenticated candidate runtime exposed one outlined-secondary Cancel button;
  clicking it closed the dialog without adding data.
- Authenticated report runtime exposed outlined-secondary Cancel and primary
  Confirm buttons; clicking Cancel closed the dialog.
- Desktop and 390x844 screenshots show stable footer alignment without overlap
  or horizontal overflow.
- Repository harness validation passed; frontend `/` and frontend-proxied
  `/test` returned HTTP 200. MySQL and Redis remained healthy and `db-migrate`
  remained `Exited (0)`.

## Runtime Evidence

- `artifacts/runs/20260713-legacy-dialog-footer/candidate-footer-desktop.png`
- `artifacts/runs/20260713-legacy-dialog-footer/report-footer-desktop.png`
- `artifacts/runs/20260713-legacy-dialog-footer/report-footer-mobile.png`

## Risks And Follow-Ups

- The candidate template's old inert Confirm placeholder remains intentionally
  omitted; restoring a nonfunctional command would reduce usability.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.

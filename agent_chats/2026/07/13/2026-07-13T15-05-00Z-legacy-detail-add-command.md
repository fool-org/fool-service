# Legacy Detail Add Command Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Keep files small and reuse shared code.

## Scope

- Restore the child collection Add command's legacy default-group geometry.
- Preserve direct-add and select-existing behavior.

## Changed Files

- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T15-05-00Z-legacy-detail-add-command.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/detailView.jade` places each collection Add
  command inside `.btn-group` and renders it as `.btn.btn-default`.
- The Vue Add command already had default secondary outlined hierarchy but its
  size remained independent from the restored detail command group.

## Implementation

- Added the existing `legacy-button-group` utility to the collection toolbar.
- Reused the shared 34px, 6x12px, 14/20px command geometry; no CSS, component,
  helper, event, or state was added.
- Updated the existing source assertions for the shared class.

## Validation

- Focused frontend suite: 82 tests passed.
- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Docker frontend production build and forced Compose recreation passed.
  Deployed image id:
  `sha256:07d07583fc41eb5d1707ea9b27827927fab89ae041668940f4d5b4e9586834a4`.
- Authenticated `/view102/1001` runtime exposed exactly one grouped Add command
  with computed 34px height, 6x12px padding, 14px text, and 20px line height.
- Add still opened the Items candidate dialog and Cancel closed it with no
  selection or data mutation.
- Desktop and 390x844 screenshots show stable placement without wrapping or
  horizontal overflow.
- Repository harness validation passed; frontend `/` and frontend-proxied
  `/test` returned HTTP 200. MySQL and Redis remained healthy and `db-migrate`
  remained `Exited (0)`.

## Runtime Evidence

- `artifacts/runs/20260713-legacy-detail-add-command/detail-add-desktop.png`
- `artifacts/runs/20260713-legacy-detail-add-command/detail-add-mobile.png`

## Risks And Follow-Ups

- A one-button group has no inner-border effect; reuse here is intentionally
  limited to the shared size and outer-radius contract.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.

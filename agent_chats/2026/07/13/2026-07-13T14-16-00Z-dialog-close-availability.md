# Dialog Close Availability Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Keep files small and reuse shared code.

## Scope

- Remove Vue-only header Close actions from legacy footer-command dialogs.
- Preserve header closing where the old templates actually provide it.

## Changed Files

- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/ShellActions.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T14-16-00Z-dialog-close-availability.md`

## Legacy Evidence

- `default.jade` system-message, info, error, and loading dialogs have no
  header `button.close`; available dismiss commands are in the footer.
- Detail operation feedback reuses those old info/error/result command
  patterns and has an explicit footer action.
- `detailView.jade` candidate selection and `view.jade` report setup do include
  a header `button.close`, so those paths must remain available.

## Implementation

- Set `closable=false` on detail info, error, operation-result, and shell
  system-message dialogs.
- Kept their existing footer events and request/state ownership unchanged.
- Left loading/save dialogs and the real candidate/report close paths intact.
- Added tight source-contract checks for all four changed dialogs.

## Validation

- Focused `payload.test.ts`: 82 tests passed.
- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Direct frontend image build and forced Compose recreation passed. Deployed
  image id:
  `sha256:70dc8d1e808262040bd8aa27feee83709ea637571619b7ac40e5da83019b4427`.
- Authenticated `/view102/1001` candidate dialog exposed one Close button and
  clicking it removed the dialog.
- Authenticated `/view101` report setup exposed one Close button while keeping
  its Output/Condition/Save tabs and footer commands.
- Repository harness validation passed; frontend `/` and frontend-proxied
  `/test` returned HTTP 200. MySQL and Redis remained healthy and `db-migrate`
  remained `Exited (0)`.

## Runtime Evidence

- `artifacts/runs/20260713-dialog-close-availability/report-setup.png`

## Risks And Follow-Ups

- The changed feedback dialogs are state-dependent and were guarded by focused
  source contracts; runtime regression covered the two close paths that must
  remain available.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.

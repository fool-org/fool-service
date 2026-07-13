# Default Find Command Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Keep files small and reuse shared code.

## Scope

- Restore the old default-command hierarchy for the shared Find action.
- Preserve list/chart layout and search interaction behavior.

## Changed Files

- `frontend/src/ViewListPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T14-05-00Z-default-find-command.md`

## Legacy Evidence

- Both `view.jade` and `viewWithChart.jade` render Find as Bootstrap
  `.btn-default`; it is not the page's blue primary action.
- The normal-list report command is also `.btn-default`, while candidate Find
  already used the migrated secondary outlined presentation.
- The Vue shared list Find was still an unqualified PrimeVue primary button.

## Implementation

- Applied PrimeVue's existing secondary outlined presentation to the single
  shared list Find button.
- Reused the same presentation already used by report and candidate commands;
  no CSS, component, state, event, or request path was added.
- Added a source-contract assertion for the default command hierarchy.

## Validation

- Focused `payload.test.ts`: 82 tests passed.
- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Direct frontend image build and forced Compose recreation passed. Deployed
  image id:
  `sha256:340c6c6c91aa916adfcd138470c3272647eae49652a0ca50504296cd6e68dab6`.
- Authenticated `/view100` computed one Find button with
  `p-button-secondary p-button-outlined`, transparent background, and secondary
  text/border instead of primary blue.
- Authenticated `/view101` desktop and 390x844 screenshots show Find aligned
  with the report command while preserving right-aligned desktop and wrapped
  mobile toolbar geometry.
- Repository harness validation passed; frontend `/` and frontend-proxied
  `/test` returned HTTP 200. MySQL and Redis remained healthy and `db-migrate`
  remained `Exited (0)`.

## Runtime Evidence

- `artifacts/runs/20260713-default-find-command/desktop.png`
- `artifacts/runs/20260713-default-find-command/mobile.png`

## Risks And Follow-Ups

- This slice restores command hierarchy with the existing PrimeVue secondary
  token rather than duplicating Bootstrap button CSS.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.

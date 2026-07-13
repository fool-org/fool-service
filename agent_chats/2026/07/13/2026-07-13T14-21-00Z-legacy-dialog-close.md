# Legacy Dialog Close Presentation

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Keep files small and reuse shared code.

## Scope

- Restore Bootstrap close presentation on the two old closable dialogs.
- Preserve PrimeVue close state, events, and accessible labels.

## Changed Files

- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/ViewReportPanel.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T14-21-00Z-legacy-dialog-close.md`

## Legacy Evidence

- Candidate selection in `detailView.jade` and report setup in `view.jade`
  render Bootstrap `button.close` with `&times;`.
- Bootstrap 3 uses a 21px bold, line-height-1, borderless transparent close
  control with 0.2 resting opacity and 0.5 hover/focus opacity.
- PrimeVue previously rendered a large outlined circular icon button.

## Implementation

- Added the same `closeicon` `×` slot to candidate and report setup dialogs.
- Added one shared style block scoped to those two dialog classes.
- Kept PrimeVue's outer button, Close accessibility name, and existing close
  events; report result mode remains non-closable as before.
- Added source-contract assertions for both shared slot consumers.

## Validation

- Focused `payload.test.ts`: 82 tests passed after correcting the assertion's
  source-slice boundary; no implementation change was required by that test.
- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Direct frontend image build and forced Compose recreation passed. Deployed
  image id:
  `sha256:63d35c5d989986498b4cabcf887fa1f18eb6d4e4424238549138d0ab00880738`.
- Authenticated `/view102/1001` candidate DOM rendered one accessible Close
  button containing `×`; computed style was borderless/transparent, 21px bold,
  0.2 resting opacity and 0.5 while focused.
- Desktop and 390x844 screenshots show the compact close control without
  covering the title or query controls.
- Repository harness validation passed; frontend `/` and frontend-proxied
  `/test` returned HTTP 200. MySQL and Redis remained healthy and `db-migrate`
  remained `Exited (0)`.

## Runtime Evidence

- `artifacts/runs/20260713-legacy-dialog-close/candidate-desktop.png`
- `artifacts/runs/20260713-legacy-dialog-close/candidate-mobile.png`

## Risks And Follow-Ups

- The close button intentionally remains faint at rest because that is the
  Bootstrap 3 contract; keyboard focus raises it to 0.5 opacity.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.

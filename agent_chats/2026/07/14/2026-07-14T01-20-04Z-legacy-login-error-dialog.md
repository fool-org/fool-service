# Legacy Login Error Dialog

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically while retaining View-first rendering and code reuse.

## Scope

- Trace old failed-login feedback through `login.js`, `showerror.js`, and
  `layout.jade` before changing Vue.
- Restore the error-dialog dismissal and CAPTCHA-refresh order.
- Keep concrete login response DTOs outside the presentation component.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/LoginPanel.vue`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T01-20-04Z-legacy-login-error-dialog.md`

## Legacy Evidence

- `../FoolFrame/src/Web/public/javascripts/app/login.js` passes failed login
  responses to `showerror.showmsg` with CAPTCHA refresh as the hidden callback.
- `../FoolFrame/src/Web/public/javascripts/app/showerror.js` runs that callback
  only after the Bootstrap modal hides.
- `../FoolFrame/src/Web/views/layout.jade` renders a `发生错误` modal with code,
  information, and one footer `关闭` command.
- Vue rendered the failure inline and refreshed the CAPTCHA immediately from
  `submitLegacyLogin`.

## Implementation

- Added one shared Pascal/camel error-code adapter beside the existing message
  adapter; `LoginPanel` receives only code/message display strings.
- Replaced the inline Message with the old modal title, code/info rows, and
  footer-only close action using the existing shared Dialog presentation.
- Removed immediate refresh from submission. Dialog dismissal clears display
  state and then reuses `refreshLoginCheckCode`.
- Kept username, password, database, and CAPTCHA input state inside the login
  View component. The old caller's reversed code/message labels were normalized
  as a presentation correction.
- `LoginPanel.vue` remains 215 lines; no new component or dependency was added.

## Validation

- Focused `npm test -- payload.test.ts viewWorkflow.test.ts`: 129 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- `docker compose build frontend` and forced frontend recreation passed with
  image `sha256:3d02fc2c7c0797ca7f5029e86f429f8843b318bfa1c75d65118a6e60bb6ff9d4`.
- Full frontend tests/build, repository harness, Compose health, backend
  `/test`, and `git diff --check` passed before the atomic commit.

## Skipped Checks And Risks

- The full old FoolFrame application was not booted; checked-in Jade and
  JavaScript supplied the old dialog lifecycle.
- Browser failure/dismissal replay is deferred to the next validation slice
  because the current browser run was already finalized before this gap was
  implemented.

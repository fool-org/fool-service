# Legacy Login HTTP Error

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically while retaining View-first rendering and code reuse.

## Scope

- Trace the old login request's HTTP-error callback before changing Vue.
- Keep transport failure separate from response-backed business login failure.
- Avoid adding a component or a second error-handling abstraction.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T01-28-09Z-legacy-login-http-error.md`

## Legacy Evidence

- `../FoolFrame/src/Web/public/javascripts/app/login.js` sends unsuccessful
  response bodies to `showerror.showmsg` from the success callback.
- The same request's `.error` callback is empty, so HTTP/transport failure does
  not open the error modal or register a CAPTCHA refresh.
- Vue's shared `runAction` exposed a thrown login request through the same
  `errorMessage` state consumed by `LoginPanel`'s business-error dialog.

## Implementation

- When `runAction` returns no login response, `loginV2` clears only the shared
  wrapper's transient display error and returns false.
- Response-backed legacy code/message handling remains unchanged.
- Existing login fields and CAPTCHA state remain component-owned and untouched.
- No new helper, component, request path, or dependency was added.

## Validation

- Focused `npm test -- payload.test.ts`: 82 tests passed.
- Full `npm test`: 179 tests passed across 14 files.
- `npm run build`: Vue type-check and Vite production build passed.
- `docker compose build frontend` and forced frontend recreation passed with
  image `sha256:752f0d84bee8ab6c45b48912c533a809c3333574adb82341f1b93c4661310c55`.
- Compose reported MySQL healthy, Redis/backend/frontend running, and
  `db-migrate` at `Exited (0)`; backend `/test` passed after startup.
- Repository harness and `git diff --check` passed before the atomic commit.

## Skipped Checks And Risks

- Browser transport-failure replay is deferred to a separate validation commit.
- The full old FoolFrame application was not booted; checked-in `login.js`
  supplies the old callback behavior.

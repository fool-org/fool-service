# Legacy Login Reset State

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically while retaining View-first rendering and code reuse.

## Scope

- Trace old `index.jade` Reset through `login.js` before changing Vue state.
- Preserve credential and database inputs while requesting a new CAPTCHA.
- Reuse the existing refresh event and CAPTCHA-key watcher.

## Changed Files

- `frontend/src/LoginPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T01-11-37Z-legacy-login-reset-state.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/index.jade` binds `重置` to `showerror()`.
- `../FoolFrame/src/Web/public/javascripts/app/login.js` refreshes the CAPTCHA
  after that modal hides but does not assign username, password, or database.
- Its shared `refresh()` only requests a new check-code key/image.
- Vue's local `reset()` cleared all four form refs and reset the database before
  emitting the same refresh event.

## Implementation

- Deleted the duplicate Reset function and all field assignments.
- Wired the button directly to the existing refresh event. The current
  CAPTCHA-key watcher remains the single code-input reset path.
- Kept the old blank error-modal intermediary omitted as a presentation cleanup;
  its only durable Reset effect was the same CAPTCHA refresh.
- `LoginPanel.vue` shrank from 207 to 199 lines.

## Validation

- Focused `npm test -- payload.test.ts`: 82 tests passed.
- `docker compose build frontend` and forced frontend recreation passed with
  image `sha256:47584bab5223758aa3d384c119fb79ea6d41034948d15432c8f8fa38e82f3d2c`.
- Docker browser replay filled username, password, and CAPTCHA inputs before
  Reset. At desktop and 390x844, username/password remained unchanged, the
  CAPTCHA input became empty, and the image source changed after the request.
- At 390x844 the viewport and document widths were both 390px; browser console
  errors were empty.
- Full frontend tests/build, repository harness, Compose health, backend
  `/test`, and `git diff --check` passed before the atomic commit.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-login-reset-state/reset-desktop.png`
- `artifacts/runs/20260714-legacy-login-reset-state/reset-mobile.png`

## Skipped Checks And Risks

- The full old FoolFrame application was not booted; checked-in Jade and
  JavaScript supplied the old state contract.

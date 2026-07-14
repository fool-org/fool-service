# Legacy Empty Login Submit

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically while retaining View-first rendering and code reuse.

## Scope

- Compare login input constraints with old `index.jade` before changing Vue.
- Keep validation and error feedback on the existing server response path.
- Avoid a second client-side validation state or message component.

## Changed Files

- `frontend/src/LoginPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T14-58-33Z-legacy-empty-login-submit.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/index.jade` renders username, password, and
  CAPTCHA inputs without `required` attributes.
- `../FoolFrame/src/Web/public/javascripts/app/login.js` reads their current
  values and submits them without a local completeness guard.
- Vue added native required constraints, preventing the old request/error
  interaction whenever any field was empty.

## Implementation

- Removed the three native required constraints from `LoginPanel`.
- Reused the existing submit event, `loginV2` response adapter, error dialog,
  and CAPTCHA-refresh-on-dismissal lifecycle.
- Added no validation helper, component state, dependency, or DTO binding.

## Validation

- Focused `npm test -- payload.test.ts`: 82 tests passed.
- Full `npm test`: 179 tests passed across 14 files.
- `npm run build`: Vue type-check and Vite production build passed.
- `docker compose build frontend` and no-dependency forced frontend recreation
  passed with image
  `sha256:189aefedf24967381da206e677c90c2077d6b09f18a39a209fe8c4de1dcaa1ae`.
- Compose retained backend/frontend/Redis/MySQL running and `db-migrate` at
  `Exited (0)`; backend `/test` passed.
- Repository harness, native-required absence check, and `git diff --check`
  passed before the atomic commit.

## Skipped Checks And Risks

- Browser empty-submit replay is deferred to a separate validation commit.
- The full old FoolFrame application was not booted; checked-in Jade and
  JavaScript supply the old validation boundary.

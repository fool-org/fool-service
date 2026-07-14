# Legacy Raw Login Input

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically while retaining View-first rendering and code reuse.

## Scope

- Trace old login control values into the request before changing Vue.
- Remove Vue-only normalization and length constraints.
- Keep the existing server-owned response and dialog lifecycle.

## Changed Files

- `frontend/src/LoginPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T15-07-13Z-legacy-raw-login-input.md`

## Legacy Evidence

- `../FoolFrame/src/Web/public/javascripts/app/login.js` reads username,
  password, and CAPTCHA directly and places those values in the login request.
- `../FoolFrame/src/Web/views/index.jade` declares no CAPTCHA maxlength.
- Vue trimmed username/CAPTCHA in `submit()` and limited CAPTCHA input to eight
  characters before the shared server validation path.

## Implementation

- `LoginPanel.submit` now emits the three component-owned input strings without
  normalization.
- Removed the CAPTCHA maxlength attribute.
- Added no helper, state, component, dependency, or DTO binding.

## Validation

- Focused `npm test -- payload.test.ts`: 82 tests passed.
- Full `npm test`: 179 tests passed across 14 files.
- `npm run build`: Vue type-check and Vite production build passed.
- `docker compose build frontend` and no-dependency forced frontend recreation
  passed with image
  `sha256:5fbcfe36761833a209c9fde34e5ad4426ddf7252ac181df87c4f4dedbde11fd4`.
- Compose retained backend/frontend/Redis/MySQL running and `db-migrate` at
  `Exited (0)`; backend `/test` passed.
- Repository harness, raw-input absence checks, and `git diff --check` passed
  before the atomic commit.

## Skipped Checks And Risks

- Browser raw-request replay is deferred to a separate validation commit.
- The full old FoolFrame application was not booted; checked-in Jade and
  JavaScript supply the old request contract.

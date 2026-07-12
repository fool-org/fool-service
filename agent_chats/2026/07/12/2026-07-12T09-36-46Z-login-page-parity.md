# Login Page Parity

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with the old
  FoolFrame Web pages, using an atomic commit for each change.

## Scope

- Compare `frontend/src/LoginPanel.vue` with old `views/index.jade` and
  `public/javascripts/app/login.js` before editing.
- Restore the old centered application image/title and narrow, unframed login
  layout instead of the invented gradient card.
- Restore Chinese username, password, database, captcha, refresh, login, and
  reset copy while retaining a usable database selector when multiple stores
  are returned.
- Keep application/footer metadata bound to `initapp` and refresh the captcha
  when resetting the form.

## Changed Files

- `frontend/src/LoginPanel.vue`
- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T09-36-46Z-login-page-parity.md`

## Validation

- `cd frontend && npm test -- --run`
  - 139 tests passed across 8 files.
- `cd frontend && npm run build`
  - Vue TypeScript checking and Vite production build passed.
- `python scripts/check_repo_harness.py` passed.

## Runtime Evidence

- `docker compose build frontend && docker compose up -d --no-deps frontend`
  rebuilt and restarted the Vue frontend successfully.
- `python scripts/runtime_doctor.py` passed all Compose, auth, View, data,
  detail, child, report, message, chart, and Sudoku checks.

## Skipped Or Downgraded Checks

- Reading and submitting the new browser captcha still requires fresh user
  authorization, so authenticated browser acceptance is not part of this
  source-and-runtime slice.

## Risks And Follow-Ups

- Desktop and 390x844 browser acceptance must still prove the login layout,
  captcha refresh/reset behavior, successful authentication, and deep-link
  resume without overflow.

# Vue Login Workflow

## Prompt
- Continue the Docker/FoolFrame/Vue migration toward a usable frontend while
  keeping View metadata ahead of data, maximizing reuse, and making atomic
  commits.

## Scope
- Compared legacy `index.jade`, `login.js`, and auth routes with the current
  Vue startup path.
- Removed the default `admin/admin` automatic login from
  `ensureLegacySession` and removed duplicate login/check-code API Tools
  panels.
- Added a focused `LoginPanel.vue` for application identity, user/password,
  database selection, captcha image/refresh, reset, errors, and submit.
- Reused the existing `initApp`, `loginV2`, `loadInitialRoute`, token storage,
  shell loading, and request DTOs rather than adding a second auth client.
- Returned stale tokens and logout to the same login preparation path with a
  fresh captcha and database metadata.
- Preserved `/view...`, `/itemview...`, and `/new...` paths while signed out,
  then resumed the requested route after successful login.
- Rendered direct detail/new routes as single-panel View-first pages so a
  freshly authenticated deep link does not leave an empty list panel.

## Changed Files
- `frontend/src/App.vue`
- `frontend/src/LoginPanel.vue`
- `frontend/src/payload.test.ts`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/10/2026-07-10T04-55-01Z-vue-login-workflow.md`

## Validation
- `cd frontend && npm test` passed: 7 files, 132 tests.
- `cd frontend && npm run build` passed.
- `docker compose build frontend` passed.
- `docker compose up -d --force-recreate --no-deps frontend` replaced the
  running frontend container.
- `python scripts/runtime_doctor.py` passed.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.

## Runtime Evidence
- A signed-out `/view100/1001` rendered application name/version, blank user
  and password inputs, the `car_wash` database, a real captcha image, refresh,
  reset, and sign-in controls.
- Signing in through the visible form with the Docker admin account resumed
  the requested metadata-driven `OrderList` detail for object `1001` / symbol
  `BTC-USDT` without an empty `Load a View` panel.
- The primary Views navigation returned the standalone detail route to the
  metadata-driven Order list.
- Sign out returned to a fresh login form with `car_wash` and a new captcha.
- At 1280px the form bounds were `445..835px`; at 390px the page width stayed
  390px and form bounds were `14..376px`.
- Browser console warnings/errors: none.
- Screenshots: `artifacts/runs/20260710-login-workflow/login-mobile.png` and
  `artifacts/runs/20260710-login-workflow/deep-link-detail.png`.

## Risks
- The backend check-code DTO still includes the generated plaintext `Code`
  compatibility field; the Vue login page intentionally renders only the
  captcha image and key-backed input.

## Follow-ups
- Continue the remaining schema/migration and low-frequency model/query edge
  work from `docs/migration/foolframe-parity.md`.

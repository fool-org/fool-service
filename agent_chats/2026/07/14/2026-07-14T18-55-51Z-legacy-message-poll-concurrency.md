# Legacy Message Poll Concurrency

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced the old `timer.js` registration loop and `message.js` callback before
  changing Vue.
- Removed the Vue-only in-flight gate so every 15-second tick may start
  `getmsg`, matching the old timer during slow requests.
- Retained the token guard, silent transport handling, session timer cleanup,
  first-message behavior, protocol adapters, routes, and DTO bindings.
- Added no component, helper, request type, or dependency.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T18-55-51Z-legacy-message-poll-concurrency.md`

## Validation

- `cd frontend && npm test -- --run payload.test.ts` (1 file, 83 tests passed)
- `cd frontend && npm test` (19 files, 193 tests passed)
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py`
- `git diff --check`
- `docker compose build frontend`
- `docker compose up -d --no-deps --force-recreate frontend`
- Authorized local CAPTCHA and `admin/admin` browser acceptance:
  backend paused at 03:00:05, with 2 established Nginx-to-backend connections
  at 03:00:26 and 5 at 03:00:52. Resume at 03:01:03 settled four accumulated
  `POST /api/v1/message/getmsg` requests together as HTTP 200; the fifth paused
  connection was an unrelated `initapp` request.
- The recovered `/main` page retained Admin, showed no error dialog, and Find
  returned all 8 View rows through an HTTP 200 `querydata`; safe logout then
  returned to the login form.
- `docker compose ps -a` confirmed backend/frontend/MySQL/Redis running and
  `db-migrate` at `Exited (0)`.
- Frontend container/image matched
  `sha256:a6be14106ff3180e0a96e594a1c55105c2c87df23a7b8cc6e85f18a978afbddc`.
- Database proof remained `SW_SYS_VIEW(100)=default 102 / interval 0`,
  `fool_sys_view(100)=interval 0`, `market_order=8`, and
  `market_order_item=4`.
- `python scripts/runtime_doctor.py` (all 67 checks passed)

## Risks And Follow-ups

- Overlapping message requests can complete out of order, which matches the old
  timer and `$http` behavior rather than adding a new ordering guarantee.
- `docs/superpowers/` is unrelated untracked work and remains untouched.

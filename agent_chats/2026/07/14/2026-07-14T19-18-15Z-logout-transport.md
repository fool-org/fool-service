# Logout Transport Handling

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced old `menuinfo.js` logout success callback before changing Vue.
- Reused `runAction`'s existing `silentTransport` option for logout.
- Retained successful root-route replacement, session cleanup, login metadata
  refresh, shell controls, payloads, routes, and DTOs.
- Added no component, helper, request type, or dependency.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T19-18-15Z-logout-transport.md`

## Validation

- `cd frontend && npm test -- --run payload.test.ts` (1 file, 83 tests passed)
- `cd frontend && npm test` (19 files, 193 tests passed)
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py`
- `git diff --check`
- `docker compose build frontend`
- `docker compose up -d --no-deps --force-recreate frontend`
- Authorized local CAPTCHA and `admin/admin` browser acceptance on `/view100`:
  after backend stop, `安全退出` settled as HTTP 502 while the URL,
  authenticated shell, token, and all 8 View rows remained with no shared
  error dialog.
- After backend recovery, the same command returned HTTP 200, replaced the URL
  with `/`, cleared the shell/token, and rendered a fresh login CAPTCHA.
- `docker compose ps -a` confirmed backend/frontend/MySQL/Redis running and
  `db-migrate` at `Exited (0)`.
- Frontend container/image matched
  `sha256:39b8a37c1696b9122c4832fc4a0538b367d2207625ed2ae603dcb95d47f73781`.
- Database proof remained `SW_SYS_VIEW(100)=default 102 / interval 0`,
  `fool_sys_view(100)=interval 0`, `market_order=8`, and
  `market_order_item=4`.
- `python scripts/runtime_doctor.py` (all 67 checks passed)

## Risks And Follow-ups

- Response-backed logout errors are outside this transport-only slice.
- `docs/superpowers/` is unrelated untracked work and remains untouched.

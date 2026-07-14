# Submenu Transport Handling

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced old `menuinfo.js` and Bootstrap parent expansion before changing Vue.
- Reused `runAction`'s existing `silentTransport` option for `getsubmenu`.
- Retained expand-before-request timing, second-click collapse, response-backed
  children, shared desktop/mobile rendering, metadata, routes, and DTOs.
- Added no component, helper, request type, or dependency.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T19-10-45Z-submenu-transport.md`

## Validation

- `cd frontend && npm test -- --run payload.test.ts` (1 file, 83 tests passed)
- `cd frontend && npm test` (19 files, 193 tests passed)
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py`
- `git diff --check`
- `docker compose build frontend`
- `docker compose up -d --no-deps --force-recreate frontend`
- Initial Nginx start failed with `host not found in upstream "backend"` because
  the backend had become stopped/paused. Backend and frontend were restored and
  both endpoints returned HTTP 200 before browser acceptance began.
- Authorized local CAPTCHA and `admin/admin` browser acceptance: with backend
  stopped, clicking Views immediately set `aria-expanded=true`, kept the 8-row
  View visible, and showed no child or shared error after `getsubmenu` settled
  as HTTP 502.
- After backend recovery, collapse then re-expand returned HTTP 200 and rendered
  `OrderList`. Selecting it navigated to `/view100`, collapsed the menu, and
  retained all rows; safe logout returned to the login form.
- `docker compose ps -a` confirmed backend/frontend/MySQL/Redis running and
  `db-migrate` at `Exited (0)`.
- Frontend container/image matched
  `sha256:267e07af1258c49319a61bbac4930ea6140fe64a284cb981075c11b70700cfc3`.
- Database proof remained `SW_SYS_VIEW(100)=default 102 / interval 0`,
  `fool_sys_view(100)=interval 0`, `market_order=8`, and
  `market_order_item=4`.
- `python scripts/runtime_doctor.py` (all 67 checks passed)

## Risks And Follow-ups

- Response-backed menu errors are outside this transport-only slice.
- `docs/superpowers/` is unrelated untracked work and remains untouched.

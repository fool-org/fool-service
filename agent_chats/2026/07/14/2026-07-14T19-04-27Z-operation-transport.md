# Operation Transport Handling

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced the old `operation.js` request and `showdetailinfo` success callback
  before changing Vue.
- Reused `runAction`'s existing `silentTransport` option for the one
  View-derived operation request.
- Retained response-backed results, edit-state guards, operation metadata,
  payloads, routes, DTO bindings, and result presentation.
- Added no component, helper, request type, or dependency.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T19-04-27Z-operation-transport.md`

## Validation

- `cd frontend && npm test -- --run payload.test.ts` (1 file, 83 tests passed)
- `cd frontend && npm test` (19 files, 193 tests passed)
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py`
- `git diff --check`
- `docker compose build frontend`
- `docker compose up -d --no-deps --force-recreate frontend`
- Authorized local CAPTCHA and `admin/admin` browser acceptance on
  `/view100/1002`: after backend stop, the enabled View operation's
  `runoperation` settled as HTTP 502 while the detail heading and Admin user
  remained visible with no shared error or operation-result dialog.
- After backend recovery, the same operation returned HTTP 200 and opened
  `执行结果 / 操作成功 / 保存成功`; `确定` dismissed it and safe logout returned to
  the login form.
- `docker compose ps -a` confirmed backend/frontend/MySQL/Redis running and
  `db-migrate` at `Exited (0)`.
- Frontend container/image matched
  `sha256:233148af80bf9ed10f33e2d83c6a9edff97444aa7973ad0155a0a6dcdaf2a741`.
- Database proof remained `SW_SYS_VIEW(100)=default 102 / interval 0`,
  `fool_sys_view(100)=interval 0`, `market_order=8`, and
  `market_order_item=4`; order 1002 retained its seeded ETH-USDT values.
- `python scripts/runtime_doctor.py` (all 67 checks passed)

## Risks And Follow-ups

- Response-backed business errors remain visible through the legacy result
  path; this slice suppresses only transport failures.
- `docs/superpowers/` is unrelated untracked work and remains untouched.

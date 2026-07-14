# Initapp Transport Handling

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced the old signed-out `index` route through `soway.initapp` and its shared
  server-side `postandget` error handler before accepting the Vue change.
- Reused `runAction`'s existing `silentTransport` option for only `initapp`.
- Retained the static fallback login shell, successful app/CAPTCHA/database
  adapters, login behavior, request payload, route, and DTO.
- Added no component, helper, request type, state owner, or dependency.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T19-37-45Z-initapp-transport.md`

## Validation

- `cd frontend && npm test -- --run payload.test.ts` (1 file, 83 tests passed)
- `cd frontend && npm test` (19 files, 193 tests passed)
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py`
- `git diff --check`
- `BUILDX_CONFIG=/private/tmp/fool-service-buildx docker compose build frontend`
- `docker compose up -d --no-deps --force-recreate frontend`
- `python scripts/runtime_doctor.py` (all 67 checks passed)
- Deployed frontend image
  `sha256:d1144dc7db08cf0b9c8cbeb636911ee1ff2e5169c06e35fdeab5ea2d932dceb1`
  served bundle `index-BmTy2PHd.js`.
- An authorized isolated browser context returned deterministic HTTP 502 for
  `initapp`; all three visible login inputs remained and no `发生错误` or
  transport text appeared.
- Removing the interception and reloading returned HTTP 200 from `initapp`.
  Reading the local CAPTCHA and submitting `admin/admin` returned HTTP 200 from
  `loginv2`, navigated to `/main`, rendered Admin and Order List, and completed
  a successful HTTP-200 logout.
- A final shared-stack replay deployed frontend image
  `sha256:86f7f14ea319c50361d3e99c87979596e21e09c80a85d119a0928c3e583dda26`;
  the running container matched it. Stopping the backend produced real Nginx
  HTTP 502/504 `initapp` responses. A fresh failed page retained the three-input
  shell without CAPTCHA or an error dialog and disabled Login; backend recovery
  and reload returned HTTP 200 and restored CAPTCHA, Login, and version metadata.
- `docker compose ps -a` confirmed backend/frontend/MySQL/Redis running and
  `db-migrate` at `Exited (0)`.
- Database proof remained `SW_SYS_VIEW(100)=default 102 / interval 0`,
  `fool_sys_view(100)=interval 0`, `market_order=8`, and
  `market_order_item=4`.

## Risks And Follow-ups

- The initial deterministic HTTP 502 avoided competing for the shared Compose
  stack; the final replay also stopped the real backend and confirmed the same
  failure/recovery states end to end.
- Vue intentionally keeps a static retryable login shell instead of reproducing
  FoolFrame's server-render navigation that never receives an error callback.
- `docs/superpowers/` is unrelated untracked work and remains untouched.

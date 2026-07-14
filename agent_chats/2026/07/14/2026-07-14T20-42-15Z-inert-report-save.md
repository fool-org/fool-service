# Inert Report Save Command

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced the `保存报表定义` footer command from `view.jade` into
  `mkreport.js` before changing Vue.
- Confirmed the old template calls `$scope.saverpt()` but the old controller
  never defines that function, leaving the otherwise-present server route
  unreachable from the browser command.
- Kept the report-name field and visible enabled footer button while removing
  the Vue-only save handler, request, report-name payload injection, and status
  messages.
- Preserved report metadata/generation, conditions, output selection, backend
  compatibility route, DTOs, and all other dialog commands.

## Changed Files

- `frontend/src/ViewReportPanel.vue`
- `frontend/src/ViewReportPanel.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T20-42-15Z-inert-report-save.md`

## Validation

- `cd frontend && npm test -- --run src/ViewReportPanel.test.ts src/payload.test.ts`
  (2 files, 88 tests passed)
- `cd frontend && npm test` (19 files, 196 tests passed)
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py`
- `git diff --check`
- The report component shrank from 401 to 391 lines.
- Built implementation commit `7069f765` from a clean archive and deployed it
  as tagged frontend image
  `sha256:a0ab8a20af2e84b7013de6c50e57346a9b24e0bbcf54f0296914e351f14e4358`.
  The running HTML referenced entry bundle `index-hTPS1fYg.js`.
- Authorized isolated-browser acceptance read the local CAPTCHA, logged in with
  `admin/admin`, opened `/view101`, and loaded `getmkqview` with HTTP 200. The
  `保存报表定义` command was visible and enabled. After entering
  `Inert parity check` and clicking it, the report dialog, name, and route were
  unchanged, zero `saverpt` requests were emitted, and no shared, success, or
  failure feedback appeared. Cancel closed the dialog and logout returned HTTP
  200.
- MySQL held eight orders and four order items before and after runtime
  verification. Order 1001 remained `BTC-USDT`, state `0`, customer `3001`,
  amount `0.2500000000`, and price `62500.0000000000`.
- `python scripts/runtime_doctor.py` passed all 67 checks, including the direct
  legacy no-op `saverpt` API surface, with Compose healthy and `db-migrate` at
  `Exited (0)`.

## Risks And Follow-ups

- The direct compatibility API remains intentionally available and covered by
  the runtime doctor; only the old browser command's missing controller action
  is reproduced.
- Browser acceptance used the normal list View 101 because chart View 100 does
  not expose the old report command.
- Concurrent Agent Session work in Maven files, `fool-agent/`, README,
  `docs/agent-sessions.md`, and its own delivery evidence remains unrelated and
  untouched.

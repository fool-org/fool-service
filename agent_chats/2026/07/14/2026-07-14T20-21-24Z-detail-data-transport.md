# Detail Data Transport Handling

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced FoolFrame's `getItem` and `initnew` server routes through the shared
  `postandget` request helper before changing Vue.
- Suppressed shared transport feedback only for `querydatadetail` and
  `initnew` after read-item View metadata succeeds.
- Preserved View-before-data ordering, response-backed business errors,
  successful rendering, payloads, routes, components, and DTOs.
- Reused `runAction`'s existing transport option; added no helper, state,
  abstraction, request type, or dependency.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T20-21-24Z-detail-data-transport.md`

## Validation

- `cd frontend && npm test -- --run src/payload.test.ts` (1 file, 84 tests
  passed)
- `cd frontend && npm test` (19 files, 195 tests passed)
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py`
- `git diff --check`
- Built implementation commit `545ed181` from a clean archive and deployed it
  as tagged frontend image
  `sha256:800448bbf2cd98fed8f701435461415e6a7b9d03dca8784b0e3820ba8e6584b0`.
  The running HTML referenced entry bundle `index-D4PLU5xf.js`.
- Authorized isolated-browser acceptance read the local CAPTCHA and logged in
  with `admin/admin`. On `/view102/1001`, `getreaditemview` returned HTTP 200
  before deterministic `querydatadetail` HTTP 502; on `/new102`, the same View
  request returned 200 before deterministic `initnew` HTTP 502. Both retained
  one View-derived panel with zero `发生错误` dialogs and zero transport text.
- A separate `querydatadetail` HTTP-200 response with code `42001` retained the
  shared business-error dialog and message. Removing both failure routes made
  View and data return HTTP 200 in order on detail and new paths, restored one
  panel per route, and logout returned HTTP 200.
- MySQL held eight orders and four order items before and after acceptance and
  runtime verification. Order 1001 remained `BTC-USDT`, state `0`, customer
  `3001`, amount `0.2500000000`, and price `62500.0000000000`.
- `python scripts/runtime_doctor.py` passed all 67 checks with Compose healthy
  and `db-migrate` at `Exited (0)`.

## Risks And Follow-ups

- Failure injection was limited to the deployed data-fetch paths, avoiding
  shared-backend interruption while proving View-before-data ordering. Live
  login, View/data success, panels, business errors, and logout were exercised
  through Nginx.
- The migrated shell renders its already-loaded View schema after a data
  transport failure instead of reproducing FoolFrame's indefinitely pending
  HTTP navigation; shared error behavior and data ordering match the old path.
- Concurrent Agent Session work in Maven files, `fool-agent/`, README,
  `docs/agent-sessions.md`, and its own delivery evidence remains unrelated and
  untouched.

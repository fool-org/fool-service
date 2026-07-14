# Read-item View Transport Handling

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced FoolFrame's `/itemview:id`, `/view:id/:objid`, and `/new:id` server
  routes through their `postandget` callbacks before changing Vue.
- Suppressed shared transport feedback only for `getreaditemview` metadata
  loading.
- Preserved the migration's View-before-`querydatadetail` / `initnew` gate,
  successful metadata cache, rendered fields, payloads, routes, and DTOs.
- Reused `runAction`'s existing transport option; added no state, helper,
  abstraction, component, request type, or dependency.

## Changed Files

- `frontend/src/useViewDataWorkflow.ts`
- `frontend/src/useViewDataWorkflow.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T20-13-52Z-read-view-transport.md`

## Validation

- `cd frontend && npm test -- --run src/useViewDataWorkflow.test.ts` (1 file,
  9 tests passed)
- `cd frontend && npm test` (19 files, 194 tests passed)
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py`
- `git diff --check`
- Built implementation commit `72e2e446` from a clean archive and deployed it
  as tagged frontend image
  `sha256:2a25c4acbcb4bec67b765f4a30e29b86b13b38dcc213ef239dfca5c60716208c`.
  The running HTML referenced entry bundle `index-DbeIIhCC.js`.
- Authorized isolated-browser acceptance read the local CAPTCHA and logged in
  with `admin/admin`. Deterministic `getreaditemview` HTTP 502 responses on
  `/itemview102`, `/view102/1001`, and `/new102` retained each route and the
  authenticated shell with zero `发生错误` dialogs and zero transport text.
  Detail and new paths issued zero downstream `querydatadetail` / `initnew`
  requests while View metadata was unavailable.
- Removing the interception returned HTTP 200 for all three View requests.
  Detail and new then returned HTTP 200 from `querydatadetail` and `initnew`,
  all three routes rendered one View-derived detail panel, and logout returned
  HTTP 200.
- MySQL held eight orders and four order items before and after acceptance and
  runtime verification. Order 1001 remained `BTC-USDT`, state `0`, customer
  `3001`, amount `0.2500000000`, and price `62500.0000000000`.
- `python scripts/runtime_doctor.py` passed all 67 checks with Compose healthy
  and `db-migrate` at `Exited (0)`.

## Risks And Follow-ups

- Failure injection was limited to the deployed `getreaditemview` fetch path,
  avoiding shared-backend interruption while proving the metadata gate. Live
  login, metadata/data success, panels, and logout were exercised through
  Nginx.
- `querydatadetail` and `initnew` transport behavior after successful View
  metadata remains a separate audit slice.
- Concurrent Agent Session work in Maven files, `fool-agent/`, README,
  `docs/agent-sessions.md`, and its own delivery evidence remains unrelated and
  untouched.

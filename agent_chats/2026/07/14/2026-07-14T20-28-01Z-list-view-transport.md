# List View Transport Handling

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced FoolFrame's authenticated `/`, `/main`, and `/view:id` routes through
  their `getlistview` `postandget` callback before changing Vue.
- Suppressed shared transport feedback only for server-rendered list View
  metadata loading.
- Preserved response-backed business errors, View-before-data ordering,
  successful list rendering, payloads, routes, components, and DTOs.
- Reused `runAction`'s existing transport option; added no state, helper,
  abstraction, component, request type, or dependency.

## Changed Files

- `frontend/src/useViewDataWorkflow.ts`
- `frontend/src/useViewDataWorkflow.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T20-28-01Z-list-view-transport.md`

## Validation

- `cd frontend && npm test -- --run src/useViewDataWorkflow.test.ts` (1 file,
  9 tests passed)
- `cd frontend && npm test` (19 files, 195 tests passed)
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py`
- `git diff --check`
- Built implementation commit `37ef7662` from a clean archive and deployed it
  as tagged frontend image
  `sha256:5994dd9cd2c76931cb767ec15bec1ae95a164036b769eaa28275740ad0f58efa`.
  The running HTML referenced entry bundle `index-CN0-wUor.js`.
- Authorized isolated-browser acceptance read the local CAPTCHA and logged in
  with `admin/admin`. Deterministic `getlistview` HTTP 502 responses on `/`,
  `/main`, and `/view100` retained each route and the authenticated shell with
  zero `发生错误` dialogs, zero transport text, and zero downstream `querydata`
  requests.
- A separate `getlistview` HTTP-200 response with code `42002` retained the
  shared business-error dialog and message without querying data. Removing the
  interception returned `getlistview` HTTP 200 before `querydata` HTTP 200,
  rendered one list panel with BTC-USDT and ETH-USDT rows, and logout returned
  HTTP 200.
- MySQL held eight orders and four order items after acceptance and runtime
  verification. Order 1001 remained `BTC-USDT`, state `0`, customer `3001`,
  amount `0.2500000000`, and price `62500.0000000000`.
- `python scripts/runtime_doctor.py` passed all 67 checks with Compose healthy
  and `db-migrate` at `Exited (0)`.

## Risks And Follow-ups

- Failure injection was limited to the deployed `getlistview` fetch path,
  avoiding shared-backend interruption while proving the metadata gate. Live
  login, View/data success, rows, business errors, and logout were exercised
  through Nginx.
- The migrated shell remains mounted while metadata is unavailable instead of
  reproducing FoolFrame's indefinitely pending HTTP navigation; shared error
  behavior and data ordering match the old path.
- Concurrent Agent Session work in Maven files, `fool-agent/`, README,
  `docs/agent-sessions.md`, and its own delivery evidence remains unrelated and
  untouched.

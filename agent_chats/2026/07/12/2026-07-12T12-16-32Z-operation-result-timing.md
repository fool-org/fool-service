# Operation Result Timing Parity

## Prompt

Keep legacy interaction logic aligned while allowing visual modernization, and
commit every behavior change atomically.

## Scope

- Trace the old operation response callback after `/data/exoperation`.
- Remove Vue-only list/detail refresh requests after success.
- Present the operation result immediately from the original response.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T12-16-32Z-operation-result-timing.md`

## Validation

- `cd frontend && npm test -- --run`: 9 files, 148 tests passed.
- `cd frontend && npm run build`: TypeScript and Vite production build passed;
  the main application chunk decreased from 257.45 kB to 257.42 kB.
- The operation-handler regression asserts that neither list nor detail query
  occurs in the response-to-result path.
- `docker compose build frontend`: passed.
- `docker compose up -d --no-deps frontend`: rebuilt frontend deployed on port
  8081.
- `python scripts/runtime_doctor.py`: all Compose, auth, View/data, detail,
  operation, report, message, and logout checks passed.
- `python scripts/check_repo_harness.py`: passed.
- `docker compose ps -a`: backend/frontend are up, MySQL/Redis are healthy, and
  `db-migrate` is `Exited (0)`.

## Source Evidence

- `operation.js` calls `showdetailinfo(...)` directly in the operation success
  callback.
- It does not query the current list or detail before showing the result.

## Risks And Follow-Ups

- Data remains refreshable through the existing explicit query, paging, and
  AutoFreshTime flows.
- Authenticated browser acceptance needs a fresh CAPTCHA confirmation.

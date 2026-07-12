# Detail Editing Operation Guard

## Prompt

Keep legacy interaction logic aligned while allowing visual modernization, and
commit every behavior change atomically.

## Scope

- Trace detail business-operation behavior while the record is being edited.
- Keep the operation command available during editing.
- Block the request and show the old feedback text before any API call.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T12-11-22Z-editing-operation-guard.md`

## Validation

- `cd frontend && npm test -- --run`: 9 files, 148 tests passed.
- `cd frontend && npm run build`: TypeScript and Vite production build passed;
  `ViewDetailPanel` remains 70.13 kB.
- Source regression asserts that editing state is emitted with the command,
  pending alone disables the operation button, and the old message is written
  before the request path.
- `docker compose build frontend`: passed.
- `docker compose up -d --no-deps frontend`: rebuilt frontend deployed on port
  8081.
- `python scripts/runtime_doctor.py`: all Compose, auth, View/data, detail,
  operation, report, message, and logout checks passed.
- `python scripts/check_repo_harness.py`: passed.
- `docker compose ps -a`: backend/frontend are up, MySQL/Redis are healthy, and
  `db-migrate` is `Exited (0)`.

## Source Evidence

- `detailview.js` keeps operation buttons active while editing.
- `exuteop(opid)` checks `isedit` first, shows `请先保存当前信息`, and only
  calls `operation.runoperation(...)` when editing is false.

## Risks And Follow-Ups

- Visual presentation remains modern PrimeVue; only interaction semantics and
  feedback are restored.
- Authenticated browser acceptance needs a fresh CAPTCHA confirmation.

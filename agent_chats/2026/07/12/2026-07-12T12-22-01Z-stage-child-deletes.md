# Stage Child Deletes Until Parent Save

## Prompt

Keep legacy interaction logic aligned while allowing visual modernization, and
commit every behavior change atomically.

## Scope

- Trace old child deletion into `obj.Itemproperties` and parent Save.
- Stage child deletes locally and hide deleted rows immediately.
- Merge multiple deletes for the same group into one Save item property.
- Remove the Vue-only confirmation and immediate persistence/query cycle.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T12-22-01Z-stage-child-deletes.md`

## Validation

- `cd frontend && npm test -- --run`: 9 files, 149 tests passed, including
  same-group delete merging, empty Pascal/camel group overrides, and
  source-level no-request staging checks.
- `cd frontend && npm run build`: TypeScript and Vite production build passed.
- `docker compose build frontend`: passed.
- `docker compose up -d --no-deps frontend`: rebuilt frontend deployed on port
  8081.
- `python scripts/runtime_doctor.py`: all checks passed, including
  `saveobj-items-delete` persistence and the surrounding detail workflow.
- `python scripts/check_repo_harness.py`: passed.
- `docker compose ps -a`: backend/frontend are up, MySQL/Redis are healthy, and
  `db-migrate` is `Exited (0)`.

## Source Evidence

- `detailview.js deleteitem(...)` removes the row locally and appends the item
  to `obj.Itemproperties[*].DelteItems`.
- Only `beginsave()` sends that parent object to `/data/save`.
- The old delete interaction has no confirmation dialog and does not query the
  detail again after marking a row.

## Risks And Follow-Ups

- Existing child updates and additions still persist immediately; they remain
  separate atomic parity slices.
- The old `undo` function is empty, so no undo interaction is added.
- Authenticated browser acceptance needs a fresh CAPTCHA confirmation.

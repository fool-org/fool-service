# Stage Child Updates Until Parent Save

## Prompt

Keep old interaction logic aligned while allowing visual modernization, reuse
the existing pending item-property flow, and commit each behavior atomically.

## Scope

- Restore the old per-row Edit/Save toggle while the parent is editing.
- Keep only one child row in edit mode and stage the previous row when switching.
- Upsert repeated edits for the same child into `Items`.
- Remove the Vue-only immediate child-update save/query cycle.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T12-40-00Z-stage-child-updates.md`

## Validation

- `cd frontend && npm test -- --run`: 9 files, 149 tests passed, including
  same-child update replacement, update/delete conflict handling, per-row
  Edit/Save state, and source-level no-request staging checks.
- `cd frontend && npm run build`: TypeScript and Vite production build passed.
- `docker compose build frontend`: passed.
- `docker compose up -d --no-deps frontend`: current frontend deployed on port
  8081.
- `python scripts/runtime_doctor.py`: all checks passed, including
  `saveobj-items-delete` and surrounding View-first detail routes.
- `python scripts/check_repo_harness.py`: passed.
- `docker compose ps -a`: backend/frontend are up, MySQL/Redis are healthy, and
  `db-migrate` is `Exited (0)`.

## Source Evidence

- `detailview.js edititem(...)` requires parent edit mode and toggles one row
  between `编辑` and `保存`.
- Switching rows recursively saves the previous row before editing the next.
- Existing rows are upserted by `ItemId` into `obj.Itemproperties[*].Items`.
- Only `beginsave()` posts the parent object to `/data/save`.

## Risks And Follow-Ups

- Child additions still persist immediately and remain a separate atomic slice.
- Authenticated browser acceptance needs a fresh CAPTCHA confirmation.

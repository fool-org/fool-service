# Restore Save View Name

## Prompt

Keep the improved Vue presentation, align old interaction logic, load View
metadata before data, avoid business DTO coupling, and commit atomically.

## Scope

- Read the save View key from generic detail `Data.Name` / `data.name`.
- Share that key across existing-object and new-object save payloads.
- Keep the numeric View id only as a missing-name fallback.
- Exercise name-shaped `obj.ViewID` through both old Web save route aliases.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/payload.test.ts`
- `scripts/runtime_doctor.py`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T13-48-03Z-restore-save-view-name.md`

## Validation

- `cd frontend && npm test -- --run`: 10 files and 152 tests passed.
- `cd frontend && npm run build`: passed.
- `python -m py_compile scripts/runtime_doctor.py`: passed.
- `python scripts/check_repo_harness.py`: passed.
- `docker compose build frontend`: passed.
- `docker compose up -d --no-deps frontend`: rebuilt frontend deployed on
  port 8081.
- `python scripts/runtime_doctor.py`: all checks passed; the old `/data/new`
  and `/data/save` aliases created and updated runtime rows with the initialized
  detail View name in `obj.ViewID`.
- `docker compose ps -a`: backend/frontend are up, MySQL/Redis are healthy,
  and `db-migrate` is `Exited (0)`.

## Source Evidence

- FoolFrame `detailView.jade` initializes `obj.ViewID` from `view.Data.Name`.
- FoolFrame `DataFormator.IObjectProxyToDetail` sets `Data.Name` from the View
  name before existing or new saves.
- The migrated DAO resolves View details by generated numeric id or the
  annotated `viewName` key, so the old name-shaped value is supported.

## Risks

- Responses without a detail View name retain the current numeric fallback.

# Restore Added Item Identity

## Prompt

Keep old interaction logic aligned, bind through View metadata, avoid business
DTO coupling, and commit each behavior atomically.

## Scope

- Mark manually created child rows as new (`IsExist=false`).
- Keep select-existing child rows marked existing (`IsExist=true`).
- Preserve readonly candidate values only for the select-existing path.

## Changed Files

- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T13-31-00Z-restore-added-item-identity.md`

## Validation

- `cd frontend && npm test -- --run`: 10 files, 152 tests passed, including
  manual false, selected-existing true, and update/delete true identity cases.
- `cd frontend && npm run build`: TypeScript and Vite production build passed.
- `docker compose build frontend`: passed.
- `docker compose up -d --no-deps frontend`: current frontend deployed on port
  8081.
- `python scripts/runtime_doctor.py`: all checks passed; its
  `saveobj-addeditems` case creates and reads back a child with `IsExist=false`,
  and the update/delete case remains green.
- `python scripts/check_repo_harness.py`: passed.
- `docker compose ps -a`: backend/frontend are up, MySQL/Redis are healthy, and
  `db-migrate` is `Exited (0)`.

## Source Evidence

- Old `detailview.js` creates manual child rows with a local sequence key and
  leaves `IsExist` false; selected candidate rows set `IsExist=true`.
- Old `DataFormator.ObjUpdateToProxy` assigns `ItemId` only when `IsExist` is
  true, otherwise the model creates the child identity.
- Current `DataQueryService.itemData(..., keepId)` preserves the same contract,
  and `runtime_doctor.py` already proves manual `AddedItems` with false.

## Risks And Follow-Ups

- The UI temporary key remains a timestamp only for stable local row identity;
  it is no longer persisted as a manual child business id.

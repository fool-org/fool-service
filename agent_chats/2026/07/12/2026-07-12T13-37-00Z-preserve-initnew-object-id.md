# Preserve Initnew Object ID

## Prompt

Keep old interaction logic aligned, load View metadata before data, avoid
business DTO coupling, and commit each behavior atomically.

## Scope

- Read new-object identity from the generic `initnew` response.
- Support Pascal and camel aliases.
- Retain the local fallback only for empty server identity.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T13-37-00Z-preserve-initnew-object-id.md`

## Validation

- `cd frontend && npm test -- --run`: 10 files and 152 tests passed.
- `cd frontend && npm run build`: passed.
- `python scripts/check_repo_harness.py`: passed.
- `docker compose build frontend`: passed.
- `docker compose up -d --no-deps frontend`: rebuilt frontend deployed on
  port 8081.
- `python scripts/runtime_doctor.py`: all checks passed, including legacy new
  routes, auth, `initnew`, `savenewobj`, and child save flows.
- `docker compose ps -a`: backend/frontend are up, MySQL/Redis are healthy,
  and `db-migrate` is `Exited (0)`.

## Source Evidence

- FoolFrame `detailView.jade` initializes `obj.Id` from `view.Data.ObjId`.
- The generic API already models `ObjId`; Vue previously discarded it.
- The current Docker `initnew` response leaves `ObjId` empty, so the fallback
  remains active for that runtime.

## Risks

- This slice does not invent a universal backend object-id generation policy.

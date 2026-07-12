# Restore Detail Lookup Owner

## Prompt

Keep improved presentation, align old interaction logic, render from View
metadata before data, avoid business DTO coupling, and commit atomically.

## Scope

- Read standalone detail lookup ownership from generic `Data.ParentId`.
- Fall back to the parent id parsed from the old new-child route.
- Leave inline child lookup ownership on the current main record id.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T13-52-56Z-restore-detail-lookup-owner.md`

## Validation

- `cd frontend && npm test -- --run`: 10 files and 152 tests passed.
- `cd frontend && npm run build`: passed.
- `python scripts/check_repo_harness.py`: passed.
- Dockerized JDK 17 Maven reactor test for
  `DataQueryServiceInputQueryTest`: 12 tests passed.
- `docker compose build frontend`: passed.
- `docker compose up -d --no-deps frontend`: rebuilt frontend deployed on
  port 8081.
- `python scripts/runtime_doctor.py`: all checks passed across legacy routes,
  auth, View/data, detail/new, save, child, lookup, report, and message flows.
- `docker compose ps -a`: backend/frontend are up, MySQL/Redis are healthy,
  and `db-migrate` is `Exited (0)`.

The host Maven command could not target Java 17 because the host default is
Java 8, so the focused backend test ran in the repository's JDK 17 Maven image.

## Source Evidence

- FoolFrame `setextype.js` sends both current `objid` and route `ownerid` to
  `inputquery` and marks new detail objects with `newadd=true`.
- FoolFrame `HandlerInputQuery` uses that owner id for an added child model's
  owner-scoped source list.
- The migrated backend keeps the same `IsAdded` plus `OwnerId` branch for `#.`
  source expressions; Vue previously omitted the top-level owner id.

## Risks

- Top-level details without an owner continue to send no optional owner id.

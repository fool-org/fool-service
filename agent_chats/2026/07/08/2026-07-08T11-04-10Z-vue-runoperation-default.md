# Vue runoperation default cleanup

## Prompt

Continue the Docker/Vue FoolFrame migration and avoid binding frontend tools to
Docker seed business data.

## Scope

- Removed the Docker-seeded `7001` operation id default from the Vue manual
  `runoperation` tool.
- Disabled the manual run button until both an operation id and object id are
  present.
- Added a source test guard so the seed operation default does not return.
- Updated `tasks.md` and `docs/migration/foolframe-parity.md`.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`

## Validation

- `cd frontend && npm test`: passed, 69 tests.
- `cd frontend && npm run build`: passed.
- `python scripts/check_repo_harness.py`: passed.
- `git diff --check`: clean.
- `docker compose up -d --build frontend`: frontend image built successfully;
  compose also rebuilt and recreated backend through the dependency graph.
- `docker compose up -d --force-recreate --no-deps frontend`: frontend
  container recreated from the rebuilt image.
- `python scripts/runtime_doctor.py`: passed all compose, auth, View/data,
  detail, `inputquery`, report, message, notify, and logout checks.

## Runtime Evidence

- Backend container was recreated and running on `http://localhost:8080`.
- Frontend container was recreated and running on `http://localhost:8081`.

## Risks

- Manual API-tool users now need to provide an operation id explicitly. The
  main View-rendered operation buttons still populate the operation id from
  loaded View metadata.

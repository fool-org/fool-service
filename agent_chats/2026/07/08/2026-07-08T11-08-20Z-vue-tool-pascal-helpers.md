# Vue tool Pascal protocol helpers

## Prompt

Continue the FoolFrame migration, keep Vue protocol handling aligned with the
legacy surface, and avoid binding panels to concrete business DTO fields.

## Scope

- Added shared Vue helpers for Pascal/camel legacy lists and fields used by
  `getsubmenu`, `getmsg`, `getnotify`, and `getenums`.
- Updated API tool panels and enum option loading to use those helpers.
- Added TypeScript aliases for `LegacySubMenuResult.Items` and
  `GetEnumResult.EnumValues` with nested `Name` / `Value` fields.
- Added focused helper coverage for Pascal payloads.
- Updated `tasks.md` and `docs/migration/foolframe-parity.md`.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/api.ts`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`

## Validation

- `cd frontend && npm test`: passed, 70 tests.
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

- The panels intentionally keep camel-case fallbacks because the migrated
  backend currently emits both surfaces.

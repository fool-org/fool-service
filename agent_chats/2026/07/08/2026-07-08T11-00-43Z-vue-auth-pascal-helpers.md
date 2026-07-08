# Vue auth Pascal protocol helpers

## Prompt

Continue the FoolFrame migration with Vue, keep page/data flow View-first, and
avoid binding frontend behavior to concrete business DTOs.

## Scope

- Added shared Vue workflow helpers for legacy auth first-hop protocol fields:
  `initapp.CheckCode`, `initapp.Dbs`, `getcheckcode.Key`, `getcheckcode.Code`,
  and `getcheckcode.ChkCodeImg`, with camel-case compatibility fallback.
- Updated `App.vue` auth controls to use those helpers instead of reading only
  camel-case fields inline.
- Added focused helper tests for Pascal and camel payloads.
- Updated `tasks.md` and `docs/migration/foolframe-parity.md`.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`

## Validation

- `cd frontend && npm test`: passed, 69 tests.
- `cd frontend && npm run build`: passed.
- `docker compose up -d --build frontend`: frontend image built successfully;
  compose also rebuilt backend through the dependency graph.
- `docker compose up -d --force-recreate --no-deps frontend`: frontend
  container recreated from the rebuilt image.
- `python scripts/runtime_doctor.py`: passed all compose, auth, View/data,
  detail, `inputquery`, report, message, notify, and logout checks.
- `python scripts/check_repo_harness.py`: passed.
- `git diff --check`: clean.

## Runtime Evidence

- Frontend container: `fool-service-frontend-1` recreated and running on
  `http://localhost:8081`.
- Runtime doctor proved the Vue proxy path still handles auth first-hop,
  View-first data loading, detail loading, lookup, report, message, notify,
  and logout checks.

## Risks

- The auth controls still support camel-case compatibility fields because the
  migrated backend emits both forms today. Removing those fallbacks should be a
  later explicit compatibility decision.

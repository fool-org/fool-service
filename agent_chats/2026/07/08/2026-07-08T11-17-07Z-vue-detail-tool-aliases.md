# Vue detail tool aliases

## Prompt

Continue the FoolFrame migration with the constraint that pages render from
View metadata first, then bind data values, without tying the frontend to
concrete business DTO fields.

## Scope

- Added shared Vue helpers for `querydatadetail` / `initnew` result data:
  `Data.SimpleData`, `Data.Items`, and their camel-case compatibility fields.
- Updated the API-tool detail and init-new tables to use computed helper
  results instead of drilling into `response.data.data.simpleData`.
- Rendered field labels and values through generic field helpers so Pascal
  `PrpShowName` / `FmtValue` rows display correctly.
- Added focused `viewWorkflow` coverage for legacy and camel detail payloads.
- Updated `tasks.md` and `docs/migration/foolframe-parity.md`.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`

## Validation

- `cd frontend && npm test`: passed, 71 tests.
- `cd frontend && npm run build`: passed.
- `python scripts/check_repo_harness.py`: passed.
- `git diff --check`: clean.
- `docker compose up -d --build frontend`: frontend and backend images built
  successfully; compose recreated backend through the dependency graph.
- `docker compose up -d --force-recreate --no-deps frontend`: frontend
  container recreated from the rebuilt image.
- `python scripts/runtime_doctor.py`: passed all compose, auth, View/data,
  detail, `inputquery`, report, message, notify, and logout checks.

## Runtime Evidence

- Backend container running on `http://localhost:8080`.
- Frontend container recreated and running on `http://localhost:8081`.

## Risks

- The helpers intentionally keep camel-case fallbacks because the migrated
  backend currently emits both protocol surfaces.

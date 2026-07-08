# Vue View summary helpers

## Prompt

Continue the FoolFrame migration with the frontend rendering from View metadata
first, then binding data, without coupling page code to concrete business DTO
fields.

## Scope

- Added shared `viewWorkflow` helpers for loaded View name, title, type, and
  input-count metadata.
- Updated the Vue page shell and View API-tool summary to use those helpers
  instead of direct `App.vue` reads of View metadata fields.
- Kept backend protocol unchanged; this slice only narrows frontend field
  access to the existing View helper boundary.
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
- `docker compose build frontend`: passed.
- `docker compose up -d --force-recreate --no-deps frontend`: frontend
  container recreated from the rebuilt image.
- `python scripts/runtime_doctor.py`: passed all compose, auth, View/data,
  detail, `inputquery`, report, message, notify, and logout checks.
- `docker compose ps`: backend, frontend, MySQL, and Redis running; MySQL and
  Redis healthy.

## Runtime Evidence

- Backend container running on `http://localhost:8080`.
- Frontend container recreated and running on `http://localhost:8081`.

## Risks

- This slice does not address `reportResponse.data.cells`; report grid alias
  normalization remains a separate follow-up to avoid mixing concerns.

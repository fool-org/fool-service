# View-first runtime contract

## Prompt

Keep the migration focused on FoolFrame's flow: render from View metadata first,
then query data for that View. Do not bind the Vue/runtime contract to concrete
business DTOs.

## Scope

- Tightened `scripts/runtime_doctor.py` so `querydata` rows must all match the
  loaded `getlistview` columns through row `Items`.
- Added a focused helper test proving DTO-only `values` rows do not satisfy the
  View/data contract.
- Updated migration task state and parity notes for the stricter runtime
  evidence.

## Changed Files

- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `tasks.md`
- `docs/migration/foolframe-parity.md`

## Validation

- `python scripts/runtime_doctor_test.py`: passed, 17 tests.
- `python scripts/check_repo_harness.py`: passed.
- `git diff --check`: clean.
- `python scripts/runtime_doctor.py`: passed all compose, auth, View/data,
  detail, `inputquery`, report, message, notify, and logout checks.
- `docker compose ps`: backend and frontend running; MySQL and Redis healthy.

## Runtime Evidence

- Backend container running on `http://localhost:8080`.
- Frontend container running on `http://localhost:8081`.
- Runtime doctor confirmed `view:getlistview` before `data:querydata`, and
  `data:querydata-items` requires rows to expose `Items` matching loaded View
  columns.

## Risks

- This does not remove the older backend `get-view` / `query-list` compatibility
  routes; it keeps active runtime proof on the legacy ViewId-driven path.

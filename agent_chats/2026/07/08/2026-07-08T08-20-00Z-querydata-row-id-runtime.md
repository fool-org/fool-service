# Querydata row id runtime path

## Prompt

Continue the Docker/Vue/FoolFrame migration and keep the View/data flow from
binding to concrete Docker seed DTOs.

## Scope

- Changed the runtime doctor detail smoke to use the object id returned by
  `querydata` before calling `querydatadetail`.
- Made the runtime doctor `querydata` checks depend on the previously loaded
  list view id.
- Added helper coverage for legacy `Data` rows and row `Items[].ObjId`.
- Removed hard-coded `1001` object id defaults from the Vue API-tool
  detail/save/runoperation fields. The main View workflow still fills those
  values after selecting a rendered row.
- Updated `tasks.md` and `docs/migration/foolframe-parity.md`.

## Validation

- `python scripts/runtime_doctor_test.py`
  - Passed: 7 tests.
- `cd frontend && npm test`
  - Passed: 3 files, 66 tests.
- `cd frontend && npm run build`
  - Passed.
- `python scripts/check_repo_harness.py`
  - Passed.
- `python scripts/runtime_doctor.py`
  - Passed, including `data:querydata` using the loaded list view id and
    returning a row object id, then `data:querydatadetail` using that id.
- `docker compose up -d --build`
  - Passed; backend and frontend images rebuilt.
- `docker compose up -d --force-recreate frontend`
  - Passed; backend and frontend containers recreated through Compose.
- `docker compose ps`
  - Passed after recreate; backend, frontend, MySQL, and Redis were running,
    with MySQL and Redis healthy.
- `python scripts/runtime_doctor.py`
  - Passed after Docker recreate.

## Risks

- This does not remove Docker seed IDs from backend smoke data or migration
  examples. It only removes the API-tool object-id defaults and hard-coded
  detail-smoke object id.

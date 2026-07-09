# 2026-07-09 SW_SYS_PROPERTY Scalar Schema

## Prompt

- Continue the Docker/FoolFrame/Vue migration goal.
- Keep schema migration work small, reuse the existing Docker SQL and runtime
  doctor surfaces, and commit atomically.

## Scope

- Completed the remaining mapped legacy `SW_SYS_PROPERTY` scalar columns in the
  Docker repair path: connection type, collection flag, DB column/property names,
  multi-map flag, key/check/generation metadata, and nullable/get/set flags.
- Extended `runtime_doctor.py` so the Docker `car_wash` baseline fails when any
  of those migrated property metadata columns are absent.

## Changed Files

- `docker/mysql/init/005-model.sql`
- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T09-30-50Z-sw-sys-property-scalar-schema.md`

## Red Test

- `python scripts/runtime_doctor_test.py` failed first because removing
  `SW_SYS_PROPERTY	PROPERTY_CONTYPE` from the simulated legacy schema still
  passed `legacy_core_schema_ok`.

## Validation

- `python scripts/runtime_doctor_test.py` passed.
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/005-model.sql` passed against the current Docker MySQL volume.
- `python scripts/check_repo_harness.py` passed.
- `python scripts/runtime_doctor.py` passed, including Docker compose, FH_JAVA
  `market_symbols`, legacy core schema, auth shell, View/data, report, message,
  and logout checks.
- `git diff --check` passed.

## Skipped Checks

- No Maven or frontend test run: this slice only changes Docker SQL, runtime
  doctor coverage, and migration evidence.

## Risks

- This is schema repair and runtime evidence only. It does not add new
  AppInstall behavior or new frontend flows.

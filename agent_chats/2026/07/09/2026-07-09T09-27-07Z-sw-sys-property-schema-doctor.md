# 2026-07-09 SW_SYS_PROPERTY Schema Doctor

## Prompt

- Continue the Docker/FoolFrame/Vue migration goal.
- Keep the migration small, reuse existing Docker SQL and runtime-doctor
  patterns, and avoid speculative APIs.

## Scope

- Compared FoolFrame `Property.cs` legacy columns with the current Docker
  `SW_SYS_PROPERTY` bootstrap.
- Added idempotent Docker repair blocks for `PROPERTY_MODEL`,
  `PROPERTY_FILTER`, `PROPERTY_SOURCE`, `PROPERTY_FORMAT`, `PROPERTY_SQLCON`,
  and `SW_SYS_MODEL_PropertiesSysId`.
- Extended `runtime_doctor.py` so Docker `car_wash` cannot pass the View/data
  smoke baseline while missing those migrated property metadata columns.

## Changed Files

- `docker/mysql/init/005-model.sql`
- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T09-27-07Z-sw-sys-property-schema-doctor.md`

## Red Test

- `python scripts/runtime_doctor_test.py` failed first because removing
  `SW_SYS_PROPERTY	PROPERTY_SOURCE` from the simulated legacy schema still
  passed `legacy_core_schema_ok`.

## Validation

- `python scripts/runtime_doctor_test.py` passed.
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/005-model.sql` passed against the current Docker MySQL volume.

## Risks

- This only guards and repairs the migrated property metadata columns already
  used by the current View/data/AppInstall paths. It does not claim every
  possible future `SW_SYS_PROPERTY` migration column is complete.

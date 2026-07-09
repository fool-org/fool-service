# 2026-07-09 Operation Trigger Schema Doctor

## Prompt

- Continue the Docker/FoolFrame/Vue migration goal.
- Keep the next migration slice small, reuse existing runtime doctor patterns,
  and commit atomically.

## Scope

- Compared existing Docker SQL, FoolFrame operation/trigger mappings, and Java
  runtime SELECTs for operation, command, operation-parameter, model-trigger,
  and property-trigger tables.
- Extended `runtime_doctor.py` so the Docker `car_wash` baseline fails if the
  migrated runoperation, trigger hydration, or AppInstall schema columns drift
  out of the live database.
- No new SQL was needed: the current Docker migration already creates these
  columns.

## Changed Files

- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T09-44-15Z-operation-trigger-schema-doctor.md`

## Red Test

- `python scripts/runtime_doctor_test.py` failed first because removing
  `SW_SYS_OPERATION	SW_MODEL_OPERATION_INVOKECLASS` from the simulated schema
  still passed `legacy_core_schema_ok`.

## Validation

- `python scripts/runtime_doctor_test.py` passed.
- `python scripts/runtime_doctor.py` passed, including Docker compose,
  FH_JAVA `market_symbols`, legacy core schema, auth shell, View/data, report,
  message, and logout checks.

## Skipped Checks

- No Maven or frontend test run: this slice only extends runtime schema guard
  coverage and migration evidence.

## Risks

- This is runtime-doctor coverage only. It does not add new operation or trigger
  behavior beyond the currently migrated paths.

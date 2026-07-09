# 2026-07-09 View Render Schema Doctor

## Prompt

- Continue the Docker/FoolFrame/Vue migration goal.
- Keep Vue on the View-first protocol and avoid business DTO binding.

## Scope

- Compared FoolFrame View/ViewItem/ViewOperation/OperationView mappings,
  Docker `006-view.sql`, and the migrated Java view runtime.
- Extended `runtime_doctor.py` so Docker cannot pass the legacy core schema
  check while missing View render columns used by list/detail metadata,
  operation buttons, operation views, or operation parameters.
- Reused the existing runtime-doctor schema check; no new verifier or protocol
  was added.

## Changed Files

- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T09-48-43Z-view-render-schema-doctor.md`

## Red Test

- `python scripts/runtime_doctor_test.py` failed first because the expected
  View render columns were not a subset of
  `runtime_doctor.LEGACY_CORE_SCHEMA_COLUMNS`.

## Validation

- `python scripts/runtime_doctor_test.py` passed.
- `python scripts/runtime_doctor.py` passed, including Docker compose,
  FH_JAVA `market_symbols`, legacy core schema, auth shell, View/data, report,
  message, and logout checks.

## Skipped Checks

- No Maven or frontend test run: this slice only extends runtime schema guard
  coverage and migration evidence.

## Risks

- This guards schema drift only. It does not add new View behavior beyond the
  currently migrated View-first workflow.

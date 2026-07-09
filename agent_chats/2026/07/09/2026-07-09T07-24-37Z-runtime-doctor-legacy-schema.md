# Agent Chat: Runtime doctor legacy core schema guard

## Prompt

- Continue the Docker/FoolFrame/Vue migration goal with atomic commits and
  reuse, while keeping the View-first workflow contract.

## Scope

- Tightened the Docker runtime doctor so the default View-first workflow also
  proves the core legacy model/view/operation schema columns are present.
- Kept this as a runtime evidence guard, not a claim that the complete
  `car_wash` schema migration is finished.

## Changed Files

- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- RED: `python scripts/runtime_doctor_test.py RuntimeDoctorTest.test_legacy_core_schema_requires_view_first_columns`
  - Failed as expected because `legacy_core_schema_ok` did not exist yet.
- GREEN: `python scripts/runtime_doctor_test.py RuntimeDoctorTest.test_legacy_core_schema_requires_view_first_columns`
- Full helper tests: `python scripts/runtime_doctor_test.py`
- Docker runtime: `python scripts/runtime_doctor.py`
  - Includes `[PASS] mysql:legacy-core-schema: View-first legacy model/view/operation schema is present`.

## Risks

- This guard checks the core columns needed by the current default View-first
  path. It does not replace a full schema migration verifier for every legacy
  table.

## Follow-ups

- Keep expanding schema evidence only when a newly migrated FoolFrame surface
  needs additional legacy tables or columns.

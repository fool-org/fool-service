# Runtime Doctor Report Grid

## Prompt

Continue the Docker/FoolFrame/Vue migration and keep progress tied to
repeatable runtime evidence.

## Scope

- Added a `getrpt` report-grid check to `scripts/runtime_doctor.py`.
- Added a focused helper test for the report-grid cell assertion.
- Updated migration parity docs and task state for the runtime evidence slice.

## Changed Files

- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- `python scripts/runtime_doctor_test.py`
  - Result: passed, 5 tests.
- `python scripts/check_repo_harness.py`
  - Result: passed.
- `git diff --check`
  - Result: passed.
- `python scripts/runtime_doctor.py`
  - Result: passed, including `report:getrpt`.

## Risks

- The doctor proves the current flat report execution path only; saved report
  persistence/execution and export remain separate migration work.

# Runoperation runtime aliases

## Prompt

Continue the FoolFrame migration with Docker runtime proof, Vue proxy coverage,
atomic commits, and no concrete business DTO binding.

## Scope

- Added a `runoperation` result alias helper to `scripts/runtime_doctor.py`.
- Extended the Docker runtime doctor with `data:runoperation-aliases`.
- Used the loaded View id and first `querydata` object id, but `OperationId=0`,
  so the smoke proves the legacy result surface without executing the seeded
  delete/save operations.
- Added focused helper tests for the Pascal result aliases.
- Updated task state and parity notes.

## Changed Files

- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `tasks.md`
- `docs/migration/foolframe-parity.md`

## Validation

- `python scripts/runtime_doctor_test.py`: passed, 18 tests.
- `python scripts/check_repo_harness.py`: passed.
- `git diff --check`: clean.
- `python scripts/runtime_doctor.py`: passed all compose, auth, View/data,
  `runoperation`, detail, `inputquery`, report, message, notify, and logout
  checks.

## Runtime Evidence

- Backend container running on `http://localhost:8080`.
- Frontend container running on `http://localhost:8081`.
- Runtime doctor reported `[PASS] data:runoperation-aliases`.

## Risks

- This proves the Docker/HTTP result alias contract for `runoperation`; it does
  not add new operation command execution parity.

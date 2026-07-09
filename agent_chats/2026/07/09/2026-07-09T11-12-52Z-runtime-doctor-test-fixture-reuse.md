# Runtime Doctor Test Fixture Reuse

## Prompt

- Continue the migration while keeping file size controlled and reusing code
  instead of pushing duplicated test fixtures.

## Scope

- Deduplicated repeated fake legacy API responses in
  `scripts/runtime_doctor_test.py`.
- Kept per-test saveobj/savenewobj assertions local to the tests that prove
  those specific runtime flows.
- Updated repo-local task state.

## Changed Files

- `scripts/runtime_doctor_test.py`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T11-12-52Z-runtime-doctor-test-fixture-reuse.md`

## Validation

- `python scripts/runtime_doctor_test.py` - passed, 30 tests.
- `git diff --check` - passed.
- `python scripts/check_repo_harness.py` - passed.
- `wc -l scripts/runtime_doctor_test.py scripts/runtime_doctor.py` -
  `runtime_doctor_test.py` is 991 lines and `runtime_doctor.py` is 1305 lines.

## Skipped Checks

- `python scripts/runtime_doctor.py` was not rerun because this slice only
  refactors the unit-test fake API responses; the executable runtime doctor did
  not change.

## Risks

- Low. The helper centralizes common happy-path fake responses, while tests
  with custom detail/save state still override the relevant endpoints locally.

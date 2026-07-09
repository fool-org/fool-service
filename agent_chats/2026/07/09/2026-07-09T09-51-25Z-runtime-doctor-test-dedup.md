# 2026-07-09 Runtime Doctor Test Dedup

## Prompt

- Continue the Docker/FoolFrame/Vue migration goal.
- Keep file size under control and maximize reuse.

## Scope

- Removed the duplicated full legacy schema fixture from
  `scripts/runtime_doctor_test.py`.
- Reused `runtime_doctor.LEGACY_CORE_SCHEMA_COLUMNS` to generate the simulated
  schema while keeping targeted missing-column assertions for regression
  coverage.

## Changed Files

- `scripts/runtime_doctor_test.py`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T09-51-25Z-runtime-doctor-test-dedup.md`

## Validation

- `python scripts/runtime_doctor_test.py` passed.

## Skipped Checks

- No Docker, Maven, or frontend run: this is a test-fixture deduplication only.

## Risks

- None expected; runtime behavior and doctor coverage are unchanged.

# 2026-07-09 Runtime Schema Catalog Split

## Prompt

- Continue the Docker/FoolFrame/Vue migration goal.
- Control file size and reuse while continuing schema/runtime migration work.

## Scope

- Moved `MARKET_SYMBOLS_COLUMNS` and `LEGACY_CORE_SCHEMA_COLUMNS` from
  `runtime_doctor.py` into `runtime_schema.py`.
- Kept `runtime_doctor.py` re-exporting the imported constants so existing
  schema tests and callers keep the same surface.
- No runtime behavior changed; this is file-size and reuse cleanup for the
  Docker smoke harness.

## Changed Files

- `scripts/runtime_doctor.py`
- `scripts/runtime_schema.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T10-22-51Z-runtime-schema-catalog-split.md`

## Red Test

- `python scripts/runtime_doctor_test.py` failed first because
  `runtime_schema` did not exist.

## Validation

- `python scripts/runtime_doctor_test.py` passed.
- `python scripts/runtime_doctor.py` passed.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.

## Skipped Checks

- No Maven or frontend test run: this slice only moves Python runtime-doctor
  schema constants.

## Risks

- None expected beyond import-path regressions covered by the runtime doctor
  unit and live smoke checks.

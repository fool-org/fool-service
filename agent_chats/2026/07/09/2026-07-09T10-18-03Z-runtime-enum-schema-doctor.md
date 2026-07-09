# 2026-07-09 Runtime Enum Schema Doctor

## Prompt

- Continue the Docker/FoolFrame/Vue migration goal.
- Keep runtime and schema proof aligned with the View-derived `getenums` path.

## Scope

- Compared Docker enum SQL and the `EnumValue` model used by migrated
  `getenums`.
- Extended `runtime_doctor.py` to guard `fool_sys_model_enum` columns:
  `name`, `value`, `remark`, and `owner`.
- No SQL, Java, or Vue behavior changed; this is runtime schema drift coverage
  only.

## Changed Files

- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T10-18-03Z-runtime-enum-schema-doctor.md`

## Red Test

- `python scripts/runtime_doctor_test.py` failed first because
  `fool_sys_model_enum` columns were not a subset of
  `runtime_doctor.LEGACY_CORE_SCHEMA_COLUMNS`.

## Validation

- `python scripts/runtime_doctor_test.py` passed.
- `python scripts/runtime_doctor.py` passed, including Docker compose,
  FH_JAVA `market_symbols`, legacy core schema, auth shell, View/data,
  `getenums`, report, message, and logout checks.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.

## Skipped Checks

- No Maven or frontend test run: this slice only extends runtime schema guard
  coverage and migration evidence.

## Risks

- This guards schema drift only. It does not add new enum loading behavior.

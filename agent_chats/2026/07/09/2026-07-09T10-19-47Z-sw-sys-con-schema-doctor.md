# 2026-07-09 SW_SYS_CON Schema Doctor

## Prompt

- Continue the Docker/FoolFrame/Vue migration goal.
- Keep Docker schema evidence aligned with the migrated legacy runtime surface.

## Scope

- Compared Docker model SQL for the legacy `SW_SYS_CON` table.
- Extended `runtime_doctor.py` to guard `SW_SYS_CON_DATASOURCE`,
  `SW_SYS_CON_INITALCATALOG`, `SW_SYS_CON_USERNAME`, `SW_SYS_CON_PASSWORD`,
  `SW_SYS_CON_INTEGRATEDSECURITY`, and `SW_SYS_CON_ISLOACL`.
- No SQL, Java, or Vue behavior changed; this is runtime schema drift coverage
  only.

## Changed Files

- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T10-19-47Z-sw-sys-con-schema-doctor.md`

## Red Test

- `python scripts/runtime_doctor_test.py` failed first because `SW_SYS_CON`
  columns were not a subset of `runtime_doctor.LEGACY_CORE_SCHEMA_COLUMNS`.

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

- This guards schema drift only. It does not add new routed connection behavior.

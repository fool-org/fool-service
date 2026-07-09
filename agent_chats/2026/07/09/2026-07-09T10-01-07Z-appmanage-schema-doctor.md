# 2026-07-09 AppManage Schema Doctor

## Prompt

- Continue the Docker/FoolFrame/Vue migration goal.
- Keep migration schema evidence aligned with the AppManage mappings and avoid
  broad behavior changes.

## Scope

- Compared `AppManageMigrationTest`, Docker schema SQL, and the runtime doctor
  schema guard.
- Extended `runtime_doctor.py` to guard remaining mapped columns for installed
  modules, model metadata, enum-value metadata, trigger/operation primary keys,
  and company/department auth graph tables used by event recipient expansion.
- No SQL or Java behavior changed; this is runtime schema drift coverage only.

## Changed Files

- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T10-01-07Z-appmanage-schema-doctor.md`

## Red Test

- `python scripts/runtime_doctor_test.py` failed first because the expected
  AppManage mapped columns were not a subset of
  `runtime_doctor.LEGACY_CORE_SCHEMA_COLUMNS`.

## Validation

- `python scripts/runtime_doctor_test.py` passed.
- `python scripts/runtime_doctor.py` passed, including Docker compose,
  FH_JAVA `market_symbols`, legacy core schema, auth shell, View/data, report,
  message, and logout checks.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.

## Skipped Checks

- No Maven or frontend test run: this slice only extends runtime schema guard
  coverage and migration evidence.

## Risks

- This guards schema drift only. It does not add new AppInstall behavior.

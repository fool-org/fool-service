# 2026-07-09 Query Catalog Schema Doctor

## Prompt

- Continue the Docker/FoolFrame/Vue migration goal.
- Keep schema migration evidence tied to actual migrated query/report paths.

## Scope

- Compared Docker query SQL with `JdbcCompareOpCatalog` and
  `JdbcSelectTypeCatalog`.
- Extended `runtime_doctor.py` to guard `SE_COMPARETYPE`,
  `SE_COMPARETYPE_PROPERTYINDEX`, `SE_SELECTEDTYPE`, and
  `SE_SELECTEDTYPE_PROPERTYINDEX` columns used by query/report metadata
  loading.
- No SQL, Java, or Vue behavior changed; this is runtime schema drift coverage
  only.

## Changed Files

- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T10-11-29Z-query-catalog-schema-doctor.md`

## Red Test

- `python scripts/runtime_doctor_test.py` failed first because the query
  catalog columns were not a subset of
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

- This guards schema drift only. It does not add new query/report behavior.

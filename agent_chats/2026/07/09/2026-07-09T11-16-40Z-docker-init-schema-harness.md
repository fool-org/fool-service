# Docker Init Schema Harness

## Prompt

- Continue the Docker/FoolFrame/Vue migration while maximizing reuse and
  controlling file size.

## Scope

- Added a repo harness check that parses `docker/mysql/init/*.sql` and verifies
  it declares every column from `scripts/runtime_schema.py`.
- Covered both legacy core schema columns and FH_JAVA `market_symbols` columns.
- Added focused tests for SQL parsing and missing-column reporting.
- Updated migration/task state.

## Changed Files

- `scripts/check_repo_harness.py`
- `scripts/check_repo_harness_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T11-16-40Z-docker-init-schema-harness.md`

## Validation

- `python scripts/check_repo_harness_test.py` - passed, 7 tests.
- `python scripts/check_repo_harness.py` - passed.
- `git diff --check` - passed.
- `PYTHONPATH=scripts python - <<'PY' ...` - confirmed 0 missing legacy columns,
  0 missing `market_symbols` columns, and 467 parsed Docker init columns.

## Skipped Checks

- `python scripts/runtime_doctor.py` was not rerun because this slice changes
  repository harness validation, not runtime doctor behavior.

## Risks

- Low. The parser intentionally covers the existing Docker init DDL shapes:
  `CREATE TABLE IF NOT EXISTS` and `ALTER TABLE ... ADD COLUMN`.

# Query Catalog Relation Guard

## Prompt
- Continue the Docker/FoolFrame/Vue migration, keeping the work atomic and
  reusable.

## Scope
- Added runtime seed-row checks for `SE_COMPARETYPE_PROPERTYINDEX` and
  `SE_SELECTEDTYPE_PROPERTYINDEX`.
- Added repository harness markers for the idempotent relation-row inserts in
  `docker/mysql/init/010-query.sql`.
- Updated migration parity and task-state docs.

## Changed Files
- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `scripts/check_repo_harness.py`
- `scripts/check_repo_harness_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/10/2026-07-10T02-30-21Z-query-catalog-relation-guard.md`

## Validation
- `python scripts/runtime_doctor_test.py` passed: 45 tests.
- `python scripts/check_repo_harness_test.py` passed: 9 tests.
- `python scripts/check_repo_harness.py` passed.
- `python scripts/runtime_doctor.py` passed against the running Docker stack.
- `git diff --check` passed.

## Runtime Evidence
- Live Docker MySQL before the patch:
  `SE_COMPARETYPE_PROPERTYINDEX=49`, `SE_SELECTEDTYPE_PROPERTYINDEX=44`.

## Risks
- This is a guard-only slice; it does not change query/report runtime behavior.

# 2026-07-09 Relation And MultiMap Schema Doctor

## Prompt

- Continue the Docker/FoolFrame/Vue migration goal.
- Keep schema migration work small, reuse existing Docker SQL and runtime
  doctor patterns, and commit atomically.

## Scope

- Compared FoolFrame `Relation.cs` and `MultiDBMap.cs` legacy table mappings
  with the current Docker schema.
- Added idempotent repair blocks for legacy `SW_SYS_RELATION` relation columns
  used by collection write/query paths.
- Added idempotent repair blocks for legacy `SW_SYS_MULTIMAP` DBMaps columns
  used by multi-column property hydration and AppInstall persistence.
- Extended `runtime_doctor.py` so Docker `car_wash` cannot pass the View/data
  baseline while missing those migrated relation/DBMaps schema columns.

## Changed Files

- `docker/mysql/init/005-model.sql`
- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T09-38-30Z-relation-multimap-schema-doctor.md`

## Red Test

- `python scripts/runtime_doctor_test.py` failed first because removing
  `SW_SYS_RELATION	SW_SYS_RELATION_TABLE` from the simulated schema still
  passed `legacy_core_schema_ok`.

## Validation

- `python scripts/runtime_doctor_test.py` passed.
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/005-model.sql` passed against the current Docker MySQL volume.
- `python scripts/runtime_doctor.py` passed, including Docker compose, FH_JAVA
  `market_symbols`, legacy core schema, auth shell, View/data, report, message,
  and logout checks.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.

## Skipped Checks

- No Maven or frontend test run: this slice only changes Docker SQL, runtime
  doctor coverage, and migration evidence.

## Risks

- This is schema repair and runtime evidence only. It does not add new
  collection behavior or new frontend flows.

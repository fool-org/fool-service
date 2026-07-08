# FH_JAVA Market Symbols Schema

## Prompt

Continue the migration goal while focusing on source-of-truth configuration
instead of hard-coding business DTO assumptions around the Docker order seed.

## Scope

- Added a Docker SQL seed for the FH_JAVA legacy `market_symbols` schema.
- Included the `market_symbols` columns introduced by
  `V2022022001__add_exchange_type.sql`.
- Added a runtime doctor MySQL schema check so live Docker evidence catches
  missing FH_JAVA symbol configuration.

## Changed Files

- `docker/mysql/init/008-fh-java-market-symbols.sql`
- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `tasks.md`
- `docs/migration/foolframe-parity.md`

## Validation

- RED: `python scripts/runtime_doctor.py --skip-compose`
  - Failed on `mysql:market_symbols` before applying the SQL to the existing
    Docker database.
- Applied current DB schema:
  `docker compose exec -T mysql mysql -uroot -pPa88word < docker/mysql/init/008-fh-java-market-symbols.sql`
- PASS: `python scripts/runtime_doctor_test.py`
- PASS: `python scripts/runtime_doctor.py --skip-compose`
- PASS: `python scripts/check_repo_harness.py`
- PASS: `git diff --check`

## Risks

- This imports only the FH_JAVA `market_symbols` schema slice. Broader `dingtou`
  tables such as plan and balance schemas remain in the database-schema backlog.

## Follow-ups

- Continue importing FH_JAVA legacy schema slices only when runtime parity needs
  them or the remaining migration list calls them out.

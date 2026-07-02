# Legacy Trigger Owner Columns

## Prompt

- Continue the Docker/Vue FoolFrame migration and keep commits atomic.

## Scope

- Added legacy collection owner columns for model/property trigger tables.
- Added legacy collection owner columns for model/property trigger command
  tables.
- Added idempotent `ALTER TABLE` blocks so already-running Docker MySQL
  databases receive the new columns.
- Updated the migration parity document.

## Changed Files

- `docker/mysql/init/005-model.sql`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/02/2026-07-02T11-50-37Z-legacy-trigger-owner-columns.md`

## Validation

- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/005-model.sql`
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SHOW COLUMNS ...; SHOW INDEX ...;"`
  - Verified owner columns and indexes:
    `SW_SYS_MODEL_TriggersMODEL_ID`,
    `SW_SYS_MODEL_TRIGGER_CommandsSysId`,
    `SW_SYS_PROPERTY_TriggersSysId`,
    `SW_SYS_PROPERTY_TRIGGER_CommandsSysId`.
- `python scripts/check_repo_harness.py`
  - Passed.
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
  - Returned `code:0`.
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list`
  - Returned `code:0` with two seeded rows.

## Skipped Checks

- Full Maven and frontend test suites; this slice only corrects Docker schema
  and parity documentation.

## Risks

- Runtime trigger execution remains separate migration work.

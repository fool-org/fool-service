# Legacy Operation Owner Columns

## Prompt

- Continue the Docker/Vue FoolFrame migration and keep commits atomic.

## Scope

- Added legacy collection owner columns for operation tables:
  `SW_SYS_MODEL_OperationsMODEL_ID`,
  `SW_SYS_OPERATION_ParamsSysId`,
  `SW_SYS_OPERATION_CommandsSysId`.
- Added idempotent `ALTER TABLE` blocks so already-running Docker MySQL
  databases receive the new columns.
- Updated the migration parity document.

## Changed Files

- `docker/mysql/init/005-model.sql`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/02/2026-07-02T11-57-01Z-legacy-operation-owner-columns.md`

## Validation

- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/005-model.sql`
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SHOW COLUMNS ...; SHOW INDEX ...;"`
  - Verified operation owner columns and indexes in `SW_SYS_OPERATION`,
    `SW_SYS_OPERATION_PARAM`, and `SW_SYS_COMMANDS`.
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

- Java app-install mappings and runtime operation execution remain separate
  migration work.

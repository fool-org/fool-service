# Legacy Model Trigger Schema

## Prompt

- Continue the Docker/Vue FoolFrame migration and keep commits atomic.

## Scope

- Compared `../FoolFrame/src/Server/SCPB05-Soway.Model/ModelTrigger.cs`.
- Compared `../FoolFrame/src/Server/SCPB05-Soway.Model/ModelTriggerCommand.cs`.
- Added Docker schema for `SW_SYS_MODEL_TRIGGER` and
  `SW_SYS_MODEL_TRIGGER_COMMANDS`.
- Updated the migration parity document.

## Changed Files

- `docker/mysql/init/005-model.sql`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/02/2026-07-02T11-43-39Z-legacy-model-trigger-schema.md`

## Validation

- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/005-model.sql`
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SHOW COLUMNS FROM SW_SYS_MODEL_TRIGGER; SHOW INDEX FROM SW_SYS_MODEL_TRIGGER; SHOW COLUMNS FROM SW_SYS_MODEL_TRIGGER_COMMANDS; SHOW INDEX FROM SW_SYS_MODEL_TRIGGER_COMMANDS;"`
  - Verified both legacy trigger tables, their generated `SysId` primary keys,
    indexed model/type columns, and command payload columns.
- `python scripts/check_repo_harness.py`
  - Passed.
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
  - Returned `code:0`.
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list`
  - Returned `code:0` with two seeded rows.

## Skipped Checks

- Full Maven and frontend test suites; this slice only adds Docker schema and
  parity documentation.

## Risks

- Property trigger schema and trigger execution behavior remain separate
  migration work.

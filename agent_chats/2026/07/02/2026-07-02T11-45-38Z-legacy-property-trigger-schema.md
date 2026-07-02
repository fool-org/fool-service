# Legacy Property Trigger Schema

## Prompt

- Continue the Docker/Vue FoolFrame migration and keep commits atomic.

## Scope

- Compared `../FoolFrame/src/Server/SCPB05-Soway.Model/Triggers/PropertyTriggers.cs`.
- Compared `../FoolFrame/src/Server/SCPB05-Soway.Model/PropertyTriggerCommand.cs`.
- Added Docker schema for `SW_SYS_PROPERTY_TRIGGER` and
  `SW_SYS_PROPERTY_TRIGGER_COMMANDS`.
- Updated the migration parity document.

## Changed Files

- `docker/mysql/init/005-model.sql`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/02/2026-07-02T11-45-38Z-legacy-property-trigger-schema.md`

## Validation

- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/005-model.sql`
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SHOW COLUMNS FROM SW_SYS_PROPERTY_TRIGGER; SHOW INDEX FROM SW_SYS_PROPERTY_TRIGGER; SHOW COLUMNS FROM SW_SYS_PROPERTY_TRIGGER_COMMANDS; SHOW INDEX FROM SW_SYS_PROPERTY_TRIGGER_COMMANDS;"`
  - Verified both property trigger tables, their generated `SysId` primary
    keys, indexed property/model/type columns, and command payload columns.
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

- Trigger execution behavior remains separate migration work.

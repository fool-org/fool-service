# Legacy SqlCon Schema

## Prompt

- Continue the Docker/Vue FoolFrame migration and keep commits atomic.

## Scope

- Compared `../FoolFrame/src/Server/SCPB05-Soway.Model/SqlCon.cs`.
- Added the legacy `SW_SYS_CON` table to Docker model SQL with the original
  `SW_SYS_CON_` prefix and legacy spellings `INITALCATALOG` and `ISLOACL`.
- Updated the migration parity document to mark the connection schema as
  seeded.

## Changed Files

- `docker/mysql/init/005-model.sql`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/02/2026-07-02T11-39-46Z-legacy-sqlcon-schema.md`

## Validation

- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/005-model.sql`
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SHOW COLUMNS FROM SW_SYS_CON; SHOW INDEX FROM SW_SYS_CON;"`
  - Verified the six legacy columns and the composite primary key over
    datasource, initial catalog, and username.
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

- This seeds the legacy table shape only. Runtime CRUD/UI surfaces for managing
  connection records remain part of broader app-management parity.

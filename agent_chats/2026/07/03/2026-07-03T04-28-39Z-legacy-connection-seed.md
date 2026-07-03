# 2026-07-03T04:28:39Z Legacy Connection Seed

## Prompt

- Continue the active goal: Docker runtime, FoolFrame migration parity, Vue
  frontend, and timely atomic commits.

## Scope

- Seed legacy `SW_SYS_CON` current-database connection metadata for the Docker
  `car_wash` MySQL service.
- Patch the running Docker MySQL volume to match the edited init SQL.

## Changed Files

- `docker/mysql/init/005-model.sql`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/03/2026-07-03T04-28-39Z-legacy-connection-seed.md`

## Validation

- RED: `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SELECT COUNT(*) AS sw_sys_con_count FROM SW_SYS_CON; SELECT MODEL_ID,MODEL_NAME,MODEL_CONTYPE,MODEL_CON FROM SW_SYS_MODEL WHERE MODEL_ID IN (100,101,102) ORDER BY MODEL_ID; SELECT VIEW_ID,VIEW_NAME,VIEW_CONTYPE FROM SW_SYS_VIEW WHERE VIEW_ID=100;"`
  - `SW_SYS_CON` count was `0`, while seeded legacy model/view rows used
    connection type `3`.
- GREEN: `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SELECT SW_SYS_CON_DATASOURCE,SW_SYS_CON_INITALCATALOG,SW_SYS_CON_USERNAME,SW_SYS_CON_INTEGRATEDSECURITY,SW_SYS_CON_ISLOACL FROM SW_SYS_CON ORDER BY SW_SYS_CON_DATASOURCE,SW_SYS_CON_INITALCATALOG,SW_SYS_CON_USERNAME; SELECT MODEL_ID,MODEL_NAME,MODEL_CONTYPE,MODEL_CON FROM SW_SYS_MODEL WHERE MODEL_ID IN (100,101,102) ORDER BY MODEL_ID; SELECT VIEW_ID,VIEW_NAME,VIEW_CONTYPE FROM SW_SYS_VIEW WHERE VIEW_ID = 100;"`
  - Returned `mysql:3306 / car_wash / root` in `SW_SYS_CON`, with existing
    model/view metadata still using connection type `3`.
- `python scripts/check_repo_harness.py`
  - Passed.
- `git diff --check`
  - Passed.
- `docker compose ps`
  - Backend/frontend running; MySQL/Redis healthy.

## Skipped

- Did not seed `WorkDataBase`; this slice only covers the legacy model
  connection metadata table referenced by the migration map.

## Risks

- Existing Docker volume was patched manually; fresh volumes get the same row
  from `005-model.sql`.

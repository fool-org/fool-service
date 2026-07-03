# 2026-07-03T04:33:03Z App Store DB Seed

## Prompt

- Continue the active goal: Docker runtime, FoolFrame migration parity, Vue
  frontend, and timely atomic commits.

## Scope

- Seed the legacy app-management application/store database records for the
  Docker `car_wash` smoke workflow.
- Patch the running Docker MySQL volume to match the edited init SQL.

## Changed Files

- `docker/mysql/init/002-app-manage.sql`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/03/2026-07-03T04-33-03Z-app-storedb-seed.md`

## Validation

- RED: app-management smoke records were absent before this slice:
  - `SW_APPLICATION` count was `0`.
  - `SW_STOREDB` count was `0`.
  - `SW_APPLICATION_SW_STOREDB` count was `0`.
  - Related DB-management tables `DB_App`, `WorkDataBase`, `DB_AppDB`, and
    `DS_DataSourceSet` were also empty, but were left for a separate
    DB-management parity slice.
- GREEN: `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SELECT SW_APP_APPLICATIONID,SW_APP_KEY,SW_APP_TYPE,SW_APP_CREATOR,SW_APP_NAME,SW_APP_OWNER,SW_APP_VERSION,SW_APP_VIEW FROM SW_APPLICATION ORDER BY SW_APP_APPLICATIONID; SELECT SW_STORE_STOREID,SW_STORE_NAME,SW_STORE_Note FROM SW_STOREDB ORDER BY SW_STORE_STOREID; SELECT SW_APPLICATION_ID,SW_STOREDB_ID FROM SW_APPLICATION_SW_STOREDB ORDER BY SW_APPLICATION_ID,SW_STOREDB_ID;"`
  - Returned application `fool-service`, store database `car_wash`, and
    relation `fool-service / car_wash`.
- `python scripts/check_repo_harness.py`
  - Passed.
- `git diff --check`
  - Passed.
- `docker compose ps`
  - Backend/frontend running; MySQL/Redis healthy.

## Skipped

- Did not seed `DB_App`, `WorkDataBase`, `DB_AppDB`, or `DS_DataSourceSet` in
  this slice. The legacy `WorkDataBase` path includes encrypted password
  payloads and should be handled with the DB-management parity behavior.

## Risks

- Existing Docker volume was patched manually; fresh volumes get the same rows
  from `002-app-manage.sql`.

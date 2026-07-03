# 2026-07-03T04:25:29Z Legacy Module Seed

## Prompt

- Continue the active goal: Docker runtime, FoolFrame migration parity, Vue
  frontend, and timely atomic commits.

## Scope

- Seed the legacy `SW_SYS_MODULE` record for the Docker market smoke metadata.
- Link the seeded legacy model shells `Order`, `OrderItem`, and `OrderState` to
  that module.
- Patch the running Docker MySQL volume to match the edited init SQL.

## Changed Files

- `docker/mysql/init/006-view.sql`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/03/2026-07-03T04-25-29Z-legacy-module-seed.md`

## Validation

- RED: `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SELECT * FROM SW_SYS_MODULE ORDER BY MODULE_NAME; SELECT MODEL_ID,MODEL_NAME,MODEL_MODULE FROM SW_SYS_MODEL ORDER BY MODEL_ID;"`
  - `SW_SYS_MODULE` was empty and seeded legacy models had null
    `MODEL_MODULE`.
- GREEN: `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SELECT MODULE_NAME,MODULE_REMARK,MODULE_ASSEMBLY,MODULE_FILENAME,MODULE_VERSION,MODULE_GENERATIONCODE FROM SW_SYS_MODULE ORDER BY MODULE_NAME; SELECT MODEL_ID,MODEL_NAME,MODEL_MODULE FROM SW_SYS_MODEL WHERE MODEL_ID IN (100,101,102) ORDER BY MODEL_ID;"`
  - Returned module `Market` and model shell links for `Order`, `OrderItem`,
    and `OrderState`.
- `python scripts/check_repo_harness.py`
  - Passed.
- `git diff --check`
  - Passed.
- `docker compose ps`
  - Backend/frontend running; MySQL/Redis healthy.

## Skipped

- Did not add new runtime app-install behavior; this slice only aligns Docker
  seed metadata with the migrated legacy module table.

## Risks

- Existing Docker volume was patched manually; fresh volumes get the same rows
  from `006-view.sql`.

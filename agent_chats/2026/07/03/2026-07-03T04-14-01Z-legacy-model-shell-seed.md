# 2026-07-03T04:14:01Z Legacy Model Shell Seed

## Prompt

- Continue the active goal: Docker runtime, FoolFrame migration parity, Vue
  frontend, and timely atomic commits.

## Scope

- Seed legacy `SW_SYS_MODEL` shell rows for the Docker `Order` and `OrderItem`
  smoke models.
- Patch the running Docker MySQL volume to match the edited init SQL.

## Changed Files

- `docker/mysql/init/006-view.sql`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/03/2026-07-03T04-14-01Z-legacy-model-shell-seed.md`

## Validation

- RED: `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SELECT MODEL_ID,MODEL_NAME,MODEL_CLASS,MODEL_DATABASETABLE,MODEL_AUTOID FROM SW_SYS_MODEL ORDER BY MODEL_ID; SELECT COUNT(*) AS sw_property_count FROM SW_SYS_PROPERTY;"`
  - Before this slice, `SW_SYS_MODEL` only had `OrderState` id `102`.
- GREEN: `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SELECT MODEL_ID,MODEL_NAME,MODEL_CLASS,MODEL_CONTYPE,MODEL_DATABASETABLE,MODEL_AUTOID FROM SW_SYS_MODEL WHERE MODEL_ID IN (100,101,102) ORDER BY MODEL_ID"`
  - Returned `Order` id `100`, `OrderItem` id `101`, and `OrderState` id
    `102`.
- `python scripts/check_repo_harness.py`
  - Passed.
- `git diff --check`
  - Passed.

## Skipped

- Did not seed `SW_SYS_PROPERTY`; this commit only closes the model-shell gap.

## Risks

- Existing Docker volume was patched manually; fresh volumes get the same rows
  from `006-view.sql`.

# 2026-07-03T04:17:26Z Legacy Property Seed

## Prompt

- Continue the active goal: Docker runtime, FoolFrame migration parity, Vue
  frontend, and timely atomic commits.

## Scope

- Seed legacy `SW_SYS_PROPERTY` rows for the Docker `Order` and `OrderItem`
  smoke models.
- Keep the existing runtime `fool_sys_model_property` rows as the source shape.
- Patch the running Docker MySQL volume to match the edited init SQL.

## Changed Files

- `docker/mysql/init/006-view.sql`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/03/2026-07-03T04-17-26Z-legacy-property-seed.md`

## Validation

- RED: `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SELECT id,name,remark,property_model,is_collection,owner,filter,source,format,\`column\`,property_type,allow_db_null,is_check,ix_group,multi_map FROM fool_sys_model_property ORDER BY id; SELECT COUNT(*) AS sw_property_count FROM SW_SYS_PROPERTY;"`
  - Runtime property rows existed, while `SW_SYS_PROPERTY` count was `0`.
- GREEN: `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SELECT SysId,PROPERTY_PROPERTYNAME,PROPERTY_TYPE,PROPERTY_MODEL,PROPERTY_ISARRAY,PROPERTY_COLNAME,PROPERTY_ALLOWDBNULL,PROPERTY_ISCHECK,SW_SYS_MODEL_PropertiesSysId FROM SW_SYS_PROPERTY ORDER BY SysId; SELECT r.SW_SYS_RELATION_SOURCEPROPERTY AS source_id, sp.PROPERTY_PROPERTYNAME AS source_name, r.SW_SYS_RELATION_TARGETPROPERTY AS target_id, tp.PROPERTY_PROPERTYNAME AS target_name FROM SW_SYS_RELATION r LEFT JOIN SW_SYS_PROPERTY sp ON sp.SysId = r.SW_SYS_RELATION_SOURCEPROPERTY LEFT JOIN SW_SYS_PROPERTY tp ON tp.SysId = r.SW_SYS_RELATION_TARGETPROPERTY WHERE r.SW_SYS_RELATION_SOURCEPROPERTY = 1004;"`
  - Returned six property rows and relation names `items -> itemId`.
- `python scripts/check_repo_harness.py`
  - Passed.
- `git diff --check`
  - Passed.
- `docker compose ps`
  - Backend/frontend running; MySQL/Redis healthy.

## Skipped

- Did not seed `SW_SYS_MULTIMAP` or trigger tables; this slice only covers the
  smoke model property rows needed by the current Docker metadata.

## Risks

- Existing Docker volume was patched manually; fresh volumes get the same rows
  from `006-view.sql`.

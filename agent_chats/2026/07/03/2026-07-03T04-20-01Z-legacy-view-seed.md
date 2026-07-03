# 2026-07-03T04:20:01Z Legacy View Seed

## Prompt

- Continue the active goal: Docker runtime, FoolFrame migration parity, Vue
  frontend, and timely atomic commits.

## Scope

- Seed legacy `SW_SYS_VIEW` and `SW_SYS_VIEW_ITEM` rows for the Docker
  `OrderList` smoke list view.
- Link legacy view items to the seeded legacy `SW_SYS_PROPERTY` rows.
- Patch the running Docker MySQL volume to match the edited init SQL.

## Changed Files

- `docker/mysql/init/006-view.sql`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/03/2026-07-03T04-20-01Z-legacy-view-seed.md`

## Validation

- RED: `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SELECT COUNT(*) AS legacy_view_count FROM SW_SYS_VIEW; SELECT COUNT(*) AS legacy_view_item_count FROM SW_SYS_VIEW_ITEM; SELECT id,view_name,view_model,view_type FROM fool_sys_view ORDER BY id; SELECT id,item_name,model_property,show_index,view_id FROM fool_sys_view_item ORDER BY id;"`
  - Runtime `OrderList` existed while legacy view and item tables had `0`
    rows.
- GREEN: `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SELECT VIEW_ID,VIEW_MODEL,VIEW_NAME,VIEW_TYPE,VIEW_CONTYPE,VIEW_AUTOFRESHINTERVAL,VIEW_CANEDIT FROM SW_SYS_VIEW WHERE VIEW_ID = 100; SELECT i.SysId,i.SW_SYS_VIEW_ItemsVIEW_ID,i.VIEW_ITEM_NAME,i.VIEW_ITEM_PROPERTY,p.PROPERTY_PROPERTYNAME,i.VIEW_ITEM_INDEX,i.VIEW_ITEM_READONLY,i.VIEW_ITEM_ISSHOW FROM SW_SYS_VIEW_ITEM i LEFT JOIN SW_SYS_PROPERTY p ON p.SysId = i.VIEW_ITEM_PROPERTY WHERE i.SW_SYS_VIEW_ItemsVIEW_ID = 100 ORDER BY i.VIEW_ITEM_INDEX;"`
  - Returned `OrderList` id `100` and view items `Order ID`, `Symbol`, and
    `State` linked to properties `orderId`, `symbol`, and `state`.
- `python scripts/check_repo_harness.py`
  - Passed.
- `git diff --check`
  - Passed.
- `docker compose ps`
  - Backend/frontend running; MySQL/Redis healthy.

## Skipped

- Did not seed legacy view files or operation views; this slice only covers the
  existing Docker smoke list view and its displayed columns.

## Risks

- Existing Docker volume was patched manually; fresh volumes get the same rows
  from `006-view.sql`.

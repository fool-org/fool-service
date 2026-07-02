# Prompt

Continue the Docker/FoolFrame/Vue migration goal with timely atomic commits.

# Scope

- Narrow Docker database parity slice for legacy `Soway.Model.ViewItem`.
- No backend or frontend runtime behavior changes.

# Changes

- Added Docker MySQL schema for legacy `SW_SYS_VIEW_ITEM`, matching explicit
  columns from `../FoolFrame/src/Server/SCPB05-Soway.Model/View/ViewItem.cs`.
- Updated `docs/migration/foolframe-parity.md` to mark `SW_SYS_VIEW_ITEM` as
  covered in the Docker schema baseline.

# Validation

- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/006-view.sql`
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SHOW COLUMNS FROM SW_SYS_VIEW_ITEM; SHOW INDEX FROM SW_SYS_VIEW_ITEM;"`
  - Confirmed `SysId`, `VIEW_ITEM_NAME`, `VIEW_ITEM_NOTE`,
    `VIEW_ITEM_FORMAT`, `VIEW_ITEM_PROPERTY`, `VIEW_ITEM_PROPERTY_SHOW`,
    `VIEW_ITEM_PROPERTY_VALUE`, `VIEW_ITEM_READONLY`, `VIEW_ITEM_INDEX`,
    `VIEW_ITEM_SUBVIEW`, `VIEW_ITEM_EDITVIEW`, `VIEW_ITEM_SELECTVIEW`,
    `VIEW_ITEM_WIDTH`, `VIEW_ITEM_ISSHOW`, `VIEW_ITEM_FILE`,
    `VIEW_ITEM_EDITTYPE`, and `VIEW_ITEM_SOURCEEXP`.
- `python scripts/check_repo_harness.py`
  - Passed.
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
  - Returned `{"code":0,"message":"success",...}`.
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list`
  - Returned `{"code":0,"message":"success",...}` with two seeded rows.

# Runtime Evidence

- Existing Compose stack remained up during validation.

# Risks

- Full Maven reactor and frontend build were not rerun because this change only
  touched Docker SQL and migration docs.

# Follow-ups

- Continue closing the remaining legacy view/operation schema gaps listed by
  the FoolFrame table comparison.

# Prompt

Continue the Docker/FoolFrame/Vue migration goal with timely atomic commits.

# Scope

- Narrow Docker database parity slice for legacy `Soway.Model.View.OperationViewItem`.
- No backend or frontend runtime behavior changes.

# Changes

- Added Docker MySQL schema for legacy `SW_SYS_OPERATIONVIEW_ITEM`, matching
  explicit columns from `../FoolFrame/src/Server/SCPB05-Soway.Model/View/OperationViewItem.cs`.
- Updated `docs/migration/foolframe-parity.md` to mark `SW_SYS_OPERATIONVIEW_ITEM`
  as covered in the Docker schema baseline.

# Validation

- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/006-view.sql`
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SHOW COLUMNS FROM SW_SYS_OPERATIONVIEW_ITEM; SHOW INDEX FROM SW_SYS_OPERATIONVIEW_ITEM;"`
  - Confirmed `SysId`, `SW_SYS_OPVIEWITEM_NAME`, `SW_SYS_OPVIEWITEM_INDEX`,
    and `SW_SYS_OPVIEWITEM_PARAM`.
- `python scripts/check_repo_harness.py`
  - Passed.
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
  - Returned `{"code":0,"message":"success",...}`.
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list`
  - Returned `{"code":0,"message":"success",...}` with two seeded rows.

# Runtime Evidence

- Existing Compose stack stayed up during validation.

# Risks

- Full Maven reactor and frontend build were not rerun because this change only
  touched Docker SQL and migration docs.

# Follow-ups

- Continue closing the remaining legacy model operation and trigger schema gaps.

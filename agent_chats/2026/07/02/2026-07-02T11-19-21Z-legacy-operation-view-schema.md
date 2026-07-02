# Prompt

Continue the Docker/FoolFrame/Vue migration goal with timely atomic commits.

# Scope

- Narrow Docker database parity slice for legacy `Soway.Model.View.OperationView`.
- No backend or frontend runtime behavior changes.

# Changes

- Added Docker MySQL schema for legacy `SW_SYS_OPERATIONVIEW`, matching
  explicit columns from `../FoolFrame/src/Server/SCPB05-Soway.Model/View/OperationView.cs`.
- Updated `docs/migration/foolframe-parity.md` to mark `SW_SYS_OPERATIONVIEW`
  as covered in the Docker schema baseline.

# Validation

- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/006-view.sql`
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SHOW COLUMNS FROM SW_SYS_OPERATIONVIEW; SHOW INDEX FROM SW_SYS_OPERATIONVIEW;"`
  - Confirmed `SysId`, `SW_SYS_OPVIEW_NAME`, `SW_SYS_OPVIEW_RESULT`,
    `SW_SYS_OPVIEW_OPREATION`, `SW_SYS_OPVIEW_SUCCESMSG`,
    `SW_SYS_OPVIEW_ERRORMSG`, `SW_SYS_OPVIEW_MSG`, `SW_SYS_OPVIEW_SHOW`,
    and `SW_SYS_OPVIEW_ConfirmMSG`.
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

- Continue closing the remaining legacy view/operation schema gaps, including
  `SW_SYS_OPERATIONVIEW_ITEM`.

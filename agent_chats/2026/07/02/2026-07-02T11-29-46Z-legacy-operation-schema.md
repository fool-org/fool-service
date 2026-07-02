# Prompt

Continue the Docker/FoolFrame/Vue migration goal with timely atomic commits.

# Scope

- Narrow Docker database parity slice for legacy `Soway.Model.Operation`.
- No backend or frontend runtime behavior changes.

# Changes

- Added Docker MySQL schema for legacy `SW_SYS_OPERATION`, matching explicit
  columns from `../FoolFrame/src/Server/SCPB05-Soway.Model/Operation/Operation.cs`.
- Updated `docs/migration/foolframe-parity.md` to mark `SW_SYS_OPERATION` as
  covered in the Docker schema baseline.

# Validation

- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/005-model.sql`
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SHOW COLUMNS FROM SW_SYS_OPERATION; SHOW INDEX FROM SW_SYS_OPERATION;"`
  - Confirmed `SysId`, `SW_MODEL_OPERATION_NAME`,
    `SW_MODEL_OPERATION_FILTER`, `SW_MODEL_OPERATION_BASETYPE`,
    `SW_MODEL_OPERATION_ARGMODEL`, `SW_MODEL_OPERATION_ARGFILTER`,
    `SW_MODEL_OPERATION_INVOKEDLL`, `SW_MODEL_OPERATION_INVOKECLASS`,
    `SW_MODEL_OPERATION_INVOKEMETHOD`, and `SW_MODEL_OPERATION_RETURNMODEL`.
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

- Continue closing the remaining legacy operation schema gaps, including
  `SW_SYS_OPERATION_PARAM` and `SW_SYS_COMMANDS`.

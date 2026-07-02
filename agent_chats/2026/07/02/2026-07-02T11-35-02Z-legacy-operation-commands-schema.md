# Prompt

Continue the Docker/FoolFrame/Vue migration goal with timely atomic commits.

# Scope

- Narrow Docker database parity slice for legacy `Soway.Model.OperationCommand`.
- No backend or frontend runtime behavior changes.

# Changes

- Added Docker MySQL schema for legacy `SW_SYS_COMMANDS`, matching explicit
  columns from `../FoolFrame/src/Server/SCPB05-Soway.Model/Operation/OperationCommand.cs`.
- Updated `docs/migration/foolframe-parity.md` to mark `SW_SYS_COMMANDS` as
  covered in the Docker schema baseline.

# Validation

- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/005-model.sql`
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SHOW COLUMNS FROM SW_SYS_COMMANDS; SHOW INDEX FROM SW_SYS_COMMANDS;"`
  - Confirmed `SysId`, `SW_SYS_COMMAND_TYPE`, `SW_SYS_COMMAND_PROPERTY`,
    `SW_SYS_COMMAND_EXP`, `SW_SYS_COMMAND_ARGMODEL`,
    `SW_SYS_COMMAND_ARGEXP`, `SW_SYS_COMMAND_ARGID`,
    `SW_SYS_COMMAND_INDEX`, `SW_SYS_COMMAND_PROPERTY_EXP`, and
    `SW_SYS_COMMAND_TEMPVALUE`.
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

- Continue closing the remaining legacy trigger schema gaps.

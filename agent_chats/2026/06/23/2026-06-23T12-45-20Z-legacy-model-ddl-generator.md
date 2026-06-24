# Legacy Model DDL Generator Migration

## Scope

- Compared legacy `../FoolFrame/src/Server/SCPB05-Soway.Model/ModelSqlServerFactory.cs`.
- Migrated the first table-DDL slice into a Java/MySQL generator.
- Added legacy model/property metadata fields needed for DDL type and key decisions.

## Legacy Mapping

- Legacy `GenerateCreateTableSql` skips array properties and properties without a database column.
- Property SQL type comes from `PropertyType`.
- `IdentifyId` maps to an auto-generated bigint identity key.
- `AutoSysId` adds a `SysId` identity primary key.
- `IsCheck` plus blank `IXGroup` on one column creates a primary key.
- `IsCheck` plus nonblank `IXGroup` creates a unique key group.

## Changes

- Added `LegacyMysqlDdlGenerator` in `fool-model`.
- Added DDL metadata fields:
  - `Model.autoSysId`
  - `Property.propertyType`
  - `Property.allowDbNull`
  - `Property.check`
  - `Property.ixGroup`
  - `Property.multiMap`
- Updated `docker/mysql/init/005-model.sql` with idempotent current-volume column additions.
- Updated `docker/mysql/init/006-view.sql` to keep `OrderList` seed properties populated with DDL metadata.
- Updated README and FoolFrame parity docs.

## TDD Evidence

- Red:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=LegacyMysqlDdlGeneratorTest -DfailIfNoTests=false test`
  - Failed because `autoSysId`/DDL generator behavior did not exist yet.
- Green:
  - same command passed after adding metadata fields and generator.
  - `Tests run: 2, Failures: 0, Errors: 0, Skipped: 0`

## Verification

- Schema apply:
  - `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/005-model.sql`
  - `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/006-view.sql`
  - exited 0.
- Schema check:
  - Confirmed `fool_sys_model.auto_sys_id`.
  - Confirmed `fool_sys_model_property.property_type`, `allow_db_null`, `is_check`, `ix_group`, and `multi_map`.
  - Confirmed `OrderList` seed properties have type/null/key metadata.
- Full Docker Maven build:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -DskipTests package`
  - 15-module reactor `BUILD SUCCESS`.
- Backend rebuild:
  - `docker compose up -d --build backend`
  - 15-module Docker Maven reactor `BUILD SUCCESS`; backend container recreated and started.
- Docker smoke:
  - `curl --retry 5 --retry-connrefused --retry-delay 2 -sS -i --max-time 10 http://localhost:8080/test`
  - returned `HTTP/1.1 200`.
  - `curl --retry 5 --retry-connrefused --retry-delay 2 -sS -i --max-time 10 -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
  - returned `HTTP/1.1 200` and `{"code":0,"message":"success"}`.
  - `curl --retry 5 --retry-connrefused --retry-delay 2 -sS -i --max-time 10 -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list`
  - returned `HTTP/1.1 200` and two seeded rows.
  - `curl -I --max-time 10 http://localhost:8081/`
  - returned `HTTP/1.1 200 OK`.
- Compose status:
  - `docker compose ps --all`
  - backend, frontend, mysql, and redis are up; mysql and redis are healthy.
- Harness:
  - `python scripts/check_repo_harness.py`
  - `Repository harness validation passed.`
- Diff check:
  - `git diff --check`
  - exited 0 with no output.

## Remaining Gaps

- `AppInstallGateway` still needs to call the DDL generator with real installed module metadata.
- Legacy relation DDL generation is still pending.

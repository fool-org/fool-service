# Legacy Relation DDL Generator Migration

## Scope

- Compared legacy `../FoolFrame/src/Server/SCPB05-Soway.Model/ModelSqlServerFactory.cs`.
- Migrated the `GetRelationSql` rules for `One2Many`, `Many2Many`, and `Recurve` into the Java/MySQL DDL generator.
- Added Java relation metadata types and the legacy `SW_SYS_RELATION` schema seed.

## Legacy Mapping

- `One2Many` adds `TargetColumn` to the relation table.
- The added `One2Many` column uses the source model key type, but legacy code removes `NOT`, so the generated MySQL column is nullable.
- `Many2Many` and `Recurve` create a relation table with the source relation column and target relation column.
- The source relation column uses the source model key type.
- The target relation column uses `TargetProperty.Model` key type when available, otherwise falls back to `Long`.

## Changes

- Added `Relation` and `RelationType` to `fool-model`.
- Extended `LegacyMysqlDdlGenerator` with `generateRelationSql(Relation relation, Model sourceModel)`.
- Added focused relation DDL coverage to `LegacyMysqlDdlGeneratorTest`.
- Added `SW_SYS_RELATION` to `docker/mysql/init/005-model.sql`.
- Updated README and FoolFrame parity docs.

## TDD Evidence

- Red:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=LegacyMysqlDdlGeneratorTest -DfailIfNoTests=false test`
  - Failed because `Relation`, `RelationType`, and relation DDL generation did not exist yet.
- Green:
  - same command passed after adding relation metadata and generator behavior.
  - `Tests run: 5, Failures: 0, Errors: 0, Skipped: 0`

## Verification

- Schema apply:
  - `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/005-model.sql`
  - exited 0.
- Schema check:
  - `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SHOW TABLES LIKE '%RELATION%';"`
  - confirmed `SW_SYS_RELATION`.
  - `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SHOW COLUMNS FROM SW_SYS_RELATION;"`
  - confirmed `SW_SYS_RELATION_TYPE`, `SW_SYS_RELATION_SOURCEPROPERTY`, `SW_SYS_RELATION_TARGETPROPERTY`, `SW_SYS_RELATION_TABLE`, `SW_SYS_RELATION_SOURCECOL`, `SW_SYS_RELATION_TARGETCOL`, and `SW_SYS_RELATION_CANBENULL`.
- Full Docker Maven build:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -DskipTests package`
  - 15-module reactor `BUILD SUCCESS`.
- Backend rebuild:
  - `docker compose up -d --build backend`
  - Docker image build `BUILD SUCCESS`; backend container recreated and started.
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
- Backend logs:
  - `docker compose logs --tail=120 backend`
  - showed normal Spring Boot startup and successful smoke requests, with no startup exception.
- Harness:
  - `python scripts/check_repo_harness.py`
  - `Repository harness validation passed.`
- Diff check:
  - `git diff --check`
  - exited 0 with no output.

## Remaining Gaps

- `AppInstallGateway` still needs to persist full relation metadata from installed modules.
- The installer still needs to call the migrated table/relation DDL generator against real installed module metadata.

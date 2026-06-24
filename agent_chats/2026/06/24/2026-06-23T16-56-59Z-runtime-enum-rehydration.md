# Runtime Enum Rehydration Parity

## Scope

- Compared legacy runtime model enum metadata behavior against the current generic DAO detail loader.
- Closed the remaining slice where stored runtime enum value rows were not rehydrated into `Model.enumValues` outside reflective module-source construction.

## Legacy Reference

- Legacy enum metadata is represented separately from regular model properties.
- Runtime model loading needs enum values available after the model shell is loaded so view/data behavior can use persisted enum metadata, not only reflective Java enum declarations.

## Changes

- Made `Model.enumValues` a generic detail-loaded collection using owner-column mapping.
- Added `EnumValue.owner` so `SqlScriptGenerator.generateSelectItems` can query enum rows by parent model id.
- Added runtime `fool_sys_model_enum` DDL to `docker/mysql/init/005-model.sql`, including an idempotent `owner` column migration for existing Compose volumes.
- Applied the same runtime table/column DDL to the currently running Compose MySQL database.
- Updated `docs/migration/foolframe-parity.md` to mark DAO rehydration of runtime enum detail rows as migrated.

## TDD

- Red:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=ModelDaoMappingTest#modelEnumValuesAreLoadedByGenericDetailMapping -DfailIfNoTests=false test`
  - Result: failed as expected at `ModelDaoMappingTest.java:30` because `Model.enumValues` was still transient and was not part of generic detail mapping; finished at `2026-06-23T16:56:59Z`.
- Green:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=ModelDaoMappingTest#modelEnumValuesAreLoadedByGenericDetailMapping -DfailIfNoTests=false test`
  - Result: 1 test, 0 failures, 0 errors, `BUILD SUCCESS`; finished at `2026-06-23T16:57:40Z`.

## Verification

- Related mapping class:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=ModelDaoMappingTest -DfailIfNoTests=false test`
  - Result: `ModelDaoMappingTest`: 2 tests, 0 failures, 0 errors, `BUILD SUCCESS`; finished at `2026-06-23T17:01:39Z`.
- Spring DAO integration test against Compose MySQL:
  - `docker run --rm -e SPRING_DATASOURCE_URL='jdbc:mysql://host.docker.internal:3307/car_wash?serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&allowMultiQueries=true&autoReconnect=true' -e SPRING_DATASOURCE_USERNAME=root -e SPRING_DATASOURCE_PASSWORD=Pa88word -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=ModelDataServiceTest#getModelRehydratesRuntimeEnumValuesFromDetailRows -DfailIfNoTests=false test`
  - Result: 1 test, 0 failures, 0 errors, `BUILD SUCCESS`; finished at `2026-06-23T17:01:11Z`.
- Related Spring test class:
  - `docker run --rm -e SPRING_DATASOURCE_URL='jdbc:mysql://host.docker.internal:3307/car_wash?serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&allowMultiQueries=true&autoReconnect=true' -e SPRING_DATASOURCE_USERNAME=root -e SPRING_DATASOURCE_PASSWORD=Pa88word -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=ModelDataServiceTest -DfailIfNoTests=false test`
  - Result: `ModelDataServiceTest`: 3 tests, 0 failures, 0 errors, `BUILD SUCCESS`; finished at `2026-06-23T17:01:42Z`.
- Running Compose MySQL schema:
  - `docker compose exec -T mysql mysql -uroot -pPa88word car_wash`
  - Applied `CREATE TABLE IF NOT EXISTS fool_sys_model_enum (...)` and idempotent `owner` column migration.
  - `SHOW COLUMNS FROM fool_sys_model_enum` confirmed `name`, `value`, `remark`, and `owner`.
- Full Maven package against Compose MySQL:
  - `docker run --rm -e SPRING_DATASOURCE_URL='jdbc:mysql://host.docker.internal:3307/car_wash?serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&allowMultiQueries=true&autoReconnect=true' -e SPRING_DATASOURCE_USERNAME=root -e SPRING_DATASOURCE_PASSWORD=Pa88word -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn package`
  - Result: `BUILD SUCCESS` across all 15 modules; finished at `2026-06-23T17:02:19Z`.
- Docker runtime smoke:
  - `docker compose build --pull=false backend`
  - Result: backend image built successfully with `mvn -DskipTests package`; current `fool-service-backend:latest` image id is `9a098189c7cb`.
  - `docker compose up -d --no-deps --force-recreate backend`
  - Result: `fool-service-backend-1` recreated and started from `fool-service-backend:latest`.
  - `docker compose ps`
  - Result: backend, frontend, MySQL, and Redis all `Up`; MySQL and Redis reported healthy.
  - `curl -sS -i http://localhost:8080/test`
  - Result: `HTTP/1.1 200`.
  - `curl -sS -i -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
  - Result: `HTTP/1.1 200`, `code: 0`, `message: success`.
  - `curl -sS -i -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list`
  - Result: `HTTP/1.1 200`, `code: 0`, `message: success`, 2 seeded rows.
  - `curl -I -sS http://localhost:8081/`
  - Result: `HTTP/1.1 200 OK` from `nginx/1.27.5`.
  - `docker compose logs --tail=90 backend`
  - Result: Spring Boot started with `docker` profile on port 8080, handled the smoke requests, and logged `SELECT name,value,remark,owner FROM fool_sys_model_enum WHERE owner=?` during data query.

## Remaining

- Generated expressions and declarative `ColumnAttribute.DefaultValue` save/DDL behavior remain separate migration slices.
- Inherited-property reflection parity beyond Java-declared fields remains separate.

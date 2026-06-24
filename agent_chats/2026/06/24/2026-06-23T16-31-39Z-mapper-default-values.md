# Mapper Default Row Values Parity

## Scope

- Compared legacy `../FoolFrame/src/Server/SCPB05-Soway.Model/SqlServer/SqlDataLoader.cs` row-loading behavior with current `fool-model` dynamic row mapping.
- Migrated the simple-column default value slice used when a loaded row has `DBNull`/SQL `NULL` for scalar properties.

## Legacy Reference

- `SqlDataLoader.LoadSqlData` calls `GetDefaultValue(property)` when a non-business property has no usable DB value.
- `GetDefaultValue` returns legacy defaults for common scalar types:
  - `Boolean`: `false`
  - `Int`/`UInt`: `0`
  - `Long`/`ULong`/`IdentifyId`: `0`
  - `Decimal`: `0`
  - `String`/`SerialNo`: empty string
  - `Date`/`DateTime`: `null`

## Changes

- Extended `MapperDbMapsTest` with `mapsLegacyDefaultValuesForNullSimpleColumns`.
- Updated `org.fool.framework.model.service.Mapper` so simple non-collection column mapping:
  - returns the JDBC value when present;
  - falls back to a legacy default when `ResultSet.getObject(column)` returns `null`;
  - falls back to the same default when a simple column lookup raises `SQLException`, matching the old row-loader behavior for missing row columns.
- Updated `docs/migration/foolframe-parity.md` to mark simple-column row default values as migrated while keeping generated expressions and declarative `ColumnAttribute.DefaultValue` save/DDL behavior in remaining work.

## TDD

- Red:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=MapperDbMapsTest -DfailIfNoTests=false test`
  - Result: `MapperDbMapsTest.mapsLegacyDefaultValuesForNullSimpleColumns` failed because `active` was `null` instead of `false`, finished at `2026-06-23T16:31:39Z`.
- Green:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=MapperDbMapsTest -DfailIfNoTests=false test`
  - Result: `MapperDbMapsTest`: 2 tests, 0 failures, 0 errors, finished at `2026-06-23T16:32:14Z`.

## Verification

- Module run with Compose MySQL datasource override:
  - `docker run --rm -e SPRING_DATASOURCE_URL='jdbc:mysql://host.docker.internal:3307/car_wash?serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&allowMultiQueries=true&autoReconnect=true' -e SPRING_DATASOURCE_USERNAME=root -e SPRING_DATASOURCE_PASSWORD=Pa88word -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -DfailIfNoTests=false test`
  - Result: reactor through `fool-model` `BUILD SUCCESS`; 10 tests, 0 failures, 0 errors, finished at `2026-06-23T16:33:10Z`.
- Full Maven package against Compose MySQL:
  - `docker run --rm -e SPRING_DATASOURCE_URL='jdbc:mysql://host.docker.internal:3307/car_wash?serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&allowMultiQueries=true&autoReconnect=true' -e SPRING_DATASOURCE_USERNAME=root -e SPRING_DATASOURCE_PASSWORD=Pa88word -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn package`
  - Result: `BUILD SUCCESS` across all 15 modules, finished at `2026-06-23T16:33:52Z`.
- Docker runtime smoke:
  - `docker compose build --pull=false backend`
  - Result: backend image rebuilt successfully; current `fool-service-backend:latest` image id is `e2e1274001e1`.
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
  - Result: Spring Boot started with `docker` profile on port 8080 and handled the smoke requests without logged errors in the captured tail.

## Remaining

- Generated expressions and declarative `ColumnAttribute.DefaultValue` save/DDL behavior remain separate migration slices.
- Enum row defaults remain unimplemented because the current Java `Model` does not yet carry legacy enum value metadata.

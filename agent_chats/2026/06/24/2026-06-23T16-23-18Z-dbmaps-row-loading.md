# DBMaps Row Loading Parity

## Scope

- Compared legacy `../FoolFrame/src/Server/SCPB05-Soway.Model/SqlServer/SqlDataLoader.cs` DBMaps loading behavior with current `fool-model` dynamic row mapping.
- Migrated the same-row multi-column DBMaps loading slice for dynamic business-object values.

## Changes

- Added `MapperDbMapsTest` to cover a `BusinessObject` property with `multiMap=true` and two DBMaps columns.
- Updated `org.fool.framework.model.service.Mapper` so:
  - simple non-collection properties still map directly from `property.column`;
  - multi-map non-collection properties create a child `DbMysqlDynamic` for `property.propertyModel`;
  - each `MultiDbMap.columnName` is read from the current `ResultSet` row and assigned to the matching target model property;
  - the child object is attached only when at least one mapped DBMaps value is non-null.
- Updated `docs/migration/foolframe-parity.md` to mark same-row DBMaps row-loading as migrated while keeping deeper DBMaps query/runtime behavior in remaining work.

## TDD

- Red:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=MapperDbMapsTest -DfailIfNoTests=false test`
  - Result: `MapperDbMapsTest` failed because `customer` was not mapped from DBMaps columns, finished at `2026-06-23T16:23:18Z`.
- Green:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=MapperDbMapsTest -DfailIfNoTests=false test`
  - Result: `MapperDbMapsTest`: 1 test, 0 failures, 0 errors, finished at `2026-06-23T16:23:55Z`.

## Verification

- Module run without datasource override:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -DfailIfNoTests=false test`
  - Result: failed in `fool-dao` Spring tests with `Communications link failure` / `Connection refused` because the default datasource was not overridden.
- Module run with Compose MySQL datasource override:
  - `docker run --rm -e SPRING_DATASOURCE_URL='jdbc:mysql://host.docker.internal:3307/car_wash?serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&allowMultiQueries=true&autoReconnect=true' -e SPRING_DATASOURCE_USERNAME=root -e SPRING_DATASOURCE_PASSWORD=Pa88word -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -DfailIfNoTests=false test`
  - Result: reactor through `fool-model` `BUILD SUCCESS`; `fool-model` tests: 9 tests, 0 failures, 0 errors, finished at `2026-06-23T16:24:44Z`.
- Full Maven package against Compose MySQL:
  - `docker run --rm -e SPRING_DATASOURCE_URL='jdbc:mysql://host.docker.internal:3307/car_wash?serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&allowMultiQueries=true&autoReconnect=true' -e SPRING_DATASOURCE_USERNAME=root -e SPRING_DATASOURCE_PASSWORD=Pa88word -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn package`
  - Result: `BUILD SUCCESS` across all 15 modules, finished at `2026-06-23T16:25:22Z`.
- Docker runtime smoke:
  - `docker compose build --pull=false backend`
  - Result: backend image rebuilt successfully; current `fool-service-backend:latest` image id is `f6b26c3fe36c`.
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
  - `docker compose logs --tail=80 backend`
  - Result: Spring Boot started with `docker` profile on port 8080 and handled the smoke requests without logged errors in the captured tail.

## Remaining

- DBMaps query generation, persistence, lazy loading, and view/query integration beyond same-row dynamic loading remain.
- Reflective inherited-property parity, generated expressions, and default values remain separate migration slices.

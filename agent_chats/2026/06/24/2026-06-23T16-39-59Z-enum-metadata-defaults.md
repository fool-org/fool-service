# Enum Metadata and Row Defaults Parity

## Scope

- Compared legacy `../FoolFrame/src/Server/SCPB08-Soway.AppManage/AssemblyModelFactory.cs` enum model creation with current reflective Java module-source discovery.
- Compared legacy `../FoolFrame/src/Server/SCPB05-Soway.Model/SqlServer/SqlDataLoader.cs` enum default row-loading behavior with current `fool-model` dynamic row mapping.
- Migrated runtime enum value metadata for reflective models and enum row defaults for SQL `NULL` values.

## Legacy Reference

- `AssemblyModelFactory.GetModel` creates enum models with `ModelType.Enum` and fills `model.EnumValues` from `Enum.GetValues(...)`; each value carries the enum member name and numeric ordinal.
- `SqlDataLoader.GetDefaultValue` returns the first enum value when a row value for a `PropertyType.Enum` property is `DBNull` or otherwise missing.

## Changes

- Added runtime `enumValues` metadata to `org.fool.framework.model.model.Model`.
- Updated `org.fool.framework.app.ReflectiveAppModuleSource` so Java enum types produce `EnumValue` entries in declaration order with ordinal string values.
- Updated `org.fool.framework.model.service.Mapper` so `PropertyType.Enum` defaults to the first enum value, parsing numeric values to `Integer` and otherwise returning the stored string.
- Extended `AppManageMigrationTest` to assert reflective package scanning includes enum values for `PackageOrderState`.
- Extended `MapperDbMapsTest` to assert a null enum column maps to the first enum value.
- Updated `docs/migration/foolframe-parity.md` to mark runtime enum metadata and enum row defaults as migrated while keeping persisted enum-value DAO loading as remaining work.

## TDD

- Red:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest#reflectiveModuleSourceScansAnnotatedModelsFromPackageLikeAssemblySource -DfailIfNoTests=false test`
  - Result: failed because expected `[OPEN:0, CLOSED:1]` but reflective enum metadata was empty; finished at `2026-06-23T16:40:01Z`.
- Red:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=MapperDbMapsTest#mapsLegacyEnumDefaultToFirstEnumValueForNullColumn -DfailIfNoTests=false test`
  - Result: failed because mapped enum status was `null` instead of `0`; finished at `2026-06-23T16:39:59Z`.
- Green:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest#reflectiveModuleSourceScansAnnotatedModelsFromPackageLikeAssemblySource -DfailIfNoTests=false test`
  - Result: 1 test, 0 failures, 0 errors, `BUILD SUCCESS`; finished at `2026-06-23T16:41:08Z`.
- Green:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=MapperDbMapsTest#mapsLegacyEnumDefaultToFirstEnumValueForNullColumn -DfailIfNoTests=false test`
  - Result: 1 test, 0 failures, 0 errors, `BUILD SUCCESS`; finished at `2026-06-23T16:41:06Z`.

## Verification

- Related app-manage class run:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest -DfailIfNoTests=false test`
  - Result: `AppManageMigrationTest`: 30 tests, 0 failures, 0 errors, `BUILD SUCCESS`; finished at `2026-06-23T16:41:33Z`.
- Related mapper class run:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=MapperDbMapsTest -DfailIfNoTests=false test`
  - Result: `MapperDbMapsTest`: 3 tests, 0 failures, 0 errors, `BUILD SUCCESS`; finished at `2026-06-23T16:41:30Z`.
- Full Maven package against Compose MySQL:
  - `docker run --rm -e SPRING_DATASOURCE_URL='jdbc:mysql://host.docker.internal:3307/car_wash?serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&allowMultiQueries=true&autoReconnect=true' -e SPRING_DATASOURCE_USERNAME=root -e SPRING_DATASOURCE_PASSWORD=Pa88word -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn package`
  - Result: `BUILD SUCCESS` across all 15 modules; finished at `2026-06-23T16:42:15Z`.
- Docker runtime smoke:
  - `docker compose build --pull=false backend`
  - Result: backend image built successfully; current `fool-service-backend:latest` image id is `956fd3d110bc`.
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

- `enumValues` is runtime/reflection metadata today; persisted enum-value DAO loading remains a separate migration slice if legacy metadata persistence requires it.
- Generated expressions and declarative `ColumnAttribute.DefaultValue` save/DDL behavior remain separate migration slices.

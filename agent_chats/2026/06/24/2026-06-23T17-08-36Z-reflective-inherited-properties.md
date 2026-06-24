# Reflective Inherited Properties Parity

## Scope

- Compared legacy `../FoolFrame/src/Server/SCPB05-Soway.Model/AssemblyModelFactory.cs` with current `ReflectiveAppModuleSource`.
- Closed the gap where Java reflective module-source discovery only used fields declared directly on the model class, while FoolFrame's `type.GetProperties()` includes inherited properties.

## Legacy Reference

- `AssemblyModelFactory.GetModelProperties` iterates `type.GetProperties()`.
- That means inherited public properties participate in model metadata, column metadata, relation metadata, and ID/show-property decisions in the legacy model factory.

## Changes

- Added a regression test proving a child model receives a non-ID field declared on its parent class.
- Changed `ReflectiveAppModuleSource` to discover model fields by walking the class hierarchy.
- Child fields are kept first and duplicate inherited field names are skipped, preserving existing child-field and child-ID precedence in the Java migration surface.
- Updated `docs/migration/foolframe-parity.md` to mark inherited-field reflective property discovery as migrated.

## TDD

- Red:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest#reflectiveModuleSourceIncludesInheritedPropertiesLikeLegacyGetProperties -DfailIfNoTests=false test`
  - Result: failed as expected with `missing property createdBy`; finished at `2026-06-23T17:08:36Z`.
- Green:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest#reflectiveModuleSourceIncludesInheritedPropertiesLikeLegacyGetProperties -DfailIfNoTests=false test`
  - Result: 1 test, 0 failures, 0 errors, `BUILD SUCCESS`; finished at `2026-06-23T17:09:14Z`.

## Verification

- Related app-manage class run:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest -DfailIfNoTests=false test`
  - Result: `AppManageMigrationTest`: 32 tests, 0 failures, 0 errors, `BUILD SUCCESS`; finished at `2026-06-23T17:09:35Z`.
- Full Maven package against Compose MySQL:
  - `docker run --rm -e SPRING_DATASOURCE_URL='jdbc:mysql://host.docker.internal:3307/car_wash?serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&allowMultiQueries=true&autoReconnect=true' -e SPRING_DATASOURCE_USERNAME=root -e SPRING_DATASOURCE_PASSWORD=Pa88word -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn package`
  - Result: `BUILD SUCCESS` across all 15 modules; finished at `2026-06-23T17:11:11Z`.
- Docker runtime smoke:
  - `docker compose build --pull=false backend`
  - Result: backend image built successfully with `mvn -DskipTests package`; current `fool-service-backend:latest` image id is `6ff4691999a2`.
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
- Arbitrary classpath dependency enumeration beyond model-type references remains separate.

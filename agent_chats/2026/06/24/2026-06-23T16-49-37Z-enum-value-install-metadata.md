# Enum Value Install Metadata Parity

## Scope

- Compared legacy `../FoolFrame/src/Server/SCPB05-Soway.Model/EnumValues.cs` and `Manage/SqlServerModuleInstaller.cs` with current Java module-source installation.
- Migrated legacy enum-value metadata persistence for module-source installs: enum models now persist their `EnumValues` entries into the legacy `SW_SYS_EMUNVALUE` table.

## Legacy Reference

- `EnumValues` maps to legacy table `SW_SYS_EMUNVALUE`.
- Its columns are `EMUN_STR` and `EMUN_VALUE`.
- `SqlServerModuleInstaller.InstallModules` adds enum models to the metadata graph while skipping only physical data-table/view/relation DDL for `ModelType.Enum`.

## Changes

- Added `AppInstalledEnumValue` mapped to `SW_SYS_EMUNVALUE`.
- Updated `DaoAppInstallGateway.installModuleSource` to persist enum values after the enum model is installed.
- Added duplicate checks by owner model id, enum string, and enum numeric value.
- Added `SW_SYS_EMUNVALUE` DDL to `docker/mysql/init/005-model.sql`.
- Applied the same DDL to the currently running Compose MySQL database because Docker init scripts only run on a fresh volume.
- Updated `docs/migration/foolframe-parity.md` to mark legacy enum metadata persistence as migrated while keeping runtime rehydration from stored enum rows as remaining work.

## TDD

- Red:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest#daoAppInstallGatewayPersistsLegacyEnumValuesWithEnumModels -DfailIfNoTests=false test`
  - First run exposed a test signature issue (`unreported exception java.lang.Exception`); fixed the test method declaration only.
  - Re-run result: failed as expected because expected 2 `AppInstalledEnumValue` records but actual was 0; finished at `2026-06-23T16:49:37Z`.
- Green:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest#daoAppInstallGatewayPersistsLegacyEnumValuesWithEnumModels -DfailIfNoTests=false test`
  - Result: 1 test, 0 failures, 0 errors, `BUILD SUCCESS`; finished at `2026-06-23T16:50:35Z`.

## Verification

- Related app-manage class run:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest -DfailIfNoTests=false test`
  - Result: `AppManageMigrationTest`: 31 tests, 0 failures, 0 errors, `BUILD SUCCESS`; finished at `2026-06-23T16:50:56Z`.
- Running Compose MySQL schema:
  - `docker compose exec -T mysql mysql -uroot -pPa88word car_wash`
  - Applied `CREATE TABLE IF NOT EXISTS SW_SYS_EMUNVALUE (...)`.
  - `SHOW COLUMNS FROM SW_SYS_EMUNVALUE` confirmed `EMUN_STR`, `EMUN_VALUE`, and `SW_SYS_MODEL_EnumValuesMODEL_ID`.
- Full Maven package against Compose MySQL:
  - `docker run --rm -e SPRING_DATASOURCE_URL='jdbc:mysql://host.docker.internal:3307/car_wash?serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&allowMultiQueries=true&autoReconnect=true' -e SPRING_DATASOURCE_USERNAME=root -e SPRING_DATASOURCE_PASSWORD=Pa88word -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn package`
  - Result: `BUILD SUCCESS` across all 15 modules; finished at `2026-06-23T16:51:46Z`.
- Docker runtime smoke:
  - `docker compose build --pull=false backend`
  - Result: backend image built successfully with `mvn -DskipTests package`; current `fool-service-backend:latest` image id is `865b72883fcc`.
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

- Runtime rehydration of stored enum rows back into `Model.enumValues` outside reflective module-source construction remains a separate slice.
- Generated expressions and declarative `ColumnAttribute.DefaultValue` save/DDL behavior remain separate migration slices.

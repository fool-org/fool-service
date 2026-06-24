# App Installer Relation Metadata

## Scope

- Compared legacy `../FoolFrame/src/Server/SCPB05-Soway.Model/Relation.cs`.
- Confirmed current Docker schema already contains `SW_SYS_RELATION`.
- Migrated module-source relation shell metadata persistence in `fool-app-manage`.
- Kept dynamic `App.SysCon` datasource routing, deeper relation/property edge cases, and reflective Java `AssemblyModuleSource` as remaining work.

## Changes

- Added `AppInstalledRelation` mapped to `SW_SYS_RELATION`.
- Updated `DaoAppInstallGateway.installModuleSource(...)` into three passes:
  - install module/model records
  - persist property records and retain property-id mapping
  - persist relation records with source/target property IDs
- Added duplicate relation lookup by source property, relation table, source column, and target column.
- Added focused test coverage for relation metadata using a module-source `Order` to `OrderLine` relation.

## TDD

- Red:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest -DfailIfNoTests=false test`
  - Failed compiling because `AppInstalledRelation` did not exist yet.
- Debugging:
  - A follow-up compile failure showed the new test used nonexistent `PropertyType.Object`.
  - Root cause was test fixture mismatch with the migrated enum; changed the fixture to `PropertyType.BusinessObject`.
- Green:
  - Same focused command passed.
  - `AppManageMigrationTest`: 14 tests, 0 failures, 0 errors.

## Verification

- Full Maven package against Compose MySQL:
  - `docker run --rm -e SPRING_DATASOURCE_URL='jdbc:mysql://host.docker.internal:3307/car_wash?serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&allowMultiQueries=true&autoReconnect=true' -e SPRING_DATASOURCE_USERNAME=root -e SPRING_DATASOURCE_PASSWORD=Pa88word -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn package`
  - Result: `BUILD SUCCESS`.
- Rebuilt and restarted backend:
  - `docker compose build --pull=false backend`
  - `docker compose up -d backend`
- Runtime smoke:
  - `GET http://localhost:8080/test`: HTTP 200 with seeded order rows.
  - `POST http://localhost:8080/api/v1/view/get-view`: HTTP 200 with `OrderList`.
  - `POST http://localhost:8080/api/v1/data/query-list`: HTTP 200 with 2 rows.
  - `HEAD http://localhost:8081/`: HTTP 200.
- `docker compose ps --all` showed backend, frontend, MySQL, and Redis running; MySQL and Redis were healthy.
- Confirmed current MySQL volume has `SW_SYS_RELATION` and `SW_SYS_RELATION_TARGETPROPERTY`.
- Backend logs showed normal Spring startup and successful smoke requests.

## Remaining

- Dynamic `App.SysCon` datasource routing.
- Deeper property/relation metadata edge cases where Java currently lacks separate legacy fields.
- Reflective Java equivalent of legacy `.NET AssemblyModuleSource`.

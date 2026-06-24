# App Installer Property Metadata

## Scope

- Compared the next `../FoolFrame` installer slice:
  - `SCPB08-Soway.AppManage/AppManager.cs` calls `SqlServerModuleInstaller.InstallModules(...)`.
  - `SCPB05-Soway.Model/Manage/SqlServerModuleInstaller.cs` persists modules and models before DDL/default-view generation.
  - `SCPB05-Soway.Model/Property.cs` maps property metadata to legacy table `SW_SYS_PROPERTY`.
- Migrated module-source property shell metadata persistence in `fool-app-manage`.
- Kept dynamic `App.SysCon` datasource routing, relation metadata persistence, deeper property edge cases, and reflective Java `AssemblyModuleSource` as remaining work.

## Changes

- Added `AppInstalledProperty` mapped to `SW_SYS_PROPERTY`.
- Updated `DaoAppInstallGateway.installModuleSource(...)` to persist module, model, then property records from module-source models.
- Added duplicate detection by installed owner model and property name.
- Added `SW_SYS_PROPERTY` to `docker/mysql/init/005-model.sql`.
- Updated the migration parity docs and README migration status.

## TDD

- Red:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest -DfailIfNoTests=false test`
  - Failed compiling because `AppInstalledProperty` did not exist yet.
- Green:
  - Same focused command passed.
  - `AppManageMigrationTest`: 13 tests, 0 failures, 0 errors.

## Verification

- Full Maven package against Compose MySQL:
  - `docker run --rm -e SPRING_DATASOURCE_URL='jdbc:mysql://host.docker.internal:3307/car_wash?serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&allowMultiQueries=true&autoReconnect=true' -e SPRING_DATASOURCE_USERNAME=root -e SPRING_DATASOURCE_PASSWORD=Pa88word -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn package`
  - Result: `BUILD SUCCESS`.
- Applied updated schema to the current MySQL volume:
  - `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/005-model.sql`
  - Confirmed `SW_SYS_PROPERTY` and `PROPERTY_PROPERTYNAME` exist.
- Rebuilt and restarted backend:
  - `docker compose build --pull=false backend`
  - `docker compose up -d backend`
- Runtime smoke:
  - `GET http://localhost:8080/test`: HTTP 200 with seeded order rows.
  - `POST http://localhost:8080/api/v1/view/get-view`: HTTP 200 with `OrderList`.
  - `POST http://localhost:8080/api/v1/data/query-list`: HTTP 200 with 2 rows.
  - `HEAD http://localhost:8081/`: HTTP 200.
- `docker compose ps --all` showed backend, frontend, MySQL, and Redis running; MySQL and Redis were healthy.
- Backend logs showed normal Spring startup and successful smoke requests.

## Remaining

- Dynamic `App.SysCon` datasource routing.
- Legacy relation metadata persistence into `SW_SYS_RELATION`.
- Deeper property metadata parity where Java currently lacks separate legacy fields.
- Reflective Java equivalent of legacy `.NET AssemblyModuleSource`.

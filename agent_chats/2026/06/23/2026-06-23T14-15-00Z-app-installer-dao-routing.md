# App Installer DAO Routing

## Scope

- Compared legacy `../FoolFrame/src/Server/SCPB08-Soway.AppManage/AppManager.cs` and `../FoolFrame/src/Server/SCPB05-Soway.Model/Manage/SqlServerModuleInstaller.cs`.
- Legacy `InstallModules(Source, ModelSqlCon, DataBaseSqlCon)` writes module/model/view/property/relation metadata through `ModelSqlCon` and executes table/relation DDL through `DataBaseSqlCon`.
- Migrated the Java `DaoAppInstallGateway` routing split so metadata operations can resolve by `sysCon` and DDL operations by the work database connection.
- Kept real multi-DataSource/JdbcTemplate construction from connection strings as remaining runtime work.

## Changes

- Added a connection-name DAO router to `DaoAppInstallGateway` while keeping the existing Spring single-`DaoService` constructor for the current Docker runtime.
- Routed application system metadata operations to `sysCon`:
  - application/authorization/user root module metadata
  - module-source module/model/property/relation metadata
  - default view persistence
  - app-system view preparation
  - menu and role writes
- Routed model/relation DDL execution to `databaseConnection`.
- Preserved the single-DAO fallback for current Compose smoke behavior.

## TDD

- Red:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest -DfailIfNoTests=false test`
  - Failed compiling because `DaoAppInstallGateway` still only accepted a single `DaoService`, not a connection router.
- Debugging:
  - First implementation compile failed because the no-connection DDL helper referenced `databaseConnection`, `resolveAuthorizedUser` still used the old signature, and `persistView` still had old DAO references.
  - After those compile issues, the new route test failed because the expected metadata write count missed the property metadata record.
- Green:
  - Same focused command passed.
  - `AppManageMigrationTest`: 16 tests, 0 failures, 0 errors.

## Verification

- Full Maven package against Compose MySQL:
  - `docker run --rm -e SPRING_DATASOURCE_URL='jdbc:mysql://host.docker.internal:3307/car_wash?serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&allowMultiQueries=true&autoReconnect=true' -e SPRING_DATASOURCE_USERNAME=root -e SPRING_DATASOURCE_PASSWORD=Pa88word -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn package`
  - Result: `BUILD SUCCESS`.
- Rebuilt and restarted backend:
  - `docker compose build --pull=false backend`
  - Result: backend image built, Maven reactor `BUILD SUCCESS`.
  - `docker compose up -d backend`
  - Result: backend container recreated and started.
- Runtime smoke:
  - `GET http://localhost:8080/test`: HTTP 200 with seeded order rows.
  - `POST http://localhost:8080/api/v1/view/get-view`: HTTP 200 with `OrderList`.
  - `POST http://localhost:8080/api/v1/data/query-list`: HTTP 200 with 2 rows.
  - `HEAD http://localhost:8081/`: HTTP 200.
- `docker compose ps --all` showed backend, frontend, MySQL, and Redis running; MySQL and Redis were healthy.
- Backend logs showed normal Spring startup and successful smoke requests.

## Remaining

- Build the real runtime router that maps `App.SysCon` / work database connection strings to distinct `DataSource`, `JdbcTemplate`, and transaction boundaries.
- Deeper legacy property/relation metadata edge cases.
- Full reflective Java model discovery equivalent to legacy `.NET AssemblyModuleSource`.

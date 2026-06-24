# App Installer DriverManager DAO Factory

## Scope

- Continued the `SCPB08-Soway.AppManage` migration after metadata/DDL DAO routing was split.
- Migrated the runtime connection-string path so `App.SysCon` and work database connection strings can resolve to distinct `DaoService` instances backed by distinct `JdbcTemplate` instances.
- Matched the existing `fool-event` DriverManager connection-string parsing style for raw `jdbc:` URLs and legacy key/value strings.

## Changes

- Added `AppDaoServiceFactory` as the app-management DAO factory contract.
- Added `DriverManagerAppDaoServiceFactory`:
  - parses raw `jdbc:` URLs;
  - parses legacy aliases for url, username/user/user id, password/pwd, and driver class;
  - creates `DriverManagerDataSource` and `JdbcTemplate`;
  - caches one `DaoService` per trimmed connection string.
- Added a manual `DaoService(SqlScriptGenerator, JdbcTemplate)` constructor while preserving the no-arg Spring/recording-test constructor.
- Changed the Spring `DaoAppInstallGateway` constructor to route non-blank connections through `AppDaoServiceFactory`; kept the single-DAO constructor for tests and current fallback usage.

## TDD

- Red:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest -DfailIfNoTests=false test`
  - Failed compiling because `AppDaoServiceFactory` and `DriverManagerAppDaoServiceFactory` did not exist yet.
- Green:
  - Same focused command passed.
  - Added direct coverage that the Spring two-argument `DaoAppInstallGateway` constructor routes non-blank connection strings through `AppDaoServiceFactory`.
  - `AppManageMigrationTest`: 18 tests, 0 failures, 0 errors.

## Verification

- Full Maven package against Compose MySQL:
  - `docker run --rm -e SPRING_DATASOURCE_URL='jdbc:mysql://host.docker.internal:3307/car_wash?serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&allowMultiQueries=true&autoReconnect=true' -e SPRING_DATASOURCE_USERNAME=root -e SPRING_DATASOURCE_PASSWORD=Pa88word -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn package`
  - Result: `BUILD SUCCESS` across all 15 modules.
- Rebuilt and restarted backend:
  - `docker compose build --pull=false backend`
  - Result: backend image built; image build Maven reactor used `-DskipTests` and finished `BUILD SUCCESS`.
  - `docker compose up -d backend`
  - Result: backend container recreated and started.
- Runtime smoke:
  - `GET http://localhost:8080/test`: HTTP 200 with 2 seeded order rows.
  - `POST http://localhost:8080/api/v1/view/get-view`: HTTP 200 with `OrderList`.
  - `POST http://localhost:8080/api/v1/data/query-list`: HTTP 200 with 2 data rows.
  - `HEAD http://localhost:8081/`: HTTP 200.
- `docker compose ps --all` showed backend, frontend, MySQL, and Redis running; MySQL and Redis were healthy.
- Backend logs showed Spring startup on port 8080 and successful smoke requests at `2026-06-23T14:27:39+0800`.

## Remaining

- Add production-grade pooling/transaction boundary handling around routed connection-string DAOs.
- Deeper legacy property/relation metadata edge cases.
- Full reflective Java model discovery equivalent to legacy `.NET AssemblyModuleSource`.

# App Installer Module Source Metadata

## Scope

- Mapped the next `../FoolFrame` `SCPB08-Soway.AppManage` installer slice: module sources now persist legacy module and model shell metadata before model schema DDL/default-view installation.
- Added `AppInstallGateway.installModuleSource(...)` and implemented it in `DaoAppInstallGateway` for `SW_SYS_MODULE` and `SW_SYS_MODEL`.
- Added conversion helpers from `AppModuleDefinition` and `Model` to legacy installed records, including work-database connection, connection type, table name, class name, module name, and `autoSysId`.
- Kept reflective Java `AssemblyModuleSource`, property/relation metadata persistence, and dynamic `App.SysCon` datasource routing as remaining work.

## TDD Evidence

- Red compile check:
  `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest -DfailIfNoTests=false test`
- Expected red result: `DaoAppInstallGateway.installModuleSource(...)` and the gateway interface hook were missing.
- After implementation, the focused test exposed a test-helper mismatch: `daoAppInstallGatewayPersistsLegacyModuleSourceMetadata` expected the new model's `autoSysId=true`, but the old helper hard-coded `false`.
- Adjusted the helper to accept the expected `autoSysId` value without changing production logic.
- Green focused check: `AppManageMigrationTest` ran 12 tests with 0 failures.

## Verification

- Full Maven check:
  `docker run --rm -e SPRING_DATASOURCE_URL='jdbc:mysql://host.docker.internal:3307/car_wash?serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&allowMultiQueries=true&autoReconnect=true' -e SPRING_DATASOURCE_USERNAME=root -e SPRING_DATASOURCE_PASSWORD=Pa88word -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn package`
- The first full Maven attempt failed because old module test profiles connect to container-local `127.0.0.1:3306/aip`; Compose exposes MySQL on host `127.0.0.1:3307` and seeds `car_wash`.
- Re-run with datasource override passed all 15 reactor modules.
- Backend image rebuild passed:
  `docker compose build --pull=false backend`
- Backend restart passed:
  `docker compose up -d backend`
- Smoke checks passed:
  - `GET http://localhost:8080/test` returned HTTP 200.
  - `POST http://localhost:8080/api/v1/view/get-view` for `OrderList` returned HTTP 200 and `code:0`.
  - `POST http://localhost:8080/api/v1/data/query-list` for `OrderList` returned HTTP 200, `code:0`, and `total:2`.
  - `HEAD http://localhost:8081/` returned HTTP 200.
- Compose status after restart: backend, frontend, MySQL, and Redis were all up; MySQL and Redis were healthy.
- Backend logs showed normal Spring Boot startup and successful smoke request handling.
- Repository harness passed:
  `python scripts/check_repo_harness.py`
- Whitespace diff check passed:
  `git diff --check`

## Remaining Gaps

- Reflective module discovery equivalent to legacy `.NET AssemblyModuleSource`.
- Full property/relation metadata persistence beyond module/model shells.
- Dynamic `App.SysCon` and work-database datasource routing in the DAO-backed installer.

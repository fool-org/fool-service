# App Installer Module Records Migration

## Scope

- Compared legacy `../FoolFrame/src/Server/SCPB08-Soway.AppManage/AppManager.cs`.
- Compared legacy `../FoolFrame/src/Server/SCPB05-Soway.Model/Manage/SqlServerModuleInstaller.cs`.
- Compared legacy `../FoolFrame/src/Server/SCPB05-Soway.Model/AssemblyModelFactory.cs`.
- Compared legacy `../FoolFrame/src/Server/SCPB05-Soway.Model/Module.cs`.
- Migrated the root module-record side effect for the three legacy app-installer module installation calls.

## Legacy Mapping

- Legacy create-app calls `SqlServerModuleInstaller.InstallModules` three times:
  - `Soway.Model.App.Application` into `App.SysCon`.
  - `SOWAY.ORM.AUTH.AuthorizedUser` into `App.SysCon`.
  - `SOWAY.ORM.AUTH.User` into each work database connection.
- Legacy `AssemblyModelFactory` stores module metadata in `SW_SYS_MODULE`; module name and assembly come from the source assembly name.
- The Java migration records the root installation modules:
  - `SCPB07` / `Soway.Model.App.Application` / `1.0.1605.2401`.
  - `SWUA02` / `SOWAY.ORM.AUTH.AuthorizedUser` / `1.0.16045.3001`.
  - `SWUA01` / `SOWAY.ORM.AUTH.User` / `1.0.16015.3001`.

## Changes

- Added `AppInstalledModule` mapped to `SW_SYS_MODULE`.
- Implemented `DaoAppInstallGateway.installApplicationModules`.
- Implemented `DaoAppInstallGateway.installAuthorizationModules`.
- Implemented `DaoAppInstallGateway.installUserModules`.
- Added minimal `SW_SYS_MODULE` schema to Docker MySQL init.
- Updated README and migration parity docs.

## TDD Evidence

- Red:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest -DfailIfNoTests=false test`
  - Failed compiling because `AppInstalledModule` did not exist.
- Green:
  - same command passed after adding the mapped model and DAO-backed gateway behavior.
  - `Tests run: 5, Failures: 0, Errors: 0, Skipped: 0`

## Remaining App Installer Gaps

- Full legacy module installation still needs model/table/relation/default-view DDL generation from module metadata.
- Dynamic `App.SysCon` datasource routing still needs a concrete adapter.

## Verification

- Focused test:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest -DfailIfNoTests=false test`
  - `Tests run: 5, Failures: 0, Errors: 0, Skipped: 0`
- Schema apply/check:
  - `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/004-event.sql`
  - `SHOW COLUMNS FROM SW_SYS_MODULE` confirmed `MODULE_NAME`, `MODULE_REMARK`, `MODULE_ASSEMBLY`, `MODULE_FILENAME`, `MODULE_VERSION`, `MODULE_GENERATIONCODE`, and `MODULE_CON`.
- Full build:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -DskipTests package`
  - 15-module reactor `BUILD SUCCESS`.
- Docker rebuild/smoke:
  - `docker compose up -d --build backend`
  - Backend image built with 15-module reactor `BUILD SUCCESS`; `backend` restarted.
  - `docker compose ps --all` showed backend/frontend up and MySQL/Redis healthy.
  - `curl --retry 5 --retry-connrefused --retry-delay 2 -i --max-time 10 http://localhost:8080/test` returned `HTTP/1.1 200` with `[]`.
  - `curl -I --max-time 10 http://localhost:8081/` returned `HTTP/1.1 200 OK`.
  - `docker compose logs --tail=120 backend` showed Spring Boot started and `/test` handled.
- Harness:
  - `python scripts/check_repo_harness.py` passed.
  - `git diff --check` passed.

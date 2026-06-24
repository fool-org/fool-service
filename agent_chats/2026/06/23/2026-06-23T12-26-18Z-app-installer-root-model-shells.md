# App Installer Root Model Shell Migration

## Scope

- Compared legacy `../FoolFrame/src/Server/SCPB08-Soway.AppManage/AppManager.cs`.
- Compared legacy `../FoolFrame/src/Server/SCPB05-Soway.Model/Manage/SqlServerModuleInstaller.cs`.
- Compared legacy `../FoolFrame/src/Server/SCPB05-Soway.Model/AssemblyModelFactory.cs`.
- Compared legacy `../FoolFrame/src/Server/SCPB05-Soway.Model/Model.cs`.
- Migrated root `SW_SYS_MODEL` shell records for the three legacy app-installer module source types.

## Legacy Mapping

- Legacy `AssemblyModelFactory` creates `SW_SYS_MODEL` records from each reflected model:
  - `MODEL_NAME` comes from `Type.Name`.
  - `MODEL_CLASS` comes from `Type.FullName`.
  - `MODEL_DATABASETABLE` comes from the ORM table attribute.
  - `MODEL_MODULE` points at the source assembly module.
  - `MODEL_CONTYPE` is `AppSys` when model and data connections match, otherwise `Current`.
- This slice records the root source models used by legacy `AppManager.CreatApp`:
  - `Application` / `Soway.Model.App.Application` / `SW_APPLICATION` / `SCPB07` / AppSys.
  - `AuthorizedUser` / `SOWAY.ORM.AUTH.AuthorizedUser` / `SW_APP_AUTH_USER` / `SWUA02` / AppSys.
  - `User` / `SOWAY.ORM.AUTH.User` / `SW_AUTH_USER` / `SWUA01` / Current.

## Changes

- Added `AppInstalledModel` mapped to `SW_SYS_MODEL`.
- Extended `DaoAppInstallGateway` module installation methods to create root model shell records alongside root module records.
- Added minimal `SW_SYS_MODEL` schema to Docker MySQL init.
- Updated README and migration parity docs.

## TDD Evidence

- Red:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest -DfailIfNoTests=false test`
  - Failed compiling because `AppInstalledModel` did not exist.
- Green:
  - same command passed after adding the mapped model and DAO-backed gateway behavior.
  - `Tests run: 5, Failures: 0, Errors: 0, Skipped: 0`

## Remaining App Installer Gaps

- Full legacy module installation still needs property/relation/default-view/table DDL generation from module metadata.
- Dynamic `App.SysCon` datasource routing still needs a concrete adapter.

## Verification

- Schema apply:
  - `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/005-model.sql`
  - exited 0.
- Schema check:
  - `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SHOW COLUMNS FROM SW_SYS_MODEL;"`
  - Confirmed `MODEL_ID`, `MODEL_NAME`, `MODEL_CLASS`, `MODEL_CONTYPE`, `MODEL_DATABASETABLE`, `MODEL_MODULE`, `MODEL_AUTOID`, and `MODEL_CON`.
- Focused test:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest -DfailIfNoTests=false test`
  - `Tests run: 5, Failures: 0, Errors: 0, Skipped: 0`
- Full Docker Maven build:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -DskipTests package`
  - 15-module reactor `BUILD SUCCESS`.
- Backend rebuild:
  - `docker compose up -d --build backend`
  - image rebuilt, backend container recreated, backend started.
- Docker runtime smoke:
  - `docker compose ps --all`
  - backend, frontend, mysql, and redis are up; mysql and redis are healthy.
  - `curl --retry 5 --retry-connrefused --retry-delay 2 -i --max-time 10 http://localhost:8080/test`
  - returned `HTTP/1.1 200` with `[]`.
  - `curl -I --max-time 10 http://localhost:8081/`
  - returned `HTTP/1.1 200 OK`.
  - `docker compose logs --tail=80 backend`
  - Spring Boot started on port 8080 and logged `/test` response latency.
- Harness:
  - `python scripts/check_repo_harness.py`
  - `Repository harness validation passed.`
- Diff check:
  - `git diff --check`
  - exited 0 with no output.

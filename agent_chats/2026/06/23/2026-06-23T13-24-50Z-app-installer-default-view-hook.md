# App Installer Default Auto-View Hook Wiring

## Scope

- Compared legacy `../FoolFrame/src/Server/SCPB05-Soway.Model/Manage/SqlServerModuleInstaller.cs`.
- Migrated the installer-side default list/detail view creation hook that follows legacy model schema installation.
- Fixed generic DAO SQL argument handling for enum fields so generated view/view-item rows can be persisted with ordinal values.
- Kept full `IModuleSource`/`AssemblyModuleSource` metadata loading and dynamic datasource routing as remaining work.

## Legacy Mapping

- Legacy `SqlServerModuleInstaller` creates default item and list views for each non-enum model after model/relation DDL setup.
- The Java migration now lets `AppInstaller` invoke a gateway default-view hook after model schema installation for each configured work database.
- `DaoAppInstallGateway` uses `LegacyAutoViewFactory` to create default detail/list views and persists their `ViewItem` rows with the generated/assigned `viewId`.

## Changes

- Added `AppInstallGateway.installDefaultViews(String sysCon, String databaseConnection, List<Model> models)`.
- Updated `AppInstaller` to call default-view installation after `installModelSchemas(...)` and before app-system view/menu preparation.
- Added `DaoAppInstallGateway.installDefaultViews(...)`, view dedupe by `view_name`, and item persistence through `DaoService.create(...)`.
- Added `fool-app-manage` dependency on `fool-view`.
- Updated `SqlScriptGenerator` to convert enum values to ordinals for insert, update, and delete SQL arguments.
- Updated README and FoolFrame parity docs to record the default auto-view hook.

## TDD Evidence

- Red app-manage:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest -DfailIfNoTests=false test`
  - Failed because `fool-app-manage` had no `fool-view` dependency and the default-view gateway API/call site did not exist.
- Red DAO:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-dao -am -Dtest=SqlScriptGeneratorMigrationTest -DfailIfNoTests=false test`
  - Failed because SQL args used the enum object (`ACTIVE`) instead of the expected ordinal (`1`).
- Green app-manage:
  - Same app-manage command passed after adding the dependency, installer hook, gateway API, DAO gateway implementation, and persistence test.
  - `Tests run: 8, Failures: 0, Errors: 0, Skipped: 0`
- Green DAO:
  - Same DAO command passed after converting enum SQL arguments to ordinals.
  - `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`

## Verification Evidence

- Full local Docker Maven package:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -DskipTests package`
  - 15-module `BUILD SUCCESS`.
- Backend image rebuild:
  - `docker compose build --pull=false backend`
  - Image-internal 15-module Maven package finished with `BUILD SUCCESS` at `2026-06-23T13:24:07Z`.
  - `docker compose up -d backend` recreated and started `fool-service-backend-1`.
- Docker smoke after restart:
  - `curl http://localhost:8080/test` returned HTTP 200 with two seeded rows.
  - `curl -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view` returned HTTP 200 and `code:0`.
  - `curl -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list` returned HTTP 200, `code:0`, and `total:2`.
  - `curl -I http://localhost:8081/` returned HTTP 200 from nginx.
  - `docker compose ps --all` showed backend/frontend up, MySQL healthy, and Redis healthy.
- Backend logs showed normal Spring Boot startup and successful `/test`, `/api/v1/view/get-view`, and `/api/v1/data/query-list` responses.
- Repo harness:
  - `python scripts/check_repo_harness.py`
  - `Repository harness validation passed.`
- Whitespace check:
  - `git diff --check`
  - passed with no output.

## Remaining Gaps

- `AppInstaller` still depends on configured model schemas; a full Java equivalent of legacy `IModuleSource`/`AssemblyModuleSource` model loading remains.
- `DaoAppInstallGateway` still executes through the current datasource; dynamic `App.SysCon` and work-database datasource routing remain.
- Full legacy property/relation/view metadata loading from modules remains incomplete.

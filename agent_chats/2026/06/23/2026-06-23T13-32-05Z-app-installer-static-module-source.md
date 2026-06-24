# App Installer Static Module Source Expansion

## Scope

- Compared legacy `../FoolFrame/src/Server/SCPB05-Soway.Model/AssemblyModuleSource.cs` and `Manage/SqlServerModuleInstaller.cs`.
- Migrated a Java-side static module source that can expose module definitions and flatten their models into the installer flow.
- Wired `AppInstaller` so module-source models feed the existing model/relation DDL and default auto-view hooks.
- Kept a reflective Java equivalent of legacy `.NET AssemblyModuleSource`, full metadata persistence, and dynamic datasource routing as remaining work.

## Legacy Mapping

- Legacy `AssemblyModuleSource.GetModules()` orders modules after dependencies.
- Legacy `AssemblyModuleSource.GetModels()` flattens the models for each module in that module order.
- Legacy `SqlServerModuleInstaller.InstallModules(...)` consumes that source to install model DDL, relation DDL, and default views.
- The Java migration now supports a `StaticAppModuleSource` with dependency-aware ordering and model flattening, then passes the resulting models through the already migrated install hooks.

## Changes

- Added `AppModuleDefinition` as a Java module metadata/configuration object with legacy defaults for assembly, file name, generated-DLL flag, dependencies, and models.
- Added `AppModuleSource` with `getModules()`, `getModels(module)`, and flattened `getModels()`.
- Added `StaticAppModuleSource`, including dependency-first module ordering.
- Added `AppBootstrapPlan.modelModuleSource`.
- Updated `AppInstaller` to merge module-source models with explicitly configured `modelSchemas`, de-duplicated by model identity.
- Updated README and FoolFrame parity docs to record static module-source model expansion.

## TDD Evidence

- Red:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest -DfailIfNoTests=false test`
  - Failed at test compile because `AppModuleDefinition`, `StaticAppModuleSource`, and the plan `modelModuleSource` API did not exist.
- Green:
  - Same command passed after adding the module source classes and installer wiring.
  - `Tests run: 10, Failures: 0, Errors: 0, Skipped: 0`

## Verification Evidence

- Full local Docker Maven package:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -DskipTests package`
  - 15-module `BUILD SUCCESS` at `2026-06-23T13:29:38Z`.
- Backend image rebuild:
  - `docker compose build --pull=false backend`
  - Image-internal 15-module Maven package finished with `BUILD SUCCESS` at `2026-06-23T13:31:27Z`.
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

- Static Java module definitions are supported; reflective discovery equivalent to legacy `.NET AssemblyModuleSource` remains.
- `DaoAppInstallGateway` still executes through the current datasource; dynamic `App.SysCon` and work-database datasource routing remain.
- Full legacy module/model/property/relation metadata persistence from module sources remains incomplete.

# App Installer Model Schema Hook Wiring

## Scope

- Compared legacy `../FoolFrame/src/Server/SCPB05-Soway.Model/Manage/SqlServerModuleInstaller.cs`.
- Migrated the installer-side wiring that passes installed model metadata into the model/relation DDL execution hook.
- Kept full `AssemblyModuleSource`/module metadata loading and dynamic `App.SysCon` datasource routing as remaining work.

## Legacy Mapping

- Legacy `InstallModules` loops each module and model from `IModuleSource`.
- For each non-enum model it collects table DDL and relation DDL, then executes the collected DDL against the data database.
- The Java migration now lets `AppBootstrapPlan` carry configured model schemas and calls the gateway model-schema hook after user modules are installed for each work database.

## Changes

- Added `AppBootstrapPlan.modelSchemas`.
- Added `AppInstallGateway.installModelSchemas(String sysCon, String databaseConnection, List<Model> models)`.
- Updated `AppInstaller` to call the schema hook for each `StoreDatabase` when configured model schemas are present.
- Updated `DaoAppInstallGateway` to expose the gateway interface method and reuse the existing `installModelSchemas(List<Model>)` DDL execution path.
- Updated README and FoolFrame parity docs to distinguish configured model schema wiring from remaining full module-source metadata loading.

## TDD Evidence

- Red:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest -DfailIfNoTests=false test`
  - Failed because `AppBootstrapPlan.setModelSchemas`, the gateway schema method, and the DAO gateway overload did not exist.
- Green:
  - Same command passed after adding plan state, gateway API, installer call site, and DAO gateway overload.
  - `Tests run: 7, Failures: 0, Errors: 0, Skipped: 0`

## Verification Evidence

- Full local Docker Maven package:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -DskipTests package`
  - 15-module `BUILD SUCCESS`.
- Backend image rebuild:
  - `docker compose build --pull=false backend`
  - Image-internal 15-module Maven package finished with `BUILD SUCCESS`.
  - `docker compose up -d backend` recreated and started the backend container.
- Docker smoke after restart:
  - `curl http://localhost:8080/test` returned HTTP 200 with two seeded rows.
  - `curl -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view` returned HTTP 200 and `code:0`.
  - `curl -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list` returned HTTP 200, `code:0`, and `total:2`.
  - `curl -I http://localhost:8081/` returned HTTP 200 from nginx.
  - `docker compose ps --all` showed backend/frontend up, MySQL healthy, and Redis healthy.
- Backend logs showed normal startup and successful `/test`, `/api/v1/view/get-view`, and `/api/v1/data/query-list`.
- Repo harness:
  - `python scripts/check_repo_harness.py`
  - `Repository harness validation passed.`
- Whitespace check:
  - `git diff --check`
  - passed with no output.

## Remaining Gaps

- `AppInstaller` still depends on configured model schemas; a full Java equivalent of legacy `IModuleSource`/`AssemblyModuleSource` model loading remains.
- `DaoAppInstallGateway` still executes through the current datasource; dynamic `App.SysCon` and work-database datasource routing remain.
- Full legacy property/relation metadata persistence remains incomplete.

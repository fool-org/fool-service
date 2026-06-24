# App Install DDL Execution Hook Migration

## Scope

- Compared legacy `../FoolFrame/src/Server/SCPB05-Soway.Model/Manage/SqlServerModuleInstaller.cs`.
- Migrated the first Java gateway hook for executing generated model table DDL and relation DDL from installed model metadata.
- Kept dynamic `App.SysCon` routing and full module-source metadata loading as remaining work.

## Legacy Mapping

- Legacy `InstallModules` collects create-table commands for each non-enum model.
- It collects relation commands for each model relation and de-duplicates relation objects.
- It executes table DDL first, then relation DDL.
- `InstallModel` executes the table DDL for a single model and then executes each relation DDL command.

## Changes

- Added `fool-app-manage` dependency on `fool-model`.
- Added `Model.relations`.
- Marked `Model.relations` as `transient` for the current generic DAO mapper; relation metadata loading is still a dedicated legacy gap because `SW_SYS_RELATION` links back through source properties rather than a direct model owner column.
- Added `DaoService.execute(String sql)` as the raw DDL execution surface.
- Added `DaoAppInstallGateway.installModelSchemas(List<Model>)`.
- The gateway skips enum models, generates table DDL, de-duplicates relations by object identity, generates relation DDL, executes nonblank statements through `DaoService`, and returns the executed statements for traceability.
- Added focused gateway coverage in `AppManageMigrationTest`.
- Added `ModelDaoMappingTest` coverage to prevent the generic mapper from querying a nonexistent `relations` column.
- Updated README and FoolFrame parity docs.

## TDD Evidence

- Red:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest -DfailIfNoTests=false test`
  - Failed because `fool-app-manage` did not depend on `fool-model` and the gateway DDL hook did not exist.
- Green:
  - same command passed after adding the dependency, model relation metadata, DAO execution surface, and gateway hook.
  - `Tests run: 6, Failures: 0, Errors: 0, Skipped: 0`
- Runtime debug:
  - First backend rebuild reached startup but failed bean creation for `DaoAppInstallGateway`: adding a package-private test constructor removed Spring's single-constructor inference, and Spring looked for a default constructor.
  - Added a focused assertion that the public `DaoService` constructor is annotated with `@Autowired`; it failed before the fix and passed after adding the annotation.
  - After that, `query-list` returned HTTP 200 with `code:-1`; backend logs showed `Unknown column 'relations' in 'where clause'` from generic collection fill on `Model.relations`.
  - Added `ModelDaoMappingTest`; red run failed because the mapper still included `relations`.
  - Marked `Model.relations` transient; green run passed:
    `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=ModelDaoMappingTest -DfailIfNoTests=false test`
  - `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`
- Re-ran gateway focused tests after the mapper fix:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest -DfailIfNoTests=false test`
  - `Tests run: 6, Failures: 0, Errors: 0, Skipped: 0`

## Verification Evidence

- Full local Docker Maven package after the mapper fix:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -DskipTests package`
  - 15-module `BUILD SUCCESS`.
- Backend image rebuild:
  - Initial `docker compose up -d --build backend` retry failed before compilation on Docker Hub auth DNS timeout.
  - `docker compose build --pull=false backend` then completed successfully, with the image-internal 15-module Maven `BUILD SUCCESS`.
  - `docker compose up -d backend` recreated and started the backend container.
- Docker smoke after restart:
  - `curl http://localhost:8080/test` returned HTTP 200 with two seeded rows.
  - `curl -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view` returned HTTP 200 and `code:0`.
  - `curl -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list` returned HTTP 200, `code:0`, and `total:2`.
  - `curl -I http://localhost:8081/` returned HTTP 200 from nginx.
  - `docker compose ps --all` showed backend/frontend up, MySQL healthy, and Redis healthy.
- Backend logs after smoke showed normal Spring startup and successful `/test`, `/api/v1/view/get-view`, and `/api/v1/data/query-list`; no startup exception and no `relations` SQL error remained.
- Repo harness:
  - `python scripts/check_repo_harness.py`
  - `Repository harness validation passed.`
- Whitespace check:
  - `git diff --check`
  - passed with no output.

## Remaining Gaps

- `AppInstaller` still needs to pass real installed module metadata into `installModelSchemas`.
- `DaoAppInstallGateway` still executes through the current datasource; dynamic `App.SysCon`/work-database datasource routing remains to be migrated.
- Full legacy property/relation metadata persistence and relation loading from `SW_SYS_RELATION` through source properties are still incomplete.

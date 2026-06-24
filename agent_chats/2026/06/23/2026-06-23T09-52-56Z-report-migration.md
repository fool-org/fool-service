# Prompt

Continue the active goal:

1. Bring the environment up with Docker.
2. Complete migration against `../FoolFrame`.
3. Use Vue for the frontend.

# Scope

This slice starts migrating `../FoolFrame/src/Server/SWRPT01-Soway.Report` into `fool-service`.

# Changes

- Added `fool-report` as a Maven reactor module and wired it into `business-application`.
- Reused the migrated `PropertyType` enum from `fool-common` for report result-table columns.
- Added report enums: `OrderType`, `StaticType`, and `CalDirection`.
- Added report definition/result DTOs: `Report`, `Param`, `ParamInput`, `ReportResult`, `ReportResultTable`, and `ReportResultTableColumn`.
- Added table-format DTOs: `CellFormat`, `TableFormat`, `ValueCell`, `StaticFormat`, `StaticCellFormate`, `StaticsCells`, and `TableHeader`.
- Added matrix DTOs and helpers: `MatrixTable`, `SingleCell`, `DataRect`, `Cell`, `ReportEmptyValue`, `IReportSource`, and `MatrixTableFactory`.
- Migrated `MatrixTableFactory.getCells` coordinate rendering and calculation-scope offset behavior.
- Added Java source-row matrix construction via `MatrixTableFactory.createMatrixTable(TableFormat, List<Map<String,Object>>)`.
- Added row and column static subtotal header insertion and calculated `DataRect` generation for legacy `StaticFormat` definitions.
- Added legacy report grid output DTOs and `ReportGridRenderer`, covering the pure output assembly from `HandlerMakeReport`.
- Left the mixed migration parity docs and README status updates unstaged for a separate status commit.

# Validation

- Red test:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -f fool-report/pom.xml test`
  failed on missing `SingleCell`/`Cell` report classes.
- Red test:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-report -am test`
  failed on missing `createMatrixTable(...)`.
- Green focused test:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-report -am test`
  passed with 6 tests, 0 failures, 0 errors.
- Red subtotal parity test:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-report -am test`
  failed on missing row/column static subtotal headers.
- Green subtotal parity test:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-report -am test`
  passed with 8 tests, 0 failures, 0 errors.
- Full backend build:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DskipTests package`
  passed across 15 Maven modules.
- Backend Docker rebuild:
  `docker compose up -d --build backend`
  rebuilt the backend image and started `fool-service-backend-1`.
- Runtime smoke:
  `docker compose ps --all`
  showed backend, frontend, MySQL, and Redis running.
  `curl --retry 5 --retry-connrefused --retry-delay 2 -i --max-time 10 http://localhost:8080/test`
  returned HTTP 200 with `[]`.
  `curl -I --max-time 10 http://localhost:8081/`
  returned HTTP 200 from nginx.
  `docker compose logs --tail=120 backend`
  showed `Started Application in 2.62 seconds`.
- Final checks:
  `python scripts/check_repo_harness.py`
  passed.
  `git diff --check`
  passed.
- Static subtotal follow-up verification:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DskipTests package`
  passed across 15 Maven modules after subtotal support.
  `docker compose up -d --build backend`
  rebuilt the backend image and started `fool-service-backend-1`.
  `docker compose ps --all`
  showed backend, frontend, MySQL, and Redis running.
  `curl --retry 5 --retry-connrefused --retry-delay 2 -i --max-time 10 http://localhost:8080/test`
  returned HTTP 200 with `[]`.
  `curl -I --max-time 10 http://localhost:8081/`
  returned HTTP 200 from nginx.
  `python scripts/check_repo_harness.py`
  passed.
  `git diff --check`
  passed.
- Report grid renderer red/green:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-report -am test`
  first failed on missing `ReportCell`, then passed with 10 tests, 0 failures, 0 errors after adding the renderer/result DTOs.
- Report grid final verification:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DskipTests package`
  passed across 15 Maven modules.
  `docker compose up -d --build backend`
  rebuilt the backend image and started `fool-service-backend-1`.
  `docker compose ps --all`
  showed backend, frontend, MySQL, and Redis running.
  `docker compose logs --tail=120 backend`
  showed `Started Application in 2.124 seconds`.
  `curl --retry 5 --retry-connrefused --retry-delay 2 -i --max-time 10 http://localhost:8080/test`
  returned HTTP 200 with `[]`.
  `curl -I --max-time 10 http://localhost:8081/`
  returned HTTP 200 from nginx.
  `python scripts/check_repo_harness.py`
  passed.
  `git diff --check`
  passed.

# Risks

- Static subtotal insertion parity now covers single-level row and column subtotal cells; deeper multi-level subtotal combinations still need parity tests.
- Report source adapters, `ReportFactory`, and query/export integration around the rendered report grid are not migrated yet.
- Existing Maven warning remains: duplicate `spring-jdbc` dependency in `fool-dao/pom.xml`.

# Follow-ups

- Complete report static subtotal parity, source adapters, and export/query integration.
- Continue event runtime behavior or app-management installer side effects from the remaining migration list.

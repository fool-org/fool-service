# Prompt

Continue the active goal:

1. Bring the environment up with Docker.
2. Complete migration against `../FoolFrame`.
3. Use Vue for the frontend.

# Scope

This continuation migrates the direct SQL executor behavior from `../FoolFrame/src/Server/SCPB03 -Soway.DB.Manage/SqlCon.cs`.

# Changes

- Added `SqlResultTable` as a Java result-table wrapper for legacy `GetTable` behavior.
- Added `SqlExecutionGateway` as the persistence boundary for raw SQL query and update execution.
- Added `SqlCon` with legacy-compatible methods:
  - `getTable(String sql)`
  - `exuteSqls(String[] sqls)`
  - `excuteSql(String sql)`
- Added `JdbcSqlExecutionGateway` as the Spring `@Repository` adapter using `JdbcTemplate` and `TransactionTemplate`.
- Added tests for:
  - result table row preservation,
  - single SQL affected-row return,
  - transactional batch execution,
  - legacy `false` return when a batch transaction fails,
  - Spring repository exposure.
- Left mixed migration parity docs and README status updates unstaged for a separate status commit.

# Validation

- Red compile/test step:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-db-manage -am -DskipTests test`
  failed first on missing SQL execution migration classes.
- Green focused test:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-db-manage -Dtest=DbManageMigrationTest test`
  passed with 14 tests, 0 failures, 0 errors.
- Full backend build:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DskipTests package`
  passed across 15 modules.
- Docker backend rebuild:
  `docker compose up -d --build backend`
  rebuilt the backend image and restarted the backend container.

# Runtime Evidence

- `docker compose ps --all` showed backend and frontend running, with MySQL and Redis healthy.
- Backend logs showed Spring Boot started with the `docker` profile on port 8080.
- `curl --retry 5 --retry-connrefused --retry-delay 2 -i --max-time 10 http://localhost:8080/test` returned HTTP 200 with `[]`.
- `curl -I --max-time 10 http://localhost:8081/` returned HTTP 200.

# Risks

- The remaining `SCPB03` work is now the old not-implemented operational paths: database creation/conversion and carry-forward operations.
- Existing Maven warning remains: duplicate `spring-jdbc` dependency in `fool-dao/pom.xml`.

# Follow-ups

- Finish the remaining `SCPB03` operational stubs or continue runtime parity work in `SCPB09-SOWAY.EVENT`.

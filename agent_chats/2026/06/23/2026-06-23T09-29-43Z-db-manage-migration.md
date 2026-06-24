# Prompt

Continue the active goal:

1. Bring the environment up with Docker.
2. Complete migration against `../FoolFrame`.
3. Use Vue for the frontend.

# Scope

This slice migrates the base behavior from `../FoolFrame/src/Server/SCPB03 -Soway.DB.Manage` into `fool-service`.

# Changes

- Added `fool-db-manage` as a Maven reactor module.
- Added legacy DB-management entities for `DB_App`, `DB_AppDB`, `DS_DataSourceSet`, and `WorkDataBase`.
- Added data-source key resolution, SQL Server-style working-database connection-string rendering, legacy DES password payload encryption/decryption, and old hex SQL byte literal helpers.
- Wired `fool-db-manage` into `business-application`.
- Used the Docker seed schema added in the runtime slice.
- Left mixed migration parity docs and README status updates unstaged for a separate status commit.

# Validation

- Red test first:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-db-manage test`
  failed on missing migration classes.
- Green focused test:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-db-manage test`
  passed with 5 tests, 0 failures, 0 errors.
- Full backend build:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DskipTests package`
  passed across 13 modules.
- Docker backend rebuild:
  `docker compose up -d --build backend`
  built the 13-module jar and restarted backend.
- Harness:
  `python scripts/check_repo_harness.py`
  passed.
- Whitespace:
  `git diff --check`
  passed with no output.

# Runtime Evidence

- `docker compose ps` showed backend, frontend, MySQL, and Redis running; MySQL and Redis were healthy.
- `curl -i --max-time 10 http://localhost:8080/test` returned HTTP 200 with `[]`.
- `curl -I --max-time 10 http://localhost:8081/` returned HTTP 200.
- `SHOW TABLES` in `car_wash` included:
  `DB_App`, `DB_AppDB`, `DS_DataSourceSet`, `SW_APPLICATION`, `SW_STOREDB`, `WorkDataBase`, and `market_order`.
- Backend logs showed Spring Boot started with the `docker` profile on port 8080.

# Risks

- Existing Maven warning remains: duplicate `spring-jdbc` dependency in `fool-dao/pom.xml`.
- `SCPB03` direct SQL execution/write factory paths are not fully implemented yet: `SqlCon`, `WorkDataBaseFactory.Create/Save/Delete`, and database creation/conversion operations.
- Full backend `mvn test` remains broader than this slice and still depends on database/test-environment cleanup.

# Follow-ups

- Complete remaining `SCPB03` factory CRUD/runtime SQL adapter behavior.
- Continue migration for `SCPB09-SOWAY.EVENT` and `SWRPT01-Soway.Report`.
- Extend complete schema and DB-backed tests once migrated modules expose runtime endpoints.

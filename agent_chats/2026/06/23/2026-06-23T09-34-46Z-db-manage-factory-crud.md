# Prompt

Continue the active goal:

1. Bring the environment up with Docker.
2. Complete migration against `../FoolFrame`.
3. Use Vue for the frontend.

# Scope

This continuation completes the `WorkDataBaseFactory` list/create/save/delete part of `../FoolFrame/src/Server/SCPB03 -Soway.DB.Manage/WorkDataBaseFactory.cs`.

# Changes

- Added `WorkingDatabaseRepository` as the persistence contract for `WorkDataBaseFactory`.
- Added `WorkDataBaseFactory` as a Spring `@Service`.
- Added `JdbcWorkingDatabaseRepository` as the runtime adapter over `WorkDataBase`, `DB_AppDB`, and `DB_App`.
- Added encrypted-password helper methods on `WorkingDatabase`.
- Added tests for:
  - app-scoped and global database listing order,
  - create next-code assignment and password encryption,
  - duplicate create conflict,
  - duplicate save conflict,
  - save password re-encryption,
  - delete by `DBNo`,
  - Spring bean exposure.
- Left mixed migration parity docs unstaged for a separate status commit.

# Validation

- Red test:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-db-manage test`
  failed first on missing `WorkingDatabaseRepository`, then failed on missing `@Service` for `WorkDataBaseFactory`.
- Green focused test:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-db-manage test`
  passed with 11 tests, 0 failures, 0 errors.
- Full backend build:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DskipTests package`
  passed across 13 modules.
- Docker backend rebuild:
  `docker compose up -d --build backend`
  rebuilt the backend image and restarted the backend container.
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

- The remaining `SCPB03` gap is now the raw SQL executor and old not-implemented operational methods: database creation, conversion, and carry-forward.
- Existing Maven warning remains: duplicate `spring-jdbc` dependency in `fool-dao/pom.xml`.

# Follow-ups

- Continue with `SCPB09-SOWAY.EVENT` or finish the remaining `SCPB03` operational stubs.

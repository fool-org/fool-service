# Prompt

Continue the active goal:

1. Bring the environment up with Docker.
2. Complete migration against `../FoolFrame`.
3. Use Vue for the frontend.

# Scope

This continuation closes the remaining `SCPB03 -Soway.DB.Manage` operation-surface gap where the legacy code exposed methods that only threw `NotImplementedException`.

# Legacy Source

- `WorkDataBaseFactory.CreateDataBase(WorkingDataBase DataBase)` throws `System.NotImplementedException`.
- `WorkDataBaseFactory.ConvertToAutoDataBase(WorkingDataBase DataBase)` throws `System.NotImplementedException`.
- `WorkDataBaseFactory.CarryForward(WorkingDataBase WorkDataBase, WorkingDataBase DisWorkDataBase, bool IsBFDB)` throws `System.NotImplementedException`.
- `WorkingDataBase.Update()` throws `System.NotImplementedException`.

# Changes

- Added Java operation surfaces on `WorkDataBaseFactory`:
  - `createDatabase(WorkingDatabase database)`
  - `convertToAutoDatabase(WorkingDatabase database)`
  - `carryForward(WorkingDatabase source, WorkingDatabase destination, boolean bulkMaterialDatabase)`
- Added `WorkingDatabase.update()`.
- Each operation throws `UnsupportedOperationException` with a message that names the legacy operation and records the FoolFrame `NotImplementedException` mapping.
- Added a parity test that exercises all four unsupported operation surfaces.
- Left mixed migration parity docs unstaged for a separate status commit.

# Validation

- Red focused test:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-db-manage -Dtest=DbManageMigrationTest test`
  failed on missing `createDatabase`, `convertToAutoDatabase`, `carryForward`, and `WorkingDatabase.update`.
- Green focused test:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-db-manage -Dtest=DbManageMigrationTest test`
  passed with 15 tests, 0 failures, 0 errors.
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

- The legacy methods still do not implement real database creation/conversion/carry-forward behavior because FoolFrame did not contain that behavior.
- Existing Maven warning remains: duplicate `spring-jdbc` dependency in `fool-dao/pom.xml`.

# Follow-ups

- Continue with remaining event runtime parity or replace these unsupported operations with new product behavior if required.

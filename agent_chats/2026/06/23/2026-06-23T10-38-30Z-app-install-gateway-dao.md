# Prompt

Continue the active goal:

1. Bring the environment up with Docker.
2. Complete migration against `../FoolFrame`.
3. Use Vue for the frontend.

# Scope

This continuation adds the first concrete DAO-backed adapter for `AppInstallGateway`, covering application creation while keeping installer side effects explicit until their DAO mappings are migrated.

# Changes

- Added `DaoAppInstallGateway` as a Spring `@Component`.
- `createApplication(ApplicationDefinition app)` now calls `DaoService.create(app)` and returns the same application instance.
- Installer side-effect methods remain explicit `UnsupportedOperationException` paths:
  - `installApplicationModules`
  - `installAuthorizationModules`
  - `createAuthorizedUser`
  - `installUserModules`
  - `prepareAppSystemView`
  - `createMenu`
  - `createRole`
- Added tests for DAO persistence, Spring component exposure, and unsupported side-effect boundaries.
- Updated migration parity docs and README status.

# Validation

- Red focused test:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -Dtest=AppManageMigrationTest test`
  failed on missing `DaoAppInstallGateway`.
- Green focused test:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -Dtest=AppManageMigrationTest test`
  passed with 5 tests, 0 failures, 0 errors.
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

- Only application creation has a concrete DAO adapter. Module installation, authorization/user setup, menu creation, role creation, and app-system view preparation still need concrete adapters.
- Existing Maven warning remains: duplicate `spring-jdbc` dependency in `fool-dao/pom.xml`.

# Follow-ups

- Add concrete DAO mappings for the remaining app installer side effects, or continue event/report runtime parity work.

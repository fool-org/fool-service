# Prompt

Continue the active goal:

1. Bring the environment up with Docker.
2. Complete migration against `../FoolFrame`.
3. Use Vue for the frontend.

# Scope

This slice migrates the create-app installer side-effect flow from `../FoolFrame/src/Server/SCPB08-Soway.AppManage/AppManager.cs`.

# Changes

- Added `AppInstaller` to orchestrate the legacy `CreatApp` side-effect order.
- Added `AppInstallGateway` as the persistence/module-installation port for application creation, module installation, authorized-user creation, view preparation, menu creation, and role creation.
- Added `BootstrapRole` and `BootstrapMenuItem.viewId` so default menus can carry the legacy resolved `ViewID`.
- Preserved the legacy default menu/role expansion from `AppBootstrapPlan.legacyDefaults()`.
- Updated migration parity docs and README status.

# Validation

- Red test:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -DskipTests test`
  failed on missing `AppInstallGateway` and `BootstrapRole`.
- Green compile:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -DskipTests test`
  passed after adding the installer contracts and service.
- Green focused test:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -Dtest=AppManageMigrationTest test`
  passed with 4 tests, 0 failures, 0 errors.
- Full Maven package:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DskipTests package`
  passed across 15 Maven modules.
- Docker and smoke:
  `docker compose up -d --build backend`
  rebuilt the backend image and started `fool-service-backend-1`.
  `docker compose ps --all`
  showed backend, frontend, MySQL, and Redis running.
  `docker compose logs --tail=120 backend`
  showed `Started Application in 1.976 seconds`.
  `curl --retry 5 --retry-connrefused --retry-delay 2 -i --max-time 10 http://localhost:8080/test`
  returned HTTP 200 with `[]`.
  `curl -I --max-time 10 http://localhost:8081/`
  returned HTTP 200 from nginx.
  `python scripts/check_repo_harness.py`
  passed.
  `git diff --check`
  passed.

# Risks

- `AppInstallGateway` is still a port; a concrete DAO-backed implementation is not migrated yet.
- Full `-pl fool-app-manage -am test` still trips existing `fool-dao` database tests inside the Maven container because they try to connect to a local database from inside the container.
- Existing Maven warning remains: duplicate `spring-jdbc` dependency in `fool-dao/pom.xml`.

# Follow-ups

- Implement and verify a concrete installer gateway against the current DAO/schema.
- Continue remaining DB-management operational paths, event recipient expansion, or report source/query/export integration.

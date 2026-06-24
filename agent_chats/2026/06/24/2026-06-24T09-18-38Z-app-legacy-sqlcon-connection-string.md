# AppManage legacy SqlCon connection string parsing

## Prompt

- Continue the active goal: bring the stack up with Docker, migrate parity against `../FoolFrame`, keep the frontend on Vue.
- User also said permission was granted and asked to try again.

## Scope

- Compared `../FoolFrame/src/Server/SCPB05-Soway.Model/SqlCon.cs`, where `SqlCon.ToString()` emits SQL Server connection-string keys such as `Data Source`, `Initial Catalog`, `Integrated Security`, `User ID`, and `Password`.
- Extended the Java AppManage DAO factory parser so legacy `SqlCon.ToString()` strings can route through the existing connection-aware `DaoService` factory instead of requiring only `Url=jdbc...` or bare `jdbc:` strings.
- Kept this slice to parsing/routing compatibility; no transaction or connection-pool behavior was added.

## Changed Files

- `fool-app-manage/src/main/java/org/fool/framework/app/DriverManagerAppDaoServiceFactory.java`
- `fool-app-manage/src/test/java/org/fool/framework/app/AppManageMigrationTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/06/24/2026-06-24T09-18-38Z-app-legacy-sqlcon-connection-string.md`

## Validation

- Red: `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -DfailIfNoTests=false -Dtest=AppManageMigrationTest#driverManagerAppDaoServiceFactoryParsesLegacySqlConStrings test`
  - Failed as expected with `IllegalArgumentException: App database JDBC url is required.`
- Green: `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -DfailIfNoTests=false -Dtest=AppManageMigrationTest#driverManagerAppDaoServiceFactoryParsesLegacySqlConStrings test`
  - Passed.
- Broader AppManage: `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -DfailIfNoTests=false -Dtest=AppManageMigrationTest test`
  - Passed: 35 tests.
- Harness: `python scripts/check_repo_harness.py`
  - Passed.
- Patch hygiene: `git diff --check`
  - Passed.
- Runtime: `docker compose up -d --build`
  - Passed; backend Docker build ran `mvn -DskipTests package` successfully, and frontend image build reused the cached Vue build layer.
- Runtime refresh: `docker compose up -d --force-recreate backend frontend`
  - Passed; backend and frontend containers were recreated.
- Runtime status: `docker compose ps`
  - `backend`, `frontend`, `mysql`, and `redis` were running; MySQL and Redis were healthy.
- Backend smoke: `curl -sf http://localhost:8080/test`
  - Passed; returned seeded order JSON.
- Frontend smoke: `curl -sfI http://localhost:8081/`
  - Passed; returned `HTTP/1.1 200 OK`.

## Downgrades / Risks

- Direct host Maven was not used because the host Java is currently 1.8 and fails this Java 17 project with `invalid target release: 17`; backend test validation used a JDK17 Maven container instead.
- The parser maps legacy SQL Server-style strings into a JDBC SQL Server URL but does not add a SQL Server driver dependency or validate a live SQL Server connection.
- Routed transaction boundaries and pooling remain open AppManage parity work.

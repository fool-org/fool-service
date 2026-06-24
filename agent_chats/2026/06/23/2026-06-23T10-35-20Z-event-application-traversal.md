# Prompt

Continue the active goal:

1. Bring the environment up with Docker.
2. Complete migration against `../FoolFrame`.
3. Use Vue for the frontend.

# Scope

This continuation migrates the orchestration shape of `../FoolFrame/src/Server/SCPB09-SOWAY.EVENT/EventMakeService.cs`: scan applications, traverse each application's databases, process each database context, sleep for one minute, and swallow one cycle's exceptions.

# Changes

- Added `EventApplicationScope` to represent an application system connection and its working database connections.
- Added `EventApplicationCatalog` as the application discovery port.
- Added `ScopedEventRuntime` as the per-application/per-database runtime port.
- Added `EventMakeService` with:
  - `workOnce()` for testable one-cycle traversal,
  - `work()`, `start()`, and `stop()` for the legacy long-running service shape,
  - a default 60,000 ms polling interval,
  - legacy-compatible one-cycle exception swallowing.
- Added `EventRuntimeResult.merge(...)` for aggregating scoped runtime results.
- Added tests for application/database traversal order and the legacy polling interval.
- Updated migration parity docs and README status.

# Validation

- Red focused test:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-event -Dtest=EventMigrationTest test`
  failed on missing `EventApplicationCatalog`, `EventApplicationScope`, and `ScopedEventRuntime`.
- Green focused test:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-event -Dtest=EventMigrationTest test`
  passed with 16 tests, 0 failures, 0 errors.
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

- `EventMakeService` is not registered as a Spring bean yet; this avoids startup failures until concrete application catalog and scoped runtime adapters exist.
- Persisted recipient relation loading and the concrete All-authorized-user source are still ports/test doubles, not full JDBC adapters.
- Existing Maven warning remains: duplicate `spring-jdbc` dependency in `fool-dao/pom.xml`.

# Follow-ups

- Add concrete application catalog/scoped runtime adapters, then complete persisted recipient relation loading and All-authorized-user wiring.

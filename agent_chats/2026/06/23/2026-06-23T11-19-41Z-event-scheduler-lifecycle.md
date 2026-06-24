# Event Scheduler Lifecycle Migration

## Scope

- Compared legacy `../FoolFrame/src/Server/SCPB09-SOWAY.EVENT/EventMakeService.cs`.
- Migrated the Java event scheduler from a plain class into a Spring-managed service with a configurable lifecycle.

## Legacy Mapping

- Legacy `EventMakeService.Start()` creates a background thread running `Work()`.
- Legacy `EventMakeService.Stop()` clears the running flag and joins the worker thread.
- Legacy polling interval is 60 seconds.
- Java already preserved traversal and interval behavior; this slice wires that behavior into Spring lifecycle management.

## Changes

- Marked `EventMakeService` as a Spring `@Service`.
- Marked its `EventApplicationCatalog`/`ScopedEventRuntime` constructor for Spring injection.
- Added `EventSchedulerProperties` with prefix `fool.event.scheduler`.
- Added `EventSchedulerLifecycle`:
  - `SmartLifecycle` component
  - auto-starts only when `fool.event.scheduler.enabled=true`
  - calls `EventMakeService.start()`
  - calls `EventMakeService.stop()` on shutdown
- Enabled `EventSchedulerProperties` in `EventAutoConfigure`.
- Updated README and migration parity docs:
  - `fool-event` Java main files: `42`.
  - remaining event gap now focuses on fuller model metadata based object-query parity.

## TDD Evidence

- Red: `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-event -Dtest=EventMigrationTest test`
  - Failed compiling because `EventSchedulerProperties` and `EventSchedulerLifecycle` did not exist.
- Green: same command passed after adding scheduler properties/lifecycle and Spring service wiring.
  - `Tests run: 30, Failures: 0, Errors: 0, Skipped: 0`

## Verification

- `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DskipTests package`
  - Passed full Maven reactor package.
  - Existing Maven warning remains: duplicate `spring-jdbc` dependency in `fool-dao/pom.xml`.
- `docker compose up -d --build backend`
  - Passed backend image build and container restart.
- `docker compose ps --all`
  - backend and frontend are running; MySQL and Redis are running and healthy.
- `docker compose logs --tail=140 backend`
  - Spring Boot application started successfully with the `docker` profile.
- `curl --retry 5 --retry-connrefused --retry-delay 2 -i --max-time 10 http://localhost:8080/test`
  - HTTP 200, body `[]`.
- `curl -I --max-time 10 http://localhost:8081/`
  - HTTP 200.
- `python scripts/check_repo_harness.py`
  - Passed.
- `git diff --check`
  - Passed.

## Remaining Event Gaps

- Object query parity still uses direct table-name SQL instead of full legacy model metadata expansion.

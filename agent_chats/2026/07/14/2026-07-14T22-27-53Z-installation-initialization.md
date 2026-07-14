# Installation and Initialization Delivery

## Prompt

The existing system-model auto-initialization was not implemented as a
complete flow. Add an overall installation and initialization process and fix
the issue.

## Scope

- Add a startup coordinator for framework system metadata, DDL, and default
  Views.
- Keep application provisioning under the existing `AppInstaller` boundary.
- Enable startup initialization for the Docker application profile while
  keeping library auto-configuration opt-in.
- Make startup recoverable and repeat-safe across partial MySQL DDL and
  nullable legacy relation metadata.

## Changes

- Added `AppInstaller.initializeSystem(...)`, `SystemInitializationResult`,
  configuration properties, and an `ApplicationRunner` wired by
  `AppManageAutoConfigure`.
- Configured the Docker profile to enable `fool.app.initialization`.
- Reused reflective package discovery for model metadata and corrected
  composite `@Id` handling so only a true single generated id may use
  `AUTO_INCREMENT`.
- Made relation-column DDL resume after MySQL duplicate-column error 1060,
  made relation metadata matching null-safe, and repaired drifted code-owned
  property structure without repeated no-op writes.
- Filled required defaults when generated Views are persisted.
- Added focused initialization, metadata-repair, partial-DDL, composite-id,
  package-discovery, and ordering tests.
- Added `docs/installation-and-initialization.md`, completed the matching
  `tasks.md` slice, and updated FoolFrame parity evidence.

## Validation

- `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppInstallerInitializationTest,AppManageMigrationTest,DaoAppInstallGatewayInitializationTest -DfailIfNoTests=false test`
  - Passed: 47 tests, 0 failures, 0 errors.
- `docker compose build backend`
  - Passed: all 16 Maven reactor modules packaged with Java 17.
- `python scripts/check_repo_harness.py`
  - Passed after initialization-specific tests were split from the source-file
    size boundary.
- `python scripts/runtime_doctor.py`
  - Passed all Compose, schema, frontend, backend, auth, View, data, report,
    message, and logout checks.
- `git diff --check`
  - Passed.

The host `mvn` command was not used as final evidence because the host runtime
is Java 8 and reports `invalid target release: 17`; Docker Java 17 is the
repository-supported replacement.

## Runtime Evidence

- Final backend startup log:
  `Fool system initialization complete: models=79, metadata=0, ddl=69, views=114`.
- `GET http://localhost:8080/test` succeeded before and after restart.
- MySQL after initialization and restart:
  - models: 83 rows / 83 distinct classes;
  - Views: 118 rows / 118 distinct names;
  - properties: 478 rows;
  - relations: 13 rows / 0 duplicate relation groups;
  - property rewrites on the second startup: 0.
- `db-migrate` remained `Exited (0)` and backend remained running on port 8080.
- Duplicate relation rows created while exercising earlier failing startup
  attempts were collapsed to their 13 distinct records before final restart
  acceptance.

## Risks

- Automatic startup intentionally does not delete metadata for classes removed
  from code; destructive reconciliation remains an explicit migration task.
- Business model packages are not scanned by default and must be added by an
  application-specific installation plan.
- The Docker profile enables initialization; other deployments must opt in and
  provide connection routing when metadata and data use separate databases.

## Follow-ups

- No required follow-up remains for this task.

## Linked Commits or PRs

- Commit: pending at evidence creation time.
- PR: none.

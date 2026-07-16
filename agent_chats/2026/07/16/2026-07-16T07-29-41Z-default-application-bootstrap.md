# Default Application Bootstrap

## Prompt

Ensure a fresh Docker startup creates the default application, administrator,
menus, and role after database and framework initialization.

## Scope

- Reuse `AppInstaller.createApp(...)` as the single application installation
  boundary.
- Run the configured default application installation after system metadata,
  DDL, and default-View initialization.
- Make the application, store-database relation, application-authorized user,
  menus, role, and authorization relations repeat-safe.
- Keep login credential creation in the prerequisite database migration to
  avoid a reverse dependency from `fool-app-manage` to `fool-auth`.

## Changed Files

- `business-application/src/main/resources/application-docker.yml`
- `docs/installation-and-initialization.md`
- `tasks.md`
- `fool-dao/src/main/java/org/fool/framework/dao/DaoService.java`
- `fool-app-manage/src/main/java/org/fool/framework/app/DaoAppInstallGateway.java`
- `fool-app-manage/src/main/java/org/fool/framework/app/autoconfigure/AppInitializationProperties.java`
- `fool-app-manage/src/main/java/org/fool/framework/app/autoconfigure/AppInitializationRunner.java`
- `fool-app-manage/src/test/java/org/fool/framework/app/AppInstallerInitializationTest.java`
- `fool-app-manage/src/test/java/org/fool/framework/app/DaoAppInstallGatewayInitializationTest.java`

## Validation

- `docker run --rm --network fool-service_default ... mvn -q -pl
  fool-app-manage -am
  -Dtest=AppInstallerInitializationTest,DaoAppInstallGatewayInitializationTest
  test` passed.
- `docker run --rm --network fool-service_default ... mvn -q -pl
  fool-app-manage -am test` passed.
- `docker run --rm --network fool-service_default ... mvn -q -DskipTests
  package` passed and produced the executable backend JAR.
- `python scripts/check_repo_harness.py` and `git diff --check` passed.
- The corrected backend JAR started twice against the existing Docker volume.
  Both starts logged `models=79`, completed the default application install,
  and returned successfully from `GET /test`.
- An isolated first-start smoke test used a new MySQL data volume. MySQL
  entrypoint initialization plus the replayable migration job completed, the
  backend logged `models=79, metadata=86, ddl=69, views=114`, and `GET /test`
  succeeded. Its second backend start logged `metadata=0`.
- Database evidence remained unchanged after the second start:
  one application, one application/database relation, one administrator login,
  one application-authorized administrator link, ten bootstrap menus, one
  administrator role, one role/user relation, ten role/menu relations, and
  eight parent/child menu relations.
- `docker compose ps -a` reported MySQL and Redis healthy, backend/frontend
  running, and `db-migrate` at `Exited (0)`.

## Risks

- The current Docker database id remains `car_wash`; renaming it to
  `fool_system` is a separate migration because runtime scripts, authorization
  resource keys, login payloads, tests, and existing named volumes currently
  reference the old id.

## Follow-ups

- Perform the database-id rename as one explicit migration when requested.

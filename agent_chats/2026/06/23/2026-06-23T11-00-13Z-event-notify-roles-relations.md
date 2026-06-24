# Event NotifyRoles Relation Migration

## Scope

- Compared legacy `../FoolFrame/src/Server/SCPB09-SOWAY.EVENT/EventDefination.cs`.
- Compared legacy auth models:
  - `../FoolFrame/src/Server/SWUA02-SOWAY.ORM.AUTH/Role.cs`
  - `../FoolFrame/src/Server/SWUA02-SOWAY.ORM.AUTH/AuthorizedUser.cs`
- Compared legacy ORM relation rules in:
  - `../FoolFrame/src/Server/SCPB01-Soway.Data/Discription/ORM/MultiType.cs`
  - `../FoolFrame/src/Server/SCPB05-Soway.Model/AssemblyModelFactory.cs`
- Migrated direct `NotifyRoles` relation loading for running event definitions, including role authorized-user expansion.

## Legacy Mapping

- `EventDefination.NotifyRoles` maps:
  - parent table: `SW_EVT_DEF`
  - child table: `SW_APP_AUTH_ROLE`
  - relation table: `SW_APP_AUTH_ROLE_SW_EVT_DEF`
  - child relation column: `SW_APP_AUTH_ROLE_ID`
  - parent relation column: `SW_EVT_DEF_ID`
- `Role` maps:
  - table: `SW_APP_AUTH_ROLE`
  - key: `AUTH_ROLE_ID`
  - name: `AUTH_ROLE_NAME`
- `Role.AuthUsers` maps the reciprocal many-to-many relation:
  - role table: `SW_APP_AUTH_ROLE`
  - authorized user table: `SW_APP_AUTH_USER`
  - relation table: `SW_APP_AUTH_ROLE_SW_APP_AUTH_USER`
  - role relation column: `SW_APP_AUTH_ROLE_ID`
  - authorized-user relation column: `SW_APP_AUTH_USER_ID`
- Runtime message delivery uses `AuthorizedUser.User.UserID`, represented by `SW_APP_AUTH_USER.APP_AUTH_USERID`.

## Changes

- Extended `JdbcEventDefinitionRecipientRelationLoader` to load persisted direct `NotifyRoles`.
- Added role authorized-user expansion through the legacy role/user relation table.
- Preserved shared-role behavior when multiple event definitions reference the same role.
- Added Docker seed schema for:
  - `SW_APP_AUTH_ROLE`
  - `SW_APP_AUTH_ROLE_SW_EVT_DEF`
  - `SW_APP_AUTH_ROLE_SW_APP_AUTH_USER`
- Updated README and migration parity docs:
  - remaining event gaps now exclude direct `NotifyUsers/NotifyRoles` relation loading and role authorized-user expansion.

## TDD Evidence

- Red: `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-event -Dtest=EventMigrationTest test`
  - Failed compiling because `JdbcEventDefinitionRecipientRelationLoader` did not yet expose the role relation SQL seams needed by the new test.
- Green: same command passed after adding role relation loading.
  - `Tests run: 22, Failures: 0, Errors: 0, Skipped: 0`
- Red: same focused command failed for a shared-role case.
  - `jdbcEventDefinitionRecipientRelationLoaderExpandsSharedRolesForEachDefinition expected [role-user-1] but was []`
- Green: same command passed after preserving all `EventRole` instances for a shared role id.
  - `Tests run: 23, Failures: 0, Errors: 0, Skipped: 0`

## Verification

- `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DskipTests package`
  - Passed full Maven reactor package.
  - Existing Maven warning remains: duplicate `spring-jdbc` dependency in `fool-dao/pom.xml`.
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/004-event.sql`
  - Applied updated event/auth relation schema to the current MySQL volume.
- `docker compose up -d --build backend`
  - Passed backend image build and container restart.
- `docker compose ps --all`
  - backend and frontend are running; MySQL and Redis are running and healthy.
- `docker compose logs --tail=120 backend`
  - Spring Boot application started successfully with the `docker` profile.
- `curl --retry 5 --retry-connrefused --retry-delay 2 -i --max-time 10 http://localhost:8080/test`
  - HTTP 200, body `[]`.
- `curl -I --max-time 10 http://localhost:8081/`
  - HTTP 200.
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SHOW COLUMNS FROM SW_APP_AUTH_ROLE; SHOW COLUMNS FROM SW_APP_AUTH_ROLE_SW_EVT_DEF; SHOW COLUMNS FROM SW_APP_AUTH_ROLE_SW_APP_AUTH_USER;"`
  - Confirmed role, event-role relation, and role-authorized-user relation columns.
- `python scripts/check_repo_harness.py`
  - Passed.
- `git diff --check`
  - Passed.

## Remaining Event Gaps

- Concrete application catalog/scoped runtime adapters are still shell ports.
- Persisted department/company recipient relation loading remains.
- Department user graph expansion still needs a concrete auth-table adapter.

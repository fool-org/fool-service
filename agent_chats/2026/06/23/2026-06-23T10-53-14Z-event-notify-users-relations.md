# Event NotifyUsers Relation Migration

## Scope

- Compared legacy `../FoolFrame/src/Server/SCPB09-SOWAY.EVENT/EventDefination.cs`.
- Compared legacy ORM relation rules in:
  - `../FoolFrame/src/Server/SCPB01-Soway.Data/Discription/ORM/MultiType.cs`
  - `../FoolFrame/src/Server/SCPB05-Soway.Model/AssemblyModelFactory.cs`
  - `../FoolFrame/src/Server/SCPB05-Soway.Model/SqlServer/SqlHelper.cs`
- Migrated direct `NotifyUsers` relation loading for running event definitions.

## Legacy Mapping

- `[MultiType]` defaults relation table names to sorted parent/child table names.
- `EventDefination.NotifyUsers` maps:
  - parent table: `SW_EVT_DEF`
  - child table: `SW_APP_AUTH_USER`
  - relation table: `SW_APP_AUTH_USER_SW_EVT_DEF`
  - child relation column: `SW_APP_AUTH_USER_ID`
  - parent relation column: `SW_EVT_DEF_ID`
- Runtime message delivery uses `AuthorizedUser.User.UserID`, represented by `SW_APP_AUTH_USER.APP_AUTH_USERID`.

## Changes

- Added `EventDefinitionRelationLoader` port.
- Added `JdbcEventDefinitionRecipientRelationLoader`.
- Updated `JdbcEventDefinitionRepository` to load running definitions, then attach persisted direct `NotifyUsers`.
- Added Docker seed schema for `SW_APP_AUTH_USER_SW_EVT_DEF`.
- Updated README and migration parity docs:
  - `fool-event` Java main files: `36`.
  - remaining event gaps now exclude direct `NotifyUsers`.

## TDD Evidence

- Red: `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-event -Dtest=EventMigrationTest test`
  - Failed compiling because `EventDefinitionRelationLoader` did not exist.
- Green: same command passed after adding relation loader and repository attach behavior.
  - `Tests run: 21, Failures: 0, Errors: 0, Skipped: 0`

## Verification

- `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DskipTests package`
  - Passed full Maven reactor package.
  - Existing Maven warning remains: duplicate `spring-jdbc` dependency in `fool-dao/pom.xml`.
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/004-event.sql`
  - Applied updated relation schema to the current MySQL volume.
- `docker compose up -d --build backend`
  - Passed backend image build and container start.
- `docker compose ps --all`
  - backend, frontend, MySQL, and Redis running; MySQL/Redis healthy.
- `curl --retry 5 --retry-connrefused --retry-delay 2 -i --max-time 10 http://localhost:8080/test`
  - HTTP 200, body `[]`.
- `curl -I --max-time 10 http://localhost:8081/`
  - HTTP 200.
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SHOW COLUMNS FROM SW_APP_AUTH_USER_SW_EVT_DEF;"`
  - Confirmed `SW_APP_AUTH_USER_ID` and `SW_EVT_DEF_ID`.
- `python scripts/check_repo_harness.py`
  - Passed.
- `git diff --check`
  - Passed.

## Remaining Event Gaps

- Concrete application catalog/scoped runtime adapters are still shell ports.
- Persisted department/role/company recipient relation loading remains.
- Department and role user graph expansion still needs concrete auth-table adapters.

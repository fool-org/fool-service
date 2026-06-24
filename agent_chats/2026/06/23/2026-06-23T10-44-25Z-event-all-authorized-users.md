# Event All Authorized Users Migration

## Scope

- Compared legacy `../FoolFrame/src/Server/SCPB09-SOWAY.EVENT/AllMessageFac.cs` and `MessageFactory.cs`.
- Compared legacy `../FoolFrame/src/Server/SWUA02-SOWAY.ORM.AUTH/AuthorizedUser.cs`.
- Migrated the `MsgNotifyType.All` fallback source from legacy `SW_APP_AUTH_USER` into `fool-event`.

## Changes

- Added `JdbcAuthorizedUserRecipientSource` as a Spring repository and `Supplier<List<EventRecipient>>`.
- Query uses legacy table/column names:
  - `SW_APP_AUTH_USER`
  - `APP_AUTH_USERID`
- Wired `LegacyEventRecipientResolver` as the runtime `EventRecipientResolver`.
- Removed `EmptyEventRecipientResolver` from component scanning so it no longer masks migrated recipient expansion.
- Added Docker seed schema for `SW_APP_AUTH_USER`.
- Updated migration docs and README status for `fool-event` Java main count `34`.

## TDD Evidence

- Red: `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-event -Dtest=EventMigrationTest test`
  - Failed compiling because `JdbcAuthorizedUserRecipientSource` did not exist.
- Green: same command passed after adding the source.
  - `Tests run: 17, Failures: 0, Errors: 0, Skipped: 0`
- Red: added Spring runtime resolver wiring expectation.
  - Failed `legacyRecipientResolverIsTheSpringRuntimeResolver`.
- Green: same command passed after wiring `LegacyEventRecipientResolver` and removing `EmptyEventRecipientResolver` from component scanning.
  - `Tests run: 18, Failures: 0, Errors: 0, Skipped: 0`
- Runtime failure found by Docker:
  - `JdbcAuthorizedUserRecipientSource`: Spring tried a default constructor because the test constructor made constructor selection ambiguous.
- Green after root-cause fix:
  - Added `@Autowired` on the `JdbcTemplate` constructor.
  - Added constructor annotation regression test.
  - `Tests run: 19, Failures: 0, Errors: 0, Skipped: 0`

## Verification

- `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DskipTests package`
  - Passed full Maven reactor package.
  - Existing Maven warning remains: duplicate `spring-jdbc` dependency in `fool-dao/pom.xml`.
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/004-event.sql`
  - Applied the updated seed schema to the current MySQL volume.
- `docker compose up -d --build backend`
  - Passed backend image build and container start after the `@Autowired` fix.
- `docker compose ps --all`
  - backend, frontend, MySQL, and Redis running; MySQL/Redis healthy.
- `curl --retry 5 --retry-connrefused --retry-delay 2 -i --max-time 10 http://localhost:8080/test`
  - HTTP 200, body `[]`.
- `curl -I --max-time 10 http://localhost:8081/`
  - HTTP 200.
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SHOW COLUMNS FROM SW_APP_AUTH_USER;"`
  - Confirmed `APP_AUTH_ID`, `APP_AUTH_USERID`, `APP_AUTH_USERLOGINNAME`, `APP_AUTH_DEP`.
- `python scripts/check_repo_harness.py`
  - Passed.
- `git diff --check`
  - Passed.

## Remaining Event Gaps

- Concrete application catalog/scoped runtime adapters are still shell ports.
- Persisted department/role/user/company recipient relation loading is still not migrated.

# Event NotifyDeps and NotifyCompanies Relation Migration

## Scope

- Compared legacy `../FoolFrame/src/Server/SCPB09-SOWAY.EVENT/EventDefination.cs`.
- Compared legacy event message expansion:
  - `../FoolFrame/src/Server/SCPB09-SOWAY.EVENT/DepMessageFactory.cs`
  - `../FoolFrame/src/Server/SCPB09-SOWAY.EVENT/CompanyMessageFactory.cs`
- Compared legacy auth models:
  - `../FoolFrame/src/Server/SWUA02-SOWAY.ORM.AUTH/Department.cs`
  - `../FoolFrame/src/Server/SWUA02-SOWAY.ORM.AUTH/Company.cs`
  - `../FoolFrame/src/Server/SWUA02-SOWAY.ORM.AUTH/AuthorizedUser.cs`
- Compared legacy ORM relation rules in:
  - `../FoolFrame/src/Server/SCPB01-Soway.Data/Discription/ORM/MultiType.cs`
  - `../FoolFrame/src/Server/SCPB01-Soway.Data/Discription/ORM/ReferToProperyAttrbute.cs`
  - `../FoolFrame/src/Server/SCPB05-Soway.Model/AssemblyModelFactory.cs`
  - `../FoolFrame/src/Server/SCPB01-Soway.Data/Discription/ORM/ORMHelper.cs`
- Migrated direct `NotifyDeps` and `NotifyCompanies` relation loading for running event definitions, including department-user graph expansion.

## Legacy Mapping

- `EventDefination.NotifyDeps` maps:
  - parent table: `SW_EVT_DEF`
  - child table: `SW_APP_AUTH_DEPARTMENT`
  - relation table: `SW_APP_AUTH_DEPARTMENT_SW_EVT_DEF`
  - child relation column: `SW_APP_AUTH_DEPARTMENT_ID`
  - parent relation column: `SW_EVT_DEF_ID`
- `Department` maps:
  - table: `SW_APP_AUTH_DEPARTMENT`
  - key: `APP_DEP_ID`
  - name: `APP_DEP_NAME`
- `Department.Users` uses `[ReferToProperyAttrbute("Department")]` and loads authorized users from `SW_APP_AUTH_USER.APP_AUTH_DEP`.
- `Department.SubDepartments` uses the legacy recursive relation table:
  - relation table: `SW_APP_AUTH_DEPARTMENT_SubDepartments`
  - parent column: `SW_APP_AUTH_DEPARTMENT_SubDepartmentsAPP_DEP_ID`
  - child column: `SW_APP_AUTH_DEPARTMENT_SUBDEPARTMENTS_ITEM`
- `EventDefination.NotifyCompanies` maps:
  - parent table: `SW_EVT_DEF`
  - child table: `SW_APP_AUTH_COMPANY`
  - relation table: `SW_APP_AUTH_COMPANY_SW_EVT_DEF`
  - child relation column: `SW_APP_AUTH_COMPANY_ID`
  - parent relation column: `SW_EVT_DEF_ID`
- `Company.Deps` is the legacy one-to-many relation stored on `SW_APP_AUTH_DEPARTMENT.SW_APP_AUTH_COMPANY_DepsAPP_COR_ID`.

## Changes

- Extended `JdbcEventDefinitionRecipientRelationLoader` to load persisted direct `NotifyDeps`.
- Added recursive department tree materialization and authorized-user expansion.
- Extended the same loader to load direct `NotifyCompanies`, then attach company departments and reuse department expansion.
- Added Docker seed schema for:
  - `SW_APP_AUTH_COMPANY`
  - `SW_APP_AUTH_DEPARTMENT`
  - `SW_APP_AUTH_DEPARTMENT_SubDepartments`
  - `SW_APP_AUTH_DEPARTMENT_SW_EVT_DEF`
  - `SW_APP_AUTH_COMPANY_SW_EVT_DEF`
- Updated README and migration parity docs:
  - remaining event gaps now exclude direct department/company recipient relation loading and department user graph expansion.

## TDD Evidence

- Red: `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-event -Dtest=EventMigrationTest test`
  - Failed compiling because `JdbcEventDefinitionRecipientRelationLoader` did not yet expose the department/company relation SQL templates or test constructor.
- Green: same command passed after adding department/company relation loading and recursive department expansion.
  - `Tests run: 25, Failures: 0, Errors: 0, Skipped: 0`

## Verification

- `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DskipTests package`
  - Passed full Maven reactor package.
  - Existing Maven warning remains: duplicate `spring-jdbc` dependency in `fool-dao/pom.xml`.
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/004-event.sql`
  - Applied updated event/auth graph schema to the current MySQL volume.
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
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SHOW COLUMNS FROM SW_APP_AUTH_DEPARTMENT; SHOW COLUMNS FROM SW_APP_AUTH_DEPARTMENT_SW_EVT_DEF; SHOW COLUMNS FROM SW_APP_AUTH_DEPARTMENT_SubDepartments; SHOW COLUMNS FROM SW_APP_AUTH_COMPANY; SHOW COLUMNS FROM SW_APP_AUTH_COMPANY_SW_EVT_DEF;"`
  - Confirmed department, recursive department, company, event-department relation, and event-company relation columns.
- `python scripts/check_repo_harness.py`
  - Passed.
- `git diff --check`
  - Passed.

## Remaining Event Gaps

- Concrete application catalog/scoped runtime adapters are still shell ports.

# Legacy Auth Role Department Relation

## Prompt

Continue the Docker/FoolFrame/Vue migration goal with timely atomic commits.

## Scope

- Added the legacy role-department many-to-many table used by
  `Role.AuthDeps` and `Department.DefaultRoles`.
- Added the app-install Java mapping and mapping test coverage.
- Updated the migration parity document.

## Changed Files

- `docker/mysql/init/004-event.sql`
- `fool-app-manage/src/main/java/org/fool/framework/app/AuthRoleDepartmentRelation.java`
- `fool-app-manage/src/test/java/org/fool/framework/app/AppManageMigrationTest.java`
- `docs/migration/foolframe-parity.md`

## Validation

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-app-manage -am -Dtest=AppManageMigrationTest#mapsApplicationAndStoreDatabaseToLegacyTables test`
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/004-event.sql`
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SHOW COLUMNS FROM SW_APP_AUTH_DEPARTMENT_SW_APP_AUTH_ROLE;"`
- `python scripts/check_repo_harness.py`
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
- `docker compose ps`

## Skipped Checks

- No full role/department permission UI flow exists yet, so runtime validation
  stayed at schema creation, mapping tests, and existing Docker smoke.

## Risks And Follow-Ups

- Full role-department permission behavior still needs service/UI parity work.

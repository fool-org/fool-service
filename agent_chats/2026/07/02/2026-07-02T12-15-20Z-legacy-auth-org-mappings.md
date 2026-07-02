# Legacy Auth Organization Mappings

## Prompt

Continue the Docker/FoolFrame/Vue migration goal with timely atomic commits.

## Scope

- Added app-install Java mappings for legacy auth company, department, and
  department-subdepartment relation tables.
- Updated migration parity docs and the app-manage mapping test.

## Changed Files

- `fool-app-manage/src/main/java/org/fool/framework/app/AuthCompany.java`
- `fool-app-manage/src/main/java/org/fool/framework/app/AuthDepartment.java`
- `fool-app-manage/src/main/java/org/fool/framework/app/AuthDepartmentSubDepartmentRelation.java`
- `fool-app-manage/src/test/java/org/fool/framework/app/AppManageMigrationTest.java`
- `docs/migration/foolframe-parity.md`

## Validation

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-app-manage -am -Dtest=AppManageMigrationTest#mapsApplicationAndStoreDatabaseToLegacyTables test`
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SHOW COLUMNS FROM SW_APP_AUTH_COMPANY; SHOW COLUMNS FROM SW_APP_AUTH_DEPARTMENT; SHOW COLUMNS FROM SW_APP_AUTH_DEPARTMENT_SubDepartments;"`
- `python scripts/check_repo_harness.py`
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
- `docker compose ps`

## Skipped Checks

- No full auth organization UI/runtime flow exists yet, so this slice stayed at
  schema/mapping and Docker smoke validation.

## Risks And Follow-Ups

- Role-department and department default-role behavior remains a later auth
  parity task.

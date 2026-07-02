# Legacy Auth Table Prefixes

## Prompt

Continue the Docker/FoolFrame/Vue migration goal with timely atomic commits.

## Scope

- Added legacy `ColPreStr` table-prefix metadata to SWUA02 app-manage auth
  table mappings.
- Covered `SW_APP_AUTH_USER`, `SW_APP_AUTH_COMPANY`,
  `SW_APP_AUTH_DEPARTMENT`, `SW_APP_AUTH_MENU`, and `SW_APP_AUTH_ROLE`.
- Extended the focused app-manage migration mapping test.
- Updated the migration parity document.

## Changed Files

- `fool-app-manage/src/main/java/org/fool/framework/app/AuthorizedUser.java`
- `fool-app-manage/src/main/java/org/fool/framework/app/AuthCompany.java`
- `fool-app-manage/src/main/java/org/fool/framework/app/AuthDepartment.java`
- `fool-app-manage/src/main/java/org/fool/framework/app/AuthMenuItem.java`
- `fool-app-manage/src/main/java/org/fool/framework/app/AuthRole.java`
- `fool-app-manage/src/test/java/org/fool/framework/app/AppManageMigrationTest.java`
- `docs/migration/foolframe-parity.md`

## Validation

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-app-manage -am -Dtest=AppManageMigrationTest#mapsApplicationAndStoreDatabaseToLegacyTables test`
- `python scripts/check_repo_harness.py`
- `curl -sS -m 5 http://localhost:8080/test`
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
- `curl -sS -m 5 http://localhost:8081/ | head -5`
- `docker compose ps`

## Skipped Checks

- Runtime behavior is unchanged; this slice only restores metadata.

## Risks And Follow-Ups

- Remaining SWUA02 factory behavior still needs migration.

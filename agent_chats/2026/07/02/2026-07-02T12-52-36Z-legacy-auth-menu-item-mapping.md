# Legacy Auth Menu Item Mapping

## Prompt

Continue the Docker/FoolFrame/Vue migration goal with timely atomic commits.

## Scope

- Added the SWUA02 legacy `MenuItem` scalar table mapping.
- Mapped `SW_APP_AUTH_MENU`, the `AUTH_MENU_` table prefix, and the legacy
  menu scalar columns through `AUTH_MENU_INDEX`.
- Added focused reflection tests for table, column, key, prefix, and generation
  metadata.
- Updated the migration parity document.

## Changed Files

- `fool-auth/src/main/java/org/fool/framework/auth/foolframework/auth/MenuItem.java`
- `fool-auth/src/test/java/org/fool/framework/auth/foolframework/auth/LegacyAuthMenuItemMappingTest.java`
- `docs/migration/foolframe-parity.md`

## Validation

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-auth -am -Dtest=LegacyAuthMenuItemMappingTest test`
- `python scripts/check_repo_harness.py`
- `curl -sS -m 5 http://localhost:8080/test`
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
- `curl -sS -m 5 http://localhost:8081/ | head -5`
- `docker compose ps`

## Skipped Checks

- `SubItems` and `Roles` collection behavior is not in this scalar mapping
  slice.

## Risks And Follow-Ups

- Remaining SWUA02 authorized-user models and menu factory behavior still need
  migration.

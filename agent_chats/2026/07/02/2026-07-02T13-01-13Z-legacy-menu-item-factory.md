# Legacy Menu Item Factory

## Prompt

Continue the Docker/FoolFrame/Vue migration goal with timely atomic commits.

## Scope

- Added transient SWUA02 collection surfaces needed by legacy menu traversal.
- Added typed `MenuItemFactory` top-menu and sub-menu filtering.
- Preserved legacy de-duplication by menu ID and ordering by menu index.
- Updated the migration parity document.

## Changed Files

- `fool-auth/src/main/java/org/fool/framework/auth/foolframework/auth/AuthorizedUser.java`
- `fool-auth/src/main/java/org/fool/framework/auth/foolframework/auth/MenuItem.java`
- `fool-auth/src/main/java/org/fool/framework/auth/foolframework/auth/MenuItemFactory.java`
- `fool-auth/src/main/java/org/fool/framework/auth/foolframework/auth/Role.java`
- `fool-auth/src/test/java/org/fool/framework/auth/foolframework/auth/LegacyMenuItemFactoryTest.java`
- `docs/migration/foolframe-parity.md`

## Validation

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-auth -am -Dtest=LegacyMenuItemFactoryTest test`
- `python scripts/check_repo_harness.py`
- `curl -sS -m 5 http://localhost:8080/test`
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
- `curl -sS -m 5 http://localhost:8081/ | head -5`
- `docker compose ps`

## Skipped Checks

- The C# `dynamic` copy overloads are not migrated in this slice.

## Risks And Follow-Ups

- `AuthoriezedFactory` behavior still needs migration.

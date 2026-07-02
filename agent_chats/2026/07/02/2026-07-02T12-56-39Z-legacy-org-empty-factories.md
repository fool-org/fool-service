# Legacy Org Empty Factories

## Prompt

Continue the Docker/FoolFrame/Vue migration goal with timely atomic commits.

## Scope

- Added SWUA02 `CompanyFactory.getUsers` and `DepFactory.getUsers`.
- Preserved the legacy behavior that both factories return new empty user lists.
- Added focused tests for empty-list and new-list-per-call behavior.
- Updated the migration parity document.

## Changed Files

- `fool-auth/src/main/java/org/fool/framework/auth/foolframework/auth/CompanyFactory.java`
- `fool-auth/src/main/java/org/fool/framework/auth/foolframework/auth/DepFactory.java`
- `fool-auth/src/test/java/org/fool/framework/auth/foolframework/auth/LegacyOrgFactoryTest.java`
- `docs/migration/foolframe-parity.md`

## Validation

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-auth -am -Dtest=LegacyOrgFactoryTest test`
- `python scripts/check_repo_harness.py`
- `curl -sS -m 5 http://localhost:8080/test`
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
- `curl -sS -m 5 http://localhost:8081/ | head -5`
- `docker compose ps`

## Skipped Checks

- `UserFactory`, `AuthoriezedFactory`, and `MenuItemFactory` behavior is left
  for separate slices because it depends on application context or menu
  traversal.

## Risks And Follow-Ups

- Remaining SWUA02 factory behavior still needs migration.

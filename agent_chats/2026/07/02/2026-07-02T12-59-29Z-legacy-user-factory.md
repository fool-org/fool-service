# Legacy User Factory

## Prompt

Continue the Docker/FoolFrame/Vue migration goal with timely atomic commits.

## Scope

- Added SWUA02 `UserFactory`.
- Preserved the legacy constructor payload and empty user-list behavior.
- Added a focused test for stored app payload, empty lists, and new list per
  call.
- Updated the migration parity document.

## Changed Files

- `fool-auth/src/main/java/org/fool/framework/auth/foolframework/auth/UserFactory.java`
- `fool-auth/src/test/java/org/fool/framework/auth/foolframework/auth/LegacyUserFactoryTest.java`
- `docs/migration/foolframe-parity.md`

## Validation

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-auth -am -Dtest=LegacyUserFactoryTest test`
- `python scripts/check_repo_harness.py`
- `curl -sS -m 5 http://localhost:8080/test`
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
- `curl -sS -m 5 http://localhost:8081/ | head -5`
- `docker compose ps`

## Skipped Checks

- The concrete legacy app type is kept opaque until a real auth lookup needs
  the app context.

## Risks And Follow-Ups

- `AuthoriezedFactory` and `MenuItemFactory` behavior still need migration.

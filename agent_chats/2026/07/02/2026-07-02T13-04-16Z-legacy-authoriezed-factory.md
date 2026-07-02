# Legacy Authoriezed Factory

## Prompt

Continue the Docker/FoolFrame/Vue migration goal with timely atomic commits.

## Scope

- Added SWUA02 `AuthoriezedFactory` with the legacy misspelled type and method
  names.
- Preserved opaque app/context payload storage.
- Mapped `getAuthrizedUser` to DAO detail lookup by `User.userId`.
- Updated the migration parity document.

## Changed Files

- `fool-auth/src/main/java/org/fool/framework/auth/foolframework/auth/AuthoriezedFactory.java`
- `fool-auth/src/test/java/org/fool/framework/auth/foolframework/auth/LegacyAuthoriezedFactoryTest.java`
- `docs/migration/foolframe-parity.md`

## Validation

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-auth -am -Dtest=LegacyAuthoriezedFactoryTest test`
- `python scripts/check_repo_harness.py`
- `curl -sS -m 5 http://localhost:8080/test`
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
- `curl -sS -m 5 http://localhost:8081/ | head -5`
- `docker compose ps`

## Skipped Checks

- Routed `App.SysCon` object-context construction is not in this slice.

## Risks And Follow-Ups

- Broader routed authorization loading still needs migration when real app
  context wiring is needed.

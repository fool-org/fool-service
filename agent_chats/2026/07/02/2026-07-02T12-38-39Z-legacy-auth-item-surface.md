# Legacy AuthItem Surface

## Prompt

Continue the Docker/FoolFrame/Vue migration goal with timely atomic commits.

## Scope

- Replaced the empty legacy `AuthItem` stub with the unsupported getter and
  no-op setter surface from `SWUA02-SOWAY.ORM.AUTH/AuthItem.cs`.
- Preserved `NotImplementedException` as the Java
  `UnsupportedOperationException("NotImplementedException")` message.
- Added focused tests for both unsupported getters and no-op setters.
- Updated the migration parity document.

## Changed Files

- `fool-auth/src/main/java/org/fool/framework/auth/foolframework/auth/AuthItem.java`
- `fool-auth/src/test/java/org/fool/framework/auth/foolframework/auth/LegacyAuthItemTest.java`
- `docs/migration/foolframe-parity.md`

## Validation

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-auth -am -Dtest=LegacyAuthItemTest test`
- `python scripts/check_repo_harness.py`
- `curl -sS -m 5 http://localhost:8080/test`
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
- `curl -sS -m 5 http://localhost:8081/ | head -5`
- `docker compose ps`

## Skipped Checks

- No runtime route uses this legacy shell yet.

## Risks And Follow-Ups

- Full SWUA02 factory behavior remains incomplete.

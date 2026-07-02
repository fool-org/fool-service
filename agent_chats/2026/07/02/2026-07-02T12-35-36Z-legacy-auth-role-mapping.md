# Legacy Auth Role Mapping

## Prompt

Continue the Docker/FoolFrame/Vue migration goal with timely atomic commits.

## Scope

- Replaced the empty legacy `Role` stub with scalar table mapping for
  `SWUA02-SOWAY.ORM.AUTH/Role.cs`.
- Mapped `SW_APP_AUTH_ROLE`, `AUTH_ROLE_ID`, and `AUTH_ROLE_NAME`.
- Added focused reflection tests for table, column, key, prefix, and generation
  metadata.
- Updated the migration parity document.

## Changed Files

- `fool-auth/src/main/java/org/fool/framework/auth/foolframework/auth/Role.java`
- `fool-auth/src/test/java/org/fool/framework/auth/foolframework/auth/LegacyAuthRoleMappingTest.java`
- `docs/migration/foolframe-parity.md`

## Validation

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-auth -am -Dtest=LegacyAuthRoleMappingTest test`
- `python scripts/check_repo_harness.py`
- `curl -sS -m 5 http://localhost:8080/test`
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
- `curl -sS -m 5 http://localhost:8081/ | head -5`
- `docker compose ps`

## Skipped Checks

- `Role.AuthUsers`, `Role.AuthDeps`, and `Role.Items` relation behavior remains
  represented by existing app-manage relation mappings instead of adding a
  `fool-auth` dependency on `fool-app-manage`.

## Risks And Follow-Ups

- Full SWUA02 factory behavior remains incomplete.

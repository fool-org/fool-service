# Legacy RoleFactory Shell

## Prompt

Continue the Docker/FoolFrame/Vue migration goal with timely atomic commits.

## Scope

- Added the empty legacy `RoleFactory` shell from
  `SWUA02-SOWAY.ORM.AUTH/RoleFactory.cs`.
- Updated the migration parity document and `fool-auth` Java main file count.

## Changed Files

- `fool-auth/src/main/java/org/fool/framework/auth/foolframework/auth/RoleFactory.java`
- `docs/migration/foolframe-parity.md`

## Validation

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-auth -am -Dtest=LegacyAuthItemTest,LegacyAuthRoleMappingTest test`
- `python scripts/check_repo_harness.py`
- `curl -sS -m 5 http://localhost:8080/test`
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
- `curl -sS -m 5 http://localhost:8081/ | head -5`
- `docker compose ps`

## Skipped Checks

- No direct behavior test was added because the legacy source class is empty.

## Risks And Follow-Ups

- Other SWUA02 factories still need behavior-specific migration.

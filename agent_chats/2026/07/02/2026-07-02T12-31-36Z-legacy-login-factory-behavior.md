# Legacy LoginFactory Behavior

## Prompt

Continue the Docker/FoolFrame/Vue migration goal with timely atomic commits.

## Scope

- Added DAO-backed legacy `LoginFactory` login, register, change-password, and
  update-user behavior.
- Login looks up users by legacy `USER_LOGINNAME` and returns the successful
  user with the plain password, matching the legacy method's returned object.
- Register/change-password persist the legacy hash because the Java DAO does
  not apply `EncryptType.MD5` on writes.
- Extended focused tests for lookup SQL, success/failure password checks,
  registration, password changes, and update delegation.
- Updated the migration parity document.

## Changed Files

- `fool-auth/src/main/java/org/fool/framework/auth/foolframework/auth/LoginFactory.java`
- `fool-auth/src/test/java/org/fool/framework/auth/foolframework/auth/LegacyLoginFactoryTest.java`
- `docs/migration/foolframe-parity.md`

## Validation

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-auth -am -Dtest=LegacyLoginFactoryTest test`
- `python scripts/check_repo_harness.py`
- `curl -sS -m 5 http://localhost:8080/test`
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
- `curl -sS -m 5 http://localhost:8081/ | head -5`
- `docker compose ps`

## Skipped Checks

- No public runtime route calls this legacy factory yet, so runtime validation
  stayed at unit coverage plus existing Docker smoke.

## Risks And Follow-Ups

- Constructor parity is intentionally mapped to `DaoService` instead of the
  legacy `SqlCon` and `ICurrentContextFactory` pair.

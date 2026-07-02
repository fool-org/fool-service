# Legacy Auth User Mapping

## Prompt

Continue the Docker/FoolFrame/Vue migration goal with timely atomic commits.

## Scope

- Added the legacy `SW_AUTH_USER` Java table mapping for
  `SWUA01-SOWAY.ORM.AUTH/User.cs`.
- Added the legacy `Sex` ordinal enum used by the auth user model.
- Added focused reflection test coverage for table, column, key, generation,
  encryption, date, and enum metadata.
- Updated the migration parity document.

## Changed Files

- `fool-auth/src/main/java/org/fool/framework/auth/foolframework/auth/User.java`
- `fool-auth/src/main/java/org/fool/framework/auth/foolframework/auth/Sex.java`
- `fool-auth/src/test/java/org/fool/framework/auth/foolframework/auth/LegacyAuthUserMappingTest.java`
- `docs/migration/foolframe-parity.md`

## Validation

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-auth -am -Dtest=LegacyAuthUserMappingTest test`
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SHOW COLUMNS FROM SW_AUTH_USER;"`
- `python scripts/check_repo_harness.py`
- `curl -sS -m 5 http://localhost:8080/test`
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
- `curl -sS -m 5 http://localhost:8081/ | head -5`
- `docker compose ps`

## Skipped Checks

- Full auth-user CRUD/runtime parity is not implemented in this slice; this
  change is limited to legacy table metadata and live schema compatibility.

## Risks And Follow-Ups

- Remaining SWUA01 surfaces such as deeper role/user behavior still need
  parity work beyond table metadata.

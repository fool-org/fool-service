# Legacy Auth Sex Enum Codes

## Prompt

- Continue the Docker/Vue FoolFrame migration with timely atomic commits.

## Scope

- Migrated auth `Sex` to explicit legacy codes: `Male(0)` and `Female(1)`.
- Kept legacy auth user table mapping unchanged.

## Changed Files

- `fool-auth/src/main/java/org/fool/framework/auth/foolframework/auth/Sex.java`
- `fool-auth/src/test/java/org/fool/framework/auth/foolframework/auth/LegacyAuthUserMappingTest.java`
- `docs/migration/foolframe-parity.md`

## Validation

- Red check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-auth -am -Dtest=org.fool.framework.auth.foolframework.auth.LegacyAuthUserMappingTest#carriesLegacyKeyGenerationAndEncryptionMetadata test`
  failed because `Sex.code()` did not exist.
- Green check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-auth -am -Dtest=org.fool.framework.auth.foolframework.auth.LegacyAuthUserMappingTest#carriesLegacyKeyGenerationAndEncryptionMetadata test`
- Module check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-auth -am test`
- Harness check:
  `python scripts/check_repo_harness.py`
- Whitespace check:
  `git diff --check`
- Runtime smoke:
  `curl -sS -m 5 http://localhost:8080/test`
  `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
  `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list`
  `curl -sS -m 5 http://localhost:8081/`
  `docker compose ps`

## Risks And Follow-Ups

- This slice preserves persisted auth enum value parity only.

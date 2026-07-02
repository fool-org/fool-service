# Legacy Common Enum Codes

## Prompt

- Continue the Docker/Vue FoolFrame migration with timely atomic commits.

## Scope

- Migrated `PropertyType` to explicit legacy codes `0..21`.
- Migrated `EncryptType` to explicit legacy codes:
  `NONE(0)`, `MD5(1)`, and `RADOM_DECS(2)`.
- Kept enum names and existing adapter behavior unchanged.

## Changed Files

- `fool-common/src/main/java/org/fool/framework/common/PropertyType.java`
- `fool-common/src/main/java/org/fool/framework/common/annotation/EncryptType.java`
- `fool-common/src/test/java/org/fool/framework/common/PropertyTypeAdaperTest.java`
- `fool-common/src/test/java/org/fool/framework/common/annotation/ColumnTest.java`
- `docs/migration/foolframe-parity.md`

## Validation

- Red check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-common -Dtest=PropertyTypeAdaperTest,ColumnTest test`
  failed because `PropertyType.code()` and `EncryptType.code()` did not exist.
- Green check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-common -Dtest=PropertyTypeAdaperTest,ColumnTest test`
  ran 6 tests, 0 failures, 0 errors.
- Module check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-common test`
  passed: 39 tests, 0 failures, 0 errors.
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

- This slice preserves common metadata enum values only. It does not change
  reflection or app-install persistence behavior.

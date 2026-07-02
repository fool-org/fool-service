# Legacy Model Type Codes

## Prompt

- Continue the Docker/Vue FoolFrame migration with timely atomic commits.

## Scope

- Migrated `ModelType` to legacy `ModelType` persisted codes:
  `DYNAMIC(0)`, `ABSTRACT_CLASS(1)`, and `ENUM(2)`.
- Covered DAO read/write mapping through the existing enum-code path.
- Updated the Docker `Order` smoke seed to store legacy class model code `0`.

## Changed Files

- `fool-model/src/main/java/org/fool/framework/model/model/ModelType.java`
- `fool-model/src/test/java/org/fool/framework/model/model/ModelTypeMigrationTest.java`
- `fool-model/src/test/java/org/fool/framework/model/service/ModelDataServiceTest.java`
- `docker/mysql/init/006-view.sql`
- `docs/migration/foolframe-parity.md`

## Validation

- Red check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-model -am -Dtest=org.fool.framework.model.model.ModelTypeMigrationTest test`
  failed because `code()` and `ABSTRACT_CLASS` did not exist.
- Green check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-model -am -Dtest=org.fool.framework.model.model.ModelTypeMigrationTest test`
- Module check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-app-manage,fool-view -am test`
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

- Existing Docker volumes initialized before this change can still contain
  `fool_sys_model.model_type = 3` until re-seeded or migrated. The init script
  now matches the legacy code for fresh environments.

# Legacy Enum Code Mapping

## Prompt

- Continue the Docker/Vue FoolFrame migration with timely atomic commits.

## Scope

- Added DAO enum read/write support for legacy enums that expose `code()`.
- Kept ordinal mapping for enums without `code()`.
- Added legacy `ViewType` codes and the missing button-list/grid view values.

## Changed Files

- `fool-dao/src/main/java/org/fool/framework/dao/EnumCode.java`
- `fool-dao/src/main/java/org/fool/framework/dao/FunctionMap.java`
- `fool-dao/src/main/java/org/fool/framework/dao/SqlScriptGenerator.java`
- `fool-dao/src/test/java/org/fool/framework/dao/SqlScriptGeneratorMigrationTest.java`
- `fool-view/src/main/java/org/fool/framework/view/model/ViewType.java`
- `fool-view/src/test/java/org/fool/framework/view/model/ViewTypeMigrationTest.java`
- `docs/migration/foolframe-parity.md`

## Validation

- Red check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-dao -am -Dtest=org.fool.framework.dao.SqlScriptGeneratorMigrationTest test`
  failed because enum insert used ordinal `1` instead of legacy code `20`,
  and enum read returned `null` for code `20`.
- Red check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=org.fool.framework.view.model.ViewTypeMigrationTest test`
  failed because legacy `view_type = 1` mapped to `QueryView` instead of
  `DetailView`.
- Green check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-dao -am -Dtest=org.fool.framework.dao.SqlScriptGeneratorMigrationTest test`
- Green check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=org.fool.framework.view.model.ViewTypeMigrationTest test`
- Module check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am test`
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

- Reflection only recognizes numeric public `code()` methods. Non-numeric
  legacy enum storage remains outside this slice.

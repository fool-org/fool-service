# ColumnAttribute GenerationExp parity

## Scope

- Migrated legacy `ColumnAttribute.GenerationExp` into Java reflective module metadata as `Column.generationExpression`.
- Added normalized `Property.generationExpression` / `generation_expression` metadata storage.
- Added MySQL DDL `DEFAULT (<expression>)` emission for non-identity property columns.
- Kept `DefaultValue` as literal default fallback when no generation expression is present.

## Legacy check

- `../FoolFrame/src/Server/SCPB01-Soway.Data/Discription/ORM/ColumnAttribute.cs` defines `GenerationExp`.
- Searches through `../FoolFrame/src/Server` found `IsAutoGenerate` used for save/insert skip/selection behavior, but no direct consumer of `GenerationExp` outside the attribute definition.
- This slice therefore preserves the metadata and schema-expression surface without adding a separate save-time evaluator.

## Red

- `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=LegacyMysqlDdlGeneratorTest#generateCreateTableUsesLegacyColumnGenerationExpressions -DfailIfNoTests=false test`
  - Failed during `fool-model` test compile because `Property` had no `setGenerationExpression(String)`.

## Green

- `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=LegacyMysqlDdlGeneratorTest#generateCreateTableUsesLegacyColumnGenerationExpressions -DfailIfNoTests=false test`
  - Passed: 1 test.
- `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest#reflectiveModuleSourcePreservesLegacyGenerationExpressions -DfailIfNoTests=false test`
  - Passed: 1 test.
- `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=LegacyMysqlDdlGeneratorTest -DfailIfNoTests=false test`
  - Passed: 8 tests.
- `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest -DfailIfNoTests=false test`
  - Passed: 34 tests.

## Schema

- Applied the same idempotent migration shape to the running Compose MySQL:
  - `ALTER TABLE fool_sys_model_property ADD COLUMN generation_expression varchar(255) DEFAULT NULL AFTER generation_type`
  - Verified `SHOW COLUMNS FROM fool_sys_model_property LIKE 'generation_expression'` returned `generation_expression varchar(255)`.

## Full verification

- Re-ran the full backend gate on the Compose network with `spring.datasource.url` pointed at `mysql:3306/car_wash`:
  - `docker run --rm --network fool-service_default ... maven:3.9-eclipse-temurin-17 mvn ... package`
  - Passed all 15 backend reactor modules in 30.370 s.

## Docker runtime

- Rebuilt the backend image after the generation-expression migration:
  - `docker compose build backend`
  - Completed with `backend  Built`.
- Restarted the Compose backend:
  - `docker compose up -d backend`
  - `mysql` and `redis` were healthy, and `backend` started.
- Smoke checked the running stack from the host:
  - `curl -fsS http://localhost:8080/test`
    - Returned seeded order JSON with order IDs `1` and `2`.
  - `curl -fsS http://localhost:8081/`
    - Returned 399 bytes.
  - `curl -fsS -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
    - Returned 309 bytes.
  - `curl -fsS -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list`
    - Returned 296 bytes.

## Final checks

- `python scripts/check_repo_harness.py`
  - Passed.
- `git diff --check`
  - Passed with no output.
- `git status --short`
  - Confirmed the repository is still in a mixed migration worktree with many pre-existing modified and untracked files; no files were staged.

# ColumnAttribute DefaultValue parity

## Scope

- Migrated legacy `ColumnAttribute.DefaultValue` into Java reflective module metadata.
- Added normalized `Property.defaultValue` / `default_value` metadata storage.
- Added MySQL DDL `DEFAULT '<literal>'` emission for non-identity property columns, with single-quote escaping.
- Left generated-expression behavior as remaining work.

## Red

- `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest#reflectiveModuleSourcePreservesLegacyDefaultValues -DfailIfNoTests=false test`
- Failed during `fool-model` test compile because `Property` had no `setDefaultValue(String)`.

## Green

- `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=LegacyMysqlDdlGeneratorTest#generateCreateTableUsesLegacyColumnDefaultValues -DfailIfNoTests=false test`
  - Passed: 1 test.
- `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest#reflectiveModuleSourcePreservesLegacyDefaultValues -DfailIfNoTests=false test`
  - Passed: 1 test.
- `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=LegacyMysqlDdlGeneratorTest -DfailIfNoTests=false test`
  - Passed: 7 tests.
- `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest -DfailIfNoTests=false test`
  - Passed: 33 tests.

## Schema

- Applied the same idempotent migration shape to the running Compose MySQL:
  - `ALTER TABLE fool_sys_model_property ADD COLUMN default_value varchar(255) DEFAULT NULL AFTER generation_type`
  - Verified `SHOW COLUMNS FROM fool_sys_model_property LIKE 'default_value'` returned `default_value varchar(255)`.

## Full verification

- Plain isolated Maven container package first failed because module tests use datasource defaults pointed at container-local `127.0.0.1:3306`.
- Re-ran the full gate on the Compose network with `spring.datasource.url` pointed at `mysql:3306/car_wash`:
  - `docker run --rm --network fool-service_default ... maven:3.9-eclipse-temurin-17 mvn ... package`
  - Passed all 15 backend reactor modules in 31.832 s.

## Docker runtime

- `docker compose build backend`
  - Passed; image build ran `mvn -DskipTests package` successfully for all 15 backend reactor modules.
- `docker compose up -d backend`
  - Recreated and started the backend service with healthy MySQL/Redis dependencies.
- Smoke checks:
  - `curl http://localhost:8081/` returned a 399-byte frontend body.
  - `curl http://localhost:8080/test` returned seeded order JSON.
  - `curl -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view` returned a 309-byte body.
  - `curl -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list` returned a 296-byte body.

## Final checks

- `python scripts/check_repo_harness.py`
  - Passed.
- `git diff --check`
  - Passed with no output.

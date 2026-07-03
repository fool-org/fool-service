# Property Source Metadata

## Prompt

- Continue the FoolFrame migration with Docker runtime, Vue frontend, and timely atomic commits.

## Scope

- Migrated the legacy `PROPERTY_SOURCE` metadata surface into the Java model and app-install persistence path.
- Kept `inputquery` source-list and owner-context execution behavior open; this change only preserves the metadata needed by that path.

## Changes

- `Property` now carries `source` metadata.
- `AppInstalledProperty.fromProperty` writes that metadata to legacy `SW_SYS_PROPERTY.PROPERTY_SOURCE`.
- Docker MySQL init now creates and upgrades `fool_sys_model_property.source`.
- The running Docker MySQL volume was patched with the same `source` column.
- Migration parity docs record this incremental parity slice.

## Validation

- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-app-manage -am -Dtest=AppManageMigrationTest#appInstalledPropertyPreservesLegacyPropertySource test`
  - Failed with `Property should expose legacy PROPERTY_SOURCE metadata`.
- GREEN: same focused command passed after adding `Property.source` and app-install persistence.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-app-manage -am test`
  - Passed: reactor build success; app-manage test module reported 41 tests, 0 failures, 0 errors.
- `python scripts/check_repo_harness.py`
  - Passed.
- `git diff --check`
  - Passed.
- `docker compose up -d --build backend`
  - Passed: backend image built and container restarted.

## Runtime Evidence

- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "ALTER TABLE fool_sys_model_property ADD COLUMN source text AFTER filter;"`
  - Applied to the running MySQL volume.
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'fool_sys_model_property' AND COLUMN_NAME = 'source';"`
  - Returned `source`.
- `curl -fsS http://localhost:8080/test`
  - Returned seeded order JSON.
- `curl -fsS -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
  - Returned `{"code":0,"message":"success",...}` for `OrderList`.
- `docker compose ps`
  - backend, frontend, MySQL, and Redis were running; MySQL was healthy.

## Risks

- `inputquery` still does not execute source-list/owner-context filtering; only the metadata is preserved now.
- Existing Maven warnings about duplicate `spring-jdbc` dependency declarations remain unrelated.

## Follow-ups

- Use `Property.source` and `VIEW_ITEM_SOURCEEXP` to migrate the remaining `inputquery` source-list/owner-context branch.

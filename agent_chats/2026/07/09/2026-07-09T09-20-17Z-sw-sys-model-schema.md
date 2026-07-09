# 2026-07-09 SW_SYS_MODEL Schema Parity

## Prompt

- Continue the Docker/FoolFrame/Vue migration goal.
- Keep file size under control, reuse existing mapping/test patterns, and avoid
  speculative migration code.

## Scope

- Compared FoolFrame `SCPB05-Soway.Model/Model.cs` `SW_SYS_MODEL` mapping with
  the Docker schema and Java AppInstall mapping.
- Added the missing parent model, id property, default format, model type,
  is-view, default list view, and default item view columns to the Docker
  `SW_SYS_MODEL` bootstrap and idempotent upgrade blocks.
- Mapped those columns in `AppInstalledModel` and added a focused table-mapping
  test instead of growing the larger migration test file.
- Extended `runtime_doctor.py` so Docker schema drift is caught at the same
  runtime evidence surface as the View-first workflow checks.

## Changed Files

- `docker/mysql/init/005-model.sql`
- `fool-app-manage/src/main/java/org/fool/framework/app/AppInstalledModel.java`
- `fool-app-manage/src/test/java/org/fool/framework/app/AppInstalledModelLegacySchemaTest.java`
- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T09-20-17Z-sw-sys-model-schema.md`

## Red Tests

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest#mapsApplicationAndStoreDatabaseToLegacyTables test`
  failed first with `java.lang.NoSuchFieldException: baseModelId`.
- `python scripts/runtime_doctor_test.py` failed first because removing
  `SW_SYS_MODEL	MODEL_PARENT` from the simulated schema still passed.

## Validation

- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/005-model.sql` passed.
- `python scripts/runtime_doctor.py` passed, including Docker compose,
  FH_JAVA `market_symbols`, legacy core schema, auth shell, View/data, report,
  message, and logout checks.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppInstalledModelLegacySchemaTest,AppManageMigrationTest#mapsApplicationAndStoreDatabaseToLegacyTables test` passed.
- `python scripts/runtime_doctor_test.py` passed.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.

## Skipped Or Downgraded Checks

- Broader `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -DfailIfNoTests=false test`
  still fails in pre-existing adjacent `fool-view`
  `DataQueryServiceRunOperationTest.runLegacyUpdateOperationConvertsStaticSetValueScalarTypes`
  with `expected: java.lang.Integer<123> but was: java.lang.Long<123>`.

## Risks

- This completes the explicit legacy `SW_SYS_MODEL` column slice found in
  FoolFrame's model mapping. It does not add arbitrary Java classpath
  dependency enumeration or new runtime business surfaces.

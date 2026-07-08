# AppInstall Model Operations

## Prompt

Continue the Docker/Vue/FoolFrame migration with reuse and atomic commits.

## Scope

- Added focused coverage for module-source model operation installation.
- Persisted `Model.operations` to legacy `SW_SYS_OPERATION`.
- Persisted operation commands to legacy `SW_SYS_COMMANDS`.
- Backfilled source operation and command ids after DAO create calls.
- Left operation params untouched because the current Java model has no
  source `OperationParam` type.

## Changed Files

- `fool-app-manage/src/main/java/org/fool/framework/app/DaoAppInstallGateway.java`
- `fool-app-manage/src/test/java/org/fool/framework/app/DaoAppInstallGatewayOperationTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Red: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-app-manage -am -Dtest=DaoAppInstallGatewayOperationTest test`
  failed with expected operation/command records missing.
- Green: same focused command passed after implementation.
- Green: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-app-manage -am -Dtest=DaoAppInstallGatewayOperationTest,AppManageMigrationTest#daoAppInstallGatewayPersistsLegacyDefaultViewsForModels test`
  passed.
- Green: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-app-manage -am test`
  passed.

## Skipped Checks

- Frontend tests/build were not run because this slice changes backend
  app-install metadata persistence only.

## Risks

- Legacy `SW_SYS_OPERATION_PARAM` remains unmigrated until the Java model has a
  source operation-param type to persist.

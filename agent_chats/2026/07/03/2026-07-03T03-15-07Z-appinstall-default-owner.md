# AppInstall Default Owner

## Prompt

- Continue the FoolFrame migration with Docker runtime, Vue frontend, and timely atomic commits.

## Scope

- Tightened legacy `MODEL_DEFAULTOWNER` app-install parity for newly installed module-source models.
- Kept broader AppInstall transaction and routed-connection behavior out of scope.

## Changes

- `DaoAppInstallGateway.installModuleSource` now runs a model-owner backfill pass after all module-source models have been created and assigned `MODEL_ID` values.
- Child models with `Model.owner` now get `AppInstalledModel.defaultOwnerId` set to the installed parent model id and are saved back to `SW_SYS_MODEL`.
- `AppManageMigrationTest` now verifies the backfill and records the saved model.
- Migration parity docs now list model default-owner id backfill as migrated AppManage behavior.

## Validation

- RED: `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -DfailIfNoTests=false -Dtest=AppManageMigrationTest#daoAppInstallGatewayBackfillsLegacyDefaultOwnerAfterModelIdsExist test`
  - Failed before implementation with `expected:<5000> but was:<null>`.
- GREEN: same focused command passed after implementation.
- AppManage regression: `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -DfailIfNoTests=false -Dtest=AppManageMigrationTest test`
  - Passed with 41 tests.

## Skipped Checks

- Frontend tests were not rerun because this change did not touch `frontend/`.

## Risks

- The backfill uses `DaoService.save`, so existing save/update semantics still control the exact SQL shape.

## Follow-ups

- Continue AppInstall transaction-boundary and routed-connection parity.

# AppInstall Default View Links

## Prompt

Continue the FoolFrame migration while keeping View metadata ahead of data and
avoiding concrete business DTO binding.

## Scope

- Backfilled source `Model.id` and `Property.id` from legacy
  `SW_SYS_MODEL` / `SW_SYS_PROPERTY` install records.
- Made generated default View items carry their source `Property`.
- Persisted generated View item `VIEW_ITEM_PROPERTY` ids.
- Persisted generated list View operations through `SW_SYS_VIEW_OPERATION`.
- Persisted command operation metadata through `SW_SYS_OPERATIONVIEW` and
  linked `SW_SYS_VIEW_OPERATION.SW_VIEW_OPERATION_MODELOPERATION`.

## Changed Files

- `fool-app-manage/src/main/java/org/fool/framework/app/DaoAppInstallGateway.java`
- `fool-app-manage/src/test/java/org/fool/framework/app/AppManageMigrationTest.java`
- `fool-view/src/main/java/org/fool/framework/view/service/LegacyAutoViewFactory.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Red: focused AppManage tests failed on missing model id backfill, missing
  default View operations, and missing operation persistence.
- Green: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-app-manage -am -Dtest=AppManageMigrationTest#daoAppInstallGatewayPersistsLegacyModuleSourceProperties+daoAppInstallGatewayPersistsLegacyDefaultViewsForModels+daoAppInstallGatewayRoutesLegacyMetadataAndDdlToSeparateConnections test`
  passed.
- Green: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -Dtest=LegacyAutoViewFactoryTest test`
  passed.
- Green: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-app-manage -am test`
  passed.

## Skipped Checks

- Frontend tests/build were not run because this slice changes backend
  app-install metadata persistence only.

## Risks

- Custom non-default OperationView params are still not generated here; the
  legacy default auto-view factory does not create params.

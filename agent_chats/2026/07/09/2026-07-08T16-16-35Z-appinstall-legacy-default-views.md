# AppInstall Legacy Default Views

## Prompt

Keep the migration focused on View-first rendering and avoid binding View/data
flow to concrete business DTOs.

## Scope

- Changed `DaoAppInstallGateway.installDefaultViews` so generated default
  detail/list Views are persisted as legacy app-install records in
  `SW_SYS_VIEW` and `SW_SYS_VIEW_ITEM`.
- Preserved installed View model/default/detail metadata through `VIEW_MODEL`
  and `VIEW_DEFAULT`.
- Persisted legacy item read-only and edit-type values for generated View
  items.
- Made `LegacyAutoViewFactory` emit `ItemEditType.ReadOnly` for list items and
  `ItemEditType.TextBox` for editable detail items.

## Changed Files

- `fool-app-manage/src/main/java/org/fool/framework/app/DaoAppInstallGateway.java`
- `fool-app-manage/src/test/java/org/fool/framework/app/AppManageMigrationTest.java`
- `fool-view/src/main/java/org/fool/framework/view/service/LegacyAutoViewFactory.java`
- `fool-view/src/test/java/org/fool/framework/view/service/LegacyAutoViewFactoryTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Red: focused AppInstall test failed before implementation with
  `ClassCastException: org.fool.framework.view.model.View cannot be cast to
  org.fool.framework.app.AppInstalledView`.
- Green: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -Dtest=LegacyAutoViewFactoryTest test`
  passed.
- Green: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-app-manage -am -Dtest=AppManageMigrationTest#daoAppInstallGatewayPersistsLegacyDefaultViewsForModels+daoAppInstallGatewayRoutesLegacyMetadataAndDdlToSeparateConnections test`
  passed.
- Green: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-app-manage -am test`
  passed.

## Skipped Checks

- Frontend tests/build were not run because this slice changes backend
  app-install metadata persistence only.

## Risks

- Generated default View items still do not bind `VIEW_ITEM_PROPERTY` to
  installed property ids, and generated View operations are not persisted in
  this slice.

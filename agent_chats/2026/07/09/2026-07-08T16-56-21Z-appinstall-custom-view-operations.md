# AppInstall Custom View Operations

## Prompt

Continue the Docker/Vue/FoolFrame migration with small reusable AppInstall
parity slices.

## Scope

- Added `AppModuleDefinition.views` as the module-source carrier for custom
  non-default Views.
- Added `AppModuleSource.getViews(...)` and wired `StaticAppModuleSource` to
  return module Views.
- Reused `DaoAppInstallGateway.persistView(...)` during module-source
  installation so custom View operations persist through legacy
  `SW_SYS_VIEW_OPERATION` / `SW_SYS_OPERATIONVIEW`.
- Updated migration parity and task state.

## Changed Files

- `fool-app-manage/src/main/java/org/fool/framework/app/AppModuleDefinition.java`
- `fool-app-manage/src/main/java/org/fool/framework/app/AppModuleSource.java`
- `fool-app-manage/src/main/java/org/fool/framework/app/StaticAppModuleSource.java`
- `fool-app-manage/src/main/java/org/fool/framework/app/DaoAppInstallGateway.java`
- `fool-app-manage/src/test/java/org/fool/framework/app/DaoAppInstallGatewayOperationTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Red: focused Maven test failed because `AppModuleDefinition.setViews(...)`
  did not exist.
- Green: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-app-manage -am -Dtest=DaoAppInstallGatewayOperationTest test`
  passed after wiring module-source Views to the existing View persistence path.
- Module: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-app-manage -am test`
  passed.
- Harness: `python scripts/check_repo_harness.py` passed.

## Runtime Artifacts

None. This is AppInstall metadata persistence only.

## Risks

- Custom View result/default references still depend on source View ordering
  and already assigned ids; this slice proves View operation metadata, not a
  full View graph dependency resolver.

## Follow-ups

- Continue remaining AppInstall parity around routed-connection transaction
  behavior, reflective relation metadata, and deeper DBMaps behavior.

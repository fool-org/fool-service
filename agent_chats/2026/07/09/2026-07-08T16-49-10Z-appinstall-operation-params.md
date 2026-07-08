# AppInstall Operation Params

## Prompt

Continue the Docker/Vue/FoolFrame migration while keeping changes small and
reusing existing code.

## Scope

- Added source `OperationParam` metadata beside existing `OperationCommand`.
- Persisted module-source operation params to legacy
  `SW_SYS_OPERATION_PARAM` during AppInstall.
- Backfilled installed param ids and owner operation ids onto source params.
- Updated migration parity and repo task state.

## Changed Files

- `fool-model/src/main/java/org/fool/framework/model/model/Operation.java`
- `fool-model/src/main/java/org/fool/framework/model/model/OperationParam.java`
- `fool-app-manage/src/main/java/org/fool/framework/app/DaoAppInstallGateway.java`
- `fool-app-manage/src/test/java/org/fool/framework/app/DaoAppInstallGatewayOperationTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Red: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-app-manage -am -Dtest=DaoAppInstallGatewayOperationTest test`
  failed because `org.fool.framework.model.model.OperationParam` was missing.
- Green: same focused command passed after adding the source model and install
  mapping.

## Runtime Artifacts

None. This is AppInstall metadata persistence only.

## Risks

- Existing installed operations still short-circuit as before; this slice adds
  params for newly installed module-source operations only.

## Follow-ups

- Continue remaining AppInstall parity around custom non-default View operation
  metadata and routed-connection transaction behavior.

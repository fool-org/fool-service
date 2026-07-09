# Remaining Work Command And AppInstall Cleanup

## Prompt

- Continue the FoolFrame migration, stay focused on the goal, maximize reuse,
  avoid concrete DTO shortcuts, and keep commits atomic.

## Scope

- Recheck the remaining-work wording against current code, tests, and
  `../FoolFrame` before choosing the next migration slice.
- Remove stale remaining-work items that are already covered or are not
  server-side runtime behavior in FoolFrame.

## Evidence

- `../FoolFrame/src/Server/SCPB05-Soway.Model/CommandsType.cs` defines command
  ordinals 0 through 8.
- `fool-model/src/main/java/org/fool/framework/model/model/CommandsType.java`
  preserves those ordinals, with focused enum coverage in
  `OperationEnumMigrationTest`.
- `../FoolFrame/src/Server/SCPB05-Soway.Model/ModelMethodContext.cs` executes
  `SetValue`, property/list/out-model execution, `Filter`, `SetParamValue`,
  and `SetConStrValue`. It calls `NotifyPropertyCanSet` for `SetAccess`, and
  has no `SetSource` execution branch.
- `ModelDataService` covers the migrated mutating command slices through the
  shared trigger/runoperation command paths; `SetAccess`/`SetSource` do not
  require Java persistence behavior.
- `DaoAppInstallGatewayTransactionTest` covers routed DAO transaction
  boundaries; `AppManageMigrationTest` and the module map cover the listed
  reflection, relation, Column metadata, enum metadata, and install-record
  slices.

## Changed Files

- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=OperationEnumMigrationTest test`
  passed: 5 tests, 0 failures, 0 errors.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=DaoAppInstallGatewayTransactionTest test`
  passed: 1 test, 0 failures, 0 errors.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.

## Risks And Follow-Ups

- No production code changed in this slice.
- Remaining migration work is now narrower: deeper DBMaps runtime/query
  behavior, arbitrary classpath dependency enumeration, richer collection
  runtime state, richer external-model edge cases, and model-runtime routed
  connection transaction behavior.

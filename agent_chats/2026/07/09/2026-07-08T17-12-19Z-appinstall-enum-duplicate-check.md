# AppInstall Enum Duplicate Check

## Prompt

Continue the FoolFrame migration with small reusable AppInstall parity fixes.

## Scope

- Reused `AppInstalledEnumValue.fromEnumValue(...)` for both duplicate checks
  and inserts into legacy `SW_SYS_EMUNVALUE`.
- Ensured `EMUN_VALUE` duplicate-check parameters use the parsed legacy integer
  value instead of the source `EnumValue.value` string.
- Updated migration parity and task state.

## Changed Files

- `fool-app-manage/src/main/java/org/fool/framework/app/DaoAppInstallGateway.java`
- `fool-app-manage/src/test/java/org/fool/framework/app/DaoAppInstallGatewayOperationTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Red: focused Maven test failed with
  `expected: java.lang.Integer<0> but was: java.lang.String<0>`.
- Green: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-app-manage -am -Dtest=DaoAppInstallGatewayOperationTest test`
  passed.
- Module: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-app-manage -am test`
  passed.
- Harness: `python scripts/check_repo_harness.py` passed.

## Runtime Artifacts

None. This is AppInstall metadata persistence behavior.

## Risks

- Non-numeric enum values still fail during legacy `EMUN_VALUE` parsing, which
  matches the current persisted integer model for this table.

## Follow-ups

- Continue AppInstall parity on routed transaction behavior and remaining
  reflective relation metadata.

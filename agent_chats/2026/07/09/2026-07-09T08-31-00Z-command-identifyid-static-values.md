# Command IdentifyId Static Value Parity

## Prompt

Continue the Docker/Vue/FoolFrame migration, keep changes atomic, maximize
reuse, avoid business DTO binding, and control file size.

## Scope

- Fixed shared operation-command static value conversion for legacy
  `IdentifyId` properties.
- Reused `OperationCommandValueResolver`, which is shared by `runoperation`
  and `ModelDataService` trigger command execution.

## Changes

- `OperationCommandValueResolver` now converts `PropertyType.IdentifyId`
  static `$...` command values to Java `Long`.
- Added
  `OperationCommandValueResolverTest#resolvesLegacyIdentifyIdStaticValuesAsLong`.
- Updated `tasks.md` and `docs/migration/foolframe-parity.md`.

## Validation

- Red test before implementation:
  `OperationCommandValueResolverTest#resolvesLegacyIdentifyIdStaticValuesAsLong`
  failed with `expected: java.lang.Long<1000> but was: java.lang.String<1000>`.
- Focused green:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=OperationCommandValueResolverTest#resolvesLegacyIdentifyIdStaticValuesAsLong -DfailIfNoTests=false test`
  passed.
- Module gate:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -DfailIfNoTests=false test`
  passed, 78 tests.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.

## Runtime Evidence

- `docker compose ps` showed backend and frontend running, with MySQL and
  Redis healthy.

## Risks

- This only covers static `$...` command value conversion; broader expression
  grammar remains on the existing shared resolver path.

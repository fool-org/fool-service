# Command Long Static Value Parity

## Prompt

Continue the Docker/Vue/FoolFrame migration, keep changes atomic, maximize
reuse, avoid business DTO binding, and control file size.

## Scope

- Fixed a shared operation-command expression conversion bug for legacy
  `Long` / `ULong` properties.
- Reused `OperationCommandValueResolver`, which is already shared by
  `runoperation` and `ModelDataService` trigger command execution.

## Changes

- `OperationCommandValueResolver` now converts `PropertyType.Long` and
  `PropertyType.ULong` static `$...` command values to Java `Long` instead of
  `Integer`.
- Added
  `OperationCommandValueResolverTest#resolvesLegacyLongStaticValuesAsLong`.
- Updated `tasks.md` and `docs/migration/foolframe-parity.md`.

## Validation

- Red test before implementation:
  `OperationCommandValueResolverTest#resolvesLegacyLongStaticValuesAsLong`
  failed with `expected: java.lang.Long<1000> but was: java.lang.Integer<1000>`.
- Focused green:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=OperationCommandValueResolverTest#resolvesLegacyLongStaticValuesAsLong -DfailIfNoTests=false test`
  passed.
- Module gate:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -DfailIfNoTests=false test`
  passed, 77 tests.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.

## Runtime Evidence

- `docker compose ps` showed backend and frontend running, with MySQL and
  Redis healthy.

## Risks

- Java still has no unsigned 64-bit primitive; `ULong` is represented as
  `Long`, matching the existing model defaults and MySQL `BIGINT` mapping.

# Property Trigger Assembly Parity

## Prompt

Continue the Docker/Vue/FoolFrame migration, keep commits atomic, maximize reuse,
and avoid DTO-first rendering or speculative abstractions.

## Scope

- Compared legacy property trigger execution against `../FoolFrame`.
- Migrated the property-trigger assembly slice in `ModelDataService`.
- Left non-assembly property-trigger base operations as remaining work.

## Changes

- `ModelDataService` now routes property `SET` and collection
  `ItemsAdd`/`ItemsDelete` triggers through a shared property-trigger executor.
- Property triggers with `BaseOperationType.Assebmly` reuse the existing
  `LegacyAssemblyInvoker` and the existing trigger command collection for
  `SetConStrValue` / `SetParamValue`.
- Added `ModelDataServiceTest#saveDataExecutesLegacyPropertySetTriggerAssembly`
  using real `SW_SYS_PROPERTY_TRIGGER` and
  `SW_SYS_PROPERTY_TRIGGER_COMMANDS` metadata.
- Updated `tasks.md` and `docs/migration/foolframe-parity.md`.

## Validation

- Red test before implementation:
  `ModelDataServiceTest#saveDataExecutesLegacyPropertySetTriggerAssembly`
  failed with `expected:<ctor> but was:<null>`.
- Focused green:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=ModelDataServiceTest#saveDataExecutesLegacyPropertySetTriggerAssembly -DfailIfNoTests=false test`
  passed.
- Module gate:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -DfailIfNoTests=false test`
  passed, 74 tests.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.

## Runtime Evidence

- `docker compose ps` showed backend and frontend running, with MySQL and Redis
  healthy.

## Risks

- Property-trigger `CREATE` / `UPDATE` / `DELETE` base operations remain open;
  this change intentionally covers only the assembly path proven by the new
  failing test.
- `invokeDll` plugin loading remains deferred, matching the existing
  `LegacyAssemblyInvoker` boundary.

## Follow-ups

- Continue remaining `SCPB05-Soway.Model` runtime slices: non-assembly
  property-trigger base operations, richer external-model edge cases, richer
  collection state parity, and routed-connection transaction behavior.

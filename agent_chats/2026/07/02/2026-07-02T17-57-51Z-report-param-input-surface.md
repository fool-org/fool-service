# Report Param Input Surface

## Prompt

Continue the active migration goal:

1. Bring the environment up with Docker.
2. Complete migration against `../FoolFrame`.
3. Use Vue for the frontend.
4. Commit atomically and promptly.

## Scope

Small `SWRPT01-Soway.Report` parity slice for legacy `Param` and `ParamInput`
property behavior.

## Changes

- Matched legacy C# `Param` getters (`Name`, `Format`) as unsupported surfaces
  with no-op setters.
- Matched legacy C# `ParamInput` getters (`Param`, `Value`, `Show`) as
  unsupported surfaces with no-op setters.
- Adjusted report migration tests to keep `Report` scalar state separate from
  unsupported parameter/input surfaces.
- Updated `docs/migration/foolframe-parity.md`.

## Validation

- RED:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-report -am -Dtest=ReportMigrationTest#paramAndInputKeepLegacyUnsupportedGetterNoOpSetterSurface test`
  failed with `expected java.lang.UnsupportedOperationException to be thrown`.
- GREEN focused:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-report -am -Dtest=ReportMigrationTest#paramAndInputKeepLegacyUnsupportedGetterNoOpSetterSurface test`
- GREEN module:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-report -am test`

## Runtime Evidence

- Docker stack was already up during the slice:
  backend `0.0.0.0:8080->8080`, frontend `0.0.0.0:8081->80`,
  MySQL healthy on `127.0.0.1:3307`, Redis healthy on `127.0.0.1:6380`.

## Risks

- This intentionally preserves legacy unsupported getter behavior. Code that
  depended on the previous Java DTO behavior was not part of legacy parity.

## Follow-ups

- Remaining report work is still table source adapters and export integration.

## Linked Commits

- Local commit: `feat(report): match legacy param input surfaces`

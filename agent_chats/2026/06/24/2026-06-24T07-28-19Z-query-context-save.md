# QueryContext Save Compatibility Surface

## Prompt

- Continue migrating FoolFrame parity after Docker permission retry.

## Scope

- Compared legacy `SWDQ01-Soway.Query/QueryContext.cs` with Java `QueryContext`.
- Legacy `Save()` is part of the public query context surface but throws `System.NotImplementedException`.
- Added Java `save()` with an explicit unsupported surface to preserve that contract without inventing persistence behavior not present in FoolFrame.

## Changed Files

- `fool-query/src/main/java/org/fool/framework/query/QueryContext.java`
- `fool-query/src/test/java/org/fool/framework/query/QueryContextTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/06/24/2026-06-24T07-28-19Z-query-context-save.md`

## Validation

- Red test first:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-query -am -Dtest=QueryContextTest#saveKeepsLegacyNotImplementedSurface -DfailIfNoTests=false test`
  - Failed compiling because `QueryContext.save()` was missing.
- Focused green:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-query -am -Dtest=QueryContextTest#saveKeepsLegacyNotImplementedSurface -DfailIfNoTests=false test`
- Query package regression:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-query -am -Dtest='org.fool.framework.query.*Test' -DfailIfNoTests=false test`

## Skipped Checks

- Full backend `mvn test` and Compose rebuild were not rerun for this narrow query context compatibility surface.

## Risks And Follow-Ups

- This intentionally does not add saved-query persistence; FoolFrame's corresponding `Save()` method is also unimplemented.

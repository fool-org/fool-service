# Query SelectedQueryTable Compatibility Surface

## Prompt

- Continue migrating FoolFrame parity after Docker permission retry.

## Scope

- Compared legacy `SWDQ01-Soway.Query/SelectedQueryTable.cs` with current Java query models.
- Legacy `Table` getter throws `NotImplementedException`; setter is a no-op.
- Added `SelectedQueryTable` Java compatibility shell with the same unsupported getter/no-op setter surface.

## Changed Files

- `fool-query/src/main/java/org/fool/framework/query/SelectedQueryTable.java`
- `fool-query/src/test/java/org/fool/framework/query/SelectedQueryTableTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/06/24/2026-06-24T07-25-54Z-query-selected-query-table.md`

## Validation

- Red test first:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-query -am -Dtest=SelectedQueryTableTest -DfailIfNoTests=false test`
  - Failed compiling because `SelectedQueryTable` was missing.
- Focused green:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-query -am -Dtest=SelectedQueryTableTest -DfailIfNoTests=false test`
- Query package regression:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-query -am -Dtest='org.fool.framework.query.*Test' -DfailIfNoTests=false test`

## Skipped Checks

- Full backend `mvn test` and Compose rebuild were not rerun for this narrow query compatibility shell.

## Risks And Follow-Ups

- Remaining Query work still includes saved-query/report execution surfaces and richer query-to-view integration.

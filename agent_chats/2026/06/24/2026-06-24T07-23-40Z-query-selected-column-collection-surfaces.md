# Query Selected Column Collection Surfaces

## Prompt

- Continue migrating FoolFrame parity after Docker permission retry.

## Scope

- Compared legacy `SWDQ01-Soway.Query/SelectedColCollection.cs` with the Java `SelectedColumnCollection`.
- Legacy `IsReadOnly` returns `true` even though the collection exposes mutation methods.
- Legacy indexed setter throws `NotImplementedException`.
- Added Java equivalents with `isReadOnly()` and `set(int, SelectedColumn)` preserving the unsupported setter surface.

## Changed Files

- `fool-query/src/main/java/org/fool/framework/query/SelectedColumnCollection.java`
- `fool-query/src/test/java/org/fool/framework/query/SelectedColumnCollectionTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/06/24/2026-06-24T07-23-40Z-query-selected-column-collection-surfaces.md`

## Validation

- Red test first:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-query -am -Dtest=SelectedColumnCollectionTest -DfailIfNoTests=false test`
  - Failed compiling because `isReadOnly()` and `set(int, SelectedColumn)` were missing.
- Focused green:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-query -am -Dtest=SelectedColumnCollectionTest -DfailIfNoTests=false test`
- Query package regression:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-query -am -Dtest='org.fool.framework.query.*Test' -DfailIfNoTests=false test`

## Skipped Checks

- Unqualified `mvn -q -pl fool-query -am -DfailIfNoTests=false test` was attempted but failed in upstream `fool-dao` integration tests because the isolated Maven container could not connect to MySQL at the test datasource host. The query package regression above avoids that unrelated datasource dependency.

## Risks And Follow-Ups

- Remaining Query work still includes saved-query/report execution surfaces and richer query-to-view integration.

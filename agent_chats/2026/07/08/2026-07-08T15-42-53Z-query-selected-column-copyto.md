# Query Selected Column CopyTo

## Prompt

Continue the FoolFrame migration while keeping the View/data workflow detached
from concrete business DTOs and avoiding oversized, speculative changes.

## Scope

- Checked FoolFrame `SWDQ01-Soway.Query/SelectedColCollection.cs`.
- Migrated the legacy `CopyTo(array, arrayIndex)` collection surface for
  selected query columns.
- Left the indexed setter unsupported because FoolFrame throws
  `NotImplementedException` there.

## Changed Files

- `fool-query/src/main/java/org/fool/framework/query/SelectedColumnCollection.java`
- `fool-query/src/test/java/org/fool/framework/query/SelectedColumnCollectionTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Red: `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-query -am -Dtest=SelectedColumnCollectionTest#copyToKeepsLegacyCollectionSurface test` failed because `SelectedColumnCollection` did not expose `copyTo`.
- Green: same focused test after adding `copyTo`.
- Green: `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-query -am -Dtest=SelectedColumnCollectionTest test`.
- Green: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-query -am test`.
- Green: `git diff --check`.
- Green: `python scripts/check_repo_harness.py`.

## Skipped Checks

- A full `fool-query -am test` run without `--network fool-service_default`
  failed with `UnknownHostException: mysql`; the same command passed after
  joining the Compose network.

## Risks

- This only closes the selected-column collection copy surface; broader Query
  saved-report execution, table state, and report-parameter behavior remain in
  the migration backlog.

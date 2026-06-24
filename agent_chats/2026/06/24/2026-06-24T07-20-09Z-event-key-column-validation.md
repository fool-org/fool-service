# Event Key Column Validation

## Prompt

- Continue migrating FoolFrame parity after Docker permission retry.

## Scope

- Compared legacy `SCPB05-Soway.Model.SqlServer.dbContext.GetBySqlCommand` with `JdbcEventObjectQuery`.
- Legacy reads the query result table, computes `SqlHelper.GetKeyCol(model)`, and throws `Can't Gerneration Query Because The Id Column Isn't Included!` when the result schema does not include that key column.
- Migrated `JdbcEventObjectQuery` from row-mapper-only access to metadata-first `ResultSetExtractor` access so it validates the object-id column before mapping matched objects.
- Preserved matched row value capture and object-id string conversion for existing event object-query behavior.

## Changed Files

- `fool-event/src/main/java/org/fool/framework/event/JdbcEventObjectQuery.java`
- `fool-event/src/test/java/org/fool/framework/event/EventMigrationTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/06/24/2026-06-24T07-20-09Z-event-key-column-validation.md`

## Validation

- Red test first:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-event -am -Dtest=EventMigrationTest#jdbcEventObjectQueryRejectsResultsWithoutLegacyKeyColumn -DfailIfNoTests=false test`
  - Failed with `expected java.lang.IllegalStateException to be thrown, but nothing was thrown`.
- Focused green:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-event -am -Dtest=EventMigrationTest#jdbcEventObjectQueryRejectsResultsWithoutLegacyKeyColumn -DfailIfNoTests=false test`
- Event regression:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-event -am -Dtest=EventMigrationTest -DfailIfNoTests=false test`

## Skipped Checks

- Full backend `mvn test` and Compose rebuild were not rerun for this narrow event object-query validation slice; Compose was already rebuilt and smoke-tested earlier in the same migration session.

## Risks And Follow-Ups

- Empty-result schema validation depends on the real JDBC result metadata; the test helper currently models row-backed result metadata.
- Remaining event work still includes deeper dynamic object-query behavior beyond null-model handling, table/id-column resolution, key-column validation, row-value capture, and legacy filter SQL construction.

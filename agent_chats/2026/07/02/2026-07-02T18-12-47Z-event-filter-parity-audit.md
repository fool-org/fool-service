# Event Filter SQL Parity Audit

## Prompt

- Continue the Docker/Vue/FoolFrame migration goal and keep commits atomic.

## Scope

- Audited the remaining `SCPB09-SOWAY.EVENT` object-query note against legacy `SqlHelper.GetQueryCommand` and current `EventSqlHelper` / `JdbcEventObjectQuery`.
- Found the raw legacy DefModel filter SQL construction already migrated and covered; updated parity docs only.

## Changed Files

- `docs/migration/foolframe-parity.md`

## Validation

- Focused event gate: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-event -am -Dtest=EventMigrationTest test`
  - Passed: `Tests run: 41, Failures: 0, Errors: 0, Skipped: 0`.

## Evidence

- Legacy source: `../FoolFrame/src/Server/SCPB09-SOWAY.EVENT/SqlHelper.cs` builds `SELECT * FROM {model.DataTableName} WHERE {def.Filter}`.
- Current tests cover `EventSqlHelper.buildQuerySql`, null-filter empty SQL, resolved model table names, and matched object ID extraction.

## Risks And Follow-Ups

- No production code changed.
- Broader event object materialization and routed runtime behavior remain outside this doc correction.

## Commit

- Local commit subject: `docs(event): clarify filter sql parity`

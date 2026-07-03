# Report ColName Projection

## Prompt

Continue the Docker/FoolFrame/Vue migration. Keep report rendering View-driven:
the page/report should resolve View metadata first, then query View data, without
binding report rows to concrete business DTO fields.

## Scope

- Compared FoolFrame `HandlerMakeReport`: selected report columns use `ColId`
  for the source query column and `ColName` as the output column name.
- Projected selected `ReportCols[].ColId` values into custom `ColName` aliases
  before rendering report cells.
- Reused the existing View row map and View/Model metadata lookup; no new report
  query engine or DTO layer was added.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/api/ReportController.java`
- `fool-view/src/test/java/org/fool/framework/view/api/ReportControllerTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/04/2026-07-03T20-17-06Z-report-colname-projection.md`

## Validation

- Red: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -DfailIfNoTests=false -Dtest=ReportControllerTest#makeReportProjectsColIdValuesToCustomReportColName test`
  failed with `expected:<[BTC-USDT]> but was:<[]>`.
- Green: the same focused test passed after the fix.
- Green: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -DfailIfNoTests=false -Dtest=ReportControllerTest test`.
- Green: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -DfailIfNoTests=false test`.
- Green: `python3 scripts/check_repo_harness.py`.
- Green: `git diff --check`.
- Runtime: `docker compose up -d --build --force-recreate backend`.
- Runtime: backend `http://localhost:8080/api/v1/report/getrpt` returned
  `code=0`, header `Pair`, and populated rows for a ColName+ColId payload.
- Runtime: frontend proxy `http://localhost:8081/api/v1/report/getrpt`
  returned the same `Pair` header and populated rows.

## Skipped Checks

- Frontend tests were not run in this slice because no frontend files changed.

## Risks And Follow-ups

- `SelectedTypeId`, aggregate query semantics, report ordering, export, and
  saved report execution remain migration backlog.

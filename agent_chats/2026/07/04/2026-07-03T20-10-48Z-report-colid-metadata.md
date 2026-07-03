# Report ColId Metadata

## Prompt

Continue the Docker/FoolFrame/Vue migration. Keep the workflow View-driven:
render the View first, query data by View, and avoid binding frontend/report
logic to concrete business DTO fields.

## Scope

- Resolved report `ReportCols[].ColId` through the selected View's Model
  metadata when `ColName` is absent.
- Kept `getrpt` data retrieval on the View-shaped list data path.
- Did not add `saverpt` persistence: FoolFrame's `HandlerSaveReport` has an
  empty business-logic implementation, so no-op success remains parity.
- Did not implement `SelectedTypeId`, report ordering, export, or saved report
  execution semantics in this slice.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/api/ReportController.java`
- `fool-view/src/test/java/org/fool/framework/view/api/ReportControllerTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/04/2026-07-03T20-10-48Z-report-colid-metadata.md`

## Validation

- Red: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -DfailIfNoTests=false -Dtest=ReportControllerTest test`
  failed with `expected:<2> but was:<0>` before the fix.
- Green: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -DfailIfNoTests=false -Dtest=ReportControllerTest test`.
- Green: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -DfailIfNoTests=false test`.
- Runtime: `docker compose up -d --build --force-recreate backend`.
- Runtime: ColId-only `getrpt` payload returned `code=0` and `Symbol` cells
  through backend `http://localhost:8080/api/v1/report/getrpt`.
- Runtime: the same payload returned `code=0` and `Symbol` cells through the
  Vue proxy `http://localhost:8081/api/v1/report/getrpt`.

## Skipped Checks

- `cd frontend && npm test && npm run build` was not rerun in this slice because
  no frontend files changed after the previous generic View workflow commit.
- Full root `mvn test` was not rerun; the changed Java code is inside
  `fool-view`, and that module's test suite passed.

## Risks And Follow-ups

- `SelectedTypeId`, report ordering, export, and saved report execution remain
  migration backlog.
- Broader report parity still needs comparison against FoolFrame report UI flows
  beyond this minimal ColId-only selected-column path.

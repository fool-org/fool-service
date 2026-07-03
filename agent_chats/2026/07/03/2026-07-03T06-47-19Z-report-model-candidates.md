# Report Model Candidate Columns

## Prompt

- Continue Docker/Vue/FoolFrame migration work and report the current overall
  completion percentage.

## Scope

- Migrated the legacy report model candidate-column lookup from
  `../FoolFrame` report builder flow into `fool-service`.
- Added `/api/v1/report/getmkqview` and `/api/v1/report/mkqview` backend
  routes that resolve `ViewId` to model properties and return candidate report
  columns with property type, enum states, compare operations, and select
  types.
- Enabled `fool-query` auto-configuration so its JDBC catalog repositories are
  available to runtime consumers.

## Changed Files

- `fool-query/src/main/java/org/fool/framework/query/JdbcCompareOpCatalog.java`
- `fool-query/src/main/java/org/fool/framework/query/JdbcSelectTypeCatalog.java`
- `fool-query/src/main/java/org/fool/framework/query/autoconfigure/FoolQueryAutoConfigure.java`
- `fool-query/src/main/resources/META-INF/spring.factories`
- `fool-view/src/main/java/org/fool/framework/view/api/ReportController.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/ReportModelResult.java`
- `fool-view/src/test/java/org/fool/framework/view/api/ReportControllerTest.java`
- `docs/migration/foolframe-parity.md`

## Validation

- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=ReportControllerTest test`
  - Result before implementation: expected `NoSuchMethodException` for
    `ReportController.getReportModel(MakeReportRequest)`.
- GREEN focused: same command after implementation and cleanup.
  - Result: `ReportControllerTest` 8 tests passed.
- Module: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false test`
  - Result: `fool-view` path 68 tests passed.
- Query/view chain: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-query,fool-view -am -DfailIfNoTests=false test`
  - Result: Maven build success; the auto-configured query catalog beans load
    in the model/view Spring test chain, and `fool-view` 68 tests passed.
- Runtime: `docker compose up -d --build backend`
  - Result: backend image rebuilt and restarted with MySQL/Redis healthy.
- Runtime smoke:
  - `curl http://localhost:8080/test`
  - `curl -H 'Content-Type: application/json' -d '{"ViewId":100}' http://localhost:8080/api/v1/report/getmkqview`
  - `curl -H 'Content-Type: application/json' -d '{"ViewId":100}' http://localhost:8080/api/v1/report/mkqview`
  - `curl -H 'Content-Type: application/json' -d '{"ViewId":100}' http://localhost:8081/api/v1/report/getmkqview`
  - Result: all returned successfully; report model responses contain
    `cols`, `compareTypes`, `queryTypes`, and enum `states`.

## Skipped Checks

- No frontend test/build rerun in this slice because no frontend files changed.

## Risks And Follow-Ups

- The current long-lived local Docker MySQL volume contains double-encoded
  query catalog display text, confirmed with `HEX(SE_COMPARESHOW)` returning
  bytes such as `C3A7...` for the compare name. The repository init SQL is
  readable UTF-8, so this appears to be local volume seed drift rather than a
  new API encoding issue.
- Vue report builder wiring for `getmkqview` remains open; this slice only
  exposes and validates the backend candidate-column API.

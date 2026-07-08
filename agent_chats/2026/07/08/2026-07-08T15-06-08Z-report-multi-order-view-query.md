# Report Multi Order View Query

## Prompt

The user called out that the migrated flow must render/load the View first,
then query data through that View context; binding report behavior to concrete
business DTO rows is wrong.

## Scope

- Preserved multiple non-NULL report `ReportCols.OrderType` entries in selected
  column order.
- Kept report ordering in the View/data SQL path: `ReportController` passes
  View tokens, `DataQueryService` resolves tokens through ViewItem/Property
  metadata, and `SqlGenerator` emits a multi-column `ORDER BY`.
- Kept the existing single-column `ModelDataService` and `SqlGenerator`
  overloads so current callers continue to use the narrow path.

## Changed Files

- `fool-model/src/main/java/org/fool/framework/model/sqlscript/SqlGenerator.java`
- `fool-model/src/main/java/org/fool/framework/model/service/ModelDataService.java`
- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `fool-view/src/main/java/org/fool/framework/view/api/ReportController.java`
- `fool-model/src/test/java/org/fool/framework/model/sqlscript/SqlGeneratorTest.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceOrderingTest.java`
- `fool-view/src/test/java/org/fool/framework/view/api/ReportControllerTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Red: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-model,fool-view -am -DfailIfNoTests=false -Dtest=SqlGeneratorTest,ReportControllerTest,DataQueryServiceOrderingTest test` failed because `SqlGenerator.OrderColumn` did not exist.
- Green: same focused command passed after adding the multi-column order path.
- Green: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am test`.
- Green: `git diff --check`.
- Green: `python scripts/check_repo_harness.py`.
- Green: `docker compose up -d --build backend`.
- Green: `python scripts/runtime_doctor.py`.
- Green: `curl -fsS http://localhost:8080/test`.
- Green: `docker compose ps`.

## Runtime Evidence

- `python scripts/runtime_doctor.py` passed through backend, frontend proxy,
  app default View, `getlistview`, `querydata`, `getmkqview`, `getrpt`, and
  `saverpt`.
- Direct frontend-proxy report probe:
  `POST http://localhost:8081/api/v1/report/getrpt` with `ViewId=100`,
  `ReportCols=[State DESC, Symbol ASC]` returned first rows:
  `Filled / ETH-USDT`, `Filled / QA-326688-USDT`,
  `Filled / QA-456235-USDT`, `Filled / QA-578367-USDT`,
  then `Open / BTC-USDT`.

## Skipped Checks

- Frontend unit/build checks were not rerun because this slice did not modify
  frontend source.

## Risks

- This only extends report order propagation. Report aggregation/select-type
  surfaces should continue to be checked against FoolFrame separately when
  they are touched.

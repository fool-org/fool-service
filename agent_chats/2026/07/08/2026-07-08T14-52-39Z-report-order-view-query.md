# Report Order View Query

## Prompt

The user clarified that the migration should load/render the View first, then
query data through that View context, and that binding behavior to concrete
business DTOs is the wrong direction.

## Scope

- Moved report `ReportCols.OrderType` handling out of rendered row-map sorting.
- Added a thin `DataQueryService.QueryOrder` boundary that resolves order tokens
  through loaded ViewItem/Property metadata before SQL data query execution.
- Kept legacy `querydata` default ordering unchanged: first `ShowIndex` View
  item descending when no explicit order is provided.
- For report `OrderType=2` / `NULL`, used the first selected report column ASC,
  matching FoolFrame report query fallback.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `fool-view/src/main/java/org/fool/framework/view/api/ReportController.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceOrderingTest.java`
- `fool-view/src/test/java/org/fool/framework/view/api/ReportControllerTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Red: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -Dtest=DataQueryServiceOrderingTest#queryLegacyViewDataUsesExplicitOrderFromRenderedViewItem test` failed because `DataQueryService.QueryOrder` did not exist.
- Green: same focused command passed after adding the View metadata order input.
- Green: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -Dtest=ReportControllerTest test`.
- Green: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -Dtest=DataQueryServiceOrderingTest test`.
- Green: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am test`.
- Green: `git diff --check`.
- Green: `docker compose up -d --build backend`.
- Green: `python scripts/runtime_doctor.py`.

## Runtime Evidence

- `python scripts/runtime_doctor.py` passed through the frontend proxy, including
  App default View, `getlistview`, `querydata`, `getmkqview`, `getrpt`, and
  `saverpt`.
- Direct frontend-proxy report probe:
  `POST http://localhost:8081/api/v1/report/getrpt` with `ViewId=100`,
  `ReportCols[0].ColId=symbol`, and `OrderType=2` returned first symbols
  `BTC-USDT`, `ETH-USDT`, `QA-326688-USDT`.
- The same probe with `OrderType=1` returned first symbols `SOL-1-USDT`,
  `SOL-0-USDT`, `SOL-0-USDT`.

## Skipped Checks

- Frontend unit/build checks were not rerun because this slice did not modify
  frontend source.

## Risks

- `DataQueryService` currently accepts a single explicit order column. Legacy
  report SQL can order by multiple selected columns, so multi-column report
  ordering remains a future data-query service enhancement.

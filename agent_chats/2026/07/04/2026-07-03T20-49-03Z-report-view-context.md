# Report View Context

## Prompt

Continue the FoolFrame migration, but keep the report/data path View-first and
avoid binding the migrated flow to concrete business DTOs.

## Scope

- Compared the report flow with `../FoolFrame`:
  `HandlerGetReportModel` loads a View, then `QueryFactory.GetQueryModel(view)`
  builds report candidate columns from `view.Items`; `ModelQueryCol.ID` is the
  model property key.
- Refactored `ReportController` to load one View/Model context before
  resolving report columns, filter expressions, selected types, and row values.
- Changed `getmkqview` candidate `ColId` values to View-derived property keys
  such as `symbol`, while keeping numeric Java property ids accepted as a
  compatibility fallback.
- Kept the current selected-type execution slice narrow: single-column
  `COUNT({0})` reports use the already-filtered `queryLegacyViewData`
  `TotalItem`; broader grouped aggregate SQL remains future work.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/api/ReportController.java`
- `fool-view/src/test/java/org/fool/framework/view/api/ReportControllerTest.java`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/04/2026-07-03T20-49-03Z-report-view-context.md`

## Validation

- RED:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -DfailIfNoTests=false -Dtest=ReportControllerTest#makeReportUsesTotalItemForSingleCountSelectedType test`
  failed before implementation because the report still returned
  `totalRecords=42` instead of a single rendered aggregate row.
- GREEN:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -DfailIfNoTests=false -Dtest=ReportControllerTest test`
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -DfailIfNoTests=false test`
- `cd frontend && npm test -- viewWorkflow.test.ts payload.test.ts`
- `python3 scripts/check_repo_harness.py`
- `git diff --check`
- `docker compose up -d --build --force-recreate backend`

## Runtime Evidence

- `docker compose ps` showed backend, frontend, MySQL, and Redis running.
- `curl -fsS http://localhost:8080/test` returned a backend list with 8 rows.
- `POST http://localhost:8080/api/v1/report/getmkqview` with `{"ViewId":100}`
  returned candidate ids `orderId`, `symbol`, `customer`, and `state`.
- `POST http://localhost:8080/api/v1/report/getrpt` and the frontend proxy
  `http://localhost:8081/api/v1/report/getrpt` with
  `ColId=symbol, SelectedTypeId=2` returned `code=0`,
  `totalRecords=1`, `totalPages=1`, and a single value row `8`.
- `POST http://localhost:8080/api/v1/report/makereport` with
  `FilterExp.Col.Name=Symbol` and `CompareOp.ID=7` returned `BTC-USDT`,
  proving filter mapping can start from the rendered View item name.

## Risks

- Full FoolFrame report execution still needs the shared query path for
  multi-column selected types, grouping, ordering before pagination, saved
  report execution, and export.

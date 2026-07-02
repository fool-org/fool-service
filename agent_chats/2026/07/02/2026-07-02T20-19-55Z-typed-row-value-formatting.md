# Agent Chat Evidence: typed legacy row value formatting

## Prompt

Continue the active migration goal:

1. Run the environment with Docker.
2. Complete migration against `../FoolFrame`.
3. Use Vue for the frontend.
4. Commit atomically and promptly.

## Scope

- Compared `../FoolFrame/src/Server/Soway.Server/DataFormator.cs` `GetAddItem` with the current `ViewDataAdapter` row `Items` output.
- Closed the next `ObjValuePair` parity slice: typed `FmtValue` and `ObjId` behavior for Date, Time, Enum, and BusinessObject values.
- Kept the existing Vue API shape unchanged; this slice only changes backend row-value formatting.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/adapter/ViewDataAdapter.java`
- `fool-view/src/test/java/org/fool/framework/view/adapter/ViewDataAdapterTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/02/2026-07-02T20-19-55Z-typed-row-value-formatting.md`

## Validation

- RED:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=ViewDataAdapterTest#listRowsFormatLegacyTypedValueItems test`
  - Failed as expected: enum `FmtValue` was `2` instead of `Closed`.
- GREEN:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=ViewDataAdapterTest#listRowsFormatLegacyTypedValueItems test`
  - Passed: 1 test, 0 failures, 0 errors.
- Module:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am test`
  - Passed. Reactor summary `BUILD SUCCESS`; `fool-view` test results: 39 tests, 0 failures, 0 errors.
- Harness:
  `python scripts/check_repo_harness.py`
  - Passed: `Repository harness validation passed.`
- Docker/runtime:
  `docker compose up -d --build backend`
  - Passed. Backend image build completed and the backend container restarted.
  `curl -fsS --retry 15 --retry-delay 2 --retry-all-errors http://localhost:8080/test`
  - Passed.
  `curl -fsS --retry 15 --retry-delay 2 --retry-all-errors -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list`
  - Passed. Response had 2 rows, and the first row had 3 legacy `items` value entries with `objId`, `prpId`, `fmtValue`, `prpShowName`, `prpType`, `prpModelId`, `readOnly`, and `editType`.

## Skipped Checks

- Frontend build was not rerun because no frontend files or API type shape changed.
- Full backend reactor test was not rerun; this slice was covered by `fool-view -am test` plus Docker backend package/build and runtime smoke.

## Risks / Follow-Ups

- BusinessObject display follows the Java `IDynamicData` show-property metadata path. Full nested object behavior still depends on upstream mapper/query parity.

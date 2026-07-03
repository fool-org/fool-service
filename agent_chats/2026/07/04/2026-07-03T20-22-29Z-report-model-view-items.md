# Report Model View Items

## Prompt

Continue the Docker/FoolFrame/Vue migration. Keep report setup aligned with the
rendered View first, then View data, instead of exposing concrete business DTO
or full Model fields.

## Scope

- Compared FoolFrame `HandlerGetReportModel`, which builds report candidate
  columns through `QueryFactory.GetQueryModel(view)`.
- Changed `getmkqview` to use configured View items when present.
- Ordered the candidate columns by View item `showIndex`, matching the rendered
  list View order.
- Kept the existing Model-property fallback for Views without item metadata.
- Reused existing ViewItem and Model metadata; no new query model layer was
  introduced.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/api/ReportController.java`
- `fool-view/src/test/java/org/fool/framework/view/api/ReportControllerTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/04/2026-07-03T20-22-29Z-report-model-view-items.md`

## Validation

- Red: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -DfailIfNoTests=false -Dtest=ReportControllerTest#getReportModelUsesConfiguredViewItemsInsteadOfAllModelProperties test`
  failed with `expected:<1> but was:<2>`.
- Red: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -DfailIfNoTests=false -Dtest=ReportControllerTest#getReportModelOrdersConfiguredViewItemsByShowIndex test`
  failed with `expected:<S[ymbol]> but was:<S[tate]>`.
- Green: the same focused test passed after the fix.
- Green: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -DfailIfNoTests=false -Dtest=ReportControllerTest test`.
- Green: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -DfailIfNoTests=false test`.
- Green: `python3 scripts/check_repo_harness.py`.
- Green: `git diff --check`.
- Runtime: `docker compose up -d --build --force-recreate backend`.
- Runtime: backend `http://localhost:8080/api/v1/report/getmkqview` returned
  `code=0`, `count=4`, names `Order ID, Symbol, Customer, State`.
- Runtime: frontend proxy `http://localhost:8081/api/v1/report/getmkqview`
  returned the same four names.
- Runtime: backend `http://localhost:8080/api/v1/view/getlistview` returned the
  same four View column names, proving report setup follows the rendered View.

## Risks And Follow-ups

- `SelectedTypeId`, aggregate query semantics, report ordering, export, and
  saved report execution remain migration backlog.

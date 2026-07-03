# Report FilterExp Parity

## Prompt

- Continue the active migration goal: Docker runtime, FoolFrame parity,
  Vue frontend, and timely atomic commits.

## Scope

- Added legacy `MakeReportOption.FilterExp` request shape to
  `/api/v1/report/makereport`.
- Converted only simple safe equality expressions into the existing raw
  `QueryFilter` execution path.
- Rejected complex or non-equality report expressions instead of ignoring
  them.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/api/ReportController.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/MakeReportRequest.java`
- `fool-view/src/test/java/org/fool/framework/view/api/ReportControllerTest.java`
- `docs/migration/foolframe-parity.md`

## Validation

- RED: focused Docker Maven test failed before implementation because
  `MakeReportRequest.BoolExp` did not exist.
- PASS: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=ReportControllerTest test`
- PASS: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false test`
- PASS: `docker compose up -d --build backend`
- PASS: `curl -fsS http://localhost:8080/test`
- PASS: `curl -fsS -H 'Content-Type: application/json' -d '{"ViewId":100,"CurrentPage":1,"PageSize":10,"FilterExp":{"Col":{"Name":"order_state"},"CompareOp":{"ID":"1","Name":"等于"},"ValueExp":"0","ValueFmt":"Open"},"ReportCols":[{"ColName":"Symbol","Index":1},{"ColName":"State","Index":2}]}' http://localhost:8080/api/v1/report/makereport`
- PASS: `curl -fsS -H 'Content-Type: application/json' -d '{"ViewId":100,"CurrentPage":1,"PageSize":10,"FilterExp":{"Col":{"Name":"order_state"},"CompareOp":{"ID":"1","Name":"等于"},"ValueExp":"0","ValueFmt":"Open"},"ReportCols":[{"ColName":"Symbol","Index":1},{"ColName":"State","Index":2}]}' http://localhost:8081/api/v1/report/makereport`
- PASS: `docker compose ps`
- PASS: `git diff --check`
- PASS: `python scripts/check_repo_harness.py`

## Skipped Checks

- Frontend unit/build was not rerun because this slice did not touch frontend
  files.

## Risks And Follow-Ups

- Complex/composite `BoolExp` trees, non-equality operations, saved report
  metadata, and export wiring remain open report parity work.

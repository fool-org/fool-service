# Legacy Report Enum Codes

## Prompt

- Continue the Docker/Vue FoolFrame migration with timely atomic commits.

## Scope

- Migrated report `OrderType`, `StaticType`, and `CalDirection` to explicit
  legacy codes.
- Kept report rendering and matrix behavior unchanged.

## Changed Files

- `fool-report/src/main/java/org/fool/framework/report/OrderType.java`
- `fool-report/src/main/java/org/fool/framework/report/StaticType.java`
- `fool-report/src/main/java/org/fool/framework/report/CalDirection.java`
- `fool-report/src/test/java/org/fool/framework/report/ReportMigrationTest.java`
- `docs/migration/foolframe-parity.md`

## Validation

- Red check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-report -am -Dtest=org.fool.framework.report.ReportMigrationTest#preservesLegacyReportEnumOrdinals test`
  failed because `code()` did not exist on the report enums.
- Green check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-report -am -Dtest=org.fool.framework.report.ReportMigrationTest#preservesLegacyReportEnumOrdinals test`
- Module check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-report -am test`
- Harness check:
  `python scripts/check_repo_harness.py`
- Whitespace check:
  `git diff --check`
- Runtime smoke:
  `curl -sS -m 5 http://localhost:8080/test`
  `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
  `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list`
  `curl -sS -m 5 http://localhost:8081/`
  `docker compose ps`

## Risks And Follow-Ups

- This slice preserves public enum-code parity only. Report source adapters and
  export integration remain in the existing migration backlog.

# Legacy Business Object

## Prompt

Continue the Docker/FoolFrame/Vue migration goal with timely atomic commits.

## Scope

- Added legacy `BusinessObject` base class in `fool-common`.
- Preserved the `BO_Id` id column, key flag, insert-generation metadata, and
  id getter/setter.
- Updated the migration parity document.

## Changed Files

- `fool-common/src/main/java/org/fool/framework/common/data/BusinessObject.java`
- `fool-common/src/test/java/org/fool/framework/common/data/BusinessObjectTest.java`
- `docs/migration/foolframe-parity.md`

## Validation

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-common test`
- `python scripts/check_repo_harness.py`
- `curl -sS -m 5 http://localhost:8080/test`
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list`
- `curl -sS -m 5 http://localhost:8081/ | head -5`
- `docker compose ps`

## Skipped Checks

- Full root `mvn test` was not run; this slice only adds an isolated
  `fool-common` base class.

## Risks And Follow-Ups

- Legacy display metadata is not represented because the current `Display`
  annotation has no fields for display name or editability.

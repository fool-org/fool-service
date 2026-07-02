# Legacy Display Metadata

## Prompt

Continue the Docker/FoolFrame/Vue migration goal with timely atomic commits.

## Scope

- Expanded the existing `Display` annotation to carry legacy
  `ShowDescriptionAttribute` metadata.
- Preserved runtime availability, display name, display flag, display index,
  editable flag, dropdown flag, and the legacy `ShowInList = true` default.
- Updated the migration parity document.

## Changed Files

- `fool-common/src/main/java/org/fool/framework/common/annotation/Display.java`
- `fool-common/src/test/java/org/fool/framework/common/annotation/DisplayTest.java`
- `docs/migration/foolframe-parity.md`

## Validation

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-common test`
- `python scripts/check_repo_harness.py`
- `curl -sS -m 5 http://localhost:8080/test`
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list`
- `curl -sS -m 5 http://localhost:8081/ | head -5`
- `docker compose ps`
- `git diff --check`

## Skipped Checks

- Full root `mvn test` was not run; this slice only changes one
  `fool-common` annotation.

## Risks And Follow-Ups

- `EntityAttribute` remains separate; this slice only covers
  `ShowDescriptionAttribute`.

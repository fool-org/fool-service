# Legacy Graphic Helper

## Prompt

Continue the Docker/FoolFrame/Vue migration goal with timely atomic commits.

## Scope

- Added legacy `GraphicNode<T>` and `Graphic<T>` helper surfaces in
  `fool-common`.
- Preserved duplicate edge suppression, top-node lookup, self-loop fallback,
  node add, edge add, and remove behavior.
- Updated the migration parity document.

## Changed Files

- `fool-common/src/main/java/org/fool/framework/common/data/graphic/GraphicNode.java`
- `fool-common/src/main/java/org/fool/framework/common/data/graphic/Graphic.java`
- `fool-common/src/test/java/org/fool/framework/common/data/graphic/GraphicTest.java`
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

- Full root `mvn test` was not run; this slice only adds isolated
  `fool-common` helper classes.

## Risks And Follow-Ups

- This keeps the legacy helper surface only; richer graph algorithms remain
  out of scope because FoolFrame did not implement them here.

# QueryFactory State String Parity

## Prompt

Continue the FoolFrame Docker/Vue migration, keep commits atomic, maximize
reuse, and keep protocol behavior aligned with FoolFrame unless deliberately
adjusted.

## Scope

- Compared FoolFrame `Soway.Model.Query.QueryFactory.GetStateStr` with the
  Java `QueryFactory.getStateStr` default method.
- Found the Java method had a non-FoolFrame reverse display-name to DB-value
  mapping and returned the input for unknown values.
- Changed the Java default to match FoolFrame: DB value maps to show name;
  missing values return an empty string.
- Updated migration docs and task state.

## Changed Files

- `fool-query/src/main/java/org/fool/framework/query/QueryFactory.java`
- `fool-query/src/test/java/org/fool/framework/query/QueryFactoryTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Red: `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=QueryFactoryTest test` failed because display-name input returned the DB value.
- Green: the same Docker Maven focused test passed after the fix.
- Module: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am test` passed.
- Harness: `python scripts/check_repo_harness.py` passed.
- Whitespace: `git diff --check` passed.

## Runtime Evidence

- `docker compose ps` showed MySQL, Redis, backend, and frontend up.
- `curl http://localhost:8081/` returned HTTP 200.
- `curl http://localhost:8080/test` returned HTTP 200.
- The same module test without `--network fool-service_default` failed in
  dependency tests with `UnknownHostException: mysql`; rerunning on the
  Compose network passed.

## Risks

- Any caller relying on the Java-only reverse mapping now gets the FoolFrame
  empty-string behavior. No production caller was found by `rg getStateStr`.

## Follow-ups

- Continue checking remaining query/report behaviors against FoolFrame before
  preserving Java-only compatibility shortcuts.

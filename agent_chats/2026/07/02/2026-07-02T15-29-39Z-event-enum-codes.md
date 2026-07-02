# Legacy Event Enum Codes

## Prompt

- Continue the Docker/Vue FoolFrame migration with timely atomic commits.

## Scope

- Migrated `EventState`, `MsgState`, `MsgNotifyType`, and
  `EventModelRefType` to explicit legacy codes.
- Kept the existing event repository mapping behavior unchanged.

## Changed Files

- `fool-event/src/main/java/org/fool/framework/event/EventState.java`
- `fool-event/src/main/java/org/fool/framework/event/MsgState.java`
- `fool-event/src/main/java/org/fool/framework/event/MsgNotifyType.java`
- `fool-event/src/main/java/org/fool/framework/event/EventModelRefType.java`
- `fool-event/src/test/java/org/fool/framework/event/EventMigrationTest.java`
- `docs/migration/foolframe-parity.md`

## Validation

- Red check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-event -am -Dtest=org.fool.framework.event.EventMigrationTest#preservesLegacyEnumOrdinals test`
  failed because `code()` did not exist on the event enums.
- Green check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-event -am -Dtest=org.fool.framework.event.EventMigrationTest#preservesLegacyEnumOrdinals test`
- Module check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-event -am test`
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

- This slice preserves public enum-code parity only. Fuller dynamic object-query
  behavior remains in the existing event migration backlog.

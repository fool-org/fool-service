# 2026-07-03T04:45:03Z Event MySQL Bracket Filter

## Prompt

- Continue the active goal: Docker runtime, FoolFrame migration parity, Vue
  frontend, and timely atomic commits.

## Scope

- Keep legacy event query filters usable against the Docker MySQL runtime when
  migrated model table names are MySQL-style.
- Preserve the existing SQL Server-style bracketed table behavior for legacy
  table names.

## Changed Files

- `fool-event/src/main/java/org/fool/framework/event/EventSqlHelper.java`
- `fool-event/src/test/java/org/fool/framework/event/EventMigrationTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/03/2026-07-03T04-45-03Z-event-mysql-bracket-filter.md`

## Validation

- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-event -am -Dtest=EventMigrationTest#eventSqlHelperNormalizesLegacyBracketFilterForMysqlTables -DfailIfNoTests=false test`
  - Failed because `EventSqlHelper` returned `[order_state]` and
    `[order_price]` unchanged.
- RED runtime check: `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SELECT * FROM market_order WHERE [order_state] = 0 LIMIT 1;"`
  - MySQL returned syntax error `1064`.
- GREEN: same focused Maven command.
  - Passed: 1 test, 0 failures, 0 errors.
- GREEN: `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e 'SELECT * FROM market_order WHERE `order_state` = 0 LIMIT 1;'`
  - Returned the Docker smoke order row.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-event -am -Dtest=EventMigrationTest -DfailIfNoTests=false test`
  - Passed: 42 tests, 0 failures, 0 errors.
- `python scripts/check_repo_harness.py`
  - Passed.
- `docker compose ps`
  - Backend/frontend running; MySQL/Redis healthy.

## Skipped

- Did not add a dialect abstraction. This only handles the Docker migration
  case where table names are already MySQL-style but legacy filters still use
  simple `[identifier]` syntax.

## Risks

- Complex SQL Server bracket expressions remain raw; add a parser only if real
  migrated event filters require it.

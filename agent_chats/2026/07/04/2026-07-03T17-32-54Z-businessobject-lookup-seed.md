# BusinessObject Lookup Seed

## Prompt

- Continue the Docker/FoolFrame/Vue migration.
- Keep the frontend View workflow metadata-driven; do not bind pages to
  concrete business DTOs.
- Watch file size and reuse shared logic.

## Scope

- Added Docker seed data for a live `Customer` BusinessObject lookup on
  `OrderList`.
- Added shared model display-property fallback logic used by SQL generation,
  ResultSet mapping, `inputquery`, and view data formatting.
- Flattened BusinessObject list values to display text so Vue consumes View
  data, not nested dynamic DTO objects.

## Changed Files

- `docker/mysql/init/001-market-order.sql`
- `docker/mysql/init/006-view.sql`
- `fool-model/src/main/java/org/fool/framework/model/service/ModelDisplayProperties.java`
- `fool-model/src/main/java/org/fool/framework/model/service/Mapper.java`
- `fool-model/src/main/java/org/fool/framework/model/sqlscript/SqlGenerator.java`
- `fool-model/src/test/java/org/fool/framework/model/service/MapperDbMapsTest.java`
- `fool-model/src/test/java/org/fool/framework/model/sqlscript/SqlGeneratorTest.java`
- `fool-view/src/main/java/org/fool/framework/view/adapter/ViewDataAdapter.java`
- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `fool-view/src/test/java/org/fool/framework/view/adapter/ViewDataAdapterTest.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceInputQueryTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/04/2026-07-03T17-32-54Z-businessobject-lookup-seed.md`

## Validation

- `docker run --rm -v "$PWD":/workspace -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-model,fool-view -am -Dtest=SqlGeneratorTest,MapperDbMapsTest,DataQueryServiceInputQueryTest,ViewDataAdapterTest -DfailIfNoTests=false test`
  - Passed.
- `python scripts/check_repo_harness.py`
  - Passed.
- `git diff --check`
  - Passed.
- `docker compose build --quiet backend`
  - Passed.
- `docker compose up -d backend`
  - Passed; backend was recreated against healthy MySQL and Redis services.

## Runtime Evidence

- `curl -fsS --retry 20 --retry-delay 1 --retry-connrefused http://localhost:8080/test`
  - Passed; backend responded.
- `curl -fsS -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
  - Passed; `Customer` is exposed as editable `BusinessObject` field
    `propertyModel=103`.
- `curl -fsS -H 'Content-Type: application/json' -d '{"viewName":"OrderList","viewItemId":"Customer","text":"Ada"}' http://localhost:8080/api/v1/data/inputquery`
  - Passed; returned `{id:"3001", text:"Ada Capital"}`.
- `curl -fsS -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageIndex":1,"pageSize":5},"filter":{"orderId":{"value":"1001"}}}' http://localhost:8080/api/v1/data/query-list`
  - Passed; returned `values.customer="Ada Capital"` and legacy
    `items[].fmtValue="Ada Capital"` for row `1001`.
- `docker compose ps`
  - Passed; backend, frontend, MySQL, and Redis are running.

## Skipped Checks

- Host Maven was not used because the local Java runtime is Java 8 while the
  project targets Java 17; Docker Maven with Temurin 17 is the validation path.
- Frontend tests/build were not rerun because this slice did not change
  frontend files. The running frontend container was left untouched.

## Risks

- The fallback display-property rule is intentionally conservative. If legacy
  metadata later carries an explicit non-string display field, that should be
  migrated as real `showProperty` metadata rather than inferred.

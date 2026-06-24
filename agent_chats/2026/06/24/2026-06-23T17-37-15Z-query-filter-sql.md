# Query filter SQL parity

## Scope

- Migrated a focused part of legacy `SWDQ01-Soway.Query` bool-expression SQL behavior into `fool-query`.
- Covered compare/between/in/composite filter SQL rendering and ordered parameter propagation.
- Kept the current Java/MySQL quoting style while preserving the legacy expression structure from `SimpleBoolExpression` and `ComplexBoolExpression`.

## Legacy check

- `../FoolFrame/src/Server/SWDQ01-Soway.Query/Soway.Query.BoolExp/SimpleBoolExpression.cs`
  - Builds one comparison SQL part from a compare operation expression and one parameter.
- `../FoolFrame/src/Server/SWDQ01-Soway.Query/Soway.Query.BoolExp/ComplexBoolExpression.cs`
  - Wraps the first expression in parentheses, appends each bool operator, wraps each following expression in parentheses, and carries parameter order forward.
- `../FoolFrame/src/Server/SWDQ01-Soway.Query/Soway.Query.BoolExp/BoolOp.cs`
  - Uses explicit `And` / `OR` operator tokens between expression parts.

## Red

- `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=CompareFilterTest -DfailIfNoTests=false test`
  - Failed with 3 assertion failures:
    - `BetweenFilter` emitted `` `ORDER_DATE` ? AND ? `` instead of `` `ORDER_DATE` BETWEEN ? AND ? ``.
    - `InFilter` emitted an unclosed/poorly spaced placeholder list.
    - `CompositeFilter` omitted the first expression parentheses and concatenated bool operators without stable spacing.

## Green

- `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=CompareFilterTest -DfailIfNoTests=false test`
  - Passed: 3 tests.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am ... test`
  - Passed the affected reactor slice against the Compose MySQL.
  - `fool-dao`: 10 tests passed.
  - `fool-query`: 3 tests passed.

## Full verification

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn ... package`
  - Passed all 15 backend reactor modules in 30.671 s.

## Docker runtime

- `docker compose build backend`
  - Completed with `backend  Built`.
- `docker compose up -d backend`
  - Restarted backend after `mysql` and `redis` were healthy.
- Running HTTP smoke:
  - `curl -fsS http://localhost:8080/test`
    - Returned seeded order JSON.
  - `curl -fsS http://localhost:8081/`
    - Returned 399 bytes.
  - `curl -fsS -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
    - Returned 309 bytes.
  - `curl -fsS -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list`
    - Returned 296 bytes with 2 seeded rows.
  - `curl -fsS -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":{"orderId":{"values":["1001","1002"]}}}' http://localhost:8080/api/v1/data/query-list`
    - Returned 296 bytes with 2 seeded rows.
- Backend logs confirmed the range filter generated:
  - `SELECT order_id,order_symbol,order_state FROM market_order ... AND ( 1=1 ) AND (order_id BETWEEN ? AND ?) LIMIT ? OFFSET ?`
  - Args: `1001`, `1002`, `10`, `0`.

## Notes

- Running `mvn -pl fool-query -am test` in the default Docker network failed before `fool-query` because existing `fool-dao` tests could not connect to MySQL. Re-running the same slice on `fool-service_default` with the Compose datasource override passed.
- `docker compose ps`
  - Confirmed backend and frontend were up, and MySQL/Redis were healthy.
- `python scripts/check_repo_harness.py`
  - Passed.
- `git diff --check`
  - Passed with no output.
- Scoped `git status --short`
  - Showed only the query filter source/test files, migration doc, and this evidence file in the current slice.

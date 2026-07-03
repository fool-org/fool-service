# querydatadetail Items Vue Persistence

## Prompt

Continue the active migration goal and answer whether the frontend is still only
component-level and mostly unusable.

## Finding

The Vue default workflow was still too shallow for child items: after adding an
`Order Items` component, child rows were session-only. Adding the collection view
item also exposed a regression where list responses tried to serialize the
collection value as a scalar column, which made the Vue `Load Orders` path show
an empty-response error.

## Scope

- Hydrated relation-backed collection rows when loading one dynamic object.
- Returned legacy `querydatadetail` collection `Items` groups with child
  `DataItem` rows.
- Kept collection fields out of scalar list results and `get-view` table/input
  metadata.
- Seeded the Docker `OrderList` view metadata with the `items` collection field.
- Changed the Vue `Order Items` panel to render persisted backend detail items
  instead of session-only additions.
- Updated parity and task-state docs.

## Changed Files

- `docker/mysql/init/006-view.sql`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `fool-model/src/main/java/org/fool/framework/model/service/ModelDataService.java`
- `fool-view/src/main/java/org/fool/framework/view/adapter/ViewAdapter.java`
- `fool-view/src/main/java/org/fool/framework/view/adapter/ViewDataAdapter.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/QueryDataDetailResult.java`
- `fool-view/src/test/java/org/fool/framework/view/adapter/ViewAdapterTest.java`
- `fool-view/src/test/java/org/fool/framework/view/adapter/ViewDataAdapterTest.java`
- `frontend/src/App.vue`
- `frontend/src/api.ts`
- `agent_chats/2026/07/03/2026-07-03T16-25-00Z-querydatadetail-items-vue.md`

## Validation

- `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=ViewAdapterTest,ViewDataAdapterTest -DfailIfNoTests=false test`
  - Passed: 29 tests.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model,fool-view -am -DfailIfNoTests=false test`
  - Passed: 104 tests.
- `cd frontend && npm test`
  - Passed: 34 tests.
- `cd frontend && npm run build`
  - Passed `vue-tsc --noEmit` and Vite build.
- `python3 scripts/check_repo_harness.py`
  - Passed.
- `docker compose up -d --build`
  - Rebuilt and restarted backend/frontend with MySQL and Redis healthy.
- `curl http://localhost:8080/test`
  - Returned Docker smoke order rows.
- `POST /api/v1/view/get-view` for `OrderList`
  - Returned table columns `orderId`, `symbol`, and `state`; no scalar
    `items` column.
- `POST /api/v1/data/query-list` with `pageInfo` and keyword `1001`
  - Returned one row with values `orderId`, `symbol`, and `state`; no `items`
    scalar value.
- `POST /api/v1/data/querydatadetail` with `viewId=100`, `objId=1001`
  - Returned `SimpleData` for order fields and one `items` group with three
    persisted rows: `Updated item`, `New item`, and `Item-813008`.
- MySQL proof:
  `SELECT item_id, order_id, item_name FROM market_order_item WHERE order_id = 1001 ORDER BY item_id;`
  returned `2001 Updated item`, `2003 New item`, and
  `1783093814663 Item-813008`.
- Playwright runtime smoke at `http://localhost:8081/`:
  - filled `Keyword=1001`
  - clicked `Load Orders`
  - verified `Order Items`, `Updated item`, `New item`, and `Item-813008`
  - verified no empty-response error, no framework overlay, and no console
    warnings/errors
  - screenshot: `/tmp/fool-service-order-items-full.png`

## Runtime Notes

The Browser plugin path failed earlier in this environment:
`domSnapshot()` raised `TypeError: o.incrementalAriaSnapshot is not a function`,
and a later Browser screenshot call timed out. Runtime proof used regular
Playwright with the Codex bundled Node runtime and local Chrome.

Local host Maven is still not used for Java validation because the host
`java -version` is Java 8. Java validation used the JDK 17 Maven Docker image.

## Residual Risk

This makes the default Docker `OrderList` list/detail/items workflow usable for
persisted reads and child adds. It is still not a complete FoolFrame frontend
replacement: arbitrary metadata-driven generated screens, child edit/delete,
select-from-existing, modal detail navigation, and full report/detail parity are
still backlog work.

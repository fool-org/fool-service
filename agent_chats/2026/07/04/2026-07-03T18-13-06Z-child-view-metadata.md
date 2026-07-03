# Child View Metadata

## Prompt

- Continue the Docker/FoolFrame/Vue migration.
- Fix the View-driven flow: render from View metadata first, then query data
  through that View, instead of binding the page workflow to concrete business
  DTO fields.
- Keep files small and reuse existing code.

## Scope

- Compared FoolFrame detail rendering and server formatting:
  `SelectedView`, `ListView`, and `EditView` are View item metadata, not
  frontend DTO guesses.
- Added child View ID metadata to migrated `ViewItem`.
- Updated backend adapters so `getlistview` columns and `querydatadetail`
  collection groups expose configured list/edit/select View IDs.
- Seeded Docker MySQL with `OrderItemList` view `101` and configured
  `Order.items` to use view `101` for list/select existing candidates.
- Left Vue behavior unchanged for this slice: it now works because the backend
  returns the same View metadata shape the existing candidate loader expects.

## Changed Files

- `docker/mysql/init/006-view.sql`
- `fool-view/src/main/java/org/fool/framework/view/adapter/ViewAdapter.java`
- `fool-view/src/main/java/org/fool/framework/view/adapter/ViewDataAdapter.java`
- `fool-view/src/main/java/org/fool/framework/view/model/ViewItem.java`
- `fool-view/src/test/java/org/fool/framework/view/adapter/ViewAdapterTest.java`
- `fool-view/src/test/java/org/fool/framework/view/adapter/ViewDataAdapterTest.java`
- `fool-view/src/test/java/org/fool/framework/view/service/ModelDaoMappingTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Red first:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=ViewDataAdapterTest#detailCollectionItemsExposeConfiguredLegacyChildViews test`
  - Failed before implementation because `ViewItem` did not expose
    `setListViewId`, `setEditViewId`, or `setSelectedViewId`.
- Focused backend adapter/mapping tests:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=ViewDataAdapterTest#detailCollectionItemsExposeConfiguredLegacyChildViews,ModelDaoMappingTest#viewItemMapsLegacySourceExpressionMetadata test`
  - Passed: 2 tests.
- Broader focused backend adapter/mapping tests:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=ViewAdapterTest,ViewDataAdapterTest,ModelDaoMappingTest test`
  - Passed: 35 tests.
- Repository harness:
  `python3 scripts/check_repo_harness.py`
  - Passed.
- Whitespace check:
  `git diff --check`
  - Passed.

## Runtime Evidence

- Applied the updated Docker seed SQL to the running MySQL container:
  `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/006-view.sql`
- Rebuilt and recreated the backend, then repeated after the final
  `ViewAdapter` metadata passthrough change:
  `docker compose build --quiet backend && docker compose up -d --no-deps --force-recreate backend`
- Docker status after recreate:
  - backend up on `0.0.0.0:8080`.
  - frontend up on `0.0.0.0:8081`.
  - MySQL and Redis healthy.
- Legacy `querydatadetail`:
  - Request: `{"viewId":100,"objId":"1001"}`
  - Result: `code=0`, `items.listViewId=101`, `items.selectedView=101`,
    `items.selectFromExists=true`, child properties `itemId` and `itemName`,
    and 3 persisted item rows.
- Legacy `getlistview`:
  - Request: `{"viewId":101}`
  - Result: `code=0`, `viewName=OrderItemList`, columns
    `itemId / Item ID` and `itemName / Item Name`.
- Legacy `querydata` candidate lookup:
  - Request: `{"viewId":101,"pageSize":5,"pageIndex":1,"keyword":"Legacy"}`
  - Result: `code=0`, `totalItem=1`, row `2001 / Legacy item`.
- Current-container curl recheck after backend rebuild:
  - `/test`: returned a non-empty JSON response.
  - `querydatadetail(100, 1001)`: `items.listViewId=101`,
    `items.selectedView=101`, `items.selectFromExists=true`, properties
    `itemId` and `itemName`, 3 rows.
  - `getlistview(101)`: `viewName=OrderItemList`, columns
    `itemId / Item ID` and `itemName / Item Name`.
  - `querydata(101, keyword=Legacy)`: `totalItem=1`, row id `2001`,
    values `itemId=2001`, `itemName=Legacy item`.
- Browser target: `http://localhost:8081/`.
- Browser plugin was available. `domSnapshot()` still fails in this environment
  with `incrementalAriaSnapshot is not a function`, so rendered proof used body
  text, read-only page evaluation, scoped locator interaction, console logs,
  and a Browser screenshot emitted to the chat.
- Browser interaction:
  - Page title: `Fool Service`.
  - Opened order `1001 / BTC-USDT`.
  - Detail panel showed `Items`, `3 rows`, `Search`, `Page`, `Page size`, and
    `Load Existing`, proving `selectFromExists=true` reached Vue.
  - Filled the child candidate `Search` field with `Legacy`.
  - Clicked `Load Existing`.
  - Candidate table rendered `2001 / Legacy item / Select`.
  - Console warnings/errors: none.
- Browser recheck after final backend rebuild:
  - Reloaded `http://localhost:8081/`.
  - Opened order `1001 / BTC-USDT`.
  - Queried child candidates with `Legacy`.
  - Candidate table still rendered `2001 / Legacy item / Select`.
  - Console warnings/errors: none.

## Skipped Checks

- Full `mvn test` was not run; this slice touched `fool-view` View metadata
  adapters and Docker seed data, so focused module tests plus Docker/browser
  runtime validation were used. Local host Maven uses Java 8, so Java
  validation used the repository-documented Maven Java 17 Docker image.
- Frontend unit/build was not rerun because no frontend source changed in this
  slice. The running Vue container was validated through the browser against
  the updated backend metadata.

## Risks

- The modern `fool_sys_view_item` table is the current Java source for view
  items. The Docker seed also mirrors the same IDs into legacy
  `SW_SYS_VIEW_ITEM`, but direct runtime hydration from legacy
  `VIEW_ITEM_SUBVIEW` / `VIEW_ITEM_EDITVIEW` / `VIEW_ITEM_SELECTVIEW` remains a
  separate migration slice if the repository later switches View loading to the
  legacy table directly.

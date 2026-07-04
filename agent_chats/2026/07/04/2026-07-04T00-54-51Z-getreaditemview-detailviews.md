# Prompt

Continue the Docker/FoolFrame/Vue migration goal after the read-item View
metadata slice.

# Scope

- Complete the next `getreaditemview` parity gap against FoolFrame:
  collection fields should appear in `DetailViews`, and each detail entry
  should include nested read-item metadata from the configured child edit View.
- Keep the adapter generic and reuse existing `ViewDataService`; do not bind
  collection detail fields to concrete business DTO rows.

# Changes

- Added `ViewAdapter.getReadItemView(View, Function<Long, View>)`.
- Kept the existing `getReadItemView(View)` overload for simple callers.
- Moved shared read-item field population into one helper.
- For collection `ViewItem`s, emit `ReadItemViewDetailInfo` and resolve
  `editViewId` through the supplied view resolver to fill nested `Items`.
- Updated `ViewController.getReadItemView` to pass a resolver backed by
  `ViewDataService.getViewData(childViewId, token)`.
- Seeded Docker `Order.items` View items with `edit_view_id=101` in addition
  to the existing list/selected child View IDs.
- Updated Vue detail group composition so child table columns come from
  `getreaditemview.DetailViews[].Items` first, then merge
  `querydatadetail.Items` row data under that View metadata.
- Updated task/parity docs.

# Validation

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=ViewAdapterTest,ViewControllerLegacyGetReadItemViewTest test`
  - Passed: 25 tests.
- `cd frontend && npm test -- --run`
  - Passed: 3 files, 66 tests.
- `cd frontend && npm run build`
  - Passed: `vue-tsc --noEmit` and Vite production build.
- `docker compose up -d --build frontend`
  - Passed. Rebuilt and restarted backend/frontend containers.
- `python scripts/runtime_doctor.py`
  - Passed all checks after Docker rebuild.

# Runtime Evidence

- Existing Docker DB was synchronized with
  `UPDATE fool_sys_view_item SET edit_view_id = 101, list_view_id = 101, selected_view_id = 101 WHERE view_id IN (100, 102) AND model_property = 'items'`.
- `docker compose ps`
  - backend: `Up`, mapped `0.0.0.0:8080->8080/tcp`
  - frontend: `Up`, mapped `0.0.0.0:8081->80/tcp`
  - mysql: `Up (healthy)`, mapped `127.0.0.1:3307->3306/tcp`
  - redis: `Up (healthy)`, mapped `127.0.0.1:6380->6379/tcp`
- Direct backend probe:
  - `http://localhost:8080` `getlistview(100)` -> `DetailViewId=102`
  - `getreaditemview(102)` -> `DetailViews[0].PrpId=items`,
    nested item count `2`, first nested item `PrpId=itemId`,
    `PrpShowName=Item ID`.
  - `querydatadetail(102, 1001)` -> `items.listViewId=101`,
    `items.detailViewId=101`, `items.selectedView=101`, and child
    properties `itemId`, `itemName`.
- Frontend proxy probe:
  - `http://localhost:8081` returned the same `DetailViewId=102`,
    `DetailViews[0].PrpId=items`, nested item count `2`, first nested
    item `PrpId=itemId`, `PrpShowName=Item ID`.
- `curl http://localhost:8081/` returned the rebuilt Vue bundle
  `/assets/index-DNkr21d8.js`.

# Risks

- `DetailViews` is filled only when a collection item has a configured
  `editViewId`; collection rows without an edit View still return an empty
  nested item list.

# FoolFrame Migration Parity

Date: 2026-06-24

This document records the current migration state from `../FoolFrame` to `fool-service`.

## Current Docker Baseline

- Backend Maven reactor builds in Docker with Java 17:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DskipTests package`
- Backend Compose image builds:
  `docker compose build backend`
- Full Compose stack starts backend, frontend, MySQL, and Redis:
  `docker compose up -d --build`
- The one-shot Compose `db-migrate` service replays every idempotent
  `docker/mysql/init/*.sql` file after MySQL is healthy and must exit `0`
  before backend startup. This upgrades existing named volumes instead of
  relying only on MySQL's first-boot `/docker-entrypoint-initdb.d` behavior.
- Docker runtime smoke is repeatable through:
  `python scripts/runtime_doctor.py`
  The smoke now fails if the Docker `car_wash` database is missing the core
  legacy model/view/operation columns required by the View-first workflow.
- Full backend Maven tests run inside the Compose network without datasource
  command-line overrides:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn test`
- Smoke routes verified:
  `curl http://localhost:8081/`
  `curl http://localhost:8080/test`
  `curl -H 'Content-Type: application/json' -d '{"viewId":100}' http://localhost:8080/api/v1/view/get-view`
  `curl -H 'Content-Type: application/json' -d '{"viewId":100}' http://localhost:8080/api/v1/view/getlistview`
  `curl -H 'Content-Type: application/json' -d '{"Token":"token-1","ViewId":100}' http://localhost:8080/api/v1/view/getlistview`
  `curl -H 'Content-Type: application/json' -d '{"id":100}' http://localhost:8081/api/v1/view/getlistview`
  `curl -H 'Content-Type: application/json' -d '{"viewId":100}' http://localhost:8080/api/v1/view/getreaditemview`
  `curl -H 'Content-Type: application/json' -d '{"viewId":100,"pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list`
  `curl -H 'Content-Type: application/json' -d '{"ViewId":100,"pageInfo":{"pageSize":10,"pageIndex":1},"filter":{"orderId":{"values":["1001","1002"]}}}' http://localhost:8080/api/v1/data/query-list`
  `curl -H 'Content-Type: application/json' -d '{"viewId":100,"pageSize":10,"pageIndex":1,"queryFilter":"order_state=\"0\""}' http://localhost:8080/api/v1/data/querydata`
  `curl -H 'Content-Type: application/json' -d '{"Token":"token-1","ViewId":100,"PageSize":2,"PageIndex":1,"QueryFilter":"order_state=\"0\"","OrderByItem":0,"OrderByType":0}' http://localhost:8080/api/v1/data/querydata`
  `curl -H 'Content-Type: application/json' -d '{"viewid":100,"filter":null,"page":1,"pagesize":2,"orderitem":0,"ordertype":0}' http://localhost:8081/api/v1/data/querylist`
  `curl -H 'Content-Type: application/json' -d '{"viewId":100,"objId":"1001"}' http://localhost:8080/api/v1/data/querydatadetail`
  `curl -H 'Content-Type: application/json' -d '{"viewId":100,"objId":"","IdExp":"$1001"}' http://localhost:8081/api/v1/data/querydatadetail`
  `curl -H 'Content-Type: application/json' -d '{"id":100,"objid":"1001","idexp":""}' http://localhost:8081/api/v1/data/querydatadetail`
  `curl -H 'Content-Type: application/json' -d '{"viewId":100,"objId":"","IdExp":""}' http://localhost:8081/api/v1/data/querydatadetail`
  `curl -H 'Content-Type: application/json' -d '{"ViewId":100,"ParentObjId":"5001"}' http://localhost:8080/api/v1/data/initnew`
  `curl -H 'Content-Type: application/json' -d '{"ViewId":100,"ParentObjId":"5001"}' http://localhost:8081/api/v1/data/initnew`
  `curl -H 'Content-Type: application/json' -d '{"modelId":"102"}' http://localhost:8080/api/v1/data/getenums`
  `curl -H 'Content-Type: application/json' -d '{"modelid":"102"}' http://localhost:8081/api/v1/data/getenum`
  `curl -H 'Content-Type: application/json' -d '{"viewId":100,"viewItemId":"symbol","text":"BTC"}' http://localhost:8080/api/v1/data/inputquery`
  `curl -H 'Content-Type: application/json' -d '{"ViewId":100,"ViewItemId":"Customer","Text":"Ada","IsAdded":false}' http://localhost:8080/api/v1/data/inputquery`
  `curl -H 'Content-Type: application/json' -d '{"Text":"Ada","ViewId":100,"ViewItemId":"Customer","ModelID":"103","ObjID":"1001","OwnerId":"5001","IsAdded":false}' http://localhost:8080/api/v1/data/inputquery`
  `curl -H 'Content-Type: application/json' -d '{"viewid":100,"itemid":"Customer","text":"Ada","objid":"1001","ownerid":"","newadd":false}' http://localhost:8081/api/v1/data/inputquery`
  `curl -H 'Content-Type: application/json' -d '{"ViewName":"100","ViewItemId":"Customer","Text":"","ObjID":"1001","OwnerId":"","IsAdded":false}' http://localhost:8081/api/v1/data/inputquery`
  `curl -H 'Content-Type: application/json' -d '{"saveObj":{"id":"1001","viewID":"100","propertyies":[{"key":"symbol","value":"BTC-USDT"},{"key":"state","value":"0"}]}}' http://localhost:8080/api/v1/data/saveobj`
  `curl -H 'Content-Type: application/json' -d '{"saveObj":{"id":"1001","viewID":"100","propertyies":[{"key":"symbol","value":"BTC-USDT"},{"key":"state","value":"0"}],"itemproperties":[{"key":"items","items":[{"itemId":"2001","isExist":true,"propertyies":[{"key":"itemName","value":"Updated item"}]}],"addedItems":[{"itemId":"2003","isExist":true,"propertyies":[{"key":"itemName","value":"New item"}]}],"delteItems":[{"itemId":"2004","isExist":true,"propertyies":[]}]}]}}}' http://localhost:8080/api/v1/data/saveobj`
  `curl -H 'Content-Type: application/json' -d '{"obj":{"Id":"930003","ViewID":"100","Propertyies":[{"Key":"symbol","Value":"SOL-USDT"}],"Itemproperties":[]}}' http://localhost:8081/api/v1/data/save`
  `curl -H 'Content-Type: application/json' -d '{"SaveObj":{"id":"930001","viewID":"100","propertyies":[{"key":"symbol","value":"SOL-USDT"},{"key":"state","value":"0"}],"itemproperties":[]}}' http://localhost:8080/api/v1/data/savenewobj`
  `curl -H 'Content-Type: application/json' -d '{"SaveObj":{"id":"930002","viewID":"100","propertyies":[{"key":"symbol","value":"SOL-USDT"},{"key":"state","value":"0"}],"itemproperties":[]}}' http://localhost:8081/api/v1/data/savenewobj`
  `curl -H 'Content-Type: application/json' -d '{"obj":{"Id":"930004","ViewID":"100","Propertyies":[{"Key":"symbol","Value":"SOL-USDT"}],"Itemproperties":[]},"ownerviewid":"","ownerid":"","prpid":""}' http://localhost:8081/api/v1/data/new`
  `curl -H 'Content-Type: application/json' -d '{"ViewId":100,"ObjectId":"970731","OperationId":7001}' http://localhost:8080/api/v1/data/runoperation`
  `curl -H 'Content-Type: application/json' -d '{"ViewId":100,"ObjectId":"970732","OperationId":7001}' http://localhost:8081/api/v1/data/runoperation`
  `curl -H 'Content-Type: application/json' -d '{"ViewId":100,"ObjectId":"1001","OperationId":7002}' http://localhost:8080/api/v1/data/runoperation`
  `curl -H 'Content-Type: application/json' -d '{"viewid":100,"objid":"1001","opid":0}' http://localhost:8081/api/v1/data/exoperation`
  `curl -H 'Content-Type: application/json' -d '{"viewId":100,"currentPage":1,"pageSize":10,"queryFilter":"order_state=\"0\"","reportCols":[{"colName":"Symbol","index":1},{"colName":"State","index":2}]}' http://localhost:8080/api/v1/report/makereport`
  `curl -H 'Content-Type: application/json' -d '{"ViewId":100,"CurrentPage":1,"PageSize":10,"FilterExp":{"Col":{"Name":"order_state"},"CompareOp":{"ID":"1","Name":"等于"},"ValueExp":"0","ValueFmt":"Open"},"ReportCols":[{"ColName":"Symbol","Index":1},{"ColName":"State","Index":2}]}' http://localhost:8080/api/v1/report/makereport`
  `curl -H 'Content-Type: application/json' -d '{"ViewId":100,"CurrentPage":1,"PageSize":10,"FilterExp":{"Col":{"Name":"symbol"},"CompareOp":{"ID":"7","Name":"包含"},"ValueExp":"BTC","ValueFmt":"BTC"},"ReportCols":[{"ColName":"Symbol","Index":1},{"ColName":"State","Index":2}]}' http://localhost:8080/api/v1/report/makereport`
  `curl -H 'Content-Type: application/json' -d '{"ViewId":100,"CurrentPage":1,"PageSize":10,"FilterExp":{"Col":{"ID":"1002"},"CompareOp":{"ID":"7","Name":"包含"},"ValueExp":"BTC","ValueFmt":"BTC"},"ReportCols":[{"ColName":"Symbol","Index":1},{"ColName":"State","Index":2}]}' http://localhost:8080/api/v1/report/makereport`
  `curl -H 'Content-Type: application/json' -d '{"ViewId":100,"CurrentPage":1,"PageSize":10,"FilterExp":{"FirstExp":{"Col":{"Name":"order_state"},"CompareOp":{"ID":"1","Name":"等于"},"ValueExp":"0","ValueFmt":"Open"},"Sequences":[{"BoolOp":{"DBName":"and","ShowName":"与"},"AddedExp":{"Col":{"Name":"symbol"},"CompareOp":{"ID":"7","Name":"包含"},"ValueExp":"BTC","ValueFmt":"BTC"}},{"BoolOp":{"DBName":"or","ShowName":"或"},"AddedExp":{"Col":{"Name":"order_price"},"CompareOp":{"ID":"3","Name":"大于"},"ValueExp":"100","ValueFmt":"100"}}]},"ReportCols":[{"ColName":"Symbol","Index":1},{"ColName":"State","Index":2}]}' http://localhost:8080/api/v1/report/makereport`
  `curl -H 'Content-Type: application/json' -d '{"ViewId":100,"CurrentPage":1,"PageSize":10,"QueryFilter":"order_state=\"0\"","ReportCols":[{"ColName":"Symbol","Index":1},{"ColName":"State","Index":2}]}' http://localhost:8080/api/v1/report/getrpt`
  `curl -H 'Content-Type: application/json' -d '{"viewid":100,"pageindex":1,"pagesize":10,"cols":[{"ColName":"Symbol","Index":1},{"ColName":"State","Index":2}],"exp":null}' http://localhost:8081/api/v1/report/mkrpt`
  `curl -H 'Content-Type: application/json' -d '{"ViewId":100}' http://localhost:8080/api/v1/report/getmkqview`
  `curl -H 'Content-Type: application/json' -d '{"viewid":100}' http://localhost:8080/api/v1/report/mkqview`
  `curl -H 'Content-Type: application/json' -d '{"ViewId":100,"ReportName":"Order Daily","ReportCols":[{"ColName":"Symbol","Index":1}],"FilterExp":{"Col":{"Name":"order_state"},"CompareOp":{"ID":"1","Name":"等于"},"ValueExp":"0","ValueFmt":"Open"}}' http://localhost:8080/api/v1/report/saverpt`
  `curl -H 'Content-Type: application/json' -d '{"ViewId":100}' http://localhost:8081/api/v1/report/getmkqview`
  `curl -H 'Content-Type: application/json' -d '{"ViewId":100,"CurrentPage":1,"PageSize":10,"QueryFilter":"order_state=\"0\"","ReportCols":[{"ColName":"Symbol","Index":1},{"ColName":"State","Index":2}]}' http://localhost:8081/api/v1/report/getrpt`
  `curl -H 'Content-Type: application/json' -d '{"ViewId":100,"ReportName":"Order Daily","ReportCols":[{"ColName":"Symbol","Index":1}],"FilterExp":{"Col":{"Name":"order_state"},"CompareOp":{"ID":"1","Name":"等于"},"ValueExp":"0","ValueFmt":"Open"}}' http://localhost:8081/api/v1/report/saverpt`
  `curl -H 'Content-Type: application/json' -d '{"viewid":100,"reportname":"Order Daily","cols":[{"ColName":"Symbol","Index":1}],"exp":null}' http://localhost:8081/api/v1/report/saverpt`
  `curl -H 'Content-Type: application/json' -d '{"token":"<login-token>"}' http://localhost:8080/api/v1/message/getmsg`
  `curl -H 'Content-Type: application/json' -d '{"token":"<login-token>"}' http://localhost:8081/api/v1/message/getmsg`
  `curl -H 'Content-Type: application/json' -d '{"Token":"<login-token>"}' http://localhost:8081/api/v1/getmsg`
  `curl -H 'Content-Type: application/json' -d '{"token":"<login-token>"}' http://localhost:8080/api/v1/message/getnotify`
  `curl -H 'Content-Type: application/json' -d '{"token":"<login-token>"}' http://localhost:8081/api/v1/message/getnotify`
  `curl -H 'Content-Type: application/json' -d '{"Token":"<login-token>"}' http://localhost:8080/api/v1/auth/getuserinfo`
  `curl -H 'Content-Type: application/json' -d '{"Token":"<login-token>"}' http://localhost:8081/api/v1/auth/getuserinfo`
  `curl -H 'Content-Type: application/json' -d '{"Token":"<login-token>"}' http://localhost:8080/api/v1/auth/getapp`
  `curl -H 'Content-Type: application/json' -d '{"Token":"<login-token>"}' http://localhost:8081/api/v1/auth/getapp`
  `curl -H 'Content-Type: application/json' -d '{"AppId":"fool-service","AppKey":"fool-service"}' http://localhost:8080/api/v1/auth/initapp`
  `curl -H 'Content-Type: application/json' -d '{"AppId":"fool-service","AppKey":"fool-service"}' http://localhost:8081/api/v1/auth/initapp`
  `curl -H 'Content-Type: application/json' -d '"<login-token>"' http://localhost:8080/api/v1/auth/getmain`
  `curl -H 'Content-Type: application/json' -d '"<login-token>"' http://localhost:8081/api/v1/auth/getmain`
  `curl -H 'Content-Type: application/json' -d '{"Token":"<login-token>"}' http://localhost:8080/api/v1/auth/getsubmenu`
  `curl -H 'Content-Type: application/json' -d '{"Token":"<login-token>","ParentAuthCode":"1"}' http://localhost:8080/api/v1/auth/getsubmenu`
  `curl -H 'Content-Type: application/json' -d '{"Token":"<login-token>","ParentAuthCode":"1"}' http://localhost:8081/api/v1/auth/getsubmenu`
  `curl -H 'Content-Type: application/json' -d '{"Token":"<login-token>","authcode":"1"}' http://localhost:8081/api/v1/auth/getmenu`
  `curl -H 'Content-Type: application/json' -d '{}' http://localhost:8080/api/v1/auth/getcheckcode`
  `curl -H 'Content-Type: application/json' -d '{}' http://localhost:8081/api/v1/auth/getchk`
  `curl -H 'Content-Type: application/json' -d '{"key":"<check-key>","code":"<check-code>"}' http://localhost:8080/api/v1/auth/checkcode`
  `curl -H 'Content-Type: application/json' -d '{}' http://localhost:8081/api/v1/auth/getcheckcode`
  `curl -H 'Content-Type: application/json' -d '{"key":"<check-key>","code":"<check-code>"}' http://localhost:8081/api/v1/auth/checkcode`
  `curl -H 'Content-Type: application/json' -d '{"UserId":"admin","PassWord":"admin","DbId":"car_wash","CheckCode":"<check-code>","AppId":"fool-service","AppKey":"fool-service","CheckCodeKey":"<check-key>"}' http://localhost:8080/api/v1/auth/loginv2`
  `curl -H 'Content-Type: application/json' -d '{"UserId":"admin","PassWord":"admin","DbId":"car_wash","CheckCode":"<check-code>","AppId":"fool-service","AppKey":"fool-service","CheckCodeKey":"<check-key>"}' http://localhost:8081/api/v1/auth/loginv2`
  `curl -H 'Content-Type: application/json' -d '{"name":"admin","pwd":"admin","dbid":"car_wash","chk":"<check-code>","chkid":"<check-key>","AppId":"fool-service","AppKey":"fool-service"}' http://localhost:8081/api/v1/auth/loginv2`
  `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e 'SELECT item_id,order_id,item_name FROM market_order_item WHERE order_id = 1001 ORDER BY item_id;'`
- Auth smoke verified with Docker-seeded admin user:
  `POST /api/v1/auth/login`, `POST /api/v1/auth/profile`, and
  `POST /api/v1/auth/auth-menus`, plus logout token invalidation through
  `POST /api/v1/auth/logout`.
- Event scheduler smoke verified with a one-off scheduler-enabled backend
  container against the Docker-seeded `Order` event definition:
  `FOOL_EVENT_SCHEDULER_ENABLED=true` produced one `SW_EVT_EVENT` row for
  object `1001` and one `SW_SYS_MSG` row for admin.
- Vue frontend local and Compose builds pass:
  `cd frontend && npm test && npm run build`
  `docker compose build frontend`
- Project-local Maven resolution uses `.mvn/settings.xml` to mirror Central through Aliyun for repeatable Docker builds on this network.
- Module profile-specific `application-dev.yml` and `application-test.yml`
  files use Spring Boot 2.4+ `spring.config.activate.on-profile`
  activation syntax instead of the deprecated `spring.profiles` document
  selector.

## Recent Parity Increments

- 2026-07-15: completed the missing installation boundary documented in
  `docs/installation-and-initialization.md`. Docker startup now discovers 79
  framework models after `db-migrate`, installs code-owned metadata, executes
  resumable model/relation DDL, and creates missing default Views before the
  application is considered initialized. Composite `@Id` groups preserve their
  declared types, legacy relation matching is null-safe, and incomplete DDL can
  restart across an already-added relation column. Runtime acceptance recorded
  83 unique model rows including the four Market seed models, 118 unique Views,
  478 properties, and 13 relations; a backend restart kept every count stable,
  produced zero duplicate model/relation groups and zero property rewrites.
- 2026-07-13: restored `viewWithChart.jade`'s template-specific layout. Its
  search form now stays left aligned, while normal `view.jade` keeps the old
  `navbar-right` placement, and the Data/Chart tab rule again spans the content
  width instead of stopping at an invented 320px cap. Docker browser geometry
  moved the chart search input from `left=936` to `left=40` and expanded the
  desktop tabs from 320px to 1200px; `/view101` remained right aligned. At
  390px, the toolbar and tabs both measured 330px with no page overflow, the
  Chart tab rendered, and browser logs were empty.
- 2026-07-13: restored `view.jade` / `viewWithChart.jade` pagination from
  `navbar.js`. Main list pages now render the record total above Previous,
  up to seven directly selectable page links, and Next; the Vue-only First,
  Last, and current-page report controls were removed. Main list `FreshTime`
  was also removed because the old templates show it only in Sudoku partials.
  Authenticated desktop/mobile browser acceptance expanded the local list from
  8 to 11 rows, selected page 2, observed its single row, then removed all
  three exact test rows and verified the list returned to 8 rows on page 1.
- 2026-07-13: fixed same-model operation execution when nullable legacy
  `ArgModel` metadata is hydrated as `0`. `DataQueryService` now treats only a
  positive argument-model id as a cross-model operation, so the seeded detail
  `保存` command reaches its normal update path and returns `保存成功`. An
  authenticated browser replay on `/view100/1002` verified the old `执行结果`
  success dialog and `确定` dismissal; a full-row database snapshot was
  identical before and after the idempotent save. The same replay clicked the
  `Orders List` Sudoku refresh, advanced `FreshTime`, retained five rows and
  stayed on `/view103`, with no browser console errors.
- 2026-07-12: returned `getnotify` to a protocol-only compatibility surface.
  The old Web never calls it and `DataService.GetNotify` throws
  `NotImplementedException`, so Vue no longer polls it or renders invented menu
  badges; response aliases, helpers, backend route, and runtime checks remain.
- 2026-07-12: restored old conditional menu-image behavior for top and child
  navigation. Vue now reads Pascal/camel `ImageUrl` through one adapter and
  renders metadata-provided images at the old 30x30 size; empty values allocate
  no placeholder. The Docker seed currently exercises the empty-image path.
- 2026-07-12: restored `tbar.jade` navigation order by moving the text-only
  `安全退出` action after the metadata menu. The right shell area now contains
  only the old avatar/user presentation; the mobile Drawer exposes the same
  logout action after its menu without duplicating shell component state.
- 2026-07-12: matched `tbar.jade`'s text-only `安全退出` command by removing
  the invented sign-out icon while preserving the logout request, disabled
  state, and return to the signed-out flow.
- 2026-07-12: restored old `default.jade` user-avatar behavior end to end.
  Legacy auth responses now hydrate `UserAvtarUrl` from
  `SW_AUTH_USER.USER_AVTAR`; the Vue shell reads it through the compatibility
  adapter and renders a circular image when present, retaining the user icon
  only as an empty-value fallback. Backend/frontend, Compose, runtime-doctor,
  and harness validation are recorded in the matching delivery evidence.
- 2026-07-12: matched the old `soway.css` avatar layout by restoring its exact
  50x50 dimensions in the Vue shell; the previous 40x40 size had no legacy
  source basis. The existing circular crop and empty-value fallback remain.
- 2026-07-12: removed Vue-only current-View `active` highlighting from top and
  child menu items. Old `tbar.jade` and `menuinfo.js` never derive active menu
  state from the route; Vue now retains only the equivalent dropdown-open state.
- 2026-07-12: restored the old Bootstrap navbar and dropdown visual contract.
  The Vue shell now uses the old neutral background, border, text, hover, and
  open-state colors; top actions occupy the 50px navbar height, and child menus
  use the old 160px dropdown/list-group geometry instead of indigo rounded UI.
- 2026-07-12: restored `default.jade`'s `h2 > small` brand structure and exact
  Bootstrap typography. Desktop and Drawer now share the old 21px/500/1.1
  application heading and 65% normal-weight version instead of a 24px strong
  label with a fixed 12px version and invented 8px gap.
- 2026-07-12: restored the old right-side user presentation metrics. User text
  now follows Bootstrap's 10px normal-weight blue link treatment and 10px/15px
  padding instead of a custom bold slate label. The empty-avatar icon fallback
  now occupies the same fixed 50x50 slot as the old `.avtar` image.
- 2026-07-12: restored `detailview.js`'s edit-state business-operation guard.
  Operation buttons are no longer silently disabled while a detail is being
  edited; clicking one now shows the old `请先保存当前信息` feedback and does not
  issue a `runoperation` request. Pending requests still disable the buttons.
- 2026-07-12: restored the old detail error-dialog outlet. The detail panel now
  receives shared action errors and opens a dismissible `发生错误` modal, making
  edit guards, local validation, and failed detail requests visible instead of
  leaving `App.errorMessage` with no rendered consumer on detail routes.
- 2026-07-12: restored `operation.js`/`showdetailinfo` result interaction.
  Completed business operations now open an `执行结果` modal with the old
  `操作成功`/`操作失败` summary and return message, replacing the Vue-only
  inline Message. Its old `确定` command clears the existing result state.
- 2026-07-12: restored legacy operation-result timing. Vue no longer waits for
  extra list and detail queries after a successful operation; like
  `operation.js`, it presents the response immediately and leaves refresh to the
  existing query, paging, and timer interactions.
- 2026-07-12: restored `detailview.js` child-delete staging. Deleting a child
  now hides it from the local detail collection and merges its `DelteItems`
  payload by group; the parent Save submits all staged deletes. The Vue-only
  confirmation and immediate `saveObj`/`queryDetail` cycle were removed.
- 2026-07-12: restored `detailview.js` inline child-edit staging. Parent edit
  leaves child rows in display mode until their row Edit action; only one row
  edits at a time, row Save upserts the group's `Items`, and switching rows
  stages the previous row. No child-update request runs before parent Save.
- 2026-07-12: restored child-add persistence timing and View-id roles from
  `detailview.js`. Manual and selected-existing rows now remain local in
  `AddedItems` until parent Save, removing immediate save/query cycles; deleting
  an unsaved row discards it locally. Candidate lookup uses `ListViewId`, while
  a configured `SelectedView` opens the old owner-aware child-new route.
- 2026-07-12: restored `AddedItems.IsExist` identity semantics. Manual child
  rows now send false so their UI-only temporary key is ignored and the model
  creates the real id; selected-existing rows send true and retain their loaded
  candidate id, matching old `DataFormator.ObjUpdateToProxy` behavior.
- 2026-07-12: restored the inline child-add interaction from `detailview.js`.
  The invented always-visible add form is removed; Add now inserts a metadata-
  shaped blank table row, enters that row's editor, stages the previously edited
  row when necessary, and writes `AddedItems` only when row Save is clicked.
- 2026-07-12: restored child-action availability from `detailView.jade`.
  Add, inline Edit, and Delete controls remain visible outside parent edit mode;
  like `edititem(...)`, inline Edit returns without changing state until parent
  Edit is active, while Add and Delete retain their original local staging paths.
- 2026-07-12: restored parent-new child collection rendering and add guard.
  View-defined child tabs remain visible on `/new{id}`; every child-add path is
  stopped before requests and opens the old `操作提示` success-style modal with
  `请先保存当前内容，再新建子项` and an explicit `确定` action.
- 2026-07-12: restored `DetailViewId` child-row action conditions. Inline
  `编辑` is hidden for select-existing groups, non-select groups retain their
  deep Edit link, and every group with a detail View regains the separate
  `详细` link. Operation and empty-state column spans follow that action matrix.
- 2026-07-12: restored `beginsave()` loading-dialog lifecycle. Parent Save now
  opens a non-dismissible `保存中` modal with `正在保存，请稍后....`; successful
  save closes it and returns only after the hide transition, while failed saves
  close it without navigation and keep the existing error surface.
- 2026-07-12: restored select-existing query timing from `initQueryView`.
  Opening the picker now loads only `ListViewId` metadata and shows
  runtime `记录未知 请查询`; candidate data is requested only by Find or paging, and
  zero results become `暂无候选记录。` only after an explicit query completes.
- 2026-07-12: restored the candidate View loading-dialog order. Add now opens
  the non-dismissible `加载中` / `正在加载，请稍后....` modal, awaits successful
  View metadata, closes loading, and only then opens the picker. Failed metadata
  loads leave the picker closed and use the existing error surface.
- 2026-07-12: restored candidate `NavbarController` result feedback. The picker
  now uses `initQueryView`'s runtime `记录未知 请查询`, changes to `共N条记录`
  after every successful query including zero results, and places record status
  and paging after the candidate table like `detailView.jade`.
- 2026-07-12: restored candidate query page timing. Explicit Find follows
  `querylistdata.js query()` and resets to page 1, while previous/next paging
  follows the Navbar callback and queries the selected target page unchanged.
- 2026-07-12: restored old `message.js` delivery behavior in the Vue shell.
  Each non-empty 15-second `getmsg` poll now immediately opens its first item
  in a `系统消息` modal with generation time, content, View-first detail
  navigation, and `确定`. The invented topbar bell, manual refresh, and history
  Popover were removed because `tbar.jade` has no message entry. Frontend,
  Compose, runtime-doctor, and harness validation are recorded in the matching
  delivery evidence.
- 2026-07-12: made top-level `TempFile` dispatch explicit. Empty/default
  `view`, `viewWithChart`, and `Sudoku` cover the old repository's actual
  top-level templates and route to list, chart, and panel renderers. Unknown
  custom template names now render a full-width migration warning and skip
  `querydata`, detail, and report surfaces instead of silently masquerading as
  a list and binding unrelated data. All 137 frontend tests, TypeScript/Vite
  and Compose frontend builds, deployed chart/Sudoku runtime doctor, and
  repository harness pass. Arbitrary external custom Jade execution is not
  claimed as a Vue feature.
- 2026-07-12: consolidated unsupported custom-template presentation. The data
  workflow still stops before querying an unknown `TempFile`, but the list
  panel now shows one Chinese warning with the metadata template name instead
  of simultaneous English error/warning messages. The transient no-View title
  is now `加载视图` rather than `Load a View`.
- 2026-07-12: completed report condition grouping beyond the old Web
  `mkreport.js` visual-only merge state. Vue can group consecutive conditions,
  wrap complete existing groups, and split the innermost group; the shared
  116-line `reportConditions.ts` module validates selection boundaries and
  serializes real recursive `FirstExp` / `Sequences` payloads. Four focused
  frontend tree tests and a new Java 17 `ReportControllerTest` prove nested
  `(A AND B) OR C` parentheses and column mapping. All 136 frontend tests,
  TypeScript/Vite and Compose frontend builds, 23 report controller tests,
  deployed runtime doctor, and repository harness pass.
- 2026-07-12: aligned report condition-group presentation with `mkreport.js`.
  Vue no longer exposes internal group-path ids as invented `G1 / G2` labels;
  nested groups use the old `#FFEBCD` / `#B2DFEE` alternating colors and the
  group-start command is again named `拆分分组`. Recursive filter serialization
  and View-metadata-bound condition values are unchanged.
- 2026-07-12: restored old `mkreport.js` condition-row initialization. Adding
  a condition now leaves its View field and comparison empty, keeps the first
  relation cell blank, and creates the value editor only after metadata drives
  a field/comparison choice. Visible relation labels again use `与/或`; the
  request values remain legacy `and/or`.
- 2026-07-15: restored `mkreport.js` report-condition value controls through
  the shared `setextype.js` metadata path. The selected report/View column now
  drives enum, Boolean, date, time, date-time, constrained numeric, and
  BusinessObject editors instead of collapsing every non-enum value to text.
  A 43-line adapter maps report metadata into the existing generic field editor;
  no concrete business DTO or duplicate editor was added. BusinessObject
  filters retain the selected id as `ValueExp` and suggestion text as
  `ValueFmt`, while Boolean filters retain `true/false` plus `是/否`. Full
  frontend tests passed (20 files, 203 tests), the production build passed, and
  authorized browser acceptance against exact commit `1c974a72` exercised all
  seven types through the real report UI and `mkrpt` request without changing
  the seeded 8-order/4-item data.
- 2026-07-12: restored the compact report condition table from `view.jade`.
  The condition tab again has icon-only add/group commands, `与/或` / `字段` /
  `运算` / `值` headers, old operation-column ordering, and add commands above
  and below the rows. The dense eight-column editor keeps a stable scrollable
  width on narrow screens instead of overlapping its controls.
- 2026-07-12: restored the `view.jade` save-report form. The report name starts
  blank instead of carrying an invented `视图报表` value, the tab shows the old
  `输入报表信息以保存该报表` heading, and the field again uses the available
  dialog width. The save request remains disabled until a name and output
  columns are present.
- 2026-07-12: restored the working part of the old report-result modal. Vue now
  renders every report matrix row as a normal striped/hoverable table row,
  uses the old `前一页` / `下一页` controls, exposes only `返回` in the result
  footer, and resets page state to one when returning to report setup. At that
  slice, the old markup's eventless export buttons remained omitted.
- 2026-07-12: restored Sudoku panel refresh semantics from old Web
  `includes/List.jade`, `querylistdata.js`, and `groupview.js`. Top-level and
  grouped list panels expose `FreshTime` with a text refresh command; targeted
  refresh merges by ViewId so shared list/chart/item data is retained. Each
  data panel schedules its own stable-key `AutoFreshTime` interval, while View
  changes and component teardown clear all panel timers. The 119-line
  `useSudokuPanels.ts` composable owns loading, merging, and timers, reducing
  `App.vue` from 1153 to 1057 lines. All 132 frontend
  tests, TypeScript/Vite and Compose frontend builds, deployed runtime doctor,
  and repository harness pass. Authenticated browser acceptance on 2026-07-13
  clicked the `Orders List` refresh, advanced its timestamp from `11:05:45` to
  `11:06:13`, preserved five rows, and kept the `/view103` route.
- 2026-07-12: restored `querylistdata.js` local `FreshTime` presentation. The
  shared Vue View helper now formats current ISO timestamps and legacy
  `/Date(...)/`-style values through the browser locale for main and Sudoku
  status rows, while preserving unparseable server text unchanged.
- 2026-07-12: repaired Vue Sudoku presentation drift against `Sudoku.jade` and
  `groupview.js`. Root panels now honor View-item `Width` on a 12-column grid,
  collapsing to one column at 390px; Group children use real tabs instead of
  simultaneously expanded nested panels. Update/refresh controls return below
  list/chart/map content, and invented panel-type badges and loaded-row status
  text are removed. Empty tables stay rendered with their View-defined columns.
- 2026-07-12: aligned detail child tables with `detailView.jade` by removing
  the invented visible `ID` column. Child `DataID` remains protocol-only for
  row keys, inline saves, deletes, BusinessObject lookup context, and detail
  navigation; visible columns now come only from each group's `Properties[]`
  View metadata plus the old operation column.
- 2026-07-12: aligned map marker fallback display with old `mapview.js`. When
  `EditType=18` title metadata is absent, Vue now derives the marker title from
  the first remaining View info item rather than exposing a fixed `Location`
  label. Empty/failed map states and the map region label now use Chinese copy.
- 2026-07-12: restored the old `setextype.js` BusinessObject typeahead
  interaction through the already-installed PrimeVue AutoComplete. Typing one
  character schedules the ViewId-bound `inputquery` request after 300ms;
  suggestions show metadata-derived `Text` and `Id`, empty results use the old
  Chinese message, and only a selected candidate id enters the save model.
  The manual search button/Listbox pair and its obsolete CSS were removed.
- 2026-07-12: replaced the invented Indigo/gradient frontend accent with old
  Bootstrap primary states. PrimeVue primary, hover, active, focus, and form
  border tokens now use `#337ab7`, `#286090`, and `#204d74`; the shell brand
  mark is flat blue rather than a purple gradient, and map markers reuse the
  same shared visual language. Neutral surfaces and semantic status colors are
  unchanged.
- 2026-07-12: aligned the shared SVG chart presentation with old
  `swchartLine.js`, which left colors to the then-current ECharts default
  theme. Vue now uses that legacy default color order, legends show only the
  View-derived series name rather than extra English `line` / `bar` /
  `scatter` type text, and fallback/accessible labels use Chinese copy.
- 2026-07-12: restored the old single View heading from `view.jade` and
  `detailView.jade`. Vue no longer repeats the business title in a workspace
  topbar or displays protocol `ViewName` beside it; list/detail panels remain
  the sole title owner, while the mobile-only topbar only carries the Drawer
  command. Unused `loadedViewName` / `viewTitle` workflow state and duplicate
  CSS were removed rather than hidden.
- 2026-07-12: removed the invented `视图 {id}` subtitle from the report dialog
  header. This matches `view.jade`, where ViewId is request context rather than
  visible report content; the prop remains the source for `getmkqview`,
  `mkrpt`, paging, and `saverpt` requests.
- 2026-07-12: restored old `mkreport.js` output selection semantics and layout.
  The output tab again uses candidate-column, output-method, and selected-column
  lists; users explicitly add outputs, may add one field with multiple output
  methods, reorder/delete selections, and set ascending/descending/no ordering.
  A 44-line pure `reportOutputs.ts` module owns duplicate prevention, stable
  reindexing, movement, removal, and ordering with focused tests. The dedicated
  171-line selector reduces `ViewReportPanel.vue` from 442 to 346 lines, and
  native `select size=10` controls keep the report chunk near its prior size.
- 2026-07-12: removed Vue's invented report page-size and reload controls from
  the output tab. Like `mkreport.js`, report requests keep page size 10 as
  protocol state and load candidate columns once from the active View metadata
  when the dialog opens.
- 2026-07-12: restored old Web operation feedback from `operation.js`. Vue now
  projects camel/Pascal `ReturnMsg`, shows a success or error result in the
  detail panel, keeps successful refresh behavior, and clears stale results
  when the View, selected object, or create flow changes. `ReturnViewId` and
  `ReturnObjId` remain protocol aliases rather than invented navigation because
  the old Web operation client does not use them. All 132 frontend tests, the
  TypeScript/Vite and Compose frontend builds, deployed runtime doctor, and
  repository harness pass. Authenticated browser acceptance on 2026-07-13
  replayed the seeded detail `保存` command, observed `操作成功` / `保存成功`, and
  closed the result with the old `确定` command without route or data drift.
- 2026-07-12: restored old Web child `DetailViewId` branching from
  `detailView.jade`. Child groups with no detail View remain inline editable;
  groups with a configured detail View render read-only row values and an Edit
  link to `/view{DetailViewId}/{DataID}`, avoiding two competing edit paths.
  Camel/Pascal detail View aliases share one helper. All 132 frontend tests,
  TypeScript/Vite build, and repository harness pass; the deep-link route is
  already covered by the Docker runtime doctor.
- 2026-07-12: removed Vue's invented main-list `New Row` and default `Open`
  commands. Matching old Web `view.jade` / `querylistdata.js`, create and row
  actions now render only from View `Operations` and their `RequireSelect` /
  result View metadata. The shared table defaults to no action, while the
  child existing-item picker explicitly opts into its old Select behavior.
  All 131 frontend tests, TypeScript/Vite build, and repository harness pass.
- 2026-07-12: restored the main detail interaction state from old Web
  `detailView.jade` / `detailview.js`. Existing records now render read-only
  values first, `querydatadetail.CanEdit` controls Edit access, new records
  enter edit mode directly, successful detail reloads return to read-only, and
  View operations plus child write controls are disabled or hidden while the
  main detail is editing. The Vue permission projection accepts both camel and
  Pascal response aliases without binding to a business DTO. All 131 frontend
  tests, TypeScript/Vite build, Compose frontend deployment, runtime doctor,
  and repository harness pass. Browser interaction stopped at a fresh local
  CAPTCHA, so visible Edit/Save replay remains in final browser acceptance.
- 2026-07-12: restored `detailView.jade`'s inline heading context. Existing
  details show `视图名 -对象ID`, create routes show `视图名 -新建`, and schema
  views retain the View title alone. The invented rounded object-ID tag and
  transient English `Detail` fallback were removed; IDs remain interaction and
  request state.
- 2026-07-12: restored the stable `detailView.jade` action toolbar. Existing
  editable records keep both Edit and Save mounted while `detailview.js`-style
  disabled states switch with edit mode, so controls no longer move after a
  click. New records start in edit mode with Save only, while metadata View
  operations remain disabled until the main edit is saved.
- 2026-07-12: removed invented protocol inputs from the select-existing child
  dialog. Matching `detailView.jade`, users now see only the query field and
  search command; candidate requests retain a fixed 10-row page size, keyword
  changes reset to page one, and previous/next commands own page movement.
  Unused page/page-size event bindings and composable mutators were deleted.
- 2026-07-12: aligned Vue shell menu hierarchy with old Web `leftbar.jade` /
  `menuinfo.js`. Loading `getsubmenu` no longer replaces the top-level menu;
  desktop and mobile render child entries under the selected parent, direct
  View navigation clears stale expansion state, and the mobile Drawer remains
  open while a parent menu loads. One shared `LegacyMenuNav.vue` component
  owns both render paths. All 130 frontend tests and the TypeScript/Vite
  production build pass; nested-menu Docker browser replay remains part of the
  final browser acceptance because the default seed may not expose a parent
  without a direct View.
- 2026-07-12: restored `default.jade`'s compact text application brand. The
  shell and mobile Drawer now render App name/version inline, the invented
  42px initial tile and its computed state are gone, and the desktop header is
  back on the old 50px navbar scale instead of a 72px product topbar.
- 2026-07-12: matched `detailView.jade` child tab labels to metadata
  `ItemName` only. Vue no longer adds an unrelated child-row count beside each
  label, and the count-only tab gap and text styles were deleted.
- 2026-07-12: restored the old child Add command placement for
  `SelectFromExists` groups. The single-button toolbar now uses the flex
  default left edge, matching `detailView.jade`'s `.btn-group`, instead of an
  invented right-aligned action row.
- 2026-07-12: restored `detailView.jade`'s two-cell child operation layout.
  The operation heading spans edit/save and delete cells, empty rows span both
  operation columns, and the obsolete one-cell action wrapper styles were
  removed without changing edit permissions or mutation handlers.
- 2026-07-12: restored `view.jade`'s text-only report tabs. The Vue report
  dialog no longer adds table, filter, or save icons to the metadata workflow,
  and the icon-only tab spacing rule was removed.
- 2026-07-12: restored the old list operation column behavior from
  `querylistdata.js`. Shared metadata tables no longer freeze the operation
  column to the right or right-align its commands, and the select-existing row
  command is plain `选择` text without an invented arrow.
- 2026-07-12: restored `view.jade`'s unmodified metadata table headings.
  Shared Vue tables now use normal 14px dark text on white, zero letter
  spacing, and no uppercase transformation, so View-provided English labels
  retain their original casing.
- 2026-07-12: restored `viewWithChart.jade`'s text-only data/chart tabs. Vue
  keeps the old `数据` then `图表` order and data-first state without adding
  table/chart icons or icon-only spacing.
- 2026-07-12: restored `view.jade`'s text-only list toolbar commands. Vue no
  longer adds search, chart, or plus icons to `查找`, `统计`, and metadata
  create operations; command order, View targets, and disabled states are
  unchanged.
- 2026-07-12: restored `detailView.jade` main-toolbar icon semantics. Edit
  keeps its pencil, while Save and every metadata View operation now reuse the
  old check mark instead of unrelated floppy-disk and lightning symbols.
- 2026-07-12: restored `view.jade`'s text-only report footer. `取消`, `确定`,
  `保存报表定义`, and result `返回` no longer carry invented icons; visibility,
  disabled states, and handlers are unchanged.
- 2026-07-12: restored text-only commands inside the `detailView.jade`
  select-existing child flow. Candidate `查找`, previous/next paging, and
  `取消` no longer carry invented icons; row selection still owns confirmation
  and closes immediately. The old inert `确定` placeholder was restored later
  as a visible no-op command.
- 2026-07-10: completed the authenticated Vue browser acceptance after explicit
  permission to read the local CAPTCHA and use the Docker `admin/admin`
  account. Desktop and 390x844 checks exercised the default View-first list,
  keyword search, detail selection, metadata new form, report execution, SVG
  chart, message API/target navigation, Sudoku list/chart/map/item/group
  panels, and legacy detail/new deep links without console warnings or
  page-level horizontal overflow. The replay exposed one shared-View bug:
  Sudoku list/chart/item and
  group panels can reuse `ListViewId=100`, so the item-detail result replaced
  already loaded row data in the panel record. `App.vue` now merges row and
  detail results for the same View id and only skips group loading when row
  data is present. The 390px shell navigation now wraps both View controls
  inside the viewport. All 130 frontend tests, the production build, Compose
  frontend deployment, runtime doctor, and repository harness pass.
- 2026-07-10: re-audited the vague remaining `SCPB05-Soway.Model` runtime
  mutation wording against FoolFrame `ModelBindingList`, `ModelMethodContext`,
  `SqlServer.dbContext`, the current implementation, and all 37
  `ModelDataServiceTest` cases. Collection add/set/delete triggers, external
  model create/update/delete/detail fallback and result mapping, filter and
  assembly commands, and active-datasource rollback are covered. All four live
  Docker `SW_SYS_MODEL` rows have null `MODEL_CON` and therefore use the active
  `car_wash` datasource; model-specific connection routing is no longer an
  open parity claim without a real migrated model that declares a different
  connection.
- 2026-07-10: moved the rendered list View surface out of `App.vue` into the
  134-line `ViewListPanel.vue`. Search, paging, create/row operations,
  View-driven table columns, `viewWithChart`, and Sudoku presentation now
  consume the existing `viewWorkflow` projections from one component while
  `App.vue` remains the single owner of requests and mutable workflow state.
  `App.vue` dropped from 1108 to 1048 lines; all 130 frontend tests, the
  TypeScript/Vite build, Compose rebuild, runtime doctor, and 390x844 login
  containment check pass.
- 2026-07-10: removed the second identical `spring-jdbc` declaration from
  `fool-dao/pom.xml`. The dependency remains compile-scoped through its
  original declaration; the five-module Java 17 reactor package and all 57
  `fool-common` / `fool-dao` tests pass without Maven's duplicate-dependency
  model warning.
- 2026-07-10: audited backend placeholder implementations against their live
  call sites and the old FoolFrame source, then deleted the unused 210-line
  `SelectStream` class. Every method in that class returned `null`, an empty
  value, or no result; the repository had no caller and FoolFrame had no
  counterpart. Future stream call sites should use the JDK `Stream` API
  directly instead of restoring a second proxy surface. The functional
  `fool-dao` main-source count now matches the documented 21 files, and the
  complete Java 17 Docker reactor package still succeeds.
- 2026-07-10: replaced duplicate top-level/Sudoku `<meter>` chart rows with a
  shared 125-line `LegacyChartPanel.vue`, restoring the useful chart behavior
  of old ECharts-backed `viewWithChart.js` / `swchartLine.js` without adding a
  second visualization dependency. The responsive SVG renderer consumes the
  existing `legacyChartData` projection and supports line, bar, and scatter
  series, zero/negative baselines, axes, sampled category labels, tooltips, and
  a multi-series legend. `App.vue` drops from 1122 to 1108 lines and
  `SudokuPanels.vue` from 126 to 115; Docker still proves chart `EditType`
  values `11` through `14` through the Vue proxy.
- 2026-07-10: replaced the Vue Sudoku Map coordinate table with an interactive
  map matching the useful behavior of old `mapview.js`. The new 90-line
  `LegacyMapPanel.vue` reuses `legacyMapMarkers` from rendered child View row
  `Items`, lazy-loads stable Leaflet 1.9.4 only when a Map panel exists, draws
  circle markers with safe DOM-built information popups, fits all valid
  coordinates, and keeps a compact location list. Vite emits Leaflet as a
  separate 150.12 kB chunk rather than adding it to the first-screen bundle;
  `SudokuPanels.vue` drops from 140 to 126 lines.
- 2026-07-10: removed the manual API console's remaining request staging from
  production Vue. Detail, `initnew`, `saveobj`, `savenewobj`, child collection
  mutations, and `runoperation` now build requests directly from the active
  View id, selected object, rendered fields, and typed `SaveKeypair[]` /
  `SaveItemProperty[]` values; the old `ref` mirrors and JSON stringify/parse
  round trip are gone. `useViewDataWorkflow` now synchronizes only the list,
  read-item, and detail View ids it owns. `App.vue` dropped from 1207 to 1122
  lines, and Docker runtime checks proved create/update/child/operation parity.
- 2026-07-10: audited all 25 routes declared by the old Web `app.js` and the
  Jade render templates. Current Spring/Vue routes cover the old entry, auth,
  View/detail/item/new, data, operation, enum, report, and message surfaces;
  `Group.jade` is only an empty document shell, while the placeholder
  `item.jade` is covered by the metadata-only item View. The production Vue
  shell now exposes only View-driven business workflows: `API Tools`, the
  migration map, raw response output, manual DTO forms, their dead state, and
  unused styles were deleted. `App.vue` dropped from 1769 to 1207 lines and
  the frontend code/test diff removed 897 lines while retaining shared
  workflow helpers.
- 2026-07-10: Docker startup now upgrades existing `car_wash` volumes through
  a one-shot `db-migrate` service that reuses all nine ordered, idempotent init
  SQL files before backend startup. Full replay passed twice against the
  existing 45-hour MySQL volume; runtime doctor now requires
  `compose:db-migrate` at `Exited (0)`, and the repository harness guards the
  migration mount, ordered loop, and backend success dependency.
- 2026-07-10: the Vue app now has a real signed-out login page matching the
  old `index.jade` / `login.js` flow: `initapp` supplies application and
  database metadata, `getcheckcode` supplies the rendered captcha, and
  `loginv2` accepts user-entered credentials before any View/data workflow is
  loaded. The default `admin/admin` auto-login and duplicate developer auth
  panels are removed; stale tokens and logout return to the same login flow,
  and successful login resumes the original `/view...` or `/new...` deep
  link. Direct detail/new routes now use the existing View-first detail
  component as a single-panel page instead of leaving an empty list shell.
- 2026-07-10: the Vue shell renders the signed-in user, 15-second legacy
  message polling, automatic message modal, and logout instead of separate
  developer panels. Message targets reuse the existing View-first detail/list
  loaders. `getnotify` remains protocol-only because the old Web never calls
  its unimplemented service. The runtime doctor refreshes its message seed
  before the destructive `getmsg` assertion so a live Vue poll cannot consume
  the shared smoke row first.
- 2026-07-10: the Vue list View toolbar now follows the old `view.jade`
  search workflow without exposing editable View IDs or raw `QueryFilter`
  syntax. The main workflow sends a trimmed `keyword` only after the View
  metadata is loaded. Raw compatibility `QueryFilter` payloads remain in the
  protocol builder but are not exposed in production Vue. Docker browser
  checks proved button and Enter-key searches (`BTC` / `ETH`), blank-search
  restoration, first-row detail selection, and 390px containment.
- 2026-07-10: the Vue list View now exposes the old `view.jade` report
  workflow from its `Report` command instead of three developer-only API
  panels. `ViewReportPanel.vue` loads candidate columns from `getmkqview`
  before report data, supports metadata-driven output type, order, output
  position, simple AND/OR conditions, paging, `mkrpt`, and the legacy
  `saverpt` no-op submission surface without exposing raw report JSON or SQL
  filters. Docker browser checks proved an unfiltered report, a `Symbol`
  contains `BTC` report, reordered output columns, and 390px containment.
- 2026-07-10: the Docker runtime doctor now requires legacy AppInfo aliases
  used by the old `layout.jade` / `default.jade` shell (`AppName`, `AppVer`,
  `AppNote`, `AppPowerBy`, `AppPowerUrl`, `AppLogoUrl`, `DefaultViewId`, and
  `AppId`) on `getapp` / `getmain` responses through the Vue proxy.
- 2026-07-10: `loginv2` responses now expose the old FoolFrame Web
  `user/login` wrapper flag `IsLogin` from the shared legacy login result DTO,
  matching `login.js` while keeping Vue auth flow on the existing protocol
  helpers; the Docker runtime doctor proves the alias through the Vue proxy.
- 2026-07-10: `querydatadetail` child collection property metadata now exposes
  the old `Properties[].Name` alias from the shared list-data value DTO,
  matching FoolFrame `ReadItemViewItem.Name` for child table headers while
  keeping Vue rendering on the existing View-first helpers; the Docker runtime
  doctor proves the alias through the Vue proxy.
- 2026-07-10: check-code responses now also expose the old FoolFrame Web
  `getchk` payload aliases `chkkey` and `chkimg` from the shared DTO, so the
  legacy `login.js` field names are present on `/api/v1/auth/getchk`; the
  Docker runtime doctor proves the aliases through the Vue proxy.
- 2026-07-10: the Docker runtime doctor now proves the old FoolFrame Web
  static `GET /about` and `GET /contact` routes return the built Vue HTML
  through the frontend fallback, matching the remaining static Jade route
  entries without adding duplicate Vue pages.
- 2026-07-10: the Docker runtime doctor now proves both `getmkqview` and the
  legacy Web `mkqview` wrapper return query catalog `CompareTypes` and
  `QueryTypes` through the Vue proxy, tying the seeded catalog relation rows
  to the report-column API surface.
- 2026-07-10: the Docker runtime doctor and repository harness now prove the
  query catalog property-index seed rows exist, not just the parent
  `SE_COMPARETYPE` / `SE_SELECTEDTYPE` rows, so report column `CompareTypes`
  and `QueryTypes` cannot silently lose their property-type bindings.
- 2026-07-10: the Vue detail panel now renders its heading from the loaded
  read-item View metadata, including the FoolFrame Pascal `ViewName` alias,
  instead of the hard-coded `Detail` heading. This keeps detail-page chrome
  aligned with the old `detailView.jade` View/Data-name heading without
  binding the title to `querydatadetail` data DTO fields.
- 2026-07-10: the Docker runtime doctor now proves the old FoolFrame Web root
  entry `GET /` returns the built Vue HTML bundle, matching the legacy
  `app.get('/', routes.index)` entry before the `/main` and View deep-link
  paths.
- 2026-07-10: the Docker runtime doctor now proves the running `car_wash`
  database has the seed rows required by the Vue/runtime smoke workflow:
  app shell, admin user, Order list/Sudoku views, query compare/select
  catalogs, event definition, and BTC smoke order. The live MySQL volume was
  repaired by replaying the existing idempotent `docker/mysql/init/010-query.sql`,
  and the seed-row probe pins the MySQL client to `utf8mb4` so Chinese query
  catalog labels are checked correctly.
- 2026-07-10: the repository harness now also guards key Docker `car_wash`
  seed markers for the Vue/runtime smoke workflow, including the app shell,
  admin/menu seed, Order list/Sudoku views, query catalogs, event definition,
  and BTC order row.
- 2026-07-10: the repository harness now guards the current Docker
  `car_wash` init script set (`001-market-order` through `010-query`) so a
  schema seed slice cannot be dropped while runtime-doctor schema columns
  still appear elsewhere.
- 2026-07-10: the Docker runtime doctor now proves the old FoolFrame Web
  Vue deep links `/main`, `/view:id`, `/view:id/:objid`, `/itemview:id`,
  `/new:id`, and `/new:id/:objid&:ownerviewid&:prpid` return the built Vue
  HTML bundle through the frontend container.
- 2026-07-10: Vue startup now recognizes the old FoolFrame Web read-item
  route `/itemview:id` as a View-definition page. It now loads only
  `getreaditemview(ViewId)`, renders the declared fields and child View
  columns, and does not call `querydatadetail` with an empty object id or bind
  the page to an arbitrary first business row.
- 2026-07-10: Vue startup now also recognizes the old FoolFrame Web
  detail/new routes `/view:id/:objid`, `/new:id`, and
  `/new:id/:objid&:ownerviewid&:prpid`. Detail routes load
  `getreaditemview(ViewId)` before `querydatadetail(ViewId, ObjId)`, and new
  routes reuse `initnew(ViewId, ParentObjId)` while preserving owner view,
  owner id, and property for the existing `savenewobj` path.
- 2026-07-12: restored `initnew` object-id precedence from `detailView.jade`.
  Vue now reads generic `Data.ObjId` / `data.objId` from the initialized View
  response and uses its local unique-id fallback only when the server leaves
  the value empty, instead of unconditionally replacing server identity.
- 2026-07-12: restored detail-save View identity from `detailView.jade`.
  Vue now sends generic `Data.Name` / `data.name` as `obj.ViewID` for both
  existing and new saves, falling back to the numeric View id only when the
  response omits its name. The runtime doctor sends the same name-shaped
  `ViewID` through the old `/data/new` and `/data/save` payload aliases.
- 2026-07-12: restored parent context for standalone detail BusinessObject
  lookup. Vue now sends generic `Data.ParentId` / `data.parentId` as
  `inputquery.ownerId`, with the parsed `/new:id/:parent&:view&:property`
  owner as fallback, matching old `setextype.js` source-list lookup behavior.
- 2026-07-12: completed the backend half of standalone child owner context.
  Like FoolFrame `IObjectProxy.Owner`, `getOneData` now uses generic child
  `Model.default_owner`, the owner's collection property, and
  `SW_SYS_RELATION` columns to restore the parent id. `ViewDataAdapter` emits
  that dynamic Owner id as `Data.ParentId`; top-level models still emit an
  empty parent id. The Docker `OrderItem` metadata now declares `Order` as its
  default owner for both current and legacy model catalogs.
- 2026-07-12: corrected `inputquery` View identity against FoolFrame
  `detailView.jade`, `detailview.js`, `setextype.js`, `DataFormator`, and
  Cloud-Social `soway.js`. Vue now sends the loaded detail `Data.Name` or
  collection `Items[].Name`; the backend hydrates collection `Name` from its
  linked list View instead of the child model. Old Web `viewid` and Cloud
  `ViewName` aliases accept that metadata name, while modern numeric `ViewId`
  remains the fallback and keeps precedence when both are present.
- 2026-07-12: restored View-first detail collection columns from FoolFrame
  `DataFormator.IObjectProxyToDetail`. `ViewDataService` now loads each
  collection's linked List View and attaches its property metadata; the detail
  adapter emits `Properties[]` and row `Values[]` from those ordered ViewItems.
  Child model properties absent from the View stay hidden, and View labels,
  editability, edit type, and format remain authoritative. The Docker doctor
  compares each detail group directly with its `ListViewId` metadata.
- 2026-07-12: removed the migrated detail adapter's incorrect
  `ItemEditType.Format` filters. FoolFrame `DataFormator.IObjectProxyToDetail`
  includes every non-collection ViewItem in `SimpleData` and every collection
  ViewItem in `Items`, regardless of edit type. List rendering still consumes
  Format items only as row classes, so the two View contexts no longer share
  an invented exclusion rule.
- 2026-07-10: Vue startup now reads the old FoolFrame Web list route
  `/view:id` from `window.location.pathname` and uses that View id before the
  legacy app default, keeping the startup path on `getlistview(ViewId)` then
  `querydata(ViewId)` instead of falling back to a seeded business DTO.
- 2026-07-10: backend message polling now exposes the old FoolFrame Web
  root `POST /getmsg` shape as `/api/v1/getmsg`, reusing the migrated
  `getmsg` service path and legacy `Messages` aliases. The Docker runtime
  doctor proves the route through the Vue proxy.
- 2026-07-10: `getmsg` now serializes legacy `GernerationTime` as
  `/Date(ms)/`, matching the parser in FoolFrame `message.js`. The Docker
  runtime doctor seeds a non-empty `SW_SYS_MSG` row and proves `MessageID`,
  `GernerationTime`, `MessageContent`, `ResultView`, and `ResultKey` through
  the Vue proxy instead of accepting an empty `Messages` list.
- 2026-07-10: backend `inputquery` compatibility was initially pinned to a
  numeric value inside Cloud-Social `ViewName`. The 2026-07-12 source audit
  corrected this: FoolFrame passes the rendered metadata View name through
  both Web `viewid` and Cloud-Social `ViewName`.
- 2026-07-10: backend `getlistview` now accepts the old FoolFrame Web `/view`
  body shape from `routes.getqueryview`, including the `id` alias at the
  shared `ViewDataRequest` boundary. The Docker runtime doctor proves the
  shape through the Vue proxy.
- 2026-07-10: backend `querydatadetail` now accepts the old FoolFrame Web
  `/itemview` payload shape from `routes.getItemPost`, including
  `id` / `objid` / `idexp` aliases at the shared DTO boundary. The Docker
  runtime doctor proves the shape through the Vue proxy.
- 2026-07-10: the Docker runtime doctor now proves the old FoolFrame Web
  `/report/saverpt` report-definition payload through `/api/v1/report/saverpt`
  with `viewid` / `cols` / `exp` / `reportname`, reusing the migrated
  no-op success surface.
- 2026-07-10: `loginv2` now accepts the old FoolFrame Web login field names
  from `login.js` (`name`, `pwd`, `dbid`, `chk`, `chkid`) at the shared
  legacy auth DTO boundary while still requiring explicit `AppId` / `AppKey`.
  The Docker runtime doctor proves the payload through the Vue proxy.
- 2026-07-10: the Docker runtime doctor now proves the old FoolFrame Web
  `/report/mkqview` candidate-column route through `/api/v1/report/mkqview`
  with the lower-case `viewid` payload from `mkreport.js`, reusing the
  migrated `getmkqview` report model path.
- 2026-07-10: auth now exposes the old FoolFrame Web check-code wrapper as
  `/api/v1/auth/getchk`, reusing the migrated `getcheckcode` generator. The
  Docker runtime doctor proves the route through the Vue proxy.
- 2026-07-10: backend `getenums` now exposes the old FoolFrame Web
  `/model/getenum` wrapper as `/api/v1/data/getenum`, accepting the lowercase
  `modelid` payload while reusing the migrated enum lookup service. The Docker
  runtime doctor proves the route through the Vue proxy.
- 2026-07-10: backend save routes now accept the old FoolFrame Web
  `detailview.js` wrappers as `/api/v1/data/save` and `/api/v1/data/new`,
  including `obj` plus `ownerviewid` / `ownerid` / `prpid`, while reusing the
  existing `saveobj` and `savenewobj` service paths. The Docker runtime doctor
  proves both routes through the Vue proxy.
- 2026-07-10: auth now exposes the old FoolFrame Web menu wrapper as
  `/api/v1/auth/getmenu`, accepting the `authcode` payload from
  `menuinfo.js` while reusing the migrated `getsubmenu` service path. The
  Docker runtime doctor proves the route through the Vue proxy.
- 2026-07-10: backend `inputquery` now accepts the old FoolFrame Web lookup
  payload from `setextype.js`, including `viewid` / `itemid` / `text` /
  `objid` / `ownerid` / `newadd` aliases, while reusing the existing
  View-first lookup service path. The Docker runtime doctor proves the lower
  case payload through the Vue proxy.
- 2026-07-10: the backend now accepts the old FoolFrame Web list-query
  protocol at `/api/v1/data/querylist`, including `viewid` / `filter` /
  `page` / `pagesize` / `orderitem` / `ordertype` aliases from
  `querylistdata.js`, while reusing the existing View-first `querydata`
  service path. The Docker runtime doctor proves the route through the Vue
  proxy.
- 2026-07-10: the backend now accepts the old FoolFrame Web report execution
  route at `/api/v1/report/mkrpt`, including the `viewid` / `cols` /
  `pagesize` / `pageindex` / `exp` payload from `mkreport.js`, while reusing
  the existing report grid renderer. The Docker runtime doctor proves the path
  through the Vue proxy.
- 2026-07-10: the backend now accepts the old FoolFrame Web operation
  protocol at `/api/v1/data/exoperation`, including legacy
  `objid` / `viewid` / `opid` request aliases from `operation.js`, while
  reusing the existing `runoperation` service path. The Docker runtime doctor
  proves this through the Vue proxy with a no-op operation.
- 2026-07-10: Vue Sudoku `Item` panels now follow FoolFrame
  `includes/Item` / `subitem.js`: the frontend loads the panel View with
  `getlistview(ListViewId)`, calls `querydatadetail(ListViewId, ObjId="")`,
  and renders detail `SimpleData`. List, chart, and map panels continue to use
  their View-first `querydata` paths.
- 2026-07-09: Docker now seeds a real `CustomerMap` child View for the Vue
  Sudoku `Map` panel. The panel points to that child View through
  `ListViewId`, `querydata` returns Customer row `Items` with legacy map
  `EditType` values `16` longitude, `17` latitude, and `18` title, and the
  runtime doctor proves the View-first `getlistview` -> `querydata` path
  through the Vue proxy.
- 2026-07-09: the Docker runtime doctor now guards the DB-management base
  schema already seeded for the local `car_wash` runtime: `DB_App`,
  `WorkDataBase`, `DB_AppDB`, and `DS_DataSourceSet` columns are part of the
  shared schema catalog, so Docker schema drift is caught before DB-management
  wiring is extended.
- 2026-07-09: Vue Sudoku `Group` simple child panels now use the legacy
  FoolFrame `GroupViewController` placeholder text (`这是简单项`) for
  `ListViewType=1`, keeping this branch aligned with
  `../FoolFrame/src/Web/public/javascripts/app/groupview.js` while avoiding a
  new DTO-bound detail shortcut.
- 2026-07-09: Vue Sudoku browser smoke now proves the Docker-rendered
  `OrderSudoku` page shows all five root panels plus `Group Orders` and
  `Group Detail` child panels. Shared `fieldTitle` rendering now falls back to
  ViewItem `Name`/`name`, so panel labels come from View metadata rather than
  generic template kinds.
- 2026-07-09: backend `getlistview` now hydrates legacy `ListViewType` from
  each configured child `ListViewId`'s View type instead of hard-coding `0`,
  matching FoolFrame `item.ListView.ViewType`. Docker now seeds a real
  `TempFile=Sudoku` fixture with `List`, `linechart`, `Map`, `Item`, and
  `Group` panel `ViewFile` metadata, and the runtime doctor proves the group
  child metadata includes both list (`ListViewType=0`) and detail/simple
  (`ListViewType=1`) child surfaces.
- 2026-07-09: Vue Sudoku `Group` panels now render one-level child tabs from
  the loaded group View's `Items`. List child tabs (`ListViewType=0`) load
  their own `ListViewId` through the existing View/data path, while simple
  child tabs (`ListViewType=1`) remain explicit simple-item placeholders
  matching the old `groupview.js` surface without adding recursive loading.
- 2026-07-09: Vue Sudoku `Map` marker data now comes from the child panel's
  loaded View/data result. Longitude, latitude, and title are derived only
  from legacy map `EditType` values `16`, `17`, and `18`; extra marker info is
  projected from the same row `Items`. The 2026-07-10 renderer upgrade consumes
  this same DTO-independent projection on a lazy-loaded interactive map.
- 2026-07-09: Vue Sudoku `linechart` data now comes from the child panel's
  loaded View/data result, reusing the same legacy chart `EditType` (`11` axis,
  `12` line, `13` bar, `14` scatter) projection used by `viewWithChart`. The
  2026-07-10 shared SVG renderer consumes this projection in both surfaces.
- 2026-07-09: Vue Sudoku panels now load their own child View/data chain from
  each item `ListViewId`: `getlistview(ListViewId)` first, then
  `querydata(ListViewId)` only when the child View has renderable columns.
  The shared current-data refresh path skips root `querydata` for
  `TempFile=Sudoku`, so panel tables cannot bind to the root View's data DTO.
- 2026-07-09: Vue now recognizes legacy `TempFile=Sudoku` and normalizes each
  View item `ViewFile` (`./includes/List`, `Group`, `Map`, `Item`,
  `linechart`) before data binding. This first slice rendered the configured
  Sudoku child panel shells from View metadata only.
- 2026-07-09: Java View item metadata, Docker seed data, Vue chart helpers, and
  the runtime doctor now carry FoolFrame chart `EditType` values `11` through
  `14` for `viewWithChart`. Docker `querydata` rows expose a chart axis plus
  line/bar series through row `Items`, and browser verification shows the Vue
  chart pane rendering non-empty `bar` and `line` series from the loaded View
  metadata.
- 2026-07-09: Vue now consumes legacy `TempFile=viewWithChart` metadata and
  renders data/chart panes before binding data rows. Chart rows are derived
  only from FoolFrame chart item `EditType` values (`11` axis and `12`/`13`/`14`
  series), so ordinary business DTO fields cannot invent a chart.
- 2026-07-09: the Docker runtime doctor now seeds and proves legacy
  `getlistview.TempFile` plus item `ViewFile` render metadata through the Vue
  proxy, so runtime smoke fails if the View render template link disappears
  before data binding starts.
- 2026-07-09: legacy `getlistview` now hydrates View-level `TempFile` and
  item-level `ViewFile` metadata from `SW_SYS_VIEW_FILE`, matching FoolFrame's
  Web render dispatch surface before Vue decides how to render the loaded
  View metadata.
- 2026-07-09: dynamic `saveData` now uses the old FoolFrame `SYSID` when a
  no-idProperty row changes `SYSID`, matching the existing explicit-id
  old-value update behavior and avoiding missed updates on legacy dynamic rows.
- 2026-07-09: dynamic `createData` now inserts FoolFrame `SYSID` when model
  metadata has no explicit id property and the dynamic row carries a legacy
  id, so `savenewobj` can create no-idProperty rows instead of relying on a
  database default.
- 2026-07-09: legacy `saveobj` / `savenewobj` dynamic data construction now
  writes request ids into FoolFrame `SYSID` when model metadata has no
  explicit id property, keeping save paths aligned with the shared dynamic id
  fallback.
- 2026-07-09: `DbMysqlDynamic.getId()` now falls back to FoolFrame `SYSID`
  when model metadata has no explicit id property, so shared dynamic row id
  consumers no longer need caller-specific fallback logic.
- 2026-07-09: legacy `inputquery` source-list owner lookups now fall back to
  FoolFrame `SYSID` when the owner/current model has no explicit id property,
  keeping child lookup candidate loading on the View metadata path.
- 2026-07-09: dynamic collection hydration for models without explicit id
  property now selects and maps FoolFrame `SYSID` through `ModelDataService`
  and uses the same id fallback when loading owned collection rows.
- 2026-07-09: legacy `inputquery` now supports BusinessObject target models
  without an explicit id property by filtering null id properties from the
  target query property list and continuing to return candidate ids through
  the shared `SYSID` fallback.
- 2026-07-09: View/data protocol IDs now fall back to FoolFrame-style `SYSID`
  when dynamic rows have no explicit id property, covering list row ids,
  detail `ObjId`, child collection `DataID`, BusinessObject lookup ids,
  `inputquery` candidates, and blank-object detail first-row selection.
- 2026-07-09: dynamic model mutations without an explicit id property now use
  FoolFrame-style `SYSID` values for save/update id resolution and model
  trigger `Filter` current-object checks, reusing the existing dynamic
  mutation and trigger command path.
- 2026-07-09: legacy runoperation `Filter` commands now bind the current
  object predicate even when the migrated model has no explicit id property,
  falling back to FoolFrame-style `SYSID` before applying the configured raw
  command expression.
- 2026-07-09: legacy runoperation now honors operation-level `ArgModel` /
  `ArgFilter` by loading or creating the target model object, executing the
  operation commands with the source row as the value source, and persisting
  the target object. This matches FoolFrame `ObjectProxyClass.TryInvokeMember`
  routing before falling back to direct source-object operation execution.
- 2026-07-09: legacy `saveobj.Itemproperties[].Items[]` child updates now map
  to `SubItemList.UpdatedList`, while `AddedItems[]` and `DelteItems[]`
  continue to drive added and deleted state. This matches FoolFrame
  `DataFormator.ObjUpdateToProxy` mutating existing collection items rather
  than treating them as newly added children.
- 2026-07-09: extracted the Vue View-first detail and child-collection panel
  into `ViewDetailPanel.vue`, keeping `App.vue` focused on workflow state and
  actions while preserving the existing metadata-driven detail, lookup,
  operation, and child-row save paths.
- 2026-07-09: legacy trigger `ExuteListMethod` now keeps FoolFrame's dynamic
  binder no-op behavior when the target list object does not expose the named
  method, while still invoking real Java list methods when present.
- 2026-07-09: the Docker runtime doctor now guards modern runtime metadata
  tables used by the Vue View-first workflow: `fool_sys_model`,
  `fool_sys_model_property`, `fool_sys_view`, and `fool_sys_view_item`.
- 2026-07-09: legacy Many2Many/Recurve relation writes now create missing
  target rows before inserting relation rows, matching FoolFrame
  `CreateComplexRelationBuild` while reusing the existing dynamic
  `createData` path.
- 2026-07-09: legacy recursive `#` owner expressions now resolve through the
  shared `OperationCommandValueResolver`, so nested owner paths such as
  `##.accountName` reuse the same expression path as one-level `#.` values.
- 2026-07-09: legacy owned collection updates now execute
  `PropertyTriggerType.ITEMS_SET` before saving an existing child row, reusing
  the same collection trigger path already used for `ItemsAdd` and
  `ItemsDelete`.
- 2026-07-09: `ReflectiveAppModuleSource` now accepts declared dependency
  packages, scans their annotated models with the existing package scanner, and
  wires them as root-module dependencies. This covers the practical Java
  replacement for FoolFrame `AssemblyModelFactory.GetReferencedAssemblies`
  without scanning the whole classpath.
- 2026-07-09: the repository harness now checks Docker init SQL against the
  runtime doctor schema catalog, failing if any legacy runtime column or
  FH_JAVA `market_symbols` column covered by `scripts/runtime_schema.py` is
  missing from `docker/mysql/init/*.sql`.
- 2026-07-09: the Docker runtime doctor now proves legacy
  `saveobj.Itemproperties[].Items[]` update and `DelteItems[]` delete through
  the Vue proxy, reading the updated child value back and then confirming the
  child row is absent after delete.
- 2026-07-09: the Docker runtime doctor now proves legacy
  `saveobj.Itemproperties[].AddedItems[]` through the Vue proxy by creating a
  temporary parent row, adding a child row from loaded detail child metadata,
  reading the child value back through `querydatadetail`, and cleaning up.
- 2026-07-09: the Docker runtime doctor now proves legacy `saveobj` through
  the Vue proxy by creating a temporary detail-View object, updating it with
  loaded detail fields, reading the changed field back, and cleaning up.
- 2026-07-09: the Docker runtime doctor now proves legacy `savenewobj`
  through the Vue proxy using fields loaded from the detail View metadata, and
  cleans up the fixed smoke order id after the check.
- 2026-07-09: dynamic create/save now omits scalar properties that are absent
  from the legacy save DTO, so `savenewobj` can use database defaults for
  missing BusinessObject foreign-key columns while explicit submitted nulls
  still write null.
- 2026-07-09: the Docker runtime doctor now proves legacy
  `querydatadetail.IdExp` through the Vue proxy by resolving `$<row id>` with
  the `DetailViewId` loaded from View metadata.
- 2026-07-09: the Docker runtime doctor now proves legacy `initnew` through
  the Vue proxy using the `DetailViewId` loaded from View metadata, and the
  shared detail-response check accepts both camel and FoolFrame Pascal aliases.
- 2026-07-09: runtime-doctor schema guard data now lives in
  `scripts/runtime_schema.py`, keeping the executable Docker smoke script
  focused on runtime flow while preserving the same schema coverage and output.
- 2026-07-09: the Docker runtime doctor now guards the legacy `SW_SYS_CON`
  connection schema seeded for routed model/query/app runtime paths, including
  FoolFrame's original `INITALCATALOG` and `ISLOACL` spellings.
- 2026-07-09: the Docker runtime doctor now guards the `fool_sys_model_enum`
  schema behind the View-derived legacy `getenums` runtime check, including the
  `owner` column used to load enum values for a model.
- 2026-07-09: the Docker runtime doctor now proves legacy `getenums` through
  the Vue proxy using an enum model id discovered from the loaded View metadata,
  keeping enum option loading View-first instead of falling back to a seeded
  model id.
- 2026-07-09: the Docker runtime doctor now guards the legacy query catalog
  schema read by report/query metadata loading: `SE_COMPARETYPE`,
  `SE_COMPARETYPE_PROPERTYINDEX`, `SE_SELECTEDTYPE`, and
  `SE_SELECTEDTYPE_PROPERTYINDEX`.
- 2026-07-09: the Docker runtime doctor now guards the event definition
  recipient relation schema used by notification expansion:
  `SW_APP_AUTH_USER_SW_EVT_DEF`, `SW_APP_AUTH_ROLE_SW_EVT_DEF`,
  `SW_APP_AUTH_DEPARTMENT_SW_EVT_DEF`, and
  `SW_APP_AUTH_COMPANY_SW_EVT_DEF`.
- 2026-07-09: the Docker runtime doctor now guards the legacy event/message
  schema used by the migrated scheduler and message polling path:
  `SW_EVT_DEF`, `SW_EVT_EVENT`, and `SW_SYS_MSG` columns read or written by
  event definition loading, event de-duplication, message persistence, and
  `getmsg` push-state updates.
- 2026-07-09: the Docker runtime doctor now guards the remaining AppManage
  mapped schema columns used by installed modules, model metadata,
  enum-value metadata, trigger/operation primary keys, and company/department
  auth graph tables used by event recipient expansion.
- 2026-07-09: the Docker runtime doctor now guards the legacy auth/app shell
  schema used before the Vue View-first workflow starts: modern `auth_user`
  login rows, legacy `SW_AUTH_USER`, `SW_APPLICATION`, `SW_STOREDB`,
  application-store relations, and `SW_APP_AUTH_*` menu/user/role relations
  used by `initapp`, `loginv2`, `getmain`, and `getsubmenu`.
- 2026-07-09: the Docker runtime doctor now guards the full legacy View
  render schema used by the Vue View-first workflow: `SW_SYS_VIEW`,
  `SW_SYS_VIEW_FILE`, `SW_SYS_VIEW_ITEM`, `SW_SYS_VIEW_OPERATION`,
  `SW_SYS_OPERATIONVIEW`, and `SW_SYS_OPERATIONVIEW_ITEM` columns used for
  list/detail metadata, operation buttons, and operation parameters.
- 2026-07-09: the Docker runtime doctor now guards the legacy
  `SW_SYS_OPERATION`, `SW_SYS_OPERATION_PARAM`, `SW_SYS_COMMANDS`,
  `SW_SYS_MODEL_TRIGGER`, `SW_SYS_MODEL_TRIGGER_COMMANDS`,
  `SW_SYS_PROPERTY_TRIGGER`, and `SW_SYS_PROPERTY_TRIGGER_COMMANDS` columns
  read by migrated runoperation, model-trigger, property-trigger, and
  AppInstall paths.
- 2026-07-09: legacy `SW_SYS_RELATION` and `SW_SYS_MULTIMAP` Docker schema
  migration now has idempotent repair blocks for the relation columns used by
  collection writes and the DBMaps columns used by multi-column property
  hydration. The runtime doctor now guards those tables alongside the core
  View/data/AppInstall schema.
- 2026-07-09: `fool-view` runoperation scalar-conversion proof now expects
  `PropertyType.Long` static SetValue results as Java `Long`, matching the
  shared `OperationCommandValueResolver` and model-layer resolver tests. The
  broader `fool-app-manage -am` Maven gate no longer fails on the stale
  `Integer<123>` expectation.
- 2026-07-09: legacy `SW_SYS_PROPERTY` Docker schema migration now has
  idempotent repair blocks for connection type, collection, DB column/property
  name, multi-map, key/check/generation, nullable/get/set flags,
  model/filter/source/format/sqlcon, and owner columns used by the migrated
  View/data/AppInstall path. The runtime doctor now fails if those property
  metadata columns drift out of the Docker `car_wash` schema.
- 2026-07-09: legacy `SW_SYS_MODEL` Docker schema and AppInstall mapping now
  include FoolFrame's parent model, id property, default format, model type,
  `IsView`, default list view, and default item view columns. The runtime doctor
  now guards those columns so the Docker baseline cannot drift back to shell-only
  model records.
- 2026-07-09: `scripts/runtime_doctor.py` now checks the core legacy
  `SW_SYS_MODEL`, `SW_SYS_PROPERTY`, `SW_SYS_VIEW`, `SW_SYS_VIEW_ITEM`,
  `SW_SYS_VIEW_OPERATION`, `SW_SYS_OPERATION`, `SW_SYS_COMMANDS`,
  `SW_SYS_OPERATIONVIEW`, and `SW_SYS_OPERATIONVIEW_ITEM` columns needed by
  the default View-first data/operation path. This keeps the Docker baseline
  from passing on API behavior alone when required legacy schema has drifted.
- 2026-07-09: legacy owner expressions now resolve through the shared
  `OperationCommandValueResolver` by reading `DbMysqlDynamic` owner metadata.
  Dynamic collection loading and collection writes attach the parent row to
  child rows before item triggers, so child expressions read parent fields
  without binding to a concrete business DTO.
- 2026-07-09: legacy static command values for `PropertyType.Long` and
  `PropertyType.ULong` now resolve as Java `Long` values through the shared
  `OperationCommandValueResolver`. This keeps runoperation and trigger command
  paths aligned with legacy long/id field semantics instead of narrowing those
  values to `Integer`.
- 2026-07-09: legacy static command values for `PropertyType.IdentifyId` now
  resolve as Java `Long` values through the shared
  `OperationCommandValueResolver`. This matches the existing default-value and
  DDL mapping for identify/id fields instead of leaving `$...` command values
  as strings.
- 2026-07-09: legacy static command values for `PropertyType.DateTime` now
  accept date-only `$yyyy-MM-dd` expressions through the shared
  `OperationCommandValueResolver`, mapping them to start-of-day
  `LocalDateTime` values. This keeps runoperation and trigger command DateTime
  handling aligned with FoolFrame `Convert.ToDateTime` while preserving the
  existing full datetime parser.
- 2026-07-09: rechecked remaining command-type and AppInstall wording against
  `../FoolFrame` and the current module map/tests. Legacy `CommandsType`
  values 0 through 8 are represented; runtime command execution covers the
  server-side mutating slices, while `SetAccess` only raises
  `NotifyPropertyCanSet` and `SetSource` is not handled by
  `ModelMethodContext`. The AppInstall remaining-work entry now keeps only the
  DBMaps runtime/query and arbitrary classpath dependency-enumeration gaps
  instead of relisting covered install, reflection, metadata, enum, and routed
  transaction slices.
- 2026-07-09: legacy collection item SQL now selects DBMaps columns for child
  rows with the same `property_mapName` aliases used by list-query DBMaps
  loading. Child collection rows with multi-column BusinessObject snapshots can
  therefore hydrate through the existing `Mapper` DBMaps path instead of losing
  those values during `generateItems`.
- 2026-07-09: legacy model runtime public write entrypoints now run inside the
  Spring transaction for the active datasource. Parent row writes and owned
  collection writes roll back together when a later write fails, while model
  triggers keep FoolFrame's post-commit `Create`/`Save` and pre-delete
  ordering.
- 2026-07-09: legacy trigger command execution now covers
  `ExuteProprtyModelMethod` and `ExuteListMethod` in the shared
  `ModelDataService` trigger path. Model, property, and collection triggers
  can invoke direct dynamic property methods, collection item methods, and
  no-arg list methods without duplicating the `runoperation` command flow.
- 2026-07-09: legacy trigger command execution now covers the focused
  external-model update/map slice in the shared `ModelDataService` trigger
  path. Runtime `Model.operations` are hydrated from `SW_SYS_OPERATION` /
  `SW_SYS_COMMANDS`, trigger command values still resolve through
  `OperationCommandValueResolver`, and target persistence reuses public
  `createData` / `saveData` / `deleteData` entrypoints instead of binding to a
  concrete business DTO.
- 2026-07-09: legacy model triggers now execute
  `BaseOperationType.Assebmly` through the same Java classpath assembly
  invoker used by `runoperation`. `SetParamValue` and `SetConStrValue`
  command values are collected by the shared trigger command path before the
  trigger handler runs; `invokeDll` plugin loading remains intentionally
  deferred until a real migrated handler needs it.
- 2026-07-09: legacy property triggers now execute
  `BaseOperationType.Assebmly` through the shared trigger-command and Java
  classpath assembly path. This follows FoolFrame's `ObjectProxy`,
  `SqlDataProxy`, and `ModelBindingList` behavior, where property `Set`,
  `ItemsAdd`, and `ItemsDelete` triggers are passed to
  `ModelMethodContext.ExcuteOperation` as `IOperation` instances.
- 2026-07-09: legacy property triggers now share the same
  `BaseOperationType.Create` / `Update` / `Delete` execution path as model
  triggers after their commands run. The focused delete test proves a property
  `Set` trigger can remove the current row through the migrated
  `ModelDataService` path, matching FoolFrame's operation switch.
- 2026-07-09: rechecked legacy operation-trigger side effects against
  `../FoolFrame` `ModelMethodContext` and `SqlServer.dbContext`: operations
  call the normal create/save/delete persistence path, and that path executes
  model triggers. The migrated `runoperation` already calls public
  `ModelDataService.createData/saveData/deleteData`, while focused tests cover
  both the runoperation public-persistence handoff and model SAVE trigger
  side effects, so operation-trigger side-effect execution is no longer listed
  as open remaining work.
- 2026-07-09: legacy `loginv2` now records the selected App id and `DbId`
  beside the runtime token. `getapp` / `getmain` resolve AppInfo from that
  token session, and `@appcon` / `@datacon` context values resolve through
  `SW_APPLICATION.SW_APP_CON` and the token-selected `SW_STOREDB.SW_STORE_CON`.
  Logout deletes the session App and DB keys with the normal token keys.
- 2026-07-09: explicit query/report ordering now resolves only through
  rendered View items. Hidden Model properties can no longer be used as order
  tokens when absent from the loaded View metadata; unknown order tokens fall
  back to the default rendered View ordering instead of binding directly to a
  concrete business DTO property.
- 2026-07-09: legacy `runoperation` command expressions now resolve
  token-backed `@userid` / `@username` values through the existing
  `LegacyContextValueService`. The token is carried through normal
  `SetValue`, assembly parameter/constructor commands, and nested external
  model command execution. No business DTO shortcut or new expression parser
  was added; later owner traversal uses the same resolver after dynamic data
  gained an owner-object carrier.
- 2026-07-09: cleaned stale remaining-work entries for
  `SWDQ01-Soway.Query` and `SWRPT01-Soway.Report` after re-checking the
  current module map, focused tests, and `../FoolFrame` sources. The listed
  query surfaces are now covered by `fool-query`, while the report
  table-source path is covered through source-row matrix construction and the
  old `ReportFactory` / `IReportSource` types remain empty shells rather than
  concrete export adapters.
- 2026-07-09: Vue list data loading now requires a loaded View with renderable
  columns before calling `querydata`. If `getlistview` returns no column
  contract, the workflow clears stale rows and stops instead of letting raw
  data DTO rows define the page. This keeps first-screen and API-tool list
  loading on the View-first path: renderable View metadata, then data for that
  loaded View id.
- 2026-07-09: Vue child delete payloads now keep
  `Itemproperties[].DelteItems[]` to the child `itemId` plus an empty
  `propertyies` list, matching FoolFrame's delete branch that removes by
  `ItemId` only. Raw child data DTO values no longer leak into delete payloads.
- 2026-07-09: Vue child update submission fallback now reuses the same
  `buildGroupItemDrafts(group, item)` path as child draft initialization. If a
  child draft map is missing during a save click, the payload still starts from
  rendered child group View columns instead of raw `querydatadetail` item DTO
  values.
- 2026-07-09: Vue select-existing child row mapping now reads candidate row
  values through rendered candidate View columns before falling back to same-key
  row items. A raw `querydata.Items` value that happens to share a child
  property key can no longer override the candidate View column value when
  building `Itemproperties[].AddedItems`.
- 2026-07-09: Vue existing child-row draft state now initializes from rendered
  child group View columns and then merges `querydatadetail` row values by
  property key. DTO-only child values no longer enter `childDrafts`, including
  the first edit path that runs before a sync, keeping child editor state and
  save payloads on the same View-first contract.
- 2026-07-09: Vue existing child-row update payloads now build
  `Itemproperties[].Items[].Propertyies` from rendered child group View
  columns (`getreaditemview.DetailViews`) instead of `querydatadetail` child
  row value DTOs. DTO-only child values no longer leak into save payloads, and
  editable View fields that are missing from the data row can still be saved
  through the shared child draft path.
- 2026-07-09: `getlistview` responses now expose `ViewId` alongside the
  existing legacy `ID`, and Vue's shared `viewId()` helper accepts
  `viewId` / `ViewId` / `ViewID` before falling back to `id` / `ID`. The main
  workflow therefore keeps the sequence as rendered View metadata first, then
  `querydata` with that loaded View id, instead of depending on a request-form
  value or a concrete business DTO shape.
- 2026-07-09: Vue metadata field editors now follow FoolFrame detail-page
  render order for scalar controls: the legacy `detailView.jade` emits
  generic field metadata, `detailview.js` passes `data-propertyType` into
  `setextype`, and save reads the same generic field attributes. Vue therefore
  chooses native inputs from `PrpType` / `PropertyType` first, supports numeric
  property-type codes for imported configurations, and treats `EditType`
  picker values only as compatibility fallback when property type metadata is
  absent. String fields no longer become date/time/checkbox controls just
  because a business DTO or imported item carries a numeric `EditType`.
- 2026-07-09: Vue readonly detection now follows the same detail-page
  metadata priority: explicit `ReadOnly` / `readOnly` wins before
  `EditType=ReadOnly` fallback. Imported rows with `ReadOnly=false` no longer
  become disabled controls just because stale item edit-type metadata is still
  set to readonly.
- 2026-07-09: Vue enum option lookup now reuses the shared `fieldModelId`
  helper in both option loading and option selection. `viewShell` no longer
  reads `prpModelId` / `PrpModelId` aliases directly, keeping model-id
  handling on the same View metadata path as enum detection.
- 2026-07-09: Vue metadata editors now treat legacy `RichTextBox` /
  numeric `ItemEditType=5` as a native `<textarea>` fallback only when
  `PrpType` / `PropertyType` metadata is absent. When field type metadata is
  present, the editor follows the FoolFrame detail-page path and lets property
  type drive the scalar control; `ComboBox`, `SelectLable`, and `DropTextBox`
  remain future slices until their View/data-source behavior is proven.
- 2026-07-09: Vue metadata editors now accept FoolFrame numeric
  `ItemEditType` enum values as compatibility aliases where View property
  metadata is missing, and for view-item-only states such as `ReadOnly=0`.
  Numeric `PrpType` / `PropertyType` codes are the primary
  source for Boolean, Date, Time, DateTime, Enum, BusinessObject, and numeric
  scalar controls.
- 2026-07-09: Vue input-query item display helpers now tolerate empty legacy
  candidate entries by returning empty display text instead of throwing during
  render. The first-screen View workflow loads the rebuilt frontend bundle
  `index-C2xMzW1f.js`, keeps list/detail rendering active after `Load View`,
  and has no current-bundle console errors for the previous `itemId` crash.
- 2026-07-09: legacy `querydatadetail.IdExp` now resolves FoolFrame
  `@userid` / `@username` user-context expressions through the shared
  `OperationCommandValueResolver`. `fool-view` consumes an optional
  `LegacyContextValueService` from `fool-common`, while `fool-auth` supplies
  the token-backed implementation, so detail lookup can use the current login
  user without adding a direct `fool-view` to `fool-auth` dependency. The
  detail service test now pins the lookup order as View metadata first, then
  View-model data loading, instead of a concrete business DTO shortcut.
- 2026-07-09: Vue child add/update draft state now lives in
  `useChildDrafts`, reusing the shared View workflow draft helpers for
  missing-value reads, default child add rows, and persisted child item edits.
  `App.vue` is down to 1926 lines while child collection editors remain bound
  to rendered `getreaditemview.DetailViews` metadata instead of concrete
  business DTO fields.
- 2026-07-09: Vue child collection editors no longer crash when a
  `getreaditemview` DetailView renders before child draft maps have been
  synchronized. `MetadataFieldEditor` child add/update bindings now read
  missing draft values as empty strings and write through shared draft helpers,
  keeping child fields bound to rendered View metadata without exposing
  `undefined.itemId` on the Docker first screen.
- 2026-07-09: Vue enum option loading now lives in `useFieldEnums` instead of
  inline `App.vue` state. Detail and child metadata editors still load enum
  options from rendered View field metadata by `PrpModelId`, cache options by
  model id, and keep the manual API-tool `getenums` panel separate. This keeps
  enum editors on the View-first data path while reducing `App.vue` to 1922
  lines under the frontend root budget.
- 2026-07-09: Vue list workflow now routes first-screen and API-tool data
  loading through `useViewDataWorkflow`: `getlistview(ViewId)` loads the
  rendered View definition first, then `querydata` uses the loaded View id and
  renders columns from View metadata rather than business DTO fields or
  `querydata` column fallbacks. `App.vue` dropped duplicated View/data state and
  request-building code, reducing the component while keeping child candidate
  lookups on the same View-first metadata path.
- 2026-07-09: legacy `runoperation` now treats WCF / JSONPOST / JSONGET
  operation base types as successful no-ops after command evaluation, matching
  FoolFrame `ModelMethodContext.ExcuteOperation` default switch behavior plus
  `HandlerRunOperation` success handling. No HTTP/WCF client layer was added;
  those operation types still do not perform external calls. WCF/JSON base
  operation parity is no longer counted as remaining migration work; a real
  external-call adapter stays out of scope unless a non-FoolFrame handler
  source appears.
- 2026-07-09: Vue metadata field editors now render legacy Boolean /
  CheckBox fields as native checkboxes from View field metadata. `PrpType=8`,
  numeric `PropertyType=8`, and `PropertyType.Boolean` flow through the
  existing shared input helper, while similarly named string fields still
  render as text even if stale `EditType=CheckBox` metadata is present. Save
  payloads keep the legacy string contract at the frontend boundary, and
  `ModelDataService` coerces Boolean string values by `PropertyType.Boolean`
  before dynamic persistence so MySQL `BIT` columns do not require a concrete
  business DTO binding.
- 2026-07-12: aligned shared metadata editor display with old `setextype.js`.
  Boolean controls now show `是` / `否`; BusinessObject ids remain the hidden
  selected value instead of being repeated as visible UI text, while lookup
  failures use a Chinese fallback and preserve server-provided error messages.
- 2026-07-09: Vue metadata field editors keep legacy `ItemEditType` picker
  names (`DatePicker`, `TimePicker`, and `DateTimePicker`) as fallback input
  hints only when `PrpType` / `PropertyType` is absent. When View field type
  metadata is available, it wins, matching FoolFrame detail rendering and
  preventing business DTO names or stale imported edit types from selecting
  controls.
- 2026-07-09: Vue metadata field editors now render `DateTime` / `PrpType=14`
  through native `datetime-local` inputs from View field metadata. The editor
  normalizes legacy display values such as `2026-07-03 09:05:06.0` only at the
  input render boundary, keeps the existing string save payload contract, and
  explicitly leaves field names like `createdAt` / `orderTime` out of widget
  selection so page rendering stays View-first instead of business-DTO-bound.
- 2026-07-09: Vue metadata field editors now render native field-specific
  inputs for low-risk scalar View metadata: `Date` uses `type=date`, `Time`
  uses `type=time`, and numeric `PropertyType` values use `type=number`.
  Enum, BusinessObject lookup, readonly, and save payload behavior continue to
  use the existing shared View workflow helpers, avoiding a custom picker or a
  second field-rendering path.
- 2026-07-09: legacy `inputquery` source-list lookup now treats `#.` as
  owner-context for both added and existing child items. Backend lookup loads
  the owner model by `OwnerId`, strips the `#.` prefix for the source-list
  key, and still falls back to the normal target-model candidate query when
  no source list is available. Vue metadata lookup editors now pass child-row
  owner context through `ownerId` and use child item ids for existing child
  lookups, keeping candidate loading tied to rendered View metadata plus the
  parent object context instead of a concrete business DTO shortcut.
- 2026-07-09: legacy `querydatadetail.IdExp` now reuses the shared
  `OperationCommandValueResolver` before detail data lookup. Empty `objId`
  requests still load the `View` first, resolve the target model from
  `View.ViewModel`, and then query `getOneData(viewModel, resolvedId)`;
  static, math, and context expressions share the existing operation-command
  expression path instead of adding a second parser. Expressions that need a
  current object (`#.` / `.`) remain unavailable in this detail-id phase
  because FoolFrame calls this handler without an object/property context.
- 2026-07-09: Vue's stale free-form `InputQueryRequest.viewName` shortcut was
  removed while lookup was ViewId-driven. The 2026-07-12 correction restores
  the field only at the shared payload boundary and derives it exclusively
  from rendered detail or child View metadata, not a business DTO label.
- 2026-07-09: `ViewDataService.getViewData` and
  `DataQueryService.queryViewDataList` now reject service-level business-name
  shortcuts before DAO lookup. Direct service callers must pass numeric
  `ViewId`, matching the controller contract and keeping View rendering/data
  loading from falling back to `ViewName` or concrete DTO labels.
- 2026-07-09: Generic View/data request DTOs for `get-view`,
  `getlistview`, `getreaditemview`, and `query-list` no longer declare a
  `viewName` shortcut. Extra `ViewName` / `viewName` JSON fields are ignored
  and the controllers still require `ViewId`, keeping rendering and data
  loading on the ViewId-first path while leaving legacy `inputquery`
  `ViewName` compatibility at its protocol boundary.
- 2026-07-09: `QueryFactory.getTable` now matches FoolFrame
  `GetTable`: table lookup uses trimmed, case-insensitive DBName only and
  throws on missing tables. The Java-only ShowName/null fallback was removed
  so View/data query setup cannot resolve tables from rendered labels or
  concrete DTO display text.
- 2026-07-09: `ModelDataService` now executes legacy trigger
  `CommandsType.Filter` commands in command-index order for model, property,
  and collection item triggers. A failed filter checks the current persisted
  row by model ID column plus the raw command expression, throws the legacy
  command message, and stops later trigger commands/base operations from
  running.
- 2026-07-09: Vue detail child-group rendering no longer appends
  `querydatadetail` DTO groups that are missing from the loaded read-item
  View `DetailViews`. The DTO payload can still provide row values for
  declared child groups, but only rendered View metadata can define child
  sections and columns.
- 2026-07-09: Vue child-group helpers now consume FoolFrame Pascal
  `querydatadetail` group aliases (`PrpId`, `Name`, `ItemName`, `ListViewId`,
  `SelectedView`, `Items`, `Properties`) when matching data rows to rendered
  read-item View `DetailViews`. Select-existing child workflows no longer
  depend on camel-case DTO fields to recover the target View id.
- 2026-07-09: Vue select-existing child controls now read the shared
  `groupSelectFromExists` helper instead of the camel-only
  `group.selectFromExists` field, so legacy Pascal `SelectFromExists` can
  expose the candidate loader after the child group is matched to rendered
  read-item View metadata.
- 2026-07-09: Vue child group titles, row counts, row keys, item ids, and
  child-item save payloads now read through shared helpers
  (`groupTitle`, `groupItems`, `itemDataId`, `groupKey`, `groupColumns`)
  instead of direct template/business DTO field reads. Pascal
  `PrpId`/`ItemName`/`Items`/`Properties` detail payloads now keep rendering
  and save identity aligned with the loaded read-item View metadata.
- 2026-07-12: rechecked Vue detail operation rendering against old Web
  `detailView.jade` and `operation.js`. Edit, Save, and View operations now
  share one toolbar; non-actionable operation parameter labels were removed
  because the old client posts only View/object/operation ids. Pascal `Params`
  / `ParamName` metadata remains available behind shared protocol helpers.
- 2026-07-09: `QueryFactory.getStateStr` now matches FoolFrame
  `GetStateStr`: it maps enum DB values to display names and returns an empty
  string when the DB value is missing. The previous Java-only reverse
  show-name-to-DB mapping was removed to keep query/report state formatting
  aligned with the legacy query factory contract.
- 2026-07-09: Vue read-item View metadata is now cached by rendered `ViewId`.
  Detail rendering, init-new rendering, and manual read-item inspection no
  longer share a single last-loaded read View, and create operations set the
  target detail `ViewId` before `initnew` data is merged. This prevents stale
  or wrong-view `querydatadetail` / `initnew` DTO values from binding to a
  different page shape.
- 2026-07-09: Vue detail rendering no longer falls back to
  `querydatadetail` / `initnew` DTO fields when the read-item View metadata is
  missing or empty. Detail fields, child-group columns, and API-tool detail
  tables now render only from loaded `getreaditemview` metadata merged with
  data values; raw DTO groups may carry values, but they cannot define page
  structure.
- 2026-07-09: Vue View workflow and lookup payloads no longer keep or send a
  `ViewName` shortcut. Legacy `getlistview`, `getreaditemview`, and
  `inputquery` now require `ViewId`, keeping the runtime flow as
  View metadata first, then data lookup from that loaded View.
- 2026-07-09: AppInstall routed module-source metadata installs, routed model
  schema DDL, and default View generation now execute through the target
  `DaoService` transaction boundary. DriverManager-created legacy connection
  DAOs use a Spring `DataSourceTransactionManager` over the cached
  `SingleConnectionDataSource`; this covers per-connection rollback scope
  without introducing a distributed transaction across sys/work databases.
- 2026-07-09: AppInstall legacy enum-value duplicate checks now use the same
  parsed integer `EMUN_VALUE` that is written to `SW_SYS_EMUNVALUE`, avoiding
  a string-vs-number mismatch between `EnumValue.value` and the legacy enum
  metadata table.
- 2026-07-09: Vue list-row selection/render keys now use loaded View columns
  before falling back to raw `querydata.Items` order when `querydata.Id` is
  absent. This keeps fallback row identity tied to the rendered View instead
  of whichever business DTO field appears first in the data payload.
- 2026-07-09: Vue manual API tools no longer default enum lookup to seeded
  model `102` or new-object save to seeded object `9001`. Metadata-driven enum
  editors still load model ids from rendered View fields; manual tools now
  require user or View context instead of starting from concrete business DTO
  identifiers.
- 2026-07-09: the Vue View workflow now lets only loaded View metadata define
  table columns. `listRenderColumns` reads `getlistview.Items` /
  `tableColumn` and no longer falls back to `querydata.Cols` or first-row
  `Items`; select-from-existing child candidate tables use the child
  `getlistview` columns the same way. Data DTO payloads now only supply rows
  and values after the View has rendered.
- 2026-07-09: AppInstall module-source installation now persists operation
  params into legacy `SW_SYS_OPERATION_PARAM`. Source `OperationParam`
  metadata keeps `name`, `viewId`, `filter`, and `value`, and installed params
  backfill their source ids plus owner operation ids alongside existing
  operation-command metadata.
- 2026-07-09: AppInstall module-source installation now accepts custom
  non-default Views from `AppModuleDefinition.views` and persists their View
  operation metadata through the existing legacy `SW_SYS_VIEW_OPERATION` /
  `SW_SYS_OPERATIONVIEW` path. Custom View `VIEW_MODEL` resolves from the
  module-source model id installed in the same pass, so View actions can reuse
  migrated `SW_SYS_OPERATION` metadata.
- 2026-07-09: AppInstall module-source installation now persists model
  operation metadata into legacy `SW_SYS_OPERATION` and operation command
  metadata into `SW_SYS_COMMANDS`. Installed operations and commands backfill
  their source ids, so generated default View command operations can link to
  real legacy operation ids without adding a separate View/business DTO path.
- 2026-07-09: AppInstall default auto-view installation now reuses persisted
  legacy model/property ids when writing generated Views. `installModuleSource`
  backfills source `Model.id` / `Property.id` from `SW_SYS_MODEL` /
  `SW_SYS_PROPERTY`, generated View items persist `VIEW_ITEM_PROPERTY`, and
  generated list View operations persist through `SW_SYS_VIEW_OPERATION` plus
  `SW_SYS_OPERATIONVIEW` for command operations. Default View operation params
  remain out of scope because the legacy default factory does not generate
  them.
- 2026-07-09: AppInstall default auto-view installation now persists generated
  detail/list Views through legacy `SW_SYS_VIEW` and `SW_SYS_VIEW_ITEM`
  records instead of the modern `fool_sys_view` tables. The installed View
  rows keep `VIEW_MODEL`, `VIEW_DEFAULT`, `VIEW_TYPE`, `VIEW_CONTYPE`,
  `VIEW_AUTOFRESHINTERVAL`, and item owner/read-only/edit-type metadata, while
  `LegacyAutoViewFactory` now emits matching `ItemEditType.ReadOnly` /
  `ItemEditType.TextBox` values from generic View metadata rather than a
  business DTO.
- 2026-07-08: the Vue View workflow now requires rendered View metadata before
  list/detail UI can consume data payloads. Main table columns go through the
  shared `listRenderColumns(view)` helper, which returns no columns until
  `getlistview` has loaded, and detail fields/child groups use read-item View
  metadata wrappers that return no form fields until `getreaditemview`
  succeeds. `queryDetail` / `initNew` now stop before `querydatadetail` /
  `initnew` if the read-item View cannot render, keeping data DTO rows from
  defining page structure.
- 2026-07-08: legacy `querydatadetail` now carries request `Token` from the
  controller into `DataQueryService` and on to `ViewDataService.getViewData`.
  This keeps detail View loading on the same token-aware View lookup boundary
  as `getlistview` / `getreaditemview` without adding a direct `fool-auth`
  dependency to `fool-view`. Auth/context `IdExp` expression evaluation
  remains future work.
- 2026-07-08: the Vue detail child-item editor now initializes child add
  drafts immediately after `getreaditemview` metadata loads. This removes the
  first-screen runtime error from rendering `newChildDrafts[group][itemId]`
  before `querydatadetail` finishes, while keeping child editor fields driven
  by read-item View metadata rather than concrete business DTO maps. Browser
  proof on the Docker frontend shows the loaded View table, detail panel, and
  row `Open` interaction update without console or page errors.
- 2026-07-08: `fool-query` now exposes the legacy
  `SelectedColCollection.CopyTo(array, arrayIndex)` collection surface.
  `SelectedColumnCollection` copies selected columns into the caller-provided
  array without changing selected indexes, matching FoolFrame's direct
  `List<T>.CopyTo` wrapper while leaving the intentionally unsupported indexed
  setter unchanged.
- 2026-07-08: `runoperation` now treats legacy `OperationBaseType.NULL` as a
  successful no-op after command evaluation, matching FoolFrame
  `ModelMethodContext.ExcuteOperation` default switch behavior and
  `HandlerRunOperation` success handling.
- 2026-07-08: backend compatibility endpoints `/api/v1/view/get-view` and
  `/api/v1/data/query-list` now require `ViewId` instead of falling back to
  `ViewName`. Legacy `getlistview(ViewId)` / `querydata(ViewId)` remain the
  primary migrated flow, and the older generic endpoints can no longer start a
  page/data query from a business-name DTO shortcut.
- 2026-07-08: the Vue render boundary no longer exposes generic record-map
  table helpers or `ListDataItem.values` typing. Result tables still load the
  View first (`getlistview` / `getreaditemview`), then render `querydata` /
  `querydatadetail` payloads through View columns and legacy row `Items`, so
  concrete business DTO maps cannot define page columns, row keys, or cells.
- 2026-07-08: `saverpt` remaining-work text now matches FoolFrame server
  truth. Legacy `HandlerSaveReport.ImplementBusinessLogic()` is empty and
  `DataService.SaveReport` only returns the base `Result`, so report metadata
  persistence and saved-report execution are not counted as open server
  parity gaps. The migrated route remains the no-op success surface covered by
  the Docker runtime doctor.
- 2026-07-08: report `getrpt` now preserves multiple non-NULL
  `ReportCols.OrderType` entries in selected-column order. `ReportController`
  passes a View token order list into `DataQueryService`, `DataQueryService`
  resolves those tokens through rendered ViewItem/Property metadata, and
  `SqlGenerator` emits the resulting SQL `ORDER BY` list before pagination.
  This matches FoolFrame report query generation while keeping report ordering
  off rendered row DTO maps.
- 2026-07-08: report `getrpt` now parses `ReportCols.OrderType` into a
  View/property `QueryOrder` before calling `querydata`, so report row ordering
  happens in the DataQueryService SQL path instead of sorting rendered row DTO
  maps. When every selected report column uses `OrderType=2` / `NULL`, the
  request falls back to the first selected report column ASC, matching the
  legacy FoolFrame report query behavior while keeping View metadata ahead of
  data binding.
- 2026-07-08: the Vue sidebar now renders legacy shell menu entries from
  `getmain.TopMenu` / `getsubmenu.Items`. Clicking a menu item with `ViewId`
  opens the existing `getlistview(ViewId)` -> `querydata(ViewId)` workflow
  instead of treating navigation as static frontend tabs or pre-binding data to
  a concrete business DTO. The first-screen workflow also treats non-zero API
  response codes as errors, clears stale stored tokens, and retries the legacy
  login before rendering.
- 2026-07-08: a fresh Vue browser session now bootstraps the Docker legacy
  session through `initapp` / `loginv2`, reads `App.DefaultViewId` from
  `getmain`, then loads the first screen through `getlistview(ViewId)` and
  `querydata(ViewId)`. The browser-verified page no longer stops at
  `ViewId=0`; changing the main list page size reloads the same View-driven
  data path.
- 2026-07-08: the Vue workspace no longer exposes the backend `/test`
  seed-data DTO route or proxies it through Vite/Nginx. `/test` remains a
  backend Docker health check, while the frontend data proof stays on the
  migrated sequence: load `getlistview(ViewId)`, then load rows with
  `querydata(ViewId)` and render legacy row `Items`.
- 2026-07-08: the Vue workflow no longer boots from seeded `ViewId=100`
  frontend defaults. View-related state starts empty, `currentViewId` is derived
  only from the loaded `getlistview` payload, and `querydata` is not called from
  the API-tool path until that View payload exists, keeping page rendering ahead
  of data loading and away from concrete business DTO assumptions.
- 2026-07-08: Docker now includes the FH_JAVA legacy `market_symbols` schema,
  including the later `exchange_type` and symbol filter precision columns from
  `V2022022001__add_exchange_type.sql`. The runtime doctor checks the live
  MySQL schema before API smokes so FH_JAVA configuration drift is visible
  without binding View/data parity to the seeded order DTO.
- 2026-07-08: `fool-query` now covers the legacy `QueryResult.GetData`
  current-page data surface. `JdbcQueryExecutor` returns a `QueryResult` that
  can reload rows for a changed `CurrentPage` through the existing paged SQL
  builder, matching FoolFrame's command-backed `GetData()` behavior without
  introducing a business DTO path.
- 2026-07-08: the Docker runtime doctor no longer falls back to seeded
  `ViewId=100` when the legacy app shell fails to provide a default View. The
  runtime proof now has to pass through `getapp/getmain` `App.DefaultViewId`
  before `getlistview(ViewId)` and `querydata(ViewId)`, so the smoke check
  cannot silently bind migration confidence to the seeded order DTO.
- 2026-07-08: repository harness now fails Vue main render paths that rebind
  page columns or cells to concrete business DTO data (`row.values` or
  `Object.keys(first)`). The allowed path remains `getlistview(ViewId)` for
  rendered View metadata, then `querydata(ViewId)` rows matched through legacy
  row `Items`.
- 2026-07-08: focused Docker Maven module validation no longer needs every
  command to repeat `-DfailIfNoTests=false`. The root Surefire config now
  ignores upstream modules with no matching tests, so scoped checks can use the
  ordinary `mvn -pl <module> -am -Dtest=<TestClass> test` shape.
- 2026-07-08: migration status now stops counting the already-tested event
  object-query runtime slice as remaining work. The module map and
  `EventMigrationTest` cover null-model, zero-row, table-name, explicit/auto
  ID-column, case-insensitive ID-column, missing-column, and matched-row value
  behavior, so future `SCPB09-SOWAY.EVENT` work should be driven by newly
  identified legacy event surfaces instead of this stale duplicate list.
- 2026-07-08: backend `inputquery` now respects configured
  `ViewItem.SelectedView` filters on the target-model lookup path. The service
  still prefers existing source-list branches first, then combines the selected
  View raw filter with the legacy text `LIKE` filter, matching FoolFrame's
  selected View candidate lookup contract without adding frontend DTO binding.
- 2026-07-08: repository harness now fails Java package-boundary drift across
  `fool-*` modules and missing required migration parity markers for the active
  FoolFrame areas. This makes package ownership and the remaining AppInstall,
  Model, Query, Event, Report, and Docker/runtime parity surfaces mechanically
  visible during `python scripts/check_repo_harness.py`.
- 2026-07-08: the Docker runtime doctor now covers `runoperation` result
  aliases through the Vue proxy. It reuses the loaded View id and object id
  from `querydata`, then sends a no-op operation id so the check proves
  `Value` / `IsSuccess` / `ReturnObjId` / `ReturnViewId` / `ReturnMsg`
  without mutating seeded rows.
- 2026-07-08: the Docker runtime doctor now requires every returned
  `querydata` row to match the loaded `getlistview` View columns through row
  `Items`. DTO-only `values` rows are explicitly rejected in the helper tests,
  keeping runtime proof aligned with FoolFrame's View-first render-then-data
  workflow instead of accepting concrete business DTO bindings.
- 2026-07-08: `runoperation` result payloads now expose FoolFrame aliases
  (`Value`, `IsSuccess`, `ReturnObjId`, `ReturnViewId`, `ReturnMsg`) while
  preserving camel-case fields. Vue operation refresh logic reads success
  through `legacyRunOperationSuccess`, and `queryCurrentViewData` verifies the
  rendered `getlistview(viewId)` metadata before calling `querydata(viewId)`,
  keeping page code on the View-first protocol boundary instead of binding to
  concrete business DTO fields.
- 2026-07-08: report grid responses now expose FoolFrame aliases for
  `ReportResult` and `ReportCell` (`ViewId`, `CurrentPage`, `PageSize`,
  `TotalRecords`, `TotalPages`, `Cells`, `Col`, `Row`, `ColSpan`,
  `RowSpan`, `FmtValue`) while preserving the existing camel-case contract.
  Vue report-grid rendering now reads cells through shared helpers, and the
  Docker runtime doctor requires `Cells` on the loaded-View `getrpt` path.
- 2026-07-08: the Vue View summary now reads loaded View name/title/type and
  input-count metadata through shared `viewWorkflow` helpers. This keeps the
  page shell on the same View-first normalization path as columns, operations,
  detail fields, lookup candidates, and report metadata instead of scattering
  direct DTO field reads through `App.vue`.
- 2026-07-08: `getmkqview` now exposes FoolFrame report model aliases
  (`Cols`, `ID`, `Name`, `PrpType`, `ModelId`, `QueryTypes`,
  `CompareTypes`, `States`, `ShowName`, `DBName`) while preserving the
  existing camel-case contract. Vue report-column rendering and default
  `ReportCols` generation now read report model metadata through shared
  `viewWorkflow` helpers, and the Docker runtime doctor requires the Pascal
  `Cols` field on the loaded-View report model path.
- 2026-07-08: `inputquery` now exposes FoolFrame response aliases
  (`Items`, `Id`, `Text`) while preserving the existing camel-case fields.
  Vue API-tool and metadata lookup consumers read candidates through shared
  helpers, and the Docker runtime doctor now requires both `items` and
  `Items` on the loaded-View `inputquery` path.
- 2026-07-08: the Vue API-tool tables for `querydatadetail` and `initnew`
  now read legacy `Data.SimpleData` / `Data.Items` through shared
  `viewWorkflow` helpers, and render row labels/values via generic field
  helpers. This keeps the panels on the same View-first metadata path as the
  main detail workflow instead of binding the template to camel-case business
  DTO fields.
- 2026-07-08: Vue API-tool panels for `getsubmenu`, `getmsg`,
  `getnotify`, and `getenums` now consume FoolFrame Pascal aliases through
  shared protocol helpers (`Items`, `Messages`, `Notifies`, `EnumValues`, and
  nested Pascal field names) while preserving camel-case compatibility.
- 2026-07-08: the Vue manual `runoperation` tool no longer defaults to the
  Docker-seeded operation id `7001`. View-rendered operation buttons still set
  the operation id from loaded metadata, while the manual tool now requires a
  positive operation id and object id before executing.
- 2026-07-08: the Vue auth first-hop controls now read `initapp` and
  `getcheckcode` payloads through shared protocol helpers that accept both
  FoolFrame Pascal fields (`CheckCode`, `Dbs`, `Key`, `Code`, `ChkCodeImg`)
  and the existing camel-case compatibility fields. This keeps the frontend's
  auth setup aligned with the legacy protocol surface without adding
  business-specific DTO bindings.
- 2026-07-08: `getcheckcode` and `initapp` now expose FoolFrame Pascal
  response aliases for the first auth hop: `Key`, `Code`, `ChkCodeImg`,
  `AppTitle`, `CheckCode`, `Dbs`, `DbId`, and related app/store fields. The
  Docker runtime doctor now requires these legacy fields before continuing to
  `checkcode` and `loginv2`, keeping auth smoke on the FoolFrame protocol
  surface instead of relying on camel-case DTO fields.
- 2026-07-08: the Docker runtime doctor now follows the same View-first path
  as the Vue workspace when proving data and report routes. It records the App
  default View id, loads `getlistview`, verifies `querydata` row `Items` match
  the loaded View columns, derives `inputquery` from the View's first
  `BusinessObject` field, and builds `getrpt` / `saverpt` smoke payloads from
  `getmkqview` candidate columns instead of hard-coded Docker order fields.
- 2026-07-08: `getmsg` and `getnotify` now expose FoolFrame Pascal response
  aliases (`Messages`, `Notifies`, and message/notify field aliases) while
  retaining the existing camel-case fields used by the Vue compatibility
  panel. The runtime doctor covers these aliases after a live `loginv2`
  session.
- 2026-07-08: Docker seed scripts that contain legacy Chinese metadata now
  run with `SET NAMES utf8mb4`. Replaying `006-view.sql` and `010-query.sql`
  against the current Docker volume repaired View operation labels and query
  compare/select labels that had been stored as mojibake when the MySQL client
  defaulted to latin1. The runtime doctor now fails if default View operations
  no longer expose the expected `删除` / `保存` labels.
- 2026-07-08: legacy auth shell responses now expose Pascal aliases such as
  `App`, `TopMenu`, `Items`, and `App.DefaultViewId` while retaining the
  existing camel-case fields. The Vue first-screen workflow reads the App
  default View id from `loginv2` / `getmain` / `getapp` shell payloads before
  calling `getlistview`, then continues to load rows through `querydata` using
  the loaded View id. The runtime doctor now carries that default View id into
  its View/data smoke path and requires Pascal shell aliases to be present
  instead of treating Docker `ViewId=100` or camel-case DTO fields as the
  source of truth.
- 2026-07-04: `getreaditemview.DetailViews` now follows the FoolFrame
  collection metadata shape for configured child edit Views. Collection
  `ViewItem` metadata is emitted as a `DetailViews[]` entry, and the adapter
  resolves `EditViewId` through the existing `ViewDataService` path to fill
  nested `Items` with read-item View metadata. This keeps child detail field
  definitions on the View metadata path instead of deriving them from business
  DTO data rows. The Docker `Order.items` seed now configures `edit_view_id`
  alongside list/selected child Views, and Vue merges `querydatadetail` child
  rows under `getreaditemview.DetailViews[].Items` so child tables render
  View-first before data values are bound. `scripts/runtime_doctor.py` now
  checks the same loaded-detail-View `getreaditemview` path and fails if child
  `DetailViews` metadata disappears.
- 2026-07-04: `getreaditemview` responses now expose FoolFrame Pascal aliases
  for read-view metadata: top-level `ViewName`, `ViewId`, `Items`, and
  `DetailViews`, plus item aliases such as `Name`, `PrpType`, `Index`,
  `PrpId`, `PrpModelId`, `ID`, `PrpShowName`, `ReadOnly`, and `EditType`.
  The Vue detail workflow now loads the active detail `ViewId` through
  `getreaditemview` before `querydatadetail`, then merges data values into the
  read-view fields so the editor is rendered from View metadata instead of
  concrete business DTO fields.
- 2026-07-04: `getlistview` responses now expose FoolFrame Pascal aliases for
  View page metadata: top-level `ID`, `Name`, `Type`, `Items`, `Operations`,
  `DetailViewId`, `TempFile`, `ShowType`, and `AutoFreshTime`; View item
  aliases such as `ID`, `Name`, `PropertyName`, `ShowIndex`, `Width`,
  `Format`, `IsReadOnly`, `PropertyType`, `PropertyModel`, and `EditType`;
  and operation aliases `ID`, `Name`, `RequireSelect`, and `ViewID`. Vue View
  helpers now read either camel-case or Pascal `Items` / `Operations` /
  `DetailViewId`, keeping page rendering on the `getlistview(ViewId)` metadata
  path before `querydata(ViewId)` and avoiding concrete business DTO binding.
- 2026-07-04: legacy `querydata` responses now expose FoolFrame Pascal aliases
  for result metadata (`TotalItem`, `TotalPage`, `PageIndex`, `Cols`,
  `FreshTime`, `AutoFreshTime`, `Data`) and row metadata (`Id`, `RowIndex`,
  `Items`, `RowFmt`) while keeping the existing Vue camel-case contract. The
  Vue View workflow reads rows, totals, detail item values, and fallback
  columns through shared helpers that accept both alias forms, so the page
  still follows the View-first sequence (`getlistview(ViewId)` then
  `querydata(ViewId)`) and does not let concrete DTO fields in `values`
  define the rendered list.
- 2026-07-04: Vue child-candidate tables and the API-tool query result table
  now reuse `ListDataTable`, the same metadata renderer used by the main View
  workflow. `App.vue` no longer calls row cell helpers directly, keeping table
  cells centralized on loaded View columns matched to legacy row `Items`.
- 2026-07-04: the main Vue View workflow now honors legacy
  `querydata.AutoFreshTime` by scheduling a native browser interval after the
  loaded View data query succeeds. The refresh follows FoolFrame's list-page
  behavior by resetting to page 1 and reusing the current `getlistview` /
  `querydata` context, while clearing the timer on component unmount.
- 2026-07-04: when `getlistview` does not provide table columns, Vue now
  prefers legacy `querydata.Cols` for fallback table headers before deriving
  columns from row `Items`. This matches FoolFrame `querylistdata.js`, which
  builds list headers from the data response `Cols` field.
- 2026-07-04: the Vue View workflow parses legacy `querydata.FreshTime` without
  adding a custom date parser. A 2026-07-13 template audit narrowed its visible
  use to the old Sudoku List/Map/Chart partials; normal and chart list pages do
  not render it.
- 2026-07-04: report-grid row projection now ignores `ListDataItem.values`
  and builds output cells from legacy row `Items` only. This keeps the
  migrated `makereport` / `getrpt` path aligned with View/query metadata and
  prevents business DTO map keys from leaking into report output.
- 2026-07-04: `scripts/runtime_doctor.py` now proves the Docker frontend proxy
  `getrpt` report execution path by checking the returned `Symbol` / `State`
  report-grid cells for a filtered open order row.
- 2026-07-08: `scripts/runtime_doctor.py` now proves the Docker frontend proxy
  `saverpt` report-definition path returns the same legacy no-op success
  surface as FoolFrame `HandlerSaveReport`; saved-report persistence remains
  separate report work.
- 2026-07-08: the Vue API-tool defaults no longer seed detail/save/operation
  object ids with Docker `1001`. The main View workflow still fills those
  fields after selecting a `querydata` row, and `scripts/runtime_doctor.py`
  now queries data from the loaded list view before reusing the returned row id
  for `querydatadetail` instead of hard-coding a concrete business object id.
- 2026-07-08: `scripts/runtime_doctor.py` now proves the legacy auth first-hop
  path through the Docker frontend proxy: `initapp` returns a seeded database,
  `getcheckcode` / `checkcode` validates a generated legacy code, `loginv2`
  returns a token for the Docker admin user, and `getuserinfo` accepts that
  token.
- 2026-07-08: the Docker runtime doctor now carries the same logged-in token
  through the legacy auth shell routes behind the Vue proxy: `getapp`,
  raw-token `getmain`, top-menu-driven `getsubmenu`, and `logout`.
- 2026-07-04: `scripts/runtime_doctor.py` now proves the Docker frontend proxy
  `querydata` route with a legacy `QueryFilter`, then verifies returned legacy
  row `Items` carry `state.objId=0`. This covers the main Vue workflow's
  View-loaded filter path with runtime evidence instead of only source tests.
- 2026-07-04: the main Vue View workflow now sends its list filter through
  legacy `querydata.QueryFilter` instead of the newer `keyword` helper path.
  This matches FoolFrame `view.jade` / `querylistdata.js`, where the filter
  text box binds directly to `QueryFilter` after loading the View id.
- 2026-07-04: the Vue View workflow no longer reads `row.values` for rendered
  row identity, table cells, or selected-existing child save drafts. Rows now
  render from loaded View columns matched to legacy `querydata.Items`, and
  object selection only uses the protocol row id or legacy item id. Separate
  render keys prevent fallback row indexes from being sent as object ids.
- 2026-07-04: `ModelDataService` now executes collection property
  `ItemsAdd` / `ItemsDelete` triggers while writing dynamic collection
  relations, matching FoolFrame `ModelBindingList` running add triggers before
  insert and delete triggers before removal. This increment covers the already
  migrated `SET_VALUE` command path for One2Many/Many2One child rows and
  Many2Many/Recurve relation rows.
- 2026-07-04: Vue row identity and selected-existing child item payloads now
  prefer legacy row `Items` metadata before falling back to `row.values`. The
  API-tools result table also uses the shared `rowObjectId(row, columns)` key
  instead of serializing the values DTO. This keeps row selection and
  child-item saves aligned with the rendered View item order while preserving
  `values` only as a compatibility fallback.
- 2026-07-04: persisted View loading now hydrates FoolFrame's
  `SW_SYS_VIEW.VIEW_DEFAULT` into `getlistview.DetailViewId`, matching
  `HandlerGetListView` returning `view.DefaultDetailView.ID`. Docker seed data
  defines a real detail View for the default smoke View, and
  `runtime_doctor` now proves the same sequence as the page: call
  `getlistview(ViewId)`, read the returned `DetailViewId`, then call
  `querydatadetail` with that loaded View id. The doctor no longer hard-codes
  the detail id, keeping the runtime check View-metadata driven rather than
  tied to a concrete business DTO.
- 2026-07-04: the Vue View workflow now resolves the default detail context
  from the loaded `getlistview` metadata's `DetailViewId` before opening the
  first row, the generic `Open` row action, or fallback `New Row`
  initialization. This matches FoolFrame `view.jade`, which passes
  `view.data.DetailViewId` into `querylistdata.js` before row data selection,
  and keeps `querydatadetail` / save / lookup calls bound to the rendered View
  context instead of reusing the list View id or a concrete business DTO.
- 2026-07-04: the Vue main list no longer builds fallback columns from
  `row.values` object keys. If `getlistview` returns no table columns, the
  fallback now reads legacy row `Items` metadata (`PrpId` / `PrpShowName`) from
  `querydata`, matching FoolFrame's View-item/value pair rendering model and
  preventing concrete data DTO fields from defining the page structure.
- 2026-07-04: the Vue main View workflow now resets `pageIndex` to `1` when
  the operator clicks `Load View` / search, matching FoolFrame
  `querylistdata.js` where `query()` sets `$scope.page = 1` before
  `querydata()`. Pagination buttons still keep their explicit target page and
  reuse the loaded View id.
- 2026-07-04: the Vue View workflow now renders previous/next paging controls
  from legacy `querydata` totals (`totalItem`, `totalPage`, `pageIndex`,
  with `pageInfo` fallback), matching FoolFrame `querylistdata.js` updating
  `NavbarController` from `TotalItem`, `pageSize`, and `currentPage`. Page
  changes reuse the loaded View id and existing `querydata` path instead of a
  business DTO list shortcut.
- 2026-07-04: Vue list rows now keep `RequireSelect=true` operations even
  when the operation `ViewId` is `0`. This matches FoolFrame
  `querylistdata.js`, which still renders the operation name for the row but
  does not attach a `setselect(...)` target. Vue renders those metadata actions
  as inert text without a button role or handler, so the page reflects the View
  metadata without inventing a concrete DTO action or fake detail transition.
- 2026-07-04: Vue list rows now render `RequireSelect=true` operations that
  have a target `ViewId`, matching FoolFrame `querylistdata.js`, which calls
  `setselect(operation.ViewId, row.Id, rowIndex)` for row-level operation
  links. Selecting through one of those buttons loads `querydatadetail` with
  the operation target View id, and subsequent detail save, lookup, child-item
  refresh, and detail operation execution reuse that active detail View id
  instead of silently falling back to the main list View.
- 2026-07-04: the Vue main View toolbar now renders list-level create
  operations from `getlistview` metadata when `RequireSelect=false` and the
  operation has a target `ViewId`. This matches FoolFrame `view.jade`, which
  renders those operations as `new{ViewID}` links before row data is selected.
  The Vue flow reuses the existing `initnew` / `savenewobj` path and saves new
  objects with the same target View id used for initialization, so create
  actions stay View-driven instead of being hard-coded to the current list
  View or a business DTO.
- 2026-07-04: the Vue detail panel now renders operation buttons from the
  loaded `querydatadetail` `operations` payload instead of reusing the main
  `getlistview` metadata. This matches FoolFrame's detail page, where
  `detailView.jade` renders `view.Operations` and `detailview.js` executes
  `runoperation(viewid, obj.Id, opid)` from the loaded detail View context.
  The main page still follows the View-first flow: load View metadata, query
  rows for that View, then load detail data for the selected object without
  binding operation rendering to a concrete business DTO.
- 2026-07-04: `querydatadetail` now accepts legacy Pascal `ViewId` and
  `ObjId` request fields in addition to the Vue camel-case fields. This keeps
  the generic detail-data route aligned with FoolFrame Web payloads and
  preserves the View-first path: render/load by View id, then fetch detail data
  by that same View context. `scripts/runtime_doctor.py` now covers
  `querydatadetail(ViewId, ObjId)` through the frontend proxy.
- 2026-07-04: `ModelDataService` now hydrates legacy
  `SW_SYS_PROPERTY_TRIGGER` metadata onto model properties and executes
  property `SET` trigger `SET_VALUE` commands during dynamic create/save
  writes. This follows FoolFrame's property setter side-effect path at the
  model layer, so View-driven `saveobj` writes can pick up metadata-defined
  side effects without binding the migrated page to concrete business DTOs.
  ItemsAdd/ItemsDelete property triggers, non-`SET_VALUE` commands, trigger
  filters, and external-model invocation paths remain backlog.
- 2026-07-04: the Vue API-tools `Query Data` action now loads
  `getlistview(ViewId)` before calling `querydata` and sends the query through
  the loaded `currentViewId`. This keeps the shared row/table state aligned
  with the rendered View definition even when the operator uses the tool panel,
  and prevents data from one View being rendered under another View's columns
  or any concrete business DTO shortcut.
- 2026-07-04: `ModelDataService` now executes hydrated legacy model triggers
  for CREATE/SAVE/DELETE write paths, with `SET_VALUE` commands applied in
  legacy index order and persisted through the trigger `OperationBaseType`
  without recursive trigger execution. The command value parser is shared with
  `DataQueryService` runoperation handling, so `$` literals, current-field
  expressions, time context, math expressions, and BusinessObject lookups do
  not fork into a second parser. Focused MySQL proof covers a SAVE trigger
  updating a dynamic row; broader command grammar remains backlog.
- 2026-07-04: Vue metadata lookup editors no longer accept or forward a
  `viewName` fallback. BusinessObject candidate search now builds `inputquery`
  from the already rendered View id plus ViewItem id, keeping the page flow on
  FoolFrame's sequence: render View metadata first, then query data and lookup
  candidates through that View context. The backend still accepts `ViewName`
  for protocol compatibility, but the frontend main workflow does not use it.
- 2026-07-04: the Vue main View workflow now keeps loaded View rows and
  `querydatadetail` detail fields visible in the first desktop screen, with
  narrow screens using table scrolling instead of squeezing row action buttons.
  This keeps the frontend on the View metadata -> data -> detail path rather
  than introducing business DTO-specific rendering.
- 2026-07-04: `querydatadetail` responses now carry FoolFrame's Pascal detail
  aliases in addition to the Vue camel-case contract: `Data`,
  `AutoFreshTime`, `CanEdit`, `Operations`, detail `ObjId` / `SimpleData` /
  `Items`, collection `Properties` / `ListViewId` / `SelectFromExists`, child
  `DataID` / `Values`, and value `ObjId` / `PrpId` / `FmtValue` fields. This
  matches `ResultDataDetail`, `DataDetail`, `PropertyDataItems`, `DataItem`,
  and `ObjValuePair` without changing the View-first data lookup path.
- 2026-07-04: `getenums` responses now carry FoolFrame's legacy
  `EnumValues[].Name` / `EnumValues[].Value` aliases in addition to the Vue
  camel-case `enumValues[].name` / `enumValues[].value` contract. This matches
  `Soway.Server/Enum/GetEnumResult.cs` and `EnumValues.cs` without changing
  the generic enum lookup path.
- 2026-07-04: `getenums` now accepts the legacy model-id request spellings
  used around FoolFrame Web: `ModelId` from `Cloud-Social/soway.js`,
  `modelid` from the web route proxy, and `ModelID` for the existing legacy
  acronym convention. Enum lookup still resolves the generic model id through
  `ModelDataService` and does not introduce business DTO binding.
- 2026-07-04: `/saveobj` now accepts FoolFrame Web's Pascal/mixed generic
  save payload: top-level `SaveObj` plus nested `Id`, `ViewID`, `ParentId`,
  `Model`, `Propertyies`, `Itemproperties`, `Items`, `AddedItems`,
  `DelteItems`, `IsExist`, `ItemId`, `Key`, and `Value`. This matches
  `Cloud-Social/soway.js` and `detailview.js` object shapes while keeping the
  save flow View-driven and free of concrete business DTO binding.
- 2026-07-04: tightened the View-first runtime context after auditing the
  rendered Vue page flow. Generic `get-view` and `query-list` now use `ViewId`
  at the controller boundary, and the Vue main
  list/detail/save/lookup refresh paths now reuse the currently loaded View id
  instead of leaking manual tool-panel ViewName/detail ViewId state into the
  primary workflow. This keeps the intended order explicit: render View
  metadata first, then query/save data through that View context, without
  binding the migrated page to concrete business DTOs.
- 2026-07-04: `inputquery` can now resolve the active View by legacy
  `ViewId` before falling back to `ViewName`. Vue metadata lookup editors pass
  the loaded View id from the current View workflow, so candidate lookup
  follows the same sequence as the rendered page: `getlistview(ViewId)` first,
  then data/lookup requests by that View context. `ViewName` remains accepted
  only as protocol compatibility.
- 2026-07-04: added `scripts/runtime_doctor.py` as a repeatable Docker smoke
  for the current Vue/View workflow. It checks compose service state plus
  backend `/test`, frontend-proxied `getlistview(ViewId)`,
  `querydata(ViewId)`, `inputquery(ViewId)`, and `getmkqview(ViewId)` so
  runtime evidence no longer depends on hand-copied curl commands.
- 2026-07-04: report column identity now follows FoolFrame's
  `QueryFactory.GetQueryModel(view)` shape more closely. `getmkqview` derives
  candidate columns from the loaded View metadata and emits View/property keys
  such as `symbol` as `QueryCol.ID`; `getrpt`, `makereport`, `FilterExp`, and
  `SelectedTypeId` resolution now share a single View context that checks
  ViewItem names/labels/model properties from the rendered View. Hidden model
  properties that are absent from the loaded View are not accepted as explicit
  report/query order tokens. Numeric Java property ids remain accepted through
  rendered View metadata for compatibility, but the Vue report flow no longer
  depends on those ids or any concrete business DTO.
- 2026-07-04: the Vue report workflow now converts loaded `getmkqview`
  candidate columns into FoolFrame-style `ReportCols` payload JSON. Loading
  report columns fills `ColName`, `ColId`, first `SelectedTypeId`, `Index`, and
  default `OrderType=2`, matching the legacy `mkreport.js` add-column shape
  and reducing hand-written report DTO work in the migrated Vue tool.
- 2026-07-04: query SQL generation now supports both FoolFrame-style
  two-argument select-type expressions such as `[{0}].[{1}]` and the current
  Docker-seeded single-argument `SE_SELECTEDEXP` expressions such as `{0}` and
  `SUM({0})`. Single-argument expressions now receive the full selected column
  expression, keeping future report `SelectedTypeId` execution on the shared
  query builder path instead of adding report-controller special cases.
- 2026-07-04: report `getmkqview` candidate columns now come from configured
  View items when the View provides them, matching FoolFrame's
  `QueryFactory.GetQueryModel(view)` path. The endpoint uses View item names as
  the report column display names, orders them by View item `showIndex`, and
  falls back to Model properties only when a View has no item metadata.
- 2026-07-04: report `getrpt` now projects selected `ReportCols[].ColId`
  values into the requested `ColName` alias before rendering cells. This
  matches the FoolFrame report path where `ColName` becomes the selected query
  output column name, so custom report headers such as `Pair` still receive
  data from the View model property resolved by `ColId`.
- 2026-07-04: report `getrpt` now resolves legacy `ReportCols[].ColId`
  through the selected View's model metadata when `ColName` is absent. This
  matches the FoolFrame report protocol shape (`ColId`, `SelectedTypeId`,
  `Index`, `OrderType`) without binding report rendering to concrete business
  DTO fields: the endpoint still queries by View and renders rows from
  View-shaped list data. Docker proof covers both backend `8080` and frontend
  proxy `8081` with a ColId-only `ReportCols` payload.
- 2026-07-04: cleaned the Vue first-screen View workflow so it no longer
  presents itself as an `OrderList` / trading DTO screen. The page still
  starts from the legacy app shell default View, and rendering begins with
  legacy `getlistview(viewId)`, data loading follows with `querydata(viewId)`,
  and tool defaults no longer prefill order/trading filters or save payload
  fields.
- 2026-07-04: accepted FoolFrame Pascal request field names on the generic
  legacy `getlistview` and `querydata` protocol DTOs: `ViewId`, `PageSize`,
  `PageIndex`, `QueryFilter`, `OrderByItem`, and `OrderByType`. This fixes
  the legacy boundary without introducing `Order` or other concrete business
  DTO binding: the migrated flow still renders the page from View metadata
  first, then queries View-shaped data rows by the same View id. Runtime proof
  uses the same Pascal payloads emitted by `../FoolFrame/src/Web/Cloud-Social/soway.js`.
- 2026-07-04: tightened the repository source-size guard from 2200 to 2100
  lines now that the Vue workflow is below 2000 lines. The current largest
  source file remains under the new limit, and the harness still runs as the
  cheap CI/local backstop before future migration work grows large files again.
- 2026-07-04: moved the Vue report-grid cell matrix calculation from
  `App.vue` into the tested `viewWorkflow.ts` helper set. `App.vue` now stays
  below 2000 lines while report rendering still uses the same sparse
  `ReportCell(row,col,fmtValue)` contract from the migrated report API.
- 2026-07-04: moved reusable Vue child-group metadata helpers out of
  `App.vue` and into `viewWorkflow.ts`: child group keys, selected child View
  IDs, and empty child drafts are now shared tested helpers. `App.vue` dropped
  the duplicate local helpers and uses the existing `displayValue` formatter
  directly, keeping the View workflow smaller without changing runtime
  behavior.
- 2026-07-04: deleted the unused Vue `buildQueryRequest` helper and its
  `QueryRequestInput` / `VisibleFilterInput` types. The frontend no longer
  ships a request builder for the business-name `query-list` shortcut; active
  data loading stays on the legacy view-id path (`getlistview` then
  `querydata`), while lookup remains the legacy `inputquery` protocol keyed by
  the selected View item.
- 2026-07-04: accepted legacy Pascal request field names on the migrated
  `inputquery` protocol DTO: `Text`, `ViewName`, `ViewItemId`, `ModelID`,
  `ObjID`, `OwnerId`, and `IsAdded`. This keeps lookup requests at the
  view/data protocol boundary: the frontend first renders from View metadata,
  then sends the selected View item to `inputquery`, without binding the flow
  to a concrete business DTO. Docker proof covers both Pascal legacy payloads
  and the existing camel-style Vue payload returning `Ada Capital`.
- 2026-07-04: removed the visible Vue workspace shortcuts for the newer
  business-name `/api/v1/view/get-view` and `/api/v1/data/query-list` routes.
  The first-screen workflow and API tools now steer users through legacy
  `getlistview(viewId)` first and then `querydata(viewId)`, with result table
  cells rendered through the shared View-data row helpers instead of direct
  `row.values[column.property]` access. Frontend tests guard that `App.vue`
  no longer contains the old view-name routes or `buildQueryRequest`.
- 2026-07-04: rendered legacy View operations in the primary Vue View
  workflow. The standalone detail page now uses loaded View metadata to show
  operation buttons beside Edit/Save in the old toolbar order and executes
  them through the existing `/api/v1/data/runoperation` legacy DTO without
  introducing a concrete business DTO binding.
- 2026-07-04: fixed legacy dynamic `BusinessObject` saves to persist the
  referenced object id into ordinary foreign-key columns instead of binding the
  dynamic object itself to JDBC. This keeps the migrated flow metadata-driven:
  `getlistview` / `querydatadetail` provide View rendering and operation
  metadata first, while `runoperation` applies dynamic model data without
  coupling to concrete business DTOs. Docker proof for
  `runoperation(7002, ObjectId=1001)` now returns `success=true`, changes
  `market_order.order_state` to `1`, and preserves
  `market_order.order_customer_id=3001`.
- 2026-07-04: hydrated legacy operation-view parameter metadata from
  `SW_SYS_OPERATIONVIEW_ITEM`. FoolFrame's `ViewOperation.Operation` points to
  an `OperationView`, and that `OperationView` carries `Params` made of
  `OperationViewItem` rows. The migrated `getlistview` / `querydatadetail`
  paths now load those rows by `SW_SYS_OPERATIONVIEW.ParamsSysId`, join the
  underlying `SW_SYS_OPERATION_PARAM`, and expose `operations[].params[]` with
  item id/name/index plus param id/name/view/filter/value. The runtime join now
  follows the legacy chain `SW_SYS_VIEW_OPERATION -> SW_SYS_OPERATIONVIEW ->
  SW_SYS_OPERATION` instead of relying on matching operation-view/model-operation
  IDs. Docker seed data now has ViewOperation `7002` point to OperationView
  `8002`, which points to model Operation `7002`, plus parameter `7201` and
  operation-view item `8101`; runtime proof shows both list-view and
  detail-view operation DTOs include `paramName=remark`, `viewId=100`, and
  `filter=state=0`.
- 2026-07-04: completed the Docker proof for select-existing child saves.
  FoolFrame saves existing objects by loading the current object first
  (`GetDetail`), applying posted fields (`ObjUpdateToProxy`), then saving; the
  migrated `saveobj` path now does the same by loading existing dynamic data
  before applying partial `Propertyies` / `Itemproperties`, so readonly fields
  omitted by the Vue metadata form do not get written back as `NULL`. The Vue
  selected-existing payload now keeps readonly candidate fields in
  `AddedItems.Propertyies`, matching FoolFrame's selected-row behavior while
  ordinary child add/update still skips readonly form fields. Docker seed data
  includes `2002 / 1002 / Existing fee` as a reusable candidate; browser proof
  selected it into order `1001`, observed `Items 4 rows`, verified
  `market_order_item.order_id=1001`, then reapplied the idempotent seed to
  restore `2002` to order `1002`.
- 2026-07-04: exposed configured child View metadata for
  select-from-existing detail groups. The migration now maps `ViewItem`
  `listViewId`, `editViewId`, and `selectedViewId` from persisted View
  metadata into both legacy list-column DTOs and `querydatadetail`
  collection groups, matching FoolFrame's `ListView` / `EditView` /
  `SelectedView` contract for detail child items. Docker seed data now includes
  an `OrderItemList` View (`viewId=101`) and configures `Order.items` to use it
  for list, edit, and selected candidates. Runtime proof: `querydatadetail` for
  `OrderList` object `1001` returns `items.listViewId=101`,
  `items.detailViewId=101`, `items.selectedView=101`, and
  `items.selectFromExists=true`; Vue then loads `getlistview(101)` before
  `querydata(101, keyword=Legacy)` and renders the `2001 / Legacy item`
  candidate row.
- 2026-07-04: added candidate search/pagination state for select-from-existing
  child collection dialogs while keeping the page View-driven. The Vue detail
  panel still loads candidate View metadata first through legacy `getlistview`,
  then requests candidate rows through legacy `querydata`; keyword search is
  sent as `keyword` instead of a frontend-built SQL `queryFilter`, and the
  backend legacy query path now reuses the existing View/model keyword filter
  logic to map visible View items to real model columns. Raw `queryFilter`
  remains available for API tool/report compatibility. Candidate state is
  isolated in `useChildCandidates` instead of expanding `App.vue`.
- 2026-07-04: made the default Vue View workflow load itself on first render.
  The app now uses Vue's native `onMounted` hook to call the existing
  `loadViewWorkflow()` path, so the first screen follows the legacy app shell
  default View, selects the first row, and renders the detail panel without
  requiring a manual setup click.
- 2026-07-04: seeded and proved a live BusinessObject lookup path for the Vue
  metadata workflow. Docker MySQL now includes `Customer` model metadata,
  `market_customer` rows, and an editable `Order.customer` View item. The
  model/query/view pipeline now shares the same display-property fallback
  logic: explicit `showProperty`, then the first non-ID string property, then
  ID. `query-list` joins and maps the display field, `inputquery` searches the
  same field, and the backend emits flattened `values.customer` /
  legacy `items[].fmtValue` display text instead of raw dynamic DTO objects.
- 2026-07-04: replaced the hand-shaped default Vue `OrderList` screen with a
  metadata-driven View workflow. The first screen now loads View metadata via
  `/api/v1/view/get-view`, queries rows via `/api/v1/data/query-list`, renders
  list columns from `tableColumn`, renders detail fields from
  `querydatadetail.simpleData`, and builds `saveobj` `Propertyies` from the
  current View field keys instead of binding to `symbol` / `state` business
  fields. Child collection add/update/delete controls now render from
  `querydatadetail.Items[].properties` and preserve the legacy
  `Itemproperties.Items` / `AddedItems` / `DelteItems` payload names. The
  workflow has since moved to `getlistview` / `querydata` / `getreaditemview`
  ViewId-first calls and now includes native date/time/number scalar inputs;
  custom formatted widgets remain future work.
- 2026-07-04: added the first Vue select-from-existing child collection path.
  When `querydatadetail.Items[]` marks a group as `selectFromExists`, the
  detail panel can load the configured `selectedView` / `listViewId` through
  legacy `getlistview` + `querydata`, show candidate rows from that View
  metadata, and add the selected row through legacy
  `saveobj.Itemproperties.AddedItems`. Candidate search/pagination is covered
  by the later `useChildCandidates` slice below.
- 2026-07-04: added metadata-driven enum editors to the default Vue View
  workflow. Detail and child collection fields with `prpType=Enum` and
  `prpModelId` now load options through the migrated `getenums` endpoint and
  render as `<select>` controls while saving the enum DB value through the
  existing `saveobj` / `savenewobj` payloads. Readonly and lookup widgets are
  now covered by later migrated slices; custom formatted widgets remain future
  work.
- 2026-07-04: aligned Vue detail editing with legacy readonly handling from
  `detailview.js` / `savetext.js`. Detail and child collection fields marked
  with `readOnly=true` or `editType=ReadOnly` render as disabled controls and
  are skipped from `saveobj` / `savenewobj` `Propertyies`, matching the legacy
  `data-readonly` save filter. Lookup and formatted field-specific widgets
  remain future work.
- 2026-07-04: migrated legacy list row formatting to the Vue View workflow.
  `EditType.Format` values are already exposed by the backend as `rowFmt`;
  Vue now applies `rowFmt` as a row class, matching the old
  `querylistdata.js` behavior where format values were appended to `<tr
  class>`. Lookup field-specific widgets remain future work.
- 2026-07-04: added metadata-driven lookup editors for BusinessObject fields.
  Fields with `prpType=BusinessObject` (or legacy type `16`) and a property
  model now search through the migrated `inputquery` endpoint and only write
  the selected candidate ID into the `saveobj` / `savenewobj` draft, matching
  the old typeahead select behavior without adding a new frontend dependency.
- 2026-07-12: restored BusinessObject current-value and clear behavior from
  `setextype.js` / `savetext.js`. Lookup inputs now edit the View-provided
  `FmtValue` instead of showing it as a placeholder, preserve selected-id
  writeback, and clear the saved foreign-key id when the input is emptied.
- 2026-07-03: added the first Vue detail child-item write workflow for
  `OrderList`. The default detail panel can now add an order item through the
  legacy `saveobj.Itemproperties.AddedItems` payload, matching FoolFrame's
  detail-page save path for in-place child additions. Full generated child
  collection rendering/edit/delete remains future work.
- 2026-07-03: extended the default Vue `OrderList` workflow with the legacy
  new-object path. The `New Order` action now initializes through
  `/api/v1/data/initnew`, creates through `/api/v1/data/savenewobj`, refreshes
  the list/detail view, and renders list cells from legacy `fmtValue` so enum
  states show as `Open` / `Filled` instead of raw codes.
- 2026-07-03: changed the default Vue surface from a raw API operator console
  to a focused `OrderList` workflow. The first screen now loads Docker-seeded
  order rows, supports row selection, renders detail fields, and saves the
  selected order symbol/state through the migrated legacy data APIs. The
  temporary developer endpoint panels have since been removed.
- 2026-07-03: hydrated legacy runtime model-trigger metadata from
  `SW_SYS_MODEL_TRIGGER` and `SW_SYS_MODEL_TRIGGER_COMMANDS` onto
  `Model.triggers`. Runtime `ModelDataService.getModel` now carries trigger
  ID/type/filter/base operation and indexed command metadata for later trigger
  execution parity. Trigger side-effect execution on create/save/delete remains
  future work.
- 2026-07-03: reconciled `SCPB05-Soway.Model` runtime mutation status against
  current tests. Existing coverage now proves simple batch saves, old-id
  dynamic save lookup, DBMaps create/update writes, One2Many child-row
  create/update/delete-list sync, Many2Many/Recurve relation-table
  insert/delete-list sync, legacy `saveobj` `Itemproperties` request mapping,
  and legacy `savenewobj` new-object/owner-relation request mapping. The
  remaining runtime mutation list now only keeps unproven richer collection
  state, remaining command types, WCF/JSON/external-model edge cases, trigger
  side effects, and routed-connection transaction behavior.
- 2026-07-03: added legacy `runoperation`
  `CommandsType.ExuteOutModelMethod` execution for external model operations.
  The migrated path loads the target model from `SW_SYS_COMMAND_ARGMODEL`,
  resolves the target operation name from `SW_SYS_COMMAND_EXP`
  case-insensitively, resolves the target object ID from
  `SW_SYS_COMMAND_ARGID`, executes target create/update/delete commands using
  the source object as the value source, and maps the returned target value
  back to the source object through `SW_SYS_COMMAND_ARGEXP`.
- 2026-07-03: covered legacy `runoperation`
  `CommandsType.ExuteOutModelMethod` detail fallback. When the target model
  has no matching operation for `SW_SYS_COMMAND_EXP`, the migrated path now has
  test evidence for the FoolFrame `db.GetDetail(model, id)` behavior: it loads
  the target object by `SW_SYS_COMMAND_ARGID`, maps
  `SW_SYS_COMMAND_ARGEXP` back to the source object, and does not persist the
  target object. Richer nested external-model edge cases and trigger side
  effects remain future work.
- 2026-07-03: added legacy `runoperation`
  `BaseOperationType.Assebmly` execution for Java classpath handlers.
  `CommandsType.SetParamValue` and `CommandsType.SetConStrValue` now collect
  method and constructor argument values in command index order, then the
  migrated path instantiates `SW_MODEL_OPERATION_INVOKECLASS` and invokes
  `SW_MODEL_OPERATION_INVOKEMETHOD` with the current `IDynamicData` plus method
  parameters. `SW_MODEL_OPERATION_INVOKEDLL` is intentionally not loaded yet;
  richer external-model edge cases and trigger side effects remain future work.
- 2026-07-03: added legacy `runoperation`
  `CommandsType.ExuteListMethod` for list proxy objects. When a command targets
  a property whose current value exposes the named no-arg method, the migrated
  path invokes that method before the base operation save/delete/create step.
  `IDynamicData` list proxies are also supported through their existing
  `invoke` surface. Richer external-model edge cases and trigger side effects
  remain future work.
- 2026-07-03: extended legacy `runoperation`
  `CommandsType.ExuteProprtyModelMethod` to collection properties. When the
  target property is marked as a collection and the current value is iterable,
  the migrated command now invokes the named method on each `IDynamicData`
  item before the base operation save/delete/create step. Richer external-model
  edge cases and trigger side effects remain future work.
- 2026-07-03: added legacy `runoperation`
  `CommandsType.ExuteProprtyModelMethod` for direct `IDynamicData` property
  values. The migrated command path now invokes the named method on the current
  object's target property before the base operation save/delete/create step.
  Richer external-model edge cases and trigger side effects remain future work.
- 2026-07-03: added legacy `querydatadetail` blank-object fallback. When both
  `objId` and `IdExp` are blank, the migrated service queries the first page of
  the selected view model and loads the first returned object before formatting
  detail data. Token-backed app/data connection context now covers `@appcon`
  and `@datacon`; broader context expressions remain future work.
- 2026-07-03: added legacy `querydatadetail` static `IdExp` object-id
  resolution. Blank `objId` requests now map `$...` expressions to the target
  object ID before loading detail data, and the `IdExp` field is preserved from
  controller to service. Broader context expressions remain future work.
- 2026-07-03: extended legacy `runoperation` to `BaseOperationType.Create`.
  The existing hydrated object and command pipeline now runs through
  `ModelDataService.createData` before returning the legacy operation success
  message. Richer external-model edge cases and trigger side effects remain
  future work.
- 2026-07-03: hydrated legacy `SW_SYS_OPERATION` runtime metadata for
  `runoperation`. View-operation loading now carries
  `SW_MODEL_OPERATION_FILTER`, `SW_MODEL_OPERATION_ARGMODEL`,
  `SW_MODEL_OPERATION_ARGFILTER`, `SW_MODEL_OPERATION_INVOKEDLL`,
  `SW_MODEL_OPERATION_INVOKECLASS`, `SW_MODEL_OPERATION_INVOKEMETHOD`, and
  `SW_MODEL_OPERATION_RETURNMODEL` onto the runtime `Operation` object. Richer
  external-model edge cases remain future work.
- 2026-07-03: added legacy `runoperation` `Filter` command execution.
  `SW_SYS_COMMANDS` runtime hydration now includes arg/property/temp columns,
  command execution preserves command index order, and `Filter` guards query the
  current object with the raw legacy SQL expression before DELETE/UPDATE. A
  failed guard returns the OperationView error prefix plus the command
  `SW_SYS_COMMAND_PROPERTY_EXP` message instead of saving. Owner traversal,
  trigger side effects, richer list/external-model edge cases, and
  constructor/parameter command types remain future work.
- 2026-07-03: added legacy `runoperation` `SetValue` time-context
  expressions. Direct `@datetime`, `@date`, and `@time` command values now map
  to Java `LocalDateTime` values before UPDATE save. Auth/user/app/database
  context values, owner traversal, trigger side effects, and other command
  types remain future work.
- 2026-07-03: added legacy `runoperation` `SetValue` static
  `BusinessObject` value loading. `$...` expressions targeting a
  `BusinessObject` property now load the referenced row through the target
  property model before assigning the command value, matching the FoolFrame
  `GetStaticVlue` object branch.
- 2026-07-03: added legacy `runoperation` `SetValue` math-expression
  evaluation. Composite expressions such as `.retryCount+$2` now reuse the
  migrated `MathExpression` parser, resolve operands through the existing
  current-object and static-value branches, and convert the result back to the
  target property type. Later slices cover auth/user/app/database context
  values and owner traversal; other command types remain future work.
- 2026-07-03: widened legacy `runoperation` `SetValue` static-value
  conversion. `$...` command expressions now follow the simple scalar
  `GetStaticVlue` branches for Boolean, Byte, Char, DateTime, Int/UInt,
  Long/ULong, Decimal, and Double/Float target properties, while string-like
  property types keep the literal value. Later slices cover
  auth/user/app/database context values and owner traversal; other command
  types remain future work.
- 2026-07-03: extended legacy `runoperation` `SetValue` expression parity.
  FoolFrame-style current-object property expressions such as `.symbol` now
  read from the target object before UPDATE save, and static `$...` values are
  converted for `Int`/`UInt` target properties before assignment.
  Later slices cover auth/user/app/database context values and owner
  traversal; other command types remain future work.
- 2026-07-03: migrated the first legacy `runoperation` command execution
  slice. Persisted `SW_SYS_COMMANDS` rows now hydrate onto operations,
  `CommandsType.SetValue` literal expressions such as `$1` are applied before
  DELETE/UPDATE execution, and Docker seed data gives operation `7002` a
  command that writes `Order.state`. Other command types and the full
  FoolFrame `GetValueExpression` grammar remain future work.
- 2026-07-03: made legacy `runoperation` preserve the OperationView
  `ErrorMsg` failure surface. Persisted operation metadata now hydrates
  `SW_SYS_OPVIEW_ERRORMSG`, and DELETE/UPDATE execution exceptions return
  `success=false` with the legacy error-message prefix instead of escaping as
  a generic API error.
- 2026-07-03: extended legacy `runoperation` from DELETE-only to UPDATE
  operations, mapping FoolFrame `BaseOperationType.Update` to the existing
  dynamic `saveData` path. Docker seed data now exposes operation `7002`.
- 2026-07-03: made legacy `querydatadetail` and `initnew` reuse the hydrated
  `ViewDataService` path, so detail payloads include persisted view-operation
  metadata instead of only list-view definitions exposing operations.
- 2026-07-03: exposed legacy `runoperation` at
  `/api/v1/data/runoperation` and in the Vue operator console. It hydrates
  persisted view-operation rows, supports the Docker-seeded DELETE operation
  `7001` through the existing dynamic delete path, and is smoke-covered
  through both backend and Vue proxy routes. Reflection/WCF operation
  execution and trigger side effects remain future work.
- 2026-07-03: exposed legacy `getrpt` at `/api/v1/report/getrpt`
  and in the Vue operator console. It reuses the migrated `makereport`
  flat-grid report path, accepts the legacy `MakeReportOption` request shape,
  and is smoke-covered through both backend and Vue proxy routes.
- 2026-07-03: exposed legacy `initapp` at `/api/v1/auth/initapp`
  and in the Vue operator console. It accepts legacy `AppId` / `AppKey`,
  validates the app catalog, returns app title/name/image/version/power/url,
  creates a migrated check code, and lists the app store databases from
  `SW_APPLICATION_SW_STOREDB`.
- 2026-07-03: exposed legacy `loginv2` at `/api/v1/auth/loginv2`
  and in the Vue operator console. It accepts legacy `UserId`, `PassWord`,
  `DbId`, `CheckCode`, `AppId`, `AppKey`, and `CheckCodeKey`, validates the
  migrated check code, app key, and app/database relation, reuses the existing
  admin login/token flow, and returns legacy-style `LoginSucess`, `Token`,
  `User`, and `App` fields. The selected App and `DbId` are now kept in token
  context for AppInfo and `@appcon` / `@datacon`; broader `CacheStore` fields
  remain future work if a migrated consumer needs them.
- 2026-07-03: exposed legacy `getapp` at `/api/v1/auth/getapp`
  and in the Vue operator console. It accepts legacy `Token`, validates the
  current token through the existing token service, maps the token-selected
  `SW_APPLICATION` record to legacy `AppInfo`, and returns `Token` plus app
  fields such as `AppName`, `AppVer`, `AppPowerBy`, `AppLogoUrl`,
  `DefaultViewId`, and `AppId`.
- 2026-07-03: exposed legacy `getmain` at `/api/v1/auth/getmain`
  and in the Vue operator console. It accepts the legacy raw token request
  body, returns `Token`, legacy `User`, token-selected `AppInfo`, and
  top-level `TopMenu` items by reusing the migrated user-info, app-info, and
  top-menu paths. The selected `DbId` also feeds `@datacon` while the selected
  App feeds `@appcon`.
- 2026-07-03: exposed legacy `getsubmenu` at
  `/api/v1/auth/getsubmenu` and in the Vue operator console. It accepts legacy
  `Token` and `ParentAuthCode`, resolves the current user through the token
  service, maps top-level or child authorized menus, and returns legacy
  `AuthItem` fields such as `AuthNo`, `Text`, `ViewId`, `AuthType`, and
  `ViewType`.
- 2026-07-03: exposed legacy `getcheckcode` / `checkcode` at
  `/api/v1/auth/getcheckcode` and `/api/v1/auth/checkcode`, backed by Redis
  with the legacy 60-second validation window and surfaced in the Vue operator
  console.
- 2026-07-03: exposed the legacy `getuserinfo` handler shape at
  `/api/v1/auth/getuserinfo` and in the Vue operator console. It accepts legacy
  `Token`, resolves the current user through the existing token service, and
  returns the legacy `User` / `Token` payload fields; the old public
  `DataService.GetUserInfo` method itself remains a `NotImplementedException`
  stub in FoolFrame.
- 2026-07-03: exposed legacy `savenewobj` create-object handling at
  `/api/v1/data/savenewobj` and in the Vue operator console. The endpoint maps
  legacy `SaveObj`, creates new simple objects through the existing dynamic
  create path, and maps owner collection requests to the seeded relation target
  column.
- 2026-07-03: exposed legacy `initnew` empty-detail initialization at
  `/api/v1/data/initnew` and in the Vue operator console. The endpoint maps
  legacy `ViewId` / `ParentObjId`, formats the selected view as an editable
  empty detail payload, and preserves the optional parent object ID.
- 2026-07-03: exposed the legacy `getnotify` notification-count contract at
  `/api/v1/message/getnotify`. FoolFrame's `DataService.GetNotify` throws
  `NotImplementedException`, so the endpoint remains an empty protocol
  compatibility surface and is not presented in the Vue shell.
- 2026-07-03: exposed legacy `getmsg` generated-message polling at
  `/api/v1/message/getmsg` and in the Vue operator console. The endpoint uses
  the auth token to resolve the current user, returns the newest generated
  `SW_SYS_MSG` row, and marks it `Push`, matching the legacy
  `HandlerGetMessage` surface.
- 2026-07-03: exposed legacy `saverpt` report-definition save parity at
  `/api/v1/report/saverpt` and in the Vue operator console. This matches the
  FoolFrame `HandlerSaveReport` no-op success surface; persisted saved-report
  metadata remains future report work.
- 2026-07-03: exposed the legacy report model candidate-column lookup in the
  Vue operator console, calling `/api/v1/report/getmkqview` by view ID and
  rendering candidate columns with compare/select catalogs and enum states.
- 2026-07-03: exposed the legacy report model candidate-column lookup at
  `/api/v1/report/getmkqview` and `/api/v1/report/mkqview`, returning the
  `ViewId` model properties with report property type, model enum states,
  compare-operation catalogs, and select-type catalogs. `fool-query` now
  auto-configures its JDBC catalog beans so the runtime path uses the seeded
  legacy `SE_COMPARETYPE` and `SE_SELECTEDTYPE` metadata.
- 2026-07-03: migrated legacy report composite `FilterExp`
  `FirstExp`/`Sequences` handling for `/api/v1/report/makereport`, recursively
  composing simple compare leaves with legacy AND/OR `BoolOp` tokens and
  rejecting unknown composite bool operators.
- 2026-07-03: extended legacy report `FilterExp` simple compare handling for
  `/api/v1/report/makereport` from equality to the seeded legacy compare
  catalog IDs 1-7, including `包含` / `LIKE` matching, and maps legacy
  `Col.Name` / `Col.ID` tokens through view-model metadata to physical DB
  columns before composing the raw report filter.
- 2026-07-03: migrated simple legacy report `FilterExp` request handling for
  `/api/v1/report/makereport`, including legacy capitalized JSON aliases and
  safe equality conversion into the existing raw `QueryFilter` path; complex
  or non-equality report expressions fail instead of being silently ignored.
- 2026-07-03: exposed the migrated legacy `makereport` grid in the Vue
  operator console with typed payload building, default Docker `OrderList`
  report columns, rendered cells, Compose frontend rebuild, and `/api`
  proxy smoke coverage.
- 2026-07-03: exposed a migrated legacy `makereport` REST slice at
  `/api/v1/report/makereport`, reusing legacy `querydata` paging/filtering and
  `ReportGridRenderer` to return flat report cells for Docker `OrderList`
  smoke data.
- 2026-07-03: exposed the Docker backend `/test` seed-data smoke route in the
  Vue operator console, including Vite dev-server and Compose Nginx proxy
  coverage for the same route.
- 2026-07-03: migrated legacy `PROPERTY_SOURCE` metadata onto
  `Property.source`, app-install property persistence, and
  `fool_sys_model_property.source`; the running Docker MySQL volume was
  patched with the same column. `inputquery` source-list execution was closed
  by the following parity increments.
- 2026-07-03: migrated the legacy `inputquery` existing-object
  `Property.Source` source-list branch. When `ObjID` is present and the
  selected property has a source collection, candidates are filtered from that
  collection before falling back to target-model SQL lookup.
- 2026-07-03: migrated runtime `ViewItem.sourceExpression` metadata onto
  `fool_sys_view_item.source_expression` and made `inputquery` prefer the
  view-item source expression before `Property.source` when resolving an
  existing-object source list. Added the same column to the running Docker
  MySQL volume.
- 2026-07-03: migrated runtime `Model.owner` / legacy
  `MODEL_DEFAULTOWNER` metadata onto `fool_sys_model.default_owner` and
  `SW_SYS_MODEL.MODEL_DEFAULTOWNER`, including the running Docker MySQL volume.
  `inputquery` now handles the legacy added-item `IsAdded + OwnerId` source-list
  branch for owner expressions such as `#.availableCustomers`.
- 2026-07-03: tightened app-install default-owner parity by backfilling
  `MODEL_DEFAULTOWNER` after newly installed module-source models receive their
  generated `MODEL_ID` values.
- 2026-07-03: exposed the migrated `inputquery` candidate lookup in the Vue
  operator console with a typed payload builder and Docker frontend smoke
  coverage.
- 2026-07-03: exposed the migrated `saveobj` writeback API in the Vue operator
  console with typed payload building for simple properties and itemproperties,
  plus Docker runtime and MySQL writeback smoke coverage.
- 2026-07-03: exposed the migrated `querydatadetail` API in the Vue operator
  console with typed payload building, simple-data display, Docker runtime, and
  backend detail smoke coverage.
- 2026-07-03: exposed the migrated `getenums` API in the Vue operator console
  with typed payload building, enum-value display, Docker runtime, and backend
  endpoint smoke coverage.
- 2026-07-03: exposed the migrated `getlistview` view-id API in the Vue
  operator console with typed payload building, shared view-definition display,
  Docker runtime, and backend endpoint smoke coverage.
- 2026-07-03: exposed the migrated `getreaditemview` API in the Vue operator
  console with typed payload building, read-item field display, Docker runtime,
  and backend endpoint smoke coverage.
- 2026-07-03: exposed the migrated legacy `querydata` API in the Vue operator
  console with typed payload building, raw `QueryFilter` input, shared result
  table display, Docker runtime, and backend endpoint smoke coverage.
- 2026-07-03: hydrated legacy `querydatadetail` collection items for the Docker
  `OrderList` view. `getOneData` now loads relation-backed collection rows,
  `querydatadetail` returns child `Items` groups, list/get-view metadata keeps
  collection fields out of scalar columns, and the Vue `Order Items` panel
  renders persisted backend rows instead of session-only additions.
- 2026-07-03: exposed auth logout in the Vue operator console with a typed
  token-only payload builder, local token clearing, Docker runtime, and backend
  token-invalidation smoke coverage.
- 2026-07-03: seeded the Docker `OrderState` enum model in runtime and legacy
  metadata tables, linked `Order.state` to that enum, and defaulted the Vue
  enum/query/save panels to the numeric enum codes.
- 2026-07-03: seeded legacy `SW_SYS_MODEL` shell records for the Docker
  `Order` and `OrderItem` smoke models.
- 2026-07-03: seeded legacy `SW_SYS_PROPERTY` rows for the Docker `Order` and
  `OrderItem` smoke models, including the enum and collection property links
  used by the existing legacy relation row.
- 2026-07-03: seeded legacy `SW_SYS_VIEW` and `SW_SYS_VIEW_ITEM` rows for the
  Docker `OrderList` smoke list view, linked to the legacy model/property
  metadata.
- 2026-07-03: seeded legacy admin auth/menu rows for the Docker `OrderList`
  smoke workflow, including `SW_AUTH_USER`, `SW_APP_AUTH_USER`,
  `SW_APP_AUTH_ROLE`, `SW_APP_AUTH_MENU`, submenu, role-user, and role-menu
  relations.
- 2026-07-03: seeded the legacy `SW_SYS_MODULE` record for the Docker market
  smoke metadata and linked the seeded legacy model shells to that module.
- 2026-07-03: seeded legacy `SW_SYS_CON` current-database connection metadata
  for the Docker `car_wash` MySQL service.
- 2026-07-03: seeded legacy app-management records for the Docker smoke
  workflow, including `SW_APPLICATION`, `SW_STOREDB`, and the
  `SW_APPLICATION_SW_STOREDB` application/store-db relation.
- 2026-07-03: seeded legacy DB-management catalog rows for the Docker smoke
  workflow, including `DB_App`, `WorkDataBase`, `DB_AppDB`, and
  `DS_DataSourceSet`; the seeded `WorkDataBase` password payload decrypts with
  the migrated legacy DES cipher.
- 2026-07-03: made event object-query filters tolerate legacy SQL Server
  bracket identifiers when the migrated event target table is MySQL-style,
  converting filters such as `[order_state] = 0` to backtick identifiers while
  preserving bracketed legacy table queries.
- 2026-07-03: migrated event scoped runtime connection parsing for legacy
  `SqlCon.ToString()` / `Data Source=...;Initial Catalog=...` strings, switched
  the Docker app/store smoke connection strings to explicit MySQL JDBC URLs,
  and seeded a running `Order` event definition plus admin notification smoke.
- 2026-07-03: migrated `QueryContext.GetResult(connectionString, pageSize)`
  runtime connection routing. The Java overload now uses the supplied
  connection string to choose a `JdbcQueryExecutor`, including legacy
  `Data Source=...;Initial Catalog=...` parsing.

## Server Source Mapping

| FoolFrame project | C# files | fool-service target | Java main files | Status |
| --- | ---: | --- | ---: | --- |
| `SCPB01-Soway.Data` | 45 | `fool-common` | 53 | Partial data annotations, legacy tree factory/level-order traversal behavior, dynamic contracts, repeatable legacy column metadata including key-nullability/sql-type/identity flags, table column-prefix metadata, legacy display metadata, legacy entity display metadata, legacy enum-note metadata, legacy dynamic-column metadata, legacy default-owner marker metadata, legacy parent-relation metadata, legacy serial-number attribute metadata, legacy `BasicEnum` value registry without the `enum.txt` debug dump, legacy math-expression operator detection and variable-aware arithmetic evaluation, legacy column generation/encryption enum codes, legacy DB context interface surfaces, legacy controller CRUD/list interface surface, legacy row-backed object interface surface, graphic node/graph helper surface, legacy DS MD5 helper surface, legacy user-context value service boundary, `ObjectWithSubItem<>` marker, legacy `IBusinessObject`/`BusinessObject`/`IItemInterface`/`IItem` parent-assignment and `BO_Id` surface, legacy `PropertyType` codes, legacy `PropertyTypeAdaper` type/default-value mapping, legacy `SubItemList` added/updated/deleted tracking, legacy `BusinesObjectsWithItem` wrapper, and legacy `SerialNoObject` length surface migrated |
| `SCPB02-Soway.DB` | 24 | `fool-dao` | 21 | Partial DAO, mapper, SQL generation, legacy enum code-aware read/write mapping, `Column.noMap` field exclusion, legacy operation type ordinals, SQL operation names, transaction command carrier surface, and `GlobalSqlContext` default/type connection registry migrated |
| `SCPB05-Soway.Model` | 115 | `fool-model` / `fool-view` | 31 / 49 | Partial model/service/sql generator, base-model metadata, enum value metadata for runtime models, DAO rehydration of runtime enum value detail rows, legacy model default-owner metadata plus `SW_SYS_MODEL` parent/id-property/type/is-view/default-format/default-view schema columns, legacy single-row `GetDetail` lookup by model/data ID, legacy simple dynamic row create/update/delete plus simple batch saves, legacy old-id dynamic save lookup, BusinessObject foreign-key dynamic save writes, legacy simple dynamic default and collection initialization, legacy DBMaps create/update writes, One2Many child-row create/update/delete-list sync, Many2Many/Recurve relation-table insert/delete-list sync, legacy `savenewobj` new-object creation and owner-collection relation mapping, legacy `SW_SYS_MULTIMAP` DBMaps hydration, multi-column DB map metadata, multi-column DBMaps row-loading for dynamic data, relation-aware collection item query SQL and parent-id bucketing, legacy list-query DBMaps aliases, legacy simple-column and enum row default values, reflective model `ShowProperty` selection, legacy list-query BusinessObject show-property joins, keyword filters, ordering, and count joins, `ColumnAttribute.DefaultValue` metadata and MySQL DDL defaults, `ColumnAttribute.GenerationExp` metadata and MySQL DDL default expressions, legacy model/relation MySQL DDL generation, legacy model type codes, legacy relation type codes, legacy `ConnectionType` codes, legacy load/save type codes, legacy operation-base/operation/command/order type codes, legacy operation command runtime model including callback-supplied context values, legacy model/property trigger type codes, legacy `SW_SYS_CON` connection schema, legacy `SW_SYS_OPERATION`/`SW_SYS_OPERATION_PARAM`/`SW_SYS_COMMANDS` schema with owner columns and Java table mappings, legacy model/property trigger schema with owner columns and app-install Java table mappings, legacy `ItemEditType` and `ViewType` codes for view items, legacy full `SW_SYS_VIEW` schema plus view-file/view/operation-view owner columns and app-install Java table mappings, and legacy default auto-view generation migrated |
| `SWDQ01-Soway.Query` | 46 | `fool-query` | 49 | Partial filter DTO/query components, legacy bool-expression SQL generation for compare/between/in/composite/report filters including `BoolOp` DBName/ShowName tokens, selected-table compare-column `SimpleBoolExpression` SQL/report-parameter/display-string behavior including bracketed identifier normalization, legacy `BoolExpression` wrapper and expression factory create/add orchestration, persisted compare-operation and select-type catalog loading with Spring auto-configuration for runtime consumers, legacy query enum codes, selected column/table state models, selected query table unsupported getter/no-op setter surface, selected column collection duplicate-name/index/read-only/unsupported indexed-setter plus direct insert/remove-at behavior, selected table join-add direction handling, legacy add-table result contract, table/column lookup collection behavior including legacy string-indexer aliases, query instance parameter/result containers, query report definition contract, legacy report-parameter refresh orchestration, `QueryFactory` DBName-only table lookup normalization plus bidirectional column state-value dictionary surface, legacy table/column/base/paged select SQL builder with named report-parameter binding and bracketed identifier normalization, JDBC paged query executor, and `QueryContext` add/clear/CanJoinSelected/save-unsupported/connection-string-routed/nonpaged-SQL/result orchestration including legacy connection-string result overload with execution-time enum state-value hydration migrated |
| `Soway.Server` | 150 | `fool-view` | 49 | Partial view/data REST surface, legacy list-query column metadata, legacy list-query refresh metadata (`FreshTime`/`AutoFreshTime`), legacy list-query row indexes, paging aliases (`TotalItem`/`TotalPage`/`PageIndex`), `Data` result alias, row `Items`/`ObjValuePair` metadata, typed `ObjValuePair` Date/Time/Enum/BusinessObject formatting, legacy `getlistview` view-id definition API, legacy `getreaditemview` simple read-item API, legacy `getenums` enum-value API, legacy `querydata` view-id/paging/raw-`QueryFilter` API, legacy `querydatadetail` explicit-object, static/math/user-context `IdExp`, and blank-object first-row simple-data API, legacy `initnew` empty-detail initialization, legacy `savenewobj` create-object API, legacy `inputquery` business-object candidate lookup API including existing-object and added-item owner source lists, legacy `saveobj` simple `Propertyies` writeback API, legacy `saveobj` `Itemproperties` update/add/delete request mapping to dynamic collection writeback, legacy `getmkqview`/`mkqview` report model candidate-column API, legacy `makereport`/`getrpt` flat-grid report API with simple and composite `FilterExp` compare mapping, legacy report output row projection from row `Items`, legacy `initapp` app/check-code/db-list surface, legacy `loginv2` check-code/app/db/user login surface, legacy `getapp` default AppInfo API, legacy `getmain` user/default-AppInfo/top-menu shell, legacy `getsubmenu` menu AuthItem API, legacy `getmsg` message polling, and legacy `getnotify` empty notification-count shell, legacy `Type`, `Name`, `ShowType`, `TempFile` empty default, and `DetailViewId`, legacy view-item `ID`, `Name`, `PropertyName` with empty missing-property fallback, `ShowIndex`, `Width`, `Format`, `IsReadOnly`, `EditType`, `PropertyId`, `EditViewId`, `EditExp`, linked-list-view empty defaults, `PropertyType`, `PropertyModel`, and `ViewFile` metadata, ordered list columns/row values, list-view raw `Filter` SQL, global keyword filtering over read-only list items including BusinessObject show properties, default list SQL ordering by the first `ShowIndex` item descending including BusinessObject show properties, and BusinessObject show-property list joins, legacy view operation metadata surface including operation names, IDs, and locations, legacy list row-format handling for `ItemEditType.Format` view items, plus Docker-seeded `OrderList` view/data and collection-write smoke and legacy `SW_SYS_VIEW_FILE`/`SW_SYS_VIEW_ITEM`/`SW_SYS_VIEW_OPERATION`/`SW_SYS_OPERATIONVIEW`/`SW_SYS_OPERATIONVIEW_ITEM` schema migrated |
| `SWUA01-SOWAY.ORM.AUTH` | 5 | `fool-auth` | 38 | Auth API, role/user models, stable MD5-hex password storage, Docker auth base schema, legacy `SW_AUTH_USER` schema and Java table mapping, legacy `Sex` enum codes, legacy `LoginFactory.ToMD5` hash algorithm plus DAO-backed login/register/change-password/update-user behavior, legacy logout token invalidation, legacy `loginv2` user/app/db/check-code wrapper, legacy user-info/app-info/main-info token wrappers, legacy check-code generation/validation, token-backed `@userid` / `@username` context values, legacy submenu token wrapper, legacy `LoginLog` empty shell, and seeded admin login smoke partially migrated |
| `SWUA02-SOWAY.ORM.AUTH` | 13 | `fool-auth` / `fool-app-manage` | 38 / 49 | Auth/menu concepts, Docker auth menu relation schema, legacy role/company/department/menu/authorized-user scalar table mappings and collection surfaces, legacy company/department/user empty user-list factories, legacy authorized-user detail factory, legacy typed menu factory top/sub menu filtering wired through the `getsubmenu` AuthItem response, legacy `AuthItem` unsupported getter/no-op setter surface, legacy `RoleFactory` empty shell, legacy auth table-prefix metadata, legacy company/department/subdepartment/role-department app-install table mappings, and seeded `OrderList` auth menu smoke partially migrated |
| `SCPB03 -Soway.DB.Manage` | 15 | `fool-db-manage` | 16 | Working database catalog, app/source mappings, connection-string rendering, legacy DES password payloads, `WorkDataBaseFactory` list/create/save/delete behavior, `SqlCon` direct SQL query/nonquery/transaction adapter, and legacy `NotImplementedException` operation surfaces migrated |
| `SCPB07-Soway.AppManage` | 5 | `fool-app-manage` | 22 | App definitions, legacy `AppType` enum codes, store DB model, app lookup, and DAO-backed create-application adapter migrated |
| `SCPB08-Soway.AppManage` | 2 | `fool-app-manage` | 32 | Bootstrap menu/admin-role plan, legacy create-app installer side-effect flow, DAO-backed app creation gateway, creator authorized-user creation, app-system view preparation, root module/model installation records, model parent/id-property/type/is-view/default-format/default-view columns, model default-owner id backfill, menu-record/subitem relation creation, role creation, role-user relation creation, role-menu relation creation, model/relation DDL execution hook, installer-to-model-schema hook wiring, default auto-view hook wiring, App.SysCon/current database DAO routing split, connection-string `DaoService`/`JdbcTemplate` factory with legacy `SqlCon.ToString()` SQL Server string parsing, routed connection reuse boundary, static module-source model expansion with legacy dependency ordering, typed reflective Java module-source model/property discovery including inherited fields, reflective enum value metadata, reflective model `ShowProperty` selection, legacy `SW_SYS_EMUNVALUE` enum metadata persistence for module-source installs, file/jar package scanning, model-reference package traversal, basic collection `One2Many`, self-collection `Recurve`, bidirectional collection `Many2Many`, `ReferToProperyAttrbute`, `MultiTypeAttribute`, legacy `ObjectWithSubItem<>` parent target-property relation generation, legacy `ColumnAttribute` key-group/key-nullability/identity/generation/generation-expression/default-value/format/encryption/no-map/multi-column DBMaps/table column-prefix metadata for reflective module sources, and module-source module/model/property/relation/DBMaps metadata persistence migrated |
| `SCPB09-SOWAY.EVENT` | 20 | `fool-event` | 46 | Event definition/event/message models, legacy enum codes, event query SQL helper including raw DefModel filter SQL and null-filter empty command rendering, model-metadata table-name, explicit object-ID-column, auto-`SYSID` object-ID resolution, case-insensitive object-ID column matching, missing key-column validation, null-`DefModel` object-query empty result, zero-match object-query empty result, matched-object row value capture, message creation, message persistence, generated-message polling plus `Generate` -> `Push` transition through `/api/v1/message/getmsg`, legacy `SW_EVT_DEF` running-definition loading with `EVTDEF_STATE = 0`, direct NotifyUsers/NotifyRoles/NotifyDeps/NotifyCompanies relation loading, role authorized-user expansion, department/company department user expansion, object checks, idempotent event creation, runtime event deal-text from `Operation.Name`, runtime service, recipient expansion logic, All-authorized-user JDBC source, application/database catalog loading, legacy `SqlCon.ToString()` connection-string parsing, scoped system/database JDBC runtime, Spring-managed scheduler lifecycle, application/database polling traversal, and Docker-seeded Order event/message smoke migrated |
| `SWRPT01-Soway.Report` | 31 | `fool-report` | 31 | Report definitions and params, legacy `ReportFactory`/`IReportSource` empty shells, legacy unsupported getter/no-op setter surfaces for `Param`, `ParamInput`, report result/source/audit fields plus `ReportResult`, `ReportResultTable`, and `ReportResultTableColumn`, table/value/static formats, legacy report enum codes, matrix cells, legacy `MatrixHeader` comparison and `StaticCellValue` helper shape, source-row matrix construction, row/column static subtotal calculation cells, nested row/column static subtotal sibling-scope behavior, deep shared-ancestor subtotal scope behavior, legacy cell ordering, `TableHeader` unsupported getter/no-op setter surface, `MatrixResult.Add` unsupported surface, and legacy report grid rendering including flat-row column coordinates wired through `/api/v1/report/makereport` migrated |
| `SCPB07.TESTS` | 2 | module tests | varies | No direct parity target |

Mapping note: the 2026-07-03 `runoperation` slices add persisted
view-operation and command hydration in `fool-view`, wire the legacy
`Soway.Server` CREATE/DELETE/UPDATE surface to migrated simple dynamic create/delete/save
paths, and apply `SetValue` literal, current-object, static-object,
math-expression, time-context, and `Filter` guard commands before the base
operation. Runtime `Operation` metadata now includes legacy filter, argument
model/filter, invoke assembly/class/method, and return-model columns; the
migrated operation path executes Java class assembly reflection, property-model
method, list-method, and external-model command slices, while WCF/JSON base
operation types intentionally retain FoolFrame's no-op success behavior.

## fool-service Module Status

| Module | Java main files | Reactor wired | Notes |
| --- | ---: | --- | --- |
| `fool-common` | 53 | yes | Shared annotations including relation-mapping annotations, legacy tree factory/level-order traversal behavior, dynamic contracts, repeatable legacy column metadata including key-nullability/sql-type/identity flags, table column-prefix metadata, legacy display metadata, legacy entity display metadata, legacy enum-note metadata, legacy dynamic-column metadata, legacy default-owner marker metadata, legacy parent-relation metadata, legacy serial-number attribute metadata, legacy `BasicEnum` value registry without the `enum.txt` debug dump, legacy math-expression operator detection and variable-aware arithmetic evaluation, legacy column generation/generation-expression/default-value/encryption enum codes, legacy DB context interface surfaces, legacy controller CRUD/list interface surface, legacy row-backed object interface surface, legacy graphic node/graph helper surface, legacy DS MD5 helper surface, legacy context-value service boundary, legacy `ObjectWithSubItem<>` marker base type, legacy `IBusinessObject`/`BusinessObject`/`IItemInterface`/`IItem` parent-assignment and `BO_Id` surface, legacy `PropertyType` codes, legacy `PropertyTypeAdaper` type/default-value mapping, legacy `SubItemList` added/updated/deleted tracking, legacy `BusinesObjectsWithItem` wrapper, and legacy `SerialNoObject` length surface |
| `fool-dao` | 21 | yes | JDBC DAO layer, SQL script generation, legacy enum code-aware read/write mapping, `Column.noMap` field exclusion, legacy operation type ordinals, SQL operation names, transaction command carrier surface, and `GlobalSqlContext` default/type connection registry |
| `fool-auth` | 38 | yes | Auth REST API, Redis token flow including logout token invalidation, legacy `initapp` app/check-code/db-list payload wrapper, legacy `loginv2` check-code/app-key/app-database/user login payload wrapper, legacy `getuserinfo` token/user payload wrapper, legacy `getapp` default AppInfo payload wrapper, legacy `getmain` raw-token user/default-AppInfo/top-menu shell, legacy check-code generation/validation, token-backed `@userid` / `@username` context values, legacy `getsubmenu` token/menu AuthItem payload wrapper, role/menu models, stable MD5-hex password storage, Docker schema and Java table mapping for legacy `SW_AUTH_USER`, legacy `Sex` enum codes, legacy `LoginFactory.ToMD5` hash algorithm plus DAO-backed login/register/change-password/update-user behavior, legacy `LoginLog` empty shell, legacy `SW_APP_AUTH_ROLE`, `SW_APP_AUTH_COMPANY`, `SW_APP_AUTH_DEPARTMENT`, `SW_APP_AUTH_MENU`, and `SW_APP_AUTH_USER` scalar table mappings and collection surfaces, legacy company/department/user empty user-list factories, legacy authorized-user detail factory, legacy typed menu factory top/sub menu filtering, legacy `AuthItem` unsupported getter/no-op setter surface, legacy `RoleFactory` empty shell, modern `auth_user`, `auth_role`, `auth_item`, `auth_user_role`, and `auth_role_auth`, plus seeded admin auth smoke |
| `fool-dto` | 3 | yes | Common request/response wrappers |
| `fool-error-handler` | 9 | yes | Exception handling/autoconfiguration |
| `fool-log` | 11 | yes | Request logging filter/interceptor; response capture no longer creates a Spring bean cycle |
| `fool-model` | 31 | yes | Dynamic model/relation metadata, base-model metadata, runtime enum value metadata, reflective model `ShowProperty` metadata, DAO rehydration of runtime enum value detail rows, legacy single-row `GetDetail` lookup by model/data ID, legacy simple dynamic row create/update/delete plus simple batch saves, legacy old-id dynamic save lookup, BusinessObject foreign-key dynamic save writes, legacy simple dynamic default and collection initialization, legacy DBMaps create/update writes, legacy One2Many child-row create/update/delete-list sync, legacy Many2Many/Recurve relation-table create/update/delete-list sync, legacy `SW_SYS_MULTIMAP` DBMaps hydration, multi-column DB map metadata, `generation_expression`/`default_value` property metadata, service layer with DBMaps row-loading for dynamic business-object values, relation-aware collection item SQL for One2Many/Many2Many/Recurve parent bucketing, legacy list-query DBMaps aliases, joined BusinessObject show-property aliases, simple-column/enum row default values, legacy model type codes, legacy relation type codes, legacy `ConnectionType` codes, legacy load/save type codes, legacy operation-base/operation/command/order type codes, legacy operation command runtime model with callback-supplied context values, legacy model/property trigger type codes, legacy `SW_SYS_CON` connection schema, legacy `SW_SYS_OPERATION`/`SW_SYS_OPERATION_PARAM`/`SW_SYS_COMMANDS` schema with owner columns and app-install Java table mappings, legacy model/property trigger schema with owner columns and app-install Java table mappings, and legacy MySQL DDL generator for model property columns, raw default expressions, literal default values, no-map skips, multi-column DB map columns, primary/unique keys, and One2Many/Many2Many/Recurve relation DDL |
| `fool-query` | 49 | yes | Query filters and request DTOs, including legacy-style compare/between/in/composite/report SQL generation with ordered parameters, report-parameter reuse, and `BoolOp` DBName/ShowName tokens, selected-table compare-column `SimpleBoolExpression` SQL/report-parameter/display-string behavior including bracketed identifier normalization, legacy `BoolExpression` wrapper and `BoolExpressionFactory` create/add orchestration with owner-preserving report parameters, JDBC loading for legacy `SE_COMPARETYPE` compare-operation catalogs and `SE_SELECTEDTYPE` select-type catalogs plus auto-configuration for runtime consumers, legacy query enum codes, selected output-column/table DTOs, selected query table unsupported getter/no-op setter surface, selected column collection duplicate-name/index/read-only/unsupported indexed-setter/direct insert/remove-at behavior, selected table join condition direction handling, non-throwing legacy add-table result contract, legacy-style table/column lookup collections including string-indexer aliases, query instance parameter/result containers including the legacy `QueryResult.GetData` current-page data surface, query report definition contract for SQL, output columns, query parameters, report name, and report number, `QueryInsFac` report-parameter refresh orchestration, `QueryFactory` DBName-only table lookup normalization, column lookup default, and bidirectional state-value dictionary mapping, a legacy-style table/column/base/paged select SQL builder with join, enum CASE, ROW_NUMBER, WHERE text, GROUP BY, page projection, filter/page parameter payload support, named report-parameter binding for JDBC positional args, and bracketed identifier normalization, a JDBC paged query executor that maps count/page rows into `QueryResult`, and `QueryContext` ownership of add/clear/CanJoinSelected/connection-string-routed/save-unsupported/getSql/getResult orchestration including the legacy connection-string result overload plus legacy enum state-value hydration before SQL generation/execution |
| `fool-view` | 49 | yes | View/data controllers and adapters, legacy list-query `Cols`, `FreshTime`, `AutoFreshTime`, row-index metadata, paging aliases (`TotalItem`/`TotalPage`/`PageIndex`), `Data` result alias, row `Items`/`ObjValuePair` metadata, typed `ObjValuePair` Date/Time/Enum/BusinessObject formatting, legacy `getlistview` view-id definition API, legacy `getreaditemview` simple read-item API, legacy `getenums` enum-value API, legacy `querydata` view-id/paging/raw-`QueryFilter` API, legacy `querydatadetail` explicit-object, static/math/user-context `IdExp`, and blank-object first-row simple-data API, legacy `initnew` empty-detail initialization, legacy `savenewobj` create-object API, legacy `inputquery` business-object candidate lookup API, legacy `saveobj` simple `Propertyies` writeback API, legacy `saveobj` `Itemproperties` update/add/delete request mapping to dynamic collection writeback, legacy `getmkqview`/`mkqview` report model candidate-column API, and legacy `makereport`/`getrpt` flat-grid report API with simple and composite `FilterExp` compare mapping, legacy top-level `Type`/`Name`/`ShowType` aliases and `TempFile` empty default, legacy default `DetailViewId`, `ViewItem.ID`, `Name`, `PropertyName` with empty missing-property fallback, and `ViewItem.ShowIndex` ordering metadata, `ViewItem.Width`, `Format`, `IsReadOnly`, `EditType`, `PropertyId`, `EditViewId`, `EditExp`, linked-list-view empty defaults, `PropertyType`, `PropertyModel`, and `ViewFile` column metadata, raw view `Filter` SQL, global keyword filtering over read-only list items including BusinessObject show properties, default descending SQL order by the first `ShowIndex` item including BusinessObject show properties, and BusinessObject show-property list joins, legacy view operation metadata including operation names, IDs, and locations in view definitions, legacy `ViewType` codes, legacy list row-format behavior for `ItemEditType.Format` view items, legacy default auto-view factory behavior, Docker-seeded `OrderList` view/data and collection-write smoke, and legacy `SW_SYS_VIEW_FILE`/`SW_SYS_VIEW_ITEM`/`SW_SYS_VIEW_OPERATION`/`SW_SYS_OPERATIONVIEW`/`SW_SYS_OPERATIONVIEW_ITEM` schema |
| `fool-app-manage` | 49 | yes | Application/store DB model, legacy `AppType` enum codes, app-key lookup, legacy bootstrap defaults, create-app installer side-effect orchestration, DAO-backed create-application gateway, creator authorized-user model/creation, app-system view preparation, legacy root module/model installation records including parent/id-property/type/is-view/default-format/default-view columns, menu-record/subitem relation creation, role creation, role-user relation creation, company/department/subdepartment/role-department relation mappings including auth table-prefix metadata, migrated model/relation DDL execution hook, connection-aware metadata/DDL DAO routing with legacy `ConnectionType` codes, connection-string `DaoService`/`JdbcTemplate` factory including legacy `SqlCon.ToString()` SQL Server string parsing, cached single-connection `DaoService` boundary per routed legacy SqlCon, installer wiring for configured model schemas or static/reflective module sources, static module-source model dependency ordering, typed reflective Java module-source model/property discovery including inherited fields, reflective enum value metadata, reflective model `ShowProperty` selection, legacy `SW_SYS_EMUNVALUE` enum metadata persistence for module-source installs, file/jar package scanning, model-reference package traversal, basic collection `One2Many`, self-collection `Recurve`, bidirectional collection `Many2Many`, `ReferToProperyAttrbute`, `MultiTypeAttribute`, legacy `ObjectWithSubItem<>` parent target-property relation generation, legacy `ColumnAttribute` key-group/key-nullability/identity/generation/generation-expression/default-value/format/encryption/no-map/multi-column DBMaps/table column-prefix metadata for reflective module sources, module-source module/model/property/relation/DBMaps metadata persistence, legacy view-file/view/operation-view app-install table mappings, and default auto-view persistence |
| `fool-db-manage` | 16 | yes | DB app/source mappings, working database connection rendering, legacy password cipher, factory CRUD/list service, direct SQL execution adapter, and explicit unsupported legacy operation stubs |
| `fool-event` | 45 | yes | Event definition/event/message models, legacy enum codes, message factory, JDBC message persistence, generated-message polling and push-state updates for the legacy `getmsg` surface, legacy `SW_EVT_DEF` running-definition loading with `EVTDEF_STATE = 0`, direct NotifyUsers/NotifyRoles/NotifyDeps/NotifyCompanies relation loading, role authorized-user expansion, department/company department user expansion, model-metadata object query adapter with raw DefModel filter SQL, null-model empty result, zero-row empty result, table, explicit ID-column, auto-`SYSID` ID-column resolution, case-insensitive ID-column matching, missing key-column validation, and matched row values, idempotent event creation, runtime event deal-text from `Operation.Name`, runtime service, recipient expansion logic, All-authorized-user JDBC source, application/database catalog loading, legacy `SqlCon.ToString()` connection-string parsing, scoped system/database JDBC runtime, Spring-managed scheduler lifecycle, application/database traversal, auto-configuration, and Docker-seeded Order event/message smoke |
| `fool-report` | 31 | yes | Report definition/result DTOs, table formats, matrix table/cell model, legacy report enum codes, legacy `Param`/`ParamInput` unsupported getter/no-op setter surfaces, legacy `MatrixHeader` comparison and `StaticCellValue` helper shape, cell coordinate rendering, source-row matrix construction, static subtotal calculation cells including nested row/column sibling-scope subtotals and deep shared-ancestor subtotal scope, legacy cell ordering, `TableHeader` unsupported getter/no-op setter surface, `MatrixResult.Add` unsupported surface, and legacy report grid rendering with flat-row column coordinates now wired through `/api/v1/report/makereport` |
| `fool-dynamic` | 0 | no | Placeholder POM only; no Java source and no current FoolFrame parity surface, so it is intentionally not wired into the root reactor |
| `fool-reflect` | 0 | no | Placeholder POM only with legacy artifact id typo `foo-reflect`; reflective module-source behavior now lives in `fool-app-manage`, so it is intentionally not wired into the root reactor |
| `fool-restapi` | 0 | no | Non-Maven-path empty controller stubs only; real auth/view/data REST surfaces are migrated in `fool-auth` and `fool-view`, so it is intentionally not wired into the root reactor |

## Legacy Web Replacement

`../FoolFrame/src/Web` is an old Node/Express/Jade/Angular app with:

- `app.js`
- `routes/index.js`
- `routes/Dictionary.js`
- Jade views for default, list, detail, group, chart, and item pages
- config/social scripts under `Cloud-Config` and `Cloud-Social`

The new Vue app under `frontend/` replaces the first operator workflow with:

- A signed-out Vue login page aligned to old `index.jade`: application image
  and title, narrow unframed form, Chinese placeholders, hidden single-store
  selection, captcha refresh, stacked login/reset actions, footer metadata,
  login errors, and logout/stale-token return behavior
- Auth profile/menu/logout calls with a Docker-seeded admin smoke account
- Legacy `initapp` loading with legacy `AppId` / `AppKey`, app metadata,
  check code, and store database list
- Legacy `loginv2` loading with legacy `UserId`, `PassWord`, `DbId`,
  `CheckCode`, `AppId`, `AppKey`, and `CheckCodeKey`
- Legacy `getuserinfo` user/token payload loading with legacy `Token` alias
- Legacy `getmain` raw-token user/default-AppInfo/top-menu loading
- Authenticated desktop and mobile shell branding from legacy
  `getmain.App.AppName` / `AppVer`, without exposing Docker/MySQL/Redis
  implementation status in the user-facing header
- Legacy `getcheckcode` captcha image loading and refresh in the login page;
  `loginv2` validates the submitted key/code pair
- Legacy `getsubmenu` top/child menu loading with legacy `ParentAuthCode`
- Auth menu loading
- Vue API type for common token-only auth requests such as logout
- Vue API types for legacy check-code result and validation payloads
- Vue API types for legacy `getsubmenu` AuthItem result payloads
- View definition lookup through `/api/v1/view/get-view` and legacy
  `/api/v1/view/getlistview`; both use `ViewId`
- Legacy data query through `/api/v1/data/querydata`; the newer
  `/api/v1/data/query-list` remains a backend compatibility route but requires
  `ViewId`
- Vue API types for view operation names, IDs, operation locations, view operations, list row format, list-query columns, legacy view types/names/show types/temp files, default detail view IDs, and refresh metadata
- Vue API types for legacy list-query row indexes, paging aliases, `Data` result alias, and row `Items`/`ObjValuePair` metadata
- Vue API types for legacy `getenums` enum-value request/result payloads
- Vue API types for legacy `getlistview` view-id payloads
- Legacy shell menu rendering in Vue from `getmain` /
  `getsubmenu`, now presented as old Web-style horizontal desktop TopMenu /
  dropdown SubMenu navigation and as the same component in a mobile Drawer;
  `ViewId` entries open the same View-driven list workflow.
- Vue API types for legacy `getreaditemview` read-item payloads
- Vue API types for legacy `querydata` request payloads
- Vue API types for legacy `querydatadetail` request/result payloads
- Vue API types for legacy `querydatadetail` collection `Items` groups and child
  `DataItem` rows
- The default Vue `OrderList` detail panel renders persisted `Order Items`
  returned by backend `querydatadetail`
- Vue API types for legacy `initnew` request/result payloads
- Vue API types for legacy `inputquery` request/result payloads
- Vue API types for legacy `saveobj` request payloads
- Vue API types for legacy `savenewobj` request payloads
- Vue API types for legacy `runoperation` request/result payloads
- Vue API types for legacy `makereport` request/result payloads
- The View report panel calls legacy report routes with metadata-derived
  columns and visible structured filters from a modal with old Web output,
  condition, and save-definition tabs. Successful execution switches the same
  modal into a separate paged result state with a back action, then renders
  returned cells. Static report controls follow `view.jade` Chinese copy while
  metadata names and protocol values remain unchanged.
- Vue API types for legacy `getmkqview` report model candidate payloads
- The View report panel calls `/api/v1/report/getmkqview` by view ID and renders
  candidate columns, compare/select catalogs, and enum states
- Vue report-definition save payload support for legacy `ReportName`
- The View report panel saves definitions through `/api/v1/report/saverpt`
  using rendered report fields rather than raw JSON inputs
- Vue API types for legacy `getmsg` generated-message polling
- A Vue shell poller that calls `/api/v1/message/getmsg` every 15 seconds,
  immediately opens the first item in the old system-message dialog, and opens
  message targets through the existing View-first loader
- Vue API types for legacy `getnotify` notification-count payloads
- Protocol-only `/api/v1/message/getnotify` compatibility route; no old Web UI
  consumes the unimplemented count service
- Old Web authenticated shell actions for Home, navigation, automatic system
  messages, and safe logout while server-provided App/user/menu/message text
  remains unchanged
- Vue API types for legacy view-item `ID`, `Name`, `PropertyName`, `ShowIndex`, `Width`, `Format`, `IsReadOnly`, `EditType`, `PropertyId`, `EditViewId`, `EditExp`, linked-list-view defaults, `PropertyType`, and `PropertyModel` metadata
- Vue API types for legacy view-item `ViewFile` metadata
- Structured visible equality/range filters that emit Spring `QueryValue`
  payloads
- Legacy-style keyword filtering over read-only list columns
- Legacy-style keyword filtering over BusinessObject show-property list columns
- Legacy-style default ordering over BusinessObject show-property list columns
- Seeded `OrderList` metadata and rows for Docker smoke coverage
- A default Vue View workflow that loads View metadata first, queries rows by
  loaded View id, and renders columns from `tableColumn` / legacy `Items`.
  Like `view.jade`, list load/search does not select the first DTO or embed a
  detail panel, the request size stays at the old 10-row default without an
  invented page-size editor, and metadata-defined row/create operations
  navigate to old Web `/view{id}/{obj}` and `/new{id}` routes. Those
  standalone routes load read
  View metadata before `querydatadetail` / `initnew`, render detail fields,
  lay simple fields out in the old two-groups-per-row desktop form pattern
  with a one-column mobile fallback, save through `saveobj` / `savenewobj`,
  use old Web Chinese edit/save and child interaction copy, and return through
  browser history.
  Fresh Docker browser sessions bootstrap the legacy session first with
  `initapp` / `loginv2` so the initial View id comes from `App.DefaultViewId`
- The main list toolbar follows `view.jade` order and copy: input condition,
  search, report, then metadata create operations. Shared tables use the old
  operation/empty wording, while View-provided column and operation text stays
  untouched.
- The main list toolbar also follows the old compact placement: the query
  prompt remains inside the input instead of becoming a visible form label,
  desktop controls align to the right, and the input alone expands on narrow
  screens.
- Shared list/detail panels use the old Bootstrap 3 geometry: `#ddd` borders,
  4px corners, `#f5f5f5` heading bands, 15px content spacing, and a subtle
  one-pixel shadow. The invented 12px floating-card treatment is removed.
- Shared Vue row tables render only after View columns exist, so data rows from
  `querydata` cannot draw a page or row actions without a rendered View shape.
- A default Vue child collection workflow that renders from
  `querydatadetail.Items[].properties`, presents each collection as an old Web
  detail tab with a metadata-derived child table, and sends legacy
  `saveobj.Itemproperties.Items`, `AddedItems`, and `DelteItems` payloads
- A default Vue select-from-existing child collection path that loads
  configured candidate Views in an old Web-style modal, keeps manual create
  hidden for `SelectFromExists` groups, and adds selected rows through legacy
  `saveobj.Itemproperties.AddedItems`
- Metadata-driven Vue enum editors that call legacy `getenums` for detail and
  child collection fields with enum model metadata
- Metadata-driven Vue readonly editors that display locked detail/child fields
  and omit them from legacy save payloads
- Legacy `EditType.Format` row classes applied from `ListDataItem.rowFmt`
- Metadata-driven Vue lookup editors for BusinessObject fields using legacy
  `inputquery`
- Metadata-driven Vue operation buttons in the detail Edit/Save toolbar;
  legacy operation parameter metadata remains protocol-only because the old
  Web execution path posts View/object/operation ids
- No production API console, raw response dump, migration map, editable View
  ID, raw SQL filter, or manual business DTO form
- Vite and Nginx proxies for `/api/*` to the backend service

## Remaining Migration Work

- Continue concrete `AppInstallGateway` parity only if a real migrated module
  needs fully automatic Java classpath dependency inference without declared
  dependency packages. Declared dependency packages now cover the practical
  Java replacement for FoolFrame assembly-reference module discovery.
  Application/user/menu/role install records, configured model/static/reflective
  module-source schema wiring, recursive file/jar package scanning, referenced
  model package traversal, module dependency ordering, reflection relation
  generation, `ColumnAttribute` metadata including DBMaps, enum metadata
  persistence, routed DAO reuse, and per-routed-connection transaction
  boundaries are covered by the current module map and tests.
- `SCPB05-Soway.Model` runtime data mutation parity is complete for the active
  datasource used by migrated modules. Collection add/set/delete state,
  external-model create/update/delete/detail fallback and result mapping,
  trigger filters and assembly handlers, and parent/owned-child rollback are
  covered by the current implementation and tests. FoolFrame itself commits
  Create/Save row transactions before running model triggers, so it does not
  provide a cross-connection atomicity contract to reproduce. Reopen
  model-specific connection routing only when a real migrated model declares a
  non-empty legacy `MODEL_CON`; all current Docker `SW_SYS_MODEL` rows leave it
  null and use the token-selected active `car_wash` datasource. The Java enum
  preserves all FoolFrame command ordinals; WCF/JSON base operations keep the
  legacy no-op success surface, `SetAccess` has no persisted server mutation,
  and `SetSource` is not executed by `ModelMethodContext`.
- Continue `SWDQ01-Soway.Query` only when a new legacy query surface is
  identified beyond the currently migrated bool-expression SQL generation,
  selected table/column state, query/report parameter containers,
  `QueryFactory`, SQL builder, JDBC executor, and `QueryContext` orchestration.
- Continue `SCPB09-SOWAY.EVENT` only when a new legacy event surface is
  identified beyond the currently migrated object-query, message, scheduler,
  and admin-notification path. The previously listed null-model, zero-row,
  table-name, explicit/auto ID-column, case-insensitive ID-column,
  missing-column, and matched-row value cases are covered by the current module
  map and tests, so they are no longer counted as open remaining work.
- Continue `SWRPT01-Soway.Report` only when a new legacy report surface is
  identified beyond the currently migrated report definitions, source-row
  matrix construction, static subtotal behavior, flat-grid rendering, and
  empty `ReportFactory` / `IReportSource` shells.
- Continue database schema coverage only when remaining model/runtime parity
  identifies a concrete missing table or column. Compose now replays the same
  ordered, idempotent catalog for fresh and existing `car_wash` volumes before
  backend startup. The catalog currently covers smoke/order, FH_JAVA legacy
  `market_symbols`, app/DB management, event/message, auth graph, View/model/
  operation/trigger/relation metadata, query catalogs, and the Vue `OrderList`
  workflow guarded by the runtime doctor.
- 2026-07-12: removed the last invented indigo detail-link color. Child
  Edit/Detail links now use Bootstrap 3's `#337ab7` link and `#23527c` hover
  colors while preserving the existing metadata-derived routes and click
  behavior.
- 2026-07-12: revalidated the current signed-out Vue page without reading the
  CAPTCHA content. At 1440x900 and 390x844, the 240px login form and all
  controls remain inside the viewport with no horizontal overflow. Reset clears
  username, password, and CAPTCHA and requests a new image; Refresh requests a
  new image, clears only CAPTCHA input, and preserves username/password. Fresh
  CAPTCHA authorization on 2026-07-13 completed the authenticated desktop/mobile
  workflow replay and the final Sudoku-refresh/operation-result interaction
  checks recorded under `artifacts/runs/20260713-final-visible-interactions/`.
- 2026-07-12: restored the old `item.jade` collection-definition interaction.
  `/itemview:id` now renders `getreaditemview.DetailViews` as selectable tabs
  with each child View's metadata field table instead of flattening every child
  into a static name/comma-list summary. It reuses the same collection tabs as
  object detail pages but hides Add, candidate selection, and mutation controls
  because item View routes remain metadata-only and never query object data.
  The old template gives neither its tab links nor tab panes an initial active
  class, so schema-only Vue pages now also wait for a user tab selection before
  showing a child table. A follow-up audit found the same static markup and no
  script-side tab activation in `detailView.jade`, so detail and new-object
  pages now share that initially inactive contract.
- 2026-07-12: restored template-specific top-level command availability.
  Normal `view.jade` pages retain search, report, and metadata create commands;
  `viewWithChart.jade` exposes only its old search command; and `Sudoku.jade`
  renders no invented root toolbar. View navigation also closes an open report,
  and report rendering is restricted to normal list templates so SPA state
  cannot leak a prior list report into chart or Sudoku pages.
- 2026-07-12: restored `viewWithChart.jade`'s entry-time data-first state
  across SPA navigation. The shared list panel now resets its active pane to
  `数据` only when the loaded View id or template kind changes; search, paging,
  auto-refresh, and row-data updates preserve the user's current pane.
- 2026-07-12: restored `default.jade`'s authenticated application footer. Vue
  now reads Pascal/camel `AppPowerBy` from the already loaded `getmain.App`
  metadata and renders the old copyright line after the workspace, with
  responsive spacing but no fixed company value or extra request.
- 2026-07-12: restored the authenticated Home route from old `index.js`.
  Desktop/mobile `首页`, the initial root route, and the old clickable desktop
  brand now reload `getmain.App.DefaultViewId` through the shared View-first
  workflow instead of doing nothing on another list View. Apps without a
  default View show the original configuration guidance and no stale View.
- 2026-07-12: restored old shell route history for SPA navigation. Top/submenu
  View targets and system-message View/detail targets now push their canonical
  `/view{id}` paths before reusing the existing loaders, while `popstate`
  replays `loadInitialRoute` so browser back/forward changes both URL and page
  state without pushing a new entry during route replay. Path generation is
  shared and no Vue Router dependency was added.
- 2026-07-12: restored `menuinfo.js`'s explicit logout route replacement.
  Successful `安全退出` now replaces the current View/detail/new URL with `/`
  before preparing a fresh login CAPTCHA, so the next login opens the App
  default View. Stale-token recovery still leaves its URL unchanged and resumes
  that requested deep link after authentication.
- 2026-07-12: restored Bootstrap dropdown toggle behavior in the shared shell
  menu. Clicking the currently expanded metadata parent now collapses and
  clears its submenu instead of issuing another request and staying open;
  desktop/mobile `aria-expanded` uses that same state, while direct View menus
  retain their existing navigation path.
- 2026-07-12: restored full-page navigation's transient shell reset in the SPA.
  Direct menu Views, Home, message targets, browser back/forward, and session
  cleanup now reuse one `closeShellNavigation` helper to close the mobile Drawer
  and clear open dropdown data. Parent expansion itself does not call it, so
  mobile users can still open a parent and choose a child View.
- 2026-07-12: restored `itemreadonly.js`'s detail-entry tab state for SPA
  object changes. When the selected object or new/existing context changes,
  the shared detail panel now selects the first metadata child group and closes
  any candidate picker from the prior object. Tab changes within one object
  remain user-controlled.
- 2026-07-13: restored `viewWithChart.jade`'s default Data tab when shell
  navigation re-enters a View, including Home, menu, and browser-history paths
  that resolve to the same View id. Query, paging, and automatic refresh keep
  the user's current tab because they do not represent page entry.
- 2026-07-13: restored the old Sudoku `Map.jade` presentation contract. The
  shared Leaflet panel now keeps marker details in the clickable popup instead
  of rendering a Vue-only coordinate list below the map, and `.sw-map`'s fixed
  200px height is preserved at desktop and mobile widths. The View-derived
  marker mapping and existing refresh footer remain unchanged.
- 2026-07-13: restored the old Sudoku `linechart.jade` fixed partial height
  without changing `viewWithChart`. Sudoku alone opts into a reusable compact
  chart mode matching `.sw-partialchart`'s 200px container; its SVG viewBox
  follows the rendered aspect ratio and mobile labels are sampled to prevent
  overlap. Normal chart pages retain the original 720x300 viewBox and sizing.
- 2026-07-13: restored `Sudoku.js`'s one-time global flow-control height lock.
  After all root and Group-list View data is ready, Vue measures the natural
  panel maximum once and assigns it to every grid row. Group tab switches and
  panel refreshes do not remeasure or shift the page; a new Sudoku page entry
  clears and recomputes the lock for that viewport.
- 2026-07-13: removed the Vue-only root data table from Sudoku pages. The
  shared list renderer now mounts its root table only for normal list and
  `viewWithChart` templates, while `Sudoku.jade` routes render only their
  View-derived child panels. Normal/chart root tables remain unchanged.
- 2026-07-13: restored `subitem.js`'s Sudoku Item field matrix. View-derived
  detail fields now render two label/value pairs per row and pad to at least
  six rows instead of showing one field per row. The 29-line Item component
  keeps this table projection out of the parent panel dispatcher and continues
  to consume only `querydatadetail` SimpleData metadata.
- 2026-07-13: removed the Vue-only root View title from Sudoku. Old
  `Sudoku.jade` starts directly with its child-panel row, so the shared list
  heading now renders only for non-Sudoku templates. All five child metadata
  titles remain visible, and normal/chart View titles are unchanged.
- 2026-07-13: restored `detailView.jade`'s exact pre-query candidate feedback.
  The select-existing dialog now shows `记录数未知,请查询` after its View
  metadata loads and before Find/paging queries any rows; queried states still
  use `共N条记录` through the same shared helper.
- 2026-07-13: restored `detailView.jade`'s candidate-search Enter submission.
  The metadata-driven candidate input now sends the same existing query event
  as its Find button when Enter is pressed, matching the old form's implicit
  submit behavior without duplicating query or paging state.
- 2026-07-13: restored the candidate dialog's shared legacy pagination. Old
  `querylistdata.js` routes candidate totals and the current page through
  `navbar.updateNavbar`, so the Vue dialog now reuses the same extracted
  seven-link Previous/Page/Next component as the main list instead of its
  invented `第 x / y 页` status. Pre-query candidate pagination stays hidden.
- 2026-07-13: restored `detailView.jade`'s candidate dismissal copy. The
  functional footer action is now `取消` and still closes without adding a
  child row. The old inert `确定` placeholder was restored later without a
  handler.
- 2026-07-13: restored `detailView.jade`'s compact right-aligned candidate
  query form. Candidate search now shares the main list's 240px desktop input
  geometry, inline Find command, and responsive input expansion; the Vue-only
  visible label was replaced by the old intended `输入条件` input prompt while
  retaining an accessible name and Enter submission.
- 2026-07-15: restored the plain-text query controls rendered by `view.jade`
  and `detailView.jade`. The shared main-list and select-existing inputs now use
  `type="text"` instead of Vue's native-search variation, removing the
  browser-specific clear affordance while retaining the intended `输入条件`
  prompt, accessible name, Enter submission, Find command, and View-derived
  keyword request. Full frontend tests passed (20 files, 205 tests), the
  production build passed, and authorized browser checks against clean commit
  `9a68d879` proved both text inputs and HTTP 200 keyword queries.
- 2026-07-13: restored `querylistdata.js`'s fixed page-row presentation. The
  shared metadata table now pads only missing rows with inert striped cells:
  normal/chart/candidate queries keep ten body rows and Sudoku List/Group List
  partials keep their configured five. Filler rows expose no operation buttons
  and never enter row-selection or View data state.
- 2026-07-13: restored `view.jade` / `viewWithChart.jade` ViewItem column
  widths. Shared list and candidate columns now consume normalized
  `width`/`Width` metadata and emit a pixel width only for positive values;
  zero remains the old automatic table layout and no data-row DTO is involved.
- 2026-07-13: restored `querylistdata.js`'s contextual row presentation.
  Existing View-first `EditType=10` values already become table-row classes;
  the shared metadata table now supplies Bootstrap 3's `active`, `success`,
  `info`, `warning`, and `danger` backgrounds and hover colors instead of
  leaving those legacy `RowFmt` classes visually inert.
- 2026-07-13: restored the shared Bootstrap `.table-hover` feedback used by
  normal lists, chart data, candidate results, and Sudoku lists. Every shared
  metadata row now uses the old `#f5f5f5` hover background; the more specific
  contextual `RowFmt` hover colors continue to take precedence.
- 2026-07-13: restored each template's Bootstrap stripe contract. Normal,
  chart, and candidate tables now stripe the first row with exact `#f9f9f9`
  and alternate with white, while Sudoku List/Group List tables opt out like
  `includes/List.jade`; one shared `striped` prop avoids separate renderers.
- 2026-07-13: restored template-specific Bootstrap table density. Normal and
  chart tables retain `.table-condensed`'s exact 5px cell padding; candidate
  and Sudoku List/Group List tables use the default 8px. A shared `condensed`
  prop replaces PrimeVue's previous unconditional small size.
- 2026-07-13: restored Bootstrap table-cell geometry in the shared renderer.
  Headers are now 14px bold, 20px high-line, bottom-aligned with a 2px `#ddd`
  divider; body cells use the same type scale, top alignment, and a 1px `#ddd`
  top border instead of PrimeVue's centered slate-bordered rows.
- 2026-07-13: restored Bootstrap `nav-tabs` presentation across chart,
  detail-collection, report, and Sudoku Group tabs. One shared `legacy-tabs`
  class now renders blue inactive links and white bordered active tabs with
  10x15px padding while preserving each existing Vue tab state and workflow.
- 2026-07-13: restored `navbar.js`'s Bootstrap pagination presentation through
  the shared list/candidate paginator. Previous and Next now render the old
  `«` / `»` controls; page links form a contiguous 34px `#ddd`-bordered group
  with Bootstrap active, hover, and disabled colors while retaining the
  already migrated seven-page algorithm and Vue page events.
- 2026-07-13: restored the old `btn-default` command hierarchy for the shared
  list search action. Normal `view.jade` and `viewWithChart.jade` Find buttons
  now use the same secondary outlined presentation already shared by the
  candidate Find and normal-list report command, without changing Enter,
  click, View-first query, or template-specific toolbar behavior.
- 2026-07-13: restored Bootstrap form-control geometry in both shared query
  toolbars. Normal/chart list and select-existing candidate inputs and commands
  now use the old 34px height, 6x12px padding, 14px type, 20px line height, and
  4px radius while retaining their existing desktop placement, mobile wrap,
  Enter submission, and View-derived query state.
- 2026-07-13: restored old dialog close-command availability. System messages,
  operation success/error feedback, and operation execution results now omit
  PrimeVue's invented header Close button and retain their old footer commands;
  select-existing and report-setup dialogs remain header-closable as their Jade
  templates specify.
- 2026-07-13: restored Bootstrap's header close presentation for the remaining
  closable candidate and report-setup dialogs. Both reuse one `×` slot and one
  shared 21px bold, borderless, transparent close style with old 0.2 resting
  and 0.5 hover/focus opacity; PrimeVue continues to own the close event and
  accessible button label.
- 2026-07-13: restored shared Bootstrap modal chrome across all migrated Vue
  dialogs: a 1px translucent outer border, 6px radius, 5x15px shadow, 15px
  header/content/footer padding, `#e5e5e5` header/footer dividers, and 18px
  medium titles. Existing dialog widths, body layouts, footer commands, close
  availability, and workflow state remain component-owned.
- 2026-07-13: restored Bootstrap modal-footer command presentation. Legacy
  default dismiss, message, and feedback actions now use outlined secondary
  buttons instead of text or filled controls; all dialog footer buttons share
  the old 34px, 6x12px, 14/20px geometry and 5px spacing while report Confirm
  and Return commands retain primary emphasis.
- 2026-07-13: restored `view.jade`'s distinct report-save command hierarchy.
  `保存报表定义` now uses the information action severity corresponding to the
  old `.btn-info`, while Cancel remains default and Confirm remains primary;
  the existing metadata-driven enablement and save route are unchanged.
- 2026-07-13: restored the report result's right-aligned Bootstrap
  `.btn-group-xs` paging presentation. Functional Previous/Next commands now
  form one contiguous 22px group with shared inner borders and outer corner
  radii; the old eventless export placeholders remain intentionally omitted.
- 2026-07-13: restored `view.jade`'s two report-output tool groups. Column
  movement/removal and sort controls now occupy separate compact, labelled
  groups with old toolbar spacing, while reusing the existing icon commands
  and metadata-driven output state.
- 2026-07-13: restored `detailView.jade`'s default detail command group. Edit,
  Save, and View operations now share one contiguous secondary outlined group
  with stable 34px geometry; existing edit/save enablement and operation events
  remain unchanged.
- 2026-07-13: restored `detailView.jade`'s lightweight child-row actions.
  Inline Edit/Save and Delete now render as text commands instead of bordered
  buttons while retaining icons, danger emphasis, staged mutations, and the
  existing parent-save boundary.
- 2026-07-13: restored the child collection Add command's shared default-group
  geometry. The existing secondary outlined command now reuses the same 34px,
  6x12px, 14/20px group contract as the detail toolbar without changing its
  direct-add or select-existing dispatch.
- 2026-07-13: restored `querylistdata.js`'s default candidate Select command.
  The shared table's candidate-only default action now renders secondary
  outlined like old `.btn.btn-default`, while metadata View operations retain
  their lightweight link presentation and both continue using one renderer.
- 2026-07-13: restored the condensed report-condition control geometry.
  Join, field, comparison, state, and text controls now share the migrated 34px,
  6x12px, 14/20px form contract while preserving the old table-like column
  layout, horizontal mobile scrolling, and metadata-driven input switching.
- 2026-07-13: restored `view.jade`'s report-output Add command hierarchy. The
  right-arrow remains icon-only and right-aligned but now uses a secondary
  outlined 34x34 command instead of an invented filled primary action. Mobile
  output lists use a shorter responsive height so the command stays above the
  dialog footer; output selection, deduplication, and state remain unchanged.
- 2026-07-13: restored `mkreport.js`'s report-output toolbar availability and
  label semantics. Move, remove, and sort commands remain available without a
  selected output and rely on their existing no-op guards at empty/boundary
  states; sorting now appends `[升序]` / `[降序]` directly with no Vue-only
  space. Request-pending protection and metadata-driven output state remain.
- 2026-07-13: restored `view.jade`'s empty report-condition structure. The
  condition tab now shows only its header controls and trailing Add row before
  any condition exists, removing the Vue-only explanatory empty-state text
  while preserving both Add entry points and all filter/group state.
- 2026-07-13: restored the visible report-condition merge glyph. The invalid
  PrimeIcons `pi-object-group` name was replaced with the installed `pi-list`
  equivalent of old `glyphicon-list`, retaining the existing icon-only label,
  grouping event, availability rule, and condition state.
- 2026-07-13: restored `mkreport.js`'s condition-merge interaction. The merge
  command remains available outside request-pending state; one selected row
  reports `不能合并单个`, gaps/partial groups report `不连续不能合并`, and a
  valid contiguous selection still groups and clears its checks. Zero selection
  is retained as a visible no-op without reproducing the old script error.
- 2026-07-13: restored `ShowReportController` paging boundary interaction.
  Previous and Next remain available outside request-pending state, while one
  local handler now performs the old in-range check before reusing `runReport`;
  first/last-page clicks are visible no-ops instead of disabled controls.
- 2026-07-14: restored `mkreport.js`'s grouped-condition selection contract.
  A completed group now exposes only its first condition's checkbox, matching
  the old removal of later `chkbox` elements; that representative selects or
  clears the complete top-level group. Merge feedback counts the group as one
  selectable unit, while selecting it with an adjacent condition still creates
  a valid outer group through the existing recursive condition model.
- 2026-07-14: restored `view.jade`'s fixed report-result table at zero data.
  The Vue result view now keeps its table and empty `tbody` mounted instead of
  replacing them with the invented `暂无报表数据。` message. A zero-match Docker
  report retains only the protocol-supplied column heading, matching the old
  `mkreport.js` row-clear-and-append flow.
- 2026-07-14: restored `detailView.jade`'s empty child-collection rendering.
  A defined child group with zero visible rows now retains its tab and table
  heading without the Vue-only `暂无子项。` row, while a detail View with no
  child-group metadata renders no empty collection panel. Existing staged
  delete behavior and parent-save persistence remain unchanged.
- 2026-07-14: restored `querylistdata.js`'s zero-result candidate table. After
  the first candidate query, zero matching rows now retain the metadata heading
  and ten legacy filler rows with `共0条记录` instead of replacing the table
  with `暂无候选记录。`. Before the first query, the candidate area remains
  visually empty and keeps the existing `记录数未知,请查询` prompt.
- 2026-07-14: restored `view.jade`'s report-output structure when the report
  model exposes no columns. The Output tab now retains its candidate, output
  type, and selected-column controls instead of replacing them with
  `暂无报表字段。`; the output-type list also stays empty until a real metadata
  candidate exists. Normal metadata-driven output selection is unchanged.
- 2026-07-14: restored `viewWithChart.js`'s zero-series chart surface. The Chart
  tab now keeps the shared SVG axes mounted after a zero-row query instead of
  replacing them with `暂无图表数据。`. Horizontal labels render only from
  legacy chart-axis metadata and no longer invent numeric positions when the
  old ECharts `xAxis.data` would be empty.
- 2026-07-14: extended the zero-series chart contract to Sudoku's
  `includes/linechart.jade` partial. A linechart panel now mounts the shared
  compact SVG regardless of series count, matching the old controller's
  initialize-before-query lifecycle instead of falling through to the generic
  `暂无数据。` panel. Existing panel loading, refresh, and sizing are unchanged.
- 2026-07-14: restored `subitem.js`'s zero-field Sudoku Item matrix. The Item
  partial now mounts `LegacyItemPanel` regardless of returned SimpleData count,
  so the existing six-row table remains visible when no fields resolve instead
  of being replaced by the generic `暂无数据。` panel. View-first detail loading
  and the shared two-fields-per-row renderer remain unchanged.
- 2026-07-14: restored `mapview.js`'s initialize-before-query lifecycle and
  wheel interaction for Sudoku Map partials. The shared Leaflet panel now mounts
  regardless of marker count, uses the old Beijing center `39.94917,116.32` at
  zoom 18 when no valid points resolve, and enables wheel zoom instead of
  showing Vue-only empty/error copy. View-first marker projection, refresh, and
  the existing shared map renderer remain unchanged.
- 2026-07-14: restored `Group.jade` / `groupview.js` empty-content behavior.
  Group panels now retain an empty body when their loaded View has no child
  Items, and unknown child `ListViewType` tabs retain an empty tab panel instead
  of showing Vue-only `暂无数据。`. List children, the legacy `这是简单项` branch,
  tab selection, and View-first child loading remain unchanged.
- 2026-07-14: removed Vue-only detail initialization copy from the old read-item
  and detail surfaces. `/itemview:id` now proceeds directly from its View title
  to View-derived fields and collection tabs like `item.jade`, without
  `已加载视图定义。`; the unreachable no-object detail placeholder was removed
  with it. The read-item route still does not query or bind a business data DTO.
- 2026-07-14: restored the shared legacy table's empty-shell lifecycle.
  `ListDataTable` now stays mounted without View columns and removes Vue-only
  `暂无数据。` / `请先加载视图。` copy, matching the empty tables in `view.jade`,
  `viewWithChart.jade`, and the candidate dialog. It still passes no rows until
  View columns exist, so data DTO rows cannot render ahead of View metadata.
- 2026-07-14: restored `Sudoku.jade`'s unmatched child `ViewFile` behavior.
  Unknown partial names now leave their existing panel content area empty
  instead of inserting the final Vue-only `暂无数据。` fallback. The five explicit
  List/Group/Map/Item/linechart branches and their View-first loaders are
  unchanged.
- 2026-07-14: restored `detailview.js`'s candidate-table initialization order.
  Opening a select-existing child dialog now shows the loaded candidate View's
  headings before the first query, with no data/filler rows; after querying it
  still pads to ten rows like `querylistdata.js`. The shared table suppresses
  PrimeVue's automatic empty row, and selecting a candidate still closes the
  dialog and stages the View-projected child without an immediate save.
- 2026-07-14: restored `setextype.js`'s numeric text-input contract. Property
  types 1/2 and 3/4 now strip non-digits and cap input at four/eight characters;
  types 5/6 preserve digits and decimal points with the same limits. Vue no
  longer maps legacy numeric metadata to native `number` inputs, so `e`, signs,
  and browser steppers cannot bypass the old keyup behavior. Property types 0,
  7, and 10 retain the old unrestricted text fallback, and save values remain
  strings derived from View metadata rather than business DTO fields.
- 2026-07-14: restored `setextype.js`'s BusinessObject suggestion template.
  Lookup results now render the old `Text - Id` information hierarchy with an
  en-dash separator and retain the fixed `查找更多` footer below result and
  empty menus. Existing View-derived `inputquery` context, debounce, empty
  feedback, explicit clearing, and selected ID writeback remain unchanged.
- 2026-07-14: restored `detailview.js`'s readonly edit rendering. Entering edit
  mode now leaves readonly detail and child fields as plain text while only
  editable View fields become controls, matching the old `SetEdit` gate instead
  of inventing disabled textboxes. Empty readonly values retain a non-breaking
  text line, and save omission for readonly fields remains unchanged.
- 2026-07-14: restored `setextype.js`'s Boolean editor presentation. Property
  type 8 now renders only its checkbox in edit mode instead of adding Vue-only
  `是` / `否` copy beside the control. The shared View-driven boolean converter
  and legacy `true` / `false` save values remain unchanged.
- 2026-07-14: restored `detailView.jade`'s empty simple-value placeholder.
  Read-only and schema-only detail rows now render a non-breaking text value
  when `FmtValue` and `ObjId` are empty, matching the old paragraph branch
  without altering the View-derived value helper or save drafts.
- 2026-07-14: restored `view.jade`'s report-footer command availability.
  `确定` and `保存报表定义` now remain actionable with zero selected outputs,
  incomplete conditions, or an empty report name, matching the old unconditional
  footer commands instead of applying Vue-only `canRun` gates. Request-pending
  duplicate protection and the existing View-driven request builders remain.
- 2026-07-14: restored `view.jade` / `mkreport.js` report-output control
  availability. The output-method list and Add arrow now remain actionable when
  no candidate column is selected, matching the old unconditional controls and
  relying on the existing `addOutput` no-op guard. Request-pending protection,
  duplicate-output handling, and View-derived report metadata remain unchanged.
- 2026-07-15: restored `mkreport.js`'s exact output-type metadata behavior. The
  old candidate change handler appends only the selected column's real
  `QueryTypes`; when none exist, the output-method list stays empty and Add is a
  no-op. Vue no longer invents an `原值` option for that case, while retaining
  the old shortcut that automatically adds a candidate changed to exactly one
  real output type. Full frontend tests passed (19 files, 197 tests), the
  production build passed, and authorized isolated-browser checks against the
  clean `9259f616` image proved real explicit Add, empty-metadata no-op,
  single-type auto-add, and HTTP 200 login/logout.
- 2026-07-15: restored `mkreport.initquery`'s initial output-method activation.
  Loading report metadata now selects the first candidate while leaving the
  output-method list empty, matching the old template until the candidate
  `change` handler runs. A real change then exposes only that candidate's
  metadata: multiple types require explicit Add, one type auto-adds, and zero
  types remain an Add no-op. The activation flag stays local to the 188-line
  selector and adds no report DTO or shared business state. Full frontend tests
  passed (19 files, 198 tests), the production build and repository harness
  passed, and authorized browser acceptance against the exact clean `31e0174d`
  image proved all three paths without changing the seeded 8-order/4-item data.
- 2026-07-14: restored `view.jade` / `mkreport.js` report-condition Add-command
  availability. Both header and trailing Add buttons now remain actionable when
  the loaded View has zero report fields, matching the old controls and creating
  one empty condition row through the existing draft builder. Request-pending
  protection and View-derived field options remain unchanged.
- 2026-07-14: restored `menuinfo.js`'s non-navigable child-menu structure.
  Submenu entries with `ViewId=0` now render as plain list text instead of
  Vue-only disabled buttons, while entries with a View id retain the shared
  desktop/mobile navigation command. Menu labels, images, expansion state, and
  View-driven routing remain unchanged.
- 2026-07-14: restored `mkreport.js`'s two-stage dialog timing. The report setup
  dialog now waits for `getmkqview` View metadata before opening, and Confirm
  hides setup while `mkrpt` runs before reopening the same surface as the report
  result. Failed requests still restore the setup dialog with Vue's existing
  error feedback, while report columns and request payloads remain View-driven.
- 2026-07-14: restored `mkreport.js`'s selected-output stability. Adding the
  first report output selects the first list option, while later additions keep
  the user's current selected output instead of jumping to the appended item.
  Duplicate additions remain no-ops and now also preserve selection, so the
  subsequent move, delete, and sort commands target the same row as old Web.
- 2026-07-14: restored `initQueryView`'s candidate-table heading matrix. The
  select-existing dialog now shows only the loaded candidate View's field
  headings, without the Vue-only `操作` label. Queried rows retain their
  row-level Select command in the unlabeled trailing column, preserving the
  shared table component and the existing View-first candidate workflow.
- 2026-07-14: restored `swchartLine.js`'s configured series presentation in the
  shared SVG chart. Line metadata now renders as a smooth filled curve without
  visible point symbols, while bar/scatter values use the old visible label
  behavior.
  Top-level `viewWithChart` and Sudoku linechart panels reuse the same renderer;
  no chart dependency, business DTO branch, or duplicated panel was added.
- 2026-07-14: restored `swchartLine.js`'s metadata horizontal-axis name. The
  first result row's `EditType=11` field label now becomes shared chart
  `axisName` and renders at the end of the horizontal axis, while its per-row
  formatted values remain the category labels. Top-level and Sudoku charts use
  the same projection and renderer without a business-field fallback.
- 2026-07-14: restored `swchartLine.js`'s `boundaryGap: false` category-axis
  placement. Multi-category charts now put the first and last category centers
  directly on the horizontal-axis ends instead of adding a half-slot inset;
  a single category remains centered to avoid division by zero. Line, bar,
  scatter, value-label, and category-label positions reuse the same coordinate,
  and the end-axis name moves to its own line so the final category stays
  readable on desktop and mobile.
- 2026-07-14: restored `swchartLine.js getOption`'s `barMaxWidth: 15` as a
  rendered-pixel cap for top-level `viewWithChart` bars. The shared SVG now
  observes its actual width and converts the 15px limit into viewBox units, so
  bars remain 15px on desktop and mobile instead of scaling from 51.877px to
  11.822px. The compact Sudoku branch retains its previous shared-renderer cap
  because the old realtime `LineChartController` did not configure
  `barMaxWidth`; View-derived series and all other chart geometry are unchanged.
- 2026-07-14: restored both old chart entrypoints' `grid.right: '20%'` and
  right-middle legend placement. Top-level and compact charts now reserve the
  right fifth of the shared SVG plot and position the metadata-series legend in
  that space instead of below the chart. Desktop retains ECharts 3.1.7's
  horizontal default; narrow screens stack the same legend entries vertically
  within the reserved column to avoid covering plotted values. Existing series,
  responsive 15px bars, and View-derived labels remain unchanged.
- 2026-07-14: restored ECharts 3.1.7's default selectable-legend interaction in
  the shared chart renderer. Legend buttons now toggle selection by metadata
  series name, use the old `#ccc` inactive treatment, and expose pressed state
  to assistive technology. Hidden series leave drawing, y-axis domain
  calculation, and visible bar grouping together, including the old all-hidden
  `0..1` fallback; top-level and compact charts reuse the same implementation
  without a business DTO branch or chart dependency.
- 2026-07-14: replaced the temporary point-native titles with
  `swchartLine.js`'s configured `tooltip.trigger: 'axis'` interaction. Moving
  within the plot now selects the nearest View-derived category, draws the old
  `#555` category pointer, and shows the category plus every selected metadata
  series using ECharts 3.1.7's default dark tooltip treatment. Legend-hidden
  series leave the tooltip, all-hidden charts show no pointer/content, and the
  same bounded overlay serves top-level, mobile, and compact charts without
  restoring transparent line symbols or adding a chart dependency.
- 2026-07-14: restored `LineChartController`'s realtime data path for Sudoku
  `linechart` panels. Vue now loads the child View first, queries one
  `querydatadetail` sample, initializes the View-derived series as a 100-point
  zero window, and appends/shifts one sample on each detail refresh. The timer
  reads detail `AutoFreshTime`; list and map panels retain their five-row
  `querydata` path, and top-level `viewWithChart` remains list-backed. Docker
  `/view103` proved 100 bars and 200 two-series labels, then retained those
  counts with two trailing `1001 / 0.25 / 62500` samples after manual refresh;
  the same chart stayed bounded at 390px and `/view100` retained eight list
  categories.
- 2026-07-14: restored Sudoku partial footer interaction boundaries from
  `List.jade`, `linechart.jade`, and `Map.jade`. List and Group List retain
  their active refresh commands and formatted `querytime`; linechart and Map
  now show the old blank `更新时间` plus an inert anchor with no click handler.
  Linechart continues its detail-driven timer, while Map matches its old
  one-shot controller and no longer schedules list `AutoFreshTime`. Docker
  `/view103` exposed exactly two real refresh buttons, passive Price Chart and
  Customer Map text had no link/button role, clicking Price Chart left its
  100-point tail unchanged, and the passive footer stayed inside a 328px panel
  with no document overflow at 390px.
- 2026-07-14: restored the realtime `LineChartController` title configured from
  `chartname`. Sudoku passes the child View-derived panel name into the shared
  chart, which renders the old 18px bold dark title only in compact mode and
  reserves plot space below it. Docker `/view103` showed exactly one inner
  `Price Chart` title above the first grid line while retaining the 200px
  partial height; at 390px the title remained bounded with no document
  overflow. `/view100` retained the old top-level chart branch without an
  inner title.
- 2026-07-14: restored both `swchartLine.js` chart branches' value-axis
  `boundaryGap: [0, '50%']` range calculation. A small tested geometry helper
  now filters finite visible values, extends the upper bound by half the raw
  span, then applies ECharts 3.1.7's cross-zero rule; empty and all-hidden
  charts retain `0..1`. Docker `/view103` and `/view100` both moved the seeded
  Price top boundary from `62,500` to `93,750`; hiding Price produced the
  Amount-only `0..0.375` range and all-hidden mode remained `0..1` without a
  pointer or tooltip. The compact chart and top tick stayed bounded at 390px.
  ECharts interval nicifying remains a separate axis-parity step.
- 2026-07-14: completed the next ECharts 3.1.7 value-axis step by restoring
  `niceTicks` / `niceExtent` interval selection. Shared chart geometry now uses
  the old 1/2/3/5/10 decimal thresholds, rounds the boundary-expanded extent
  outward, and emits the resulting variable tick count. Seeded Price renders
  six `0..100,000` ticks at `20,000` intervals in both `/view103` and
  `/view100`; Amount-only renders `0..0.4` at `0.1`, and all-hidden renders six
  `0..1` ticks at `0.2`. All six mobile labels remained inside the 328px pane
  with no document overflow.
- 2026-07-14: restored realtime `LineChartController` `stack: 'a'` coordinate
  semantics for compact charts. Tested geometry now follows ECharts 3.1.7's
  same-sign cumulative values across line/bar/scatter series, separate
  positive/negative bar-layout bases, and immediate same-sign line area bases;
  consecutive line bases reuse the shared smooth path. Legend filtering removes
  hidden series before stack recomputation, while non-compact unique-name
  series remain independent. Docker `/view103` retained 100 bars, 200 raw value
  labels, and six nice ticks; the Price line's final y coordinate moved from
  `66.49981` with Amount visible to `66.5` when Amount was hidden, proving the
  compact stack was removed and recalculated. The restored stack remained
  bounded in the 390px viewport.
- 2026-07-14: restored ECharts 3.1.7's default zero-value bar geometry. The
  shared chart renderer no longer forces every bar to at least one rendered
  pixel, matching the old `barMinHeight: 0` default in both `swchartLine.js`
  branches. Docker `/view103` rendered 99 zero-height bars plus the natural
  `0.00019` height of its one nonzero Amount sample; `/view100` rendered six
  zero-height bars and retained the natural `0.00354` / `0.00059` heights for
  `1.5` and `0.25`. The same compact counts held at 390px with no document
  overflow, while the stacked Price endpoint remained y=`66.49981`.
- 2026-07-14: restored `swchartLine.js` stack-id semantics in the shared chart
  model. Top-level View projection now carries each metadata `PrpShowName` as
  its ECharts stack id, while realtime rolling series retain the common `a`
  stack. Geometry accumulates only matching stack chains, links line-area bases
  to the previous matching series, and gives same-stack bars one shared slot;
  duplicate legend names now render once and still toggle the whole name. Unit
  coverage proves interleaved same-name bars/lines stack without merging an
  unrelated name. The seeded runtime has only unique `Amount` / `Price` names,
  so Docker verification covered regressions: `/view103` retained 100 bars,
  two legends, and Price y=`66.49981`; `/view100` retained eight bars and two
  legends. The compact pane remained within `31..359` at 390px with no overflow.
- 2026-07-14: restored `querylistdata.js`'s non-target row-operation
  presentation. `RequireSelect=true` operations whose metadata `ViewId` is
  zero now remain visible as inert Bootstrap-link-colored text, with no button
  role or selection handler, instead of Vue-only disabled buttons. Operations
  with a target View retain the existing shared navigation button. Docker
  `/view100` showed all 16 seeded `删除` / `保存` names across eight rows with
  zero table-action buttons and computed `#337ab7` color; at 390px the table
  pane stayed 328px wide and document scroll width remained exactly 390px.
- 2026-07-14: restored `mapview.js`'s asynchronous marker lifecycle and popup
  boundary. The shared Leaflet map now keeps one marker layer, clears and
  redraws it when child View query data arrives, and retains the old Beijing
  zoom-18 fallback when no valid coordinates exist. A marker receives a popup
  only when View-derived title or information metadata exists; coordinate-only
  rows remain visible without Vue-only fallback text. Before the fix,
  authenticated `/view103` announced two locations but rendered zero Leaflet
  marker paths. The rebuilt Docker page rendered two actual paths, and both
  seeded popups showed their View-derived customer name and id. At 390px the
  same two markers remained visible in a 328px map with no document overflow.
- 2026-07-14: restored `mapview.js`'s info-window geometry and detail grouping.
  Leaflet popups now use the old 240px content width and fixed 100px content
  height, with View-derived information items grouped two per paragraph row
  instead of one Vue-only row per field. Popup content remains DOM-built and
  escaped, and the shared component still supplies title fallback and popup
  eligibility. Docker `/view103` measured a 241x100 content box after browser
  rounding and a 287px outer popup; at 390px that popup stayed within
  coordinates `66..353` with no document overflow.
- 2026-07-14: restored each Sudoku include's template-specific header instead
  of rendering every ViewFile through one Prime panel title. List, Map, and
  linechart now use the old single active Bootstrap tab presentation; Item
  uses the gray `panel-heading` with its inert blue `详细` text; Group starts
  directly with its View-derived child tabs and restores the List child's
  inert footer `详细` text. The shared loop now adds only a ViewFile-kind class
  and two conditional spans; data loading and tab state are unchanged. Docker
  `/view103` exposed four visible outer headers rather than the Vue-only five,
  retained both Group tabs, and showed two inert Detail texts. At 390px all
  five 330px panel shells stayed inside `30..360` with no document overflow.
- 2026-07-14: restored `subitem.js`'s Item footer by adding Item to the shared
  passive-footer contract already used by linechart and Map. The six-row
  View-derived matrix is now followed by blank `更新时间` text and an inert
  `刷新` anchor with no href, click handler, or button role; only List and
  Group List remain manually refreshable. Docker `/view103` rendered one Item
  footer with zero buttons, one passive anchor, and exactly two executable
  refresh buttons across the page. At 390px the footer ended at x=359 with no
  document overflow.
- 2026-07-14: restored `groupview.js`'s simple-child text presentation. The
  `ListViewType=1` tab still renders the View-contract string `这是简单项`, but it
  no longer routes that text through the Vue-wide 128px centered gray empty
  state. A focused class now leaves it as a top-left 14px/20px `#333` line,
  matching the old controller's direct append into the child panel. Docker
  `/view103` retained Group tab switching and rendered a 20px-high line with
  zero Group empty-state elements. At 390px the text remained within
  `48..342` and document overflow stayed false.
- 2026-07-14: restored ECharts 3.1.7's default square bar corners. The shared
  SVG renderer no longer applies a Vue-only `rx=2` radius to top-level or
  compact bars; old `swchartLine.js` supplies no `barBorderRadius`, so the
  ECharts `BarView` fallback is zero. Docker `/view100` rendered eight 15px
  bars with no `rx`/`ry`, while `/view103` retained its 100 realtime bar nodes
  with the same square-corner contract. Desktop and 390px checks had no
  document overflow or browser warnings. The adjacent endpoint-clipping audit
  also found that ECharts 3.1.7 does not clip the bar group to the grid, so the
  existing half-width first/last extension remains correct and unchanged.
- 2026-07-14: removed the Vue-only card frame around the shared chart surface.
  Old `viewWithChart.jade` supplies a plain chart tab pane, while Sudoku's
  `.sw-partialchart` sets only width and 200px height; neither adds another
  border, radius, or content inset around ECharts. The shared Vue pane now has
  zero border, radius, and padding for both entrypoints. Docker `/view100`
  measured the SVG and pane at the same `40,235` desktop origin and `30,403.84`
  mobile origin; `/view103` likewise retained an edge-aligned 328x200 compact
  chart. Both pages remained free of document overflow and browser warnings.
- 2026-07-14: restored `viewWithChart.js`'s one-time chart-height lock. After
  the first View-derived data result renders, the Vue parent now records its
  existing data-pane height and passes only that presentation value to the
  shared chart. Search, paging, and refresh preserve the lock; a new View page
  entry clears it. Docker `/view100` moved from a 566.66px aspect-ratio chart
  to the data pane's 414px desktop height and used 470px for the 470.25px
  mobile pane. The responsive viewBox now samples category labels by rendered
  plot width, retaining eight non-overlapping desktop labels and only the two
  non-overlapping endpoints at 390px. `/view103` stayed on its independent
  328x200 compact contract, and browser warnings/overflow remained absent.
- 2026-07-14: restored ECharts 3.1.7's default axis presentation. The old Web
  package declares ECharts `^3.1.7`, whose `axisDefault.js` uses `#333` 1px
  axis lines, `#333` 12px labels, and `#ccc` 1px split lines. The shared Vue
  chart now uses those values instead of its lighter 11px slate treatment for
  both top-level and Sudoku entrypoints. Vitest now processes `style.css` so
  existing and new CSS contracts inspect actual content rather than its
  default empty replacement. Docker `/view100` computed the legacy colors,
  widths, and font size at desktop and 390x844 while retaining its 414px and
  470px chart-height locks; `/view103` retained its 328x200 compact chart.
  Labels did not overlap, document overflow stayed false, and all three page
  checks had no browser warnings or errors.
- 2026-07-14: restored ECharts 3.1.7's legend-item geometry and typography.
  The shared legend now uses the old 25x14 item slot, 5px icon/text gap, 10px
  item gap, normal 12px `#333` text, and 5px right inset. Bar series use the
  old 3.5px-radius round rectangle; line series use the old 1px line with its
  centered 11.2px fallback circle; scatter retains the centered 14px circle.
  Hidden items keep those dimensions and switch glyph/text to `#ccc`. The
  existing ResizeObserver also measures dynamic View-derived legend names, so
  the plot keeps the old 20% right grid unless the real legend is wider.
  Docker `/view100` retained 92.28px desktop clearance; at 390x844 its 73.98px
  legend ended exactly where the plot began, with no overlap or overflow.
  `/view103` retained the same zero-overlap rule in its 328x200 compact pane,
  and all checked pages had no browser warnings or errors.
- 2026-07-14: restored ECharts 3.1.7's legend hover-link state in the shared
  chart. Old `LegendView.js` dispatches series highlight/downplay on legend
  mouseover/mouseout; Vue now tracks the View-derived series name on
  enter/leave and applies one shared emphasis class. Bar and scatter colors use
  zrender's default 1.1 RGB lift; scatter also follows the old 400ms
  `max(size * 1.1, size + 3)` enlargement for its current 12px symbol. The
  existing line path uses old `symbol: 'none'`, so whole-series legend hover
  has no point symbol to emphasize. Docker click-selection regression passed,
  `/view100` retained its 414px desktop and 470px mobile chart contracts, and
  `/view103` remained 328x200 with no overflow or browser warnings. The browser
  control surface could click at pointer coordinates but did not establish CSS
  `:hover` or DOM `mouseenter`; the event-to-class runtime transition therefore
  remains covered by production-source contracts rather than direct pointer
  observation in this slice.
- 2026-07-14: restored ECharts 3.1.7's default scatter-symbol presentation.
  Old `swchartLine.js` configures `symbol: 'circle'` without a size or style
  override, so `ScatterSeries.js` supplies a 10px symbol and 0.8 opacity while
  `Symbol.js` expands emphasis to 13px. The shared SVG now converts the 5px
  radius through its existing rendered-width ratio, keeping the actual marker
  diameter at 10px despite responsive viewBox scaling, and uses a 1.3 emphasis
  scale. A temporary, restored View-first metadata probe changed seeded Price
  from `EditType=12` to `14`: Docker `/view100` rendered eight 10px/0.8 scatter
  marks at both 1280x720 and 390x844, retained click hide/show, 414px/470px
  chart heights, two sampled mobile labels, and no overflow or browser logs.
  Both metadata tables were restored to `EditType=12`; after backend restart,
  the default page again rendered one Price line and zero scatter marks.
- 2026-07-14: restored ECharts 3.1.7's direct bar/scatter hover targets. Old
  `BarView.js` binds hover style to each bar, `Symbol.js` binds color lift and
  animation to each scatter symbol, and old line paths are silent. Vue's
  transparent axis-tooltip rectangle had been painted above all series, so a
  seeded nonzero bar's center resolved to `.chart-axis-hit` instead of the bar.
  The shared SVG now receives plot movement while that transparent rectangle
  sits below the series; direct bar/scatter CSS reuses the existing 1.1 lift
  and 10-to-13px scatter emphasis. Docker `/view100` then resolved the same bar
  center to `.chart-bar` and all eight temporary View-derived scatter centers
  to `.chart-scatter`. Real pointer movement over both item types retained the
  axis pointer and tooltip, including `1002 / Amount: 1.5 / Price: 3,450` at
  desktop and 390x844. Mobile retained its 470px chart, two sampled labels, no
  overflow, and empty browser logs. Both metadata tables were restored to
  Price `EditType=12`; the default page again rendered eight bars, one line,
  and zero scatter marks.
- 2026-07-14: restored ECharts 3.1.7's line-area opacity. Both old
  `swchartLine.js` branches enable `areaStyle.normal` without an opacity
  override, and `LineView.js` assigns the resulting polygon `opacity = 0.7`.
  The shared Vue renderer now uses 0.7 instead of its invented 0.2 while
  retaining the View-derived series color. Docker `/view100` changed the one
  Price area from computed `0.2` to `0.7` with fill `#2f4554`; desktop retained
  its 414px chart and eight labels, while 390x844 retained 470px, two labels,
  legend hide/show, no overflow, and empty browser logs. The container and
  browser both loaded the rebuilt `index-Ct6fVG-u.css` asset; a cache-busting
  query was used after the existing browser tab initially reused the prior
  hashed stylesheet.
- 2026-07-14: restored ECharts 3.1.7's symbol-less line-label behavior. Both
  old chart branches configure line series with `symbol: 'none'`. ECharts
  propagates that visual symbol, `SymbolDraw.js` skips creating the Symbol that
  owns label text, and `LineView.js` has no separate line-label renderer.
  Vue now keeps value labels only for bar/scatter series while preserving line
  values in the axis tooltip. Docker `/view100` changed from 16 value labels
  (eight bar plus eight invented line) to eight bar labels and zero line labels;
  pointer movement still showed `Price: 3,450`. At 390x844 it retained the same
  counts, a 470px chart, two category labels, and no overflow. Sudoku `/view103`
  changed from 200 to 100 labels by retaining all 100 realtime bar labels and
  removing all 100 line labels inside its 328x200 pane. This supersedes the
  earlier provisional 200-label runtime count as a parity target; browser logs
  remained empty in all checks.
- 2026-07-14: restored ECharts 3.1.7's default bar/scatter value-label
  placement and style. Both old `swchartLine.js` branches enable labels without
  specifying position or text style; ECharts therefore falls back to `inside`,
  12px sans-serif text, and white fill. The shared Vue renderer now centers
  bar labels in their actual stacked rectangle and scatter labels on their
  symbol instead of placing 10px dark text seven pixels above each value.
  Scatter labels also inherit the symbol's 0.8 opacity. Docker `/view100`
  measured a zero-pixel label/bar-center delta and rendered 12px white text at
  desktop and 390x844, with no pane overflow. A temporary View-first metadata
  probe changed seeded Price from `EditType=12` to `14`; all eight scatter
  labels matched their circle centers on both axes and used opacity 0.8. Both
  metadata tables were restored to `12`, and the restarted default page again
  rendered eight bars, one line, zero scatter marks, and eight value labels.
  Sudoku `/view103` retained 100 bar labels in its contained 328x200 chart.
- 2026-07-14: restored ECharts 3.1.7's chart value-text precision. Both old
  `swchartLine.js` paths push View `FmtValue` strings directly into series data.
  ECharts bar labels use that raw option text, scatter labels use its numeric
  value, and axis tooltips apply `addCommas` without truncating decimals. The
  shared chart model now retains optional View-formatted values beside numeric
  geometry and one 19-line formatting helper serves labels, ticks, and
  tooltips. Docker `/view100` changed Amount labels from `1.5` to
  `1.5000000000` and the category-1002 tooltip from `3,450` to
  `3,450.0000000000`; desktop and 390x844 tooltips stayed contained and the
  settled 330px pane had no overflow. `/view103` retained 100 bar labels in its
  328x200 pane and showed `0.2500000000 / 62,500.0000000000` for its latest
  sample. A temporary Price `EditType=14` probe confirmed scatter labels remain
  numeric `3450 / 62500` while their tooltip preserves View precision. Both
  metadata tables were restored to `12`; the default page returned to eight
  bars, one line, and zero scatter marks. This supersedes earlier short tooltip
  examples as parity targets.
- 2026-07-14: restored ECharts 3.1.7's coordinate-system boundary for axis
  tooltips. Old `TooltipView._showAxisTooltip` calls `containPoint` and hides
  the pointer/content when no grid contains the mouse. Vue previously handled
  movement across the whole SVG by horizontal position alone, so the tooltip
  remained visible over title whitespace and category-label space. The shared
  renderer now converts both pointer coordinates into its viewBox and clears
  the tooltip outside the actual plot rectangle. Docker `/view100` showed one
  tooltip inside and none above/below the plot at desktop and 390x844;
  `/view103` passed the same checks in its 328x200 compact chart. The deployed
  frontend image was `sha256:546232e72b4aeb567e0372e36b21d91ea5b87b061826b6216eaae34c665481b6`.
- 2026-07-14: restored ECharts 3.1.7's tooltip refresh after chart resize. Old
  `ECharts.resize` updates the chart, then `TooltipView.render` replays its last
  canvas-local pointer through `_manuallyShowTip`, allowing the new coordinate
  system to reposition or hide it. Vue previously left the old absolute
  tooltip position active: shrinking desktop `/view100` to 390x844 produced a
  330px pane with 852px scroll width and an 882px full-page screenshot. The
  shared renderer now retains only the last in-plot chart-local point and
  reuses its existing bounds/nearest-category function after chart or legend
  resize. The same path clears points that moved outside the new plot and
  recalculates points that remain inside. Docker `/view100` returned to a 390px
  full-page width after the failing desktop-to-mobile path, while `/view103`
  also cleared its out-of-range compact tooltip. Browser logs were empty and
  the deployed frontend image was
  `sha256:58ce23aa75747250dd7a998f513f2f1696b408a7658728ef8411ff552f9520b9`.
- 2026-07-14: restored ECharts 3.1.7's default 100ms tooltip hide delay. Old
  `TooltipModel` sets `hideDelay: 100`; `TooltipContent.hideLater` schedules
  one hide while `show` cancels it on re-entry. Vue previously removed the
  shared axis tooltip in the same out-of-plot event: Docker `/view100` changed
  from `inside=1, immediate=0, 10ms=0, 50ms=0` to
  `inside=1, immediate=1, 50ms=1, 120ms=0`. Re-entering at 50ms retained one
  tooltip and recalculated its category, while a later full leave still hid it.
  The shared scheduler does not reset on repeated outside movement, is cleared
  on explicit hide/unmount, and leaves legend/no-data cleanup immediate. Mobile
  `/view103` passed the same `1/1/1/0` timing contract in its compact chart,
  with empty browser logs. The deployed frontend image was
  `sha256:57e7266ab950e6205ed61401f78def30e5e44e3ba77485c986628f7ea53ec5cb`.
- 2026-07-14: restored ECharts 3.1.7's tooltip box and movement defaults. An
  exact 720x300 old-library probe measured `display:block`, 21px line height,
  73px three-row height, nowrap text, `z-index:9999999`, a 20px pointer gap,
  and 0.4s `left/top` transitions using
  `cubic-bezier(0.23, 1, 0.32, 1)`. Vue's shared tooltip used a 2px-gap grid,
  20.3px inherited line height, 74.89px height, 8px offset, `z-index:2`, and no
  movement transition. The shared CSS now matches the old metrics while
  removing the redundant child nowrap rule. Docker `/view100` measured the
  exact 73px box, 20px transform, and dual 0.4s transitions; moving left from
  260px to 460px sampled 292.15px immediately and 435.22px at 100ms before
  settling. Desktop, 390px `/view100`, and 390px compact `/view103` screenshots
  retained visible tooltips without document overflow or browser logs. The
  deployed frontend image was
  `sha256:d0773decc43337eb65fcd489fa936c0de787880fc88933b3dfec5bae7cb33d4d`.
- 2026-07-14: restored `view.jade`'s complete report-result command row. The
  old template places eventless `导出当前页` / `导出全部` buttons after the
  working paging pair, and `mkreport.js` defines no export handler. Vue now
  renders the same two no-op commands in the existing extra-small group without
  inventing a request, export DTO, download path, or business state. This
  supersedes the earlier intentional omission while preserving the old
  interaction boundary. The result heading now wraps that intact command group
  below its page summary when needed, preventing overlap at 390px. Authenticated
  Docker `/view101` rendered the four commands in old order; at 390x844 the
  summary ended at y=268.73 and the 225px command group began at y=276.73 with
  document width exactly 390px. Clicking both placeholders left the dialog and
  URL unchanged. The deployed frontend image was
  `sha256:b6016cd9be7c542565a42413dfe13716f388f3245a1f363d7f50e572a54d6dec`.
- 2026-07-14: restored `showerror.js` no-target system-message interaction.
  Old `default.jade` always renders `查看详细` as an active anchor and
  `showevtmsg` assigns `href='#'` when `ResultView` is empty or zero; clicking
  that placeholder does not close the modal. Vue no longer disables the
  command. Its existing handler now returns before either emit when the shared
  View-first adapter resolves no target, while positive targets retain the
  existing close-and-navigate path. A seeded Docker message with null View and
  object ids was marked pushed by the real 15-second polling route. Desktop
  and 390x844 rendered one enabled command; clicking it left one dialog and
  `http://localhost:8081/` unchanged. The 390px dialog stayed within the
  viewport with no document overflow or browser errors. The deployed frontend
  image was
  `sha256:4765222fedd4d9f671e93aa4ba4004ed4f906ba1038254c34539a3d983050f04`.
- 2026-07-14: restored `message.js` generated-time parsing and formatting. The
  old polling controller extracts the epoch from Pascal `GernerationTime`,
  creates a local `Date`, and renders `yyyy-MM-dd hh:mm:ss`. Vue previously
  extracted the first digits from every alias and formatted through UTC, so a
  camel LocalDateTime such as `2099-01-01T19:04:05` became 1970. The shared
  adapter now accepts current camel LocalDateTime and legacy `/Date(ms)/`,
  preserves invalid server text, and formats through local date fields. Its
  parser is also reused by list refresh-time formatting instead of adding a
  message-only conversion path. A fresh authorized `admin/admin` API login
  proved the real response exposes both aliases for one instant. Authenticated
  Docker polling rendered `2099-01-01 19:04:05` at 390x844 with document width
  exactly 390px and no browser errors. The deployed frontend image was
  `sha256:e470df2f8e0c9821a53f076c8baccd29458a136f68b6e8f756fac0a19ff9f7ae`.
- 2026-07-14: restored the old shell's user source and first-message timing.
  `default.jade` renders `data.User` from the initial main response, while
  `message.js` only registers its `getmsg` callback with the 15-second timer;
  neither performs an immediate message request nor repeatedly refreshes user
  info. Vue previously discarded `getmain.User`, immediately consumed one
  message after login, and paired every message poll with an invented
  `getuserinfo` request. The shell now renders both user aliases directly from
  `getmain`, keeps no second user-response state, and runs only `getmsg` when
  the interval expires. A fresh authorized Docker login displayed `Admin`
  immediately, showed no message dialog at about 1.5 or 8.5 seconds, then
  opened the seeded dialog after the first 15-second period and moved its DB
  state from 0 to 1. The 1280px page had no document overflow or browser errors.
  The deployed frontend image was
  `sha256:3d78d766dddb77ff8af1c2948504965b36619f66b289d34e5f2ddc85966d1d48`.
- 2026-07-14: restored the two old no-default-View home routes instead of
  conflating their templates. Old login replaces the URL with `/main`;
  `routes.main` renders `Sudoku.jade`'s long configuration guidance when
  `App.DefaultViewId` is empty, while authenticated `/` renders `main.jade`'s
  `默认首页 还没有配置`. Vue previously stayed on `/` after login and displayed
  the Sudoku copy for every unconfigured home. Successful login now replaces
  the path with `/main`, and the existing metadata-only default-View branch
  selects the checked-in text for the current old route. No business data DTO,
  router dependency, or duplicate View decision was added. The deployed bundle
  contains both exact template strings in one app chunk and the frontend image
  was `sha256:d83c8470e885da5a2cd8f0aa7cba4b622e9e36a9aa3be12c12d746899c672345`.
  An authenticated Docker replay temporarily changed `SW_APP_VIEW` from 100 to
  0: login landed on `/main` with only the long guidance, Home changed the URL
  to `/` with only `默认首页 还没有配置`, and both routes stayed at exactly
  390px document width on a 390x844 viewport. Browser errors were empty, the
  test session logged out, and `SW_APP_VIEW=100` was restored afterward.
- 2026-07-14: restored `login.js` Reset state semantics. Old `showerror()`
  registers CAPTCHA refresh for modal dismissal but never clears username,
  password, or the hidden/single database selection. Vue's Reset instead
  erased every field before requesting a new code. The button now emits the
  same existing refresh event as the explicit Refresh command; the established
  CAPTCHA-key watcher clears only the code input. The obsolete blank error
  modal remains intentionally omitted as presentation cleanup. `LoginPanel.vue`
  shrank by eight lines and no second reset path or state adapter was added.
  This supersedes the earlier migration note that described full-field Reset
  clearing as parity. The deployed frontend image was
  `sha256:47584bab5223758aa3d384c119fb79ea6d41034948d15432c8f8fa38e82f3d2c`.
  Docker browser replay filled all three visible inputs, then proved Reset kept
  username/password, cleared only the CAPTCHA input, and changed the CAPTCHA
  image at desktop and 390x844. The mobile viewport and document widths were
  both 390px and browser errors were empty.
- 2026-07-14: restored the old login-failure dialog lifecycle. `login.js` sends
  unsuccessful login responses to `showerror.showmsg` and registers CAPTCHA
  refresh for `hidden.bs.modal`; `layout.jade` renders `发生错误`, code/info
  paragraphs, and a single footer `关闭`. Vue previously displayed an inline
  error and replaced the CAPTCHA before the operator could read it. The shared
  response adapter now exposes only display code/message primitives, the login
  panel renders the old non-header-closable dialog, and dismissal clears the
  display state before requesting the next code. Username/password/database
  state stays component-owned and unchanged. Legacy code/message labels are
  normalized instead of reproducing the old caller's reversed arguments. The
  deployed frontend image was
  `sha256:3d02fc2c7c0797ca7f5029e86f429f8843b318bfa1c75d65118a6e60bb6ff9d4`.
  Docker browser replay submitted an invalid code at desktop and 390x844:
  both runs displayed `10006` / `Check code error.`, retained username,
  password, entered code, and CAPTCHA image until dismissal, then preserved
  credentials while clearing the code and replacing the CAPTCHA. The mobile
  dialog stayed inside a 390px document with no browser errors.
- 2026-07-14: restored `login.js`'s silent HTTP-error branch. The old
  `$http.post(...).error` callback is empty, so a transport/server failure
  leaves the login fields and CAPTCHA in place without opening the business
  error dialog. Vue's shared request wrapper previously exposed that exception
  through the same `发生错误` surface used for a successful HTTP response with
  `IsLogin=false`. `loginV2` now clears only the wrapper's transient display
  error when no response exists; response-backed legacy error code/message
  handling and dialog-dismissal refresh remain unchanged. No component,
  request path, or second error abstraction was added. The deployed frontend
  image was
  `sha256:752f0d84bee8ab6c45b48912c533a809c3333574adb82341f1b93c4661310c55`.
  Docker browser replay stopped the backend after loading the login page and
  submitted `admin/admin` with a test code. The request settled without a
  dialog; all three inputs and the CAPTCHA image remained unchanged, the Login
  action returned to enabled, and browser errors were empty. The backend was
  restored afterward and `/test` passed.
- 2026-07-14: restored `index.jade`'s single-database binding boundary. The old
  template emits one hidden `dbid` only when `data.Dbs.length == 1`; it does not
  render a multi-database picker. Vue previously invented a visible Select and
  retained a hard-coded `car_wash` fallback in the shell. The login View now
  derives its hidden submission value through the existing initapp adapter,
  which returns an id only for exactly one database, and the shell starts with
  no database default. This removes the picker, its local option/name helpers,
  and 26 lines from `LoginPanel.vue` without adding a component or business DTO
  state. Seeded Docker still supplies the single `car_wash` database through
  initapp rather than frontend configuration. The deployed frontend image was
  `sha256:f42ba03afdc7e2aec2ed5513e9197d4a31ba5622d1557f8aa2f52be03333e045`.
  A versioned Docker browser entry loaded the current bundle with zero database
  controls while initapp exposed exactly one `car_wash` database. Authorized
  `admin/admin` login within the CAPTCHA lifetime reached `/main`; the backend
  success log recorded `DbId=car_wash`, and the View-first shell rendered Order
  List. Logout returned to the same three-input/no-picker page at 1280px with
  no document overflow or browser errors.
- 2026-07-14: restored `index.jade`'s server-owned empty-login validation. The
  old username, password, and CAPTCHA inputs have no `required` attributes;
  `login.js` submits their current values and lets the login response drive the
  shared error dialog. Vue's native form constraints previously stopped an
  empty submission before `loginV2`, creating a separate browser validation
  path absent from FoolFrame. The three constraints are removed, so the
  existing submit, response adapter, dialog, and dismissal/CAPTCHA refresh
  lifecycle handle empty values without a second local error state. The
  deployed frontend image was
  `sha256:189aefedf24967381da206e677c90c2077d6b09f18a39a209fe8c4de1dcaa1ae`.
  A versioned Docker browser entry confirmed all three controls had no native
  required flag. Submitting them empty reached `loginv2`, whose backend log
  recorded empty user/password/code with `DbId=car_wash`, and displayed the
  response-backed `10006` / `Check code error.` dialog. Inputs and CAPTCHA image
  stayed unchanged until Close; dismissal kept the inputs empty and replaced
  the CAPTCHA. The complete run ended with no browser errors.
- 2026-07-14: restored `login.js`'s raw login-input submission. The old
  controller forwards username, password, and CAPTCHA exactly as read from the
  controls, and `index.jade` places no maximum length on the CAPTCHA input. Vue
  previously trimmed username/CAPTCHA and capped the latter at eight
  characters, silently changing the request before the server-owned validation
  path. `LoginPanel` now emits all three original strings and removes the
  maxlength constraint while retaining the same component-owned state,
  submit event, and response dialog lifecycle. The deployed frontend image was
  `sha256:5fbcfe36761833a209c9fde34e5ad4426ddf7252ac181df87c4f4dedbde11fd4`.
  A versioned Docker browser replay accepted a 13-character CAPTCHA input and
  submitted credentials with surrounding spaces. The backend log recorded
  `UserId=" admin "`, `PassWord=" secret "`, and
  `CheckCode="0123456789ABC"` exactly. The response-backed `10006` dialog kept
  all values and CAPTCHA image unchanged until Close; dismissal preserved both
  credential strings, cleared the code, replaced the image, and ended with no
  browser errors.
- 2026-07-14: restored `login.js`'s always-available login controls. The old
  page has no request-pending state, so Login keeps its fixed label and Login,
  Refresh, and Reset remain available while an HTTP request is in flight. Vue
  no longer passes global workflow pending state into `LoginPanel`, renders a
  spinner/`登录中...`, or locks those controls. The submit button retains only
  the pre-render compatibility guard for a missing CAPTCHA key because the old
  server-rendered page is not shown before that key exists. The shared request
  runner already allows concurrent requests, so no second request path or
  component state was added. Frontend tests/build, Compose replacement,
  runtime doctor, and repository harness pass; the deployed frontend image is
  `sha256:e07db3e06f2df0d420f5707b55daf3a3e7379a5313edf103a8e610ecc3fe9d8c`.
  Docker browser acceptance paused the backend after filling `admin/admin` and
  `WAIT`, then submitted Login. At 800ms the request remained in flight while
  Login, Refresh, and Reset were all enabled, Login retained its fixed label
  and no busy state, all fields remained intact, and no dialog opened. The
  1280x720 artifact is
  `artifacts/runs/20260714-legacy-login-request-controls/in-flight-controls.jpg`.
- 2026-07-14: restored `login.js`'s silent CAPTCHA-refresh HTTP-error branch.
  The old refresh request defines only a success callback, so transport/server
  failure leaves the current image and login fields in place without opening
  the shared error modal. Vue's `loadCheckCode` now clears only `runAction`'s
  transient transport message when no response exists, mirroring the existing
  login-request boundary. Successful refresh still replaces the View's key and
  image, while response-backed login errors remain unchanged. No component,
  request route, or error abstraction was added. Frontend tests/build, Compose
  replacement, runtime doctor, and repository harness pass; the deployed image
  is `sha256:e6808b1d432cb229b1deab15ed416e1316d8a0329583590ea710a42fb2ddc537`.
  A separate Docker browser replay stopped the backend before Refresh and
  waited for Nginx to record the settled `502` response. The page retained the
  exact CAPTCHA image and `admin/admin/KEEP4` fields, rendered no dialog, and
  kept all three controls enabled. The backend was restored, `/test` passed,
  and the 1280x720 artifact is
  `artifacts/runs/20260714-legacy-login-request-controls/refresh-failure.jpg`.
- 2026-07-14: restored `index.jade`'s external vendor-link intent. The old
  template treats `AppUrl` as a bare host and prefixes `http://`, while Vue
  previously bound the value directly and therefore resolved a bare host as a
  path under the current application. `LoginPanel` now adds the old HTTP scheme
  only when the View metadata has no scheme, while retaining already absolute
  `http://` or `https://` values supported by the current backend contract. The
  same initapp aliases and single footer link remain; no router, URL helper, or
  business DTO state was added. Frontend tests/build, Compose replacement,
  runtime doctor, and repository harness pass; the deployed frontend image is
  `sha256:ce7a2f81272de55d1209e60a9bf79335e206945e62d738347fc13e8bc6dc5884`.
  Docker browser acceptance temporarily set the same app metadata to
  `Legacy Vendor` with `legacy.vendor.test/path`, then with
  `https://legacy.vendor.test/secure`. The rendered anchor's attribute and
  resolved URL were respectively `http://legacy.vendor.test/path` and the
  unchanged HTTPS value, both with `_blank`; document width remained 1280px.
  The original `NULL` company/URL row was restored, initapp returned both
  values empty again, the link disappeared, and all 67 runtime-doctor checks
  passed. The inspected 1280x720 bare-host artifact is
  `artifacts/runs/20260714-legacy-login-vendor-link/bare-url.jpg`.
- 2026-07-14: guarded `index.jade`'s implicit Enter login contract. The old
  Login button omits `type`, making it the form's default submit button; HTML
  implicit submission fires that button's click, so `ng-click="hello()"` runs
  before Angular prevents page submission. Vue's existing submit button and
  `@submit.prevent="submit"` already reproduce that interaction. A source
  contract now locks both pieces together without adding a key listener,
  component state, request path, or DTO binding. Authorized Chrome acceptance
  filled `admin/admin` and the current local CAPTCHA, focused the CAPTCHA input,
  and pressed Enter without clicking Login. The deployed page entered the Admin
  shell and loaded Order List, proving the existing form submit path reached
  `loginv2`; no implementation change was needed.
- 2026-07-14: restored `tbar.jade`'s always-available shell navigation. The old
  Home, metadata menu, submenu, and `安全退出` controls have no request-pending
  disabled state; their Angular handlers remain callable while another HTTP
  request is in flight. Vue no longer passes global `pendingAction` into the
  shared desktop/mobile menu or disables either logout command. View, detail,
  save, and report controls retain their existing request boundaries. Removing
  the unused menu prop shrinks the shared component without adding state or a
  second navigation path. Frontend tests/build, Compose replacement, runtime
  doctor, and the repository harness pass; the deployed frontend image is
  `sha256:588a1016381c2b8e7d192c7a85c54690bd3013ab5ad0eec25092263f08f48a75`.
  Authorized Docker browser acceptance logged in as `admin`, paused the backend,
  and started a real `querydata` request. At 800ms Home, Views, and
  `安全退出` remained enabled while the View-local Find command was disabled and
  no dialog opened. Unpausing completed the request, restored Find, left all
  shell controls enabled on `/main`, and produced no browser console errors.
  Backend `/test`, Compose, and all 67 runtime-doctor checks passed afterward.
- 2026-07-14: restored `view.jade` / `viewWithChart.jade` top-command request
  availability. Their Find, Report, and metadata create controls have no
  request-pending disabled state, so the operators can invoke them while an
  earlier View request is in flight. Vue now leaves Find and create commands
  active and retains only Report's pre-View initialization guard. Shared row
  actions, Sudoku panels, pagination, detail, and report internals keep their
  existing request boundaries; no new prop, state, or request path was added.
  Authorized Docker browser acceptance opened normal list `/view101`, paused
  the backend, and started a real `querydata` request. At 600ms Find and Report
  remained enabled; Report was clicked during that pending query and its dialog
  loaded normally once the backend resumed. The current Docker metadata has no
  non-row create operation, so the 179-test frontend suite retains the runtime
  metadata fixture that proves create commands remain active. Backend `/test`,
  Compose state, and all 67 runtime-doctor checks passed afterward.
- 2026-07-14: restored `detailView.jade`'s existing-item picker command
  availability. The old modal and its shared `querylistdata.js` controller have
  no request-pending lock, so Find, row Select, pagination, header close,
  backdrop dismissal, and Cancel remain actionable while a candidate query is
  in flight. Vue now removes only those invented picker guards; candidate View
  loading, detail saves, child mutation, and report controls retain their
  existing request boundaries. The change deletes conditional UI state, adds
  no request path or DTO binding, and keeps the shared table/paginator.
  Authorized Docker browser acceptance opened `/view102/1001`, loaded four
  View-driven candidates, paused the backend, and started real candidate
  `querydata` requests. Find, all four Select commands, Page 1, Close, Cancel,
  and backdrop dismissal stayed actionable while requests were pending. Select
  staged `2002 / Existing fee` locally and closed the picker; reload discarded
  it without a save, and MySQL still mapped item `2002` to order `1002`.
  Physical backdrop clicks covered PrimeVue's mousedown/up mask contract. The
  backend was restored after every request; `/view102/1001` showed no error
  dialog, Compose and `/test` passed, and all 67 runtime-doctor checks were
  green afterward.
- 2026-07-15: restored `detailView.jade`'s child collection command
  availability. Its metadata Add, inline Edit/Save, Delete, and detail links
  have no global request-pending state; `detailview.js` gates inline editing on
  the existing page edit state instead. Vue now removes `pendingAction` only
  from Add, inline Edit/Save, and Delete while retaining that edit guard and
  leaving the already-active detail links unchanged. Candidate loading, main
  detail save, View operations, and report boundaries are untouched; no state,
  request path, DTO binding, or duplicate component was added. Authorized
  Docker browser acceptance opened `/view102/1001`, paused the backend, and
  started a real `getsubmenu` request. Add remained enabled and started the
  existing candidate-loading dialog; Delete staged removal of item `2001`
  locally; all three Detail links retained their legacy paths. A temporary
  View-metadata switch from `edit_view_id/selected_view_id=101/101` to `0/0`
  exposed the old inline branch, where Edit and Save both remained actionable
  during the same pending request. No detail save was submitted. Reload
  discarded the staged change, metadata was restored to `101/101`, MySQL child
  rows were unchanged, Compose returned healthy, and the restored page again
  rendered Add, Delete, and Detail links.
- 2026-07-15: aligned `detailView.jade`'s parent-detail command availability.
  The old Edit, Save, View-operation, and field lookup controls do not inherit
  a page-wide HTTP pending flag. Vue now removes that global lock, keeps Edit
  tied to its edit session, and disables Save only when no edit session exists
  or its own save request is running. Lookup editors retain their existing
  local loading state. The change deletes the now-unused detail `pending` prop
  and lookup context flag without adding state, requests, DTO bindings, or
  duplicate components. Authorized Docker browser acceptance logged in with
  `admin/admin`, opened `/view102/1001`, paused the backend, and started a real
  `getsubmenu` request from Views. Edit remained enabled and entered the edit
  session; Save and the Customer AutoComplete were then both enabled while the
  unrelated request was pending. No save was submitted. After unpausing and
  reloading, the detail returned to read-only `BTC-USDT / Ada Capital / Open`,
  MySQL order and child rows were unchanged, Compose was healthy, and all 67
  runtime-doctor checks passed. Current runtime metadata has no parent View
  operation, so its no-global-lock branch remains covered by the source
  contract.
- 2026-07-15: aligned `view.jade` row-navigation and `navbar.js` paging request
  availability. Old row links navigate immediately, and old page links are
  disabled only by first/last-page boundaries; neither inherits the active
  `querylistdata` request state. Vue now leaves main-list row commands and the
  shared legacy paginator active during unrelated requests. The table keeps an
  optional disabled boundary for Sudoku callers, while Sudoku refresh remains
  unchanged. Removing redundant false props also simplifies the already-active
  detail candidate table. No state, request, route, or DTO binding was added.
  Authorized Docker browser acceptance temporarily added three clearly marked
  local orders to expose Page 2 and set operation `7002`'s result View to `102`
  so its old row-link branch rendered. With the backend paused, Page 2 remained
  enabled during `getsubmenu`, switched immediately, and loaded order `1001`
  after resume. During a separate pending list query, row `1001`'s Save-labelled
  link remained enabled and navigated directly to `/view102/1001`; no operation
  or save request ran. The operation result View was restored to `NULL`, all
  three temporary rows were deleted, the list returned to eight rows with no
  second page or active Save row buttons, and all 67 runtime-doctor checks
  passed.
- 2026-07-15: aligned `view.jade` / `mkreport.js` report-command request
  availability. The old output, condition, footer, result-page, Return, and
  modal-dismiss controls do not read a global HTTP pending flag. Vue now
  removes that flag from the report panel and output selector while preserving
  the old result header's lack of a close button. Initial generation still
  hides setup before `mkrpt`, while result paging now leaves the result dialog
  visible like `ShowReportController`; a paging response also cannot reopen a
  result after Return has restored setup. The existing `reportRunning` flag is
  now limited to initial generation, so Return restores setup immediately even
  while a page request is still in flight. Request paths, page-boundary guards,
  and metadata adapters are unchanged.
  The change deletes one prop chain and all report-only disabled bindings; it
  adds no state, request, route, DTO binding, or duplicate component.
  Authorized Docker browser acceptance used `view101` and seven temporary
  order-item rows to expose two report pages. During a paused `saverpt` request,
  every setup button/select remained enabled and Add Condition created a local
  row. Initial Confirm still hid setup and returned a two-page report. During a
  paused next-page request, results and Return remained visible; Return restored
  setup immediately, and the late failed response did not reopen results. The
  result header retained no close button, while setup Close remained active.
  Browser locator clicks centered on the dialog instead of the empty mask, so
  physical mask dismissal remains source-contract coverage rather than claimed
  browser evidence. All seven temporary items were removed, `/view101` returned
  to four rows, Compose was healthy, and all 67 runtime-doctor checks passed.
- 2026-07-15: aligned `GroupViewController` Sudoku refresh request availability.
  Its generated root/group Refresh anchors and registered list timers invoke
  query directly without a global pending check. Vue now removes the remaining
  `disabled` prop chain from `App` through `ViewListPanel` and `SudokuPanels`,
  and panel timers no longer skip refresh solely because another request is in
  flight. Map passive refresh, panel-specific timers, requests, View metadata,
  and rendered panel data remain unchanged. The change deletes state plumbing
  without adding a path, DTO binding, or component. Authorized Docker browser
  acceptance opened `/view103`, paused the backend, and clicked the Orders List
  Refresh. Both that root command and Order Group's child-list Refresh retained
  `disabled=false`; the second command also fired while the first request was
  pending. After resume both remained active, the list timestamp updated, no
  error dialog appeared, Compose was healthy, and all 67 runtime-doctor checks
  passed. Seeded panel refresh intervals are zero, so timer concurrency remains
  focused source-contract coverage.
- 2026-07-15: aligned `timer.js` main View auto-refresh concurrency. The old
  timer invokes each registered query callback when due without checking for an
  active HTTP request. Vue now removes its extra global-pending guard while
  retaining the metadata interval, timer cleanup, page-one reset, and shared
  `queryCurrentViewData` path. The change removes one conditional and adds no
  state, request type, route, DTO binding, or component. Docker image
  `sha256:5e8bb2a8e6cbb7167f035ad884c171ff5ba577fdeb63d627a396ca81741ef391`
  was accepted with both View 100 interval rows temporarily changed from zero
  to one second. With the backend paused, the visible Find command stayed
  active while the manual query and scheduled refreshes overlapped; the Nginx
  log recorded 36 `querydata` requests after the acceptance marker. Both rows
  were restored to zero, a fresh `/view100` load showed the eight seeded rows,
  and another 2.6-second window recorded no refresh request. Compose was
  healthy and all 67 runtime-doctor checks passed.
- 2026-07-15: aligned the `querylistdata.js` visible-table timer gate for
  `viewWithChart`. The old timer registers `$scope.query`, which skips its
  scheduled request when `#datalist` is hidden by the Chart tab, while the
  separate `ViewWithChartController.query` path keeps manual Find active. Vue
  now derives table visibility from its existing metadata-selected template
  and active tab, reports that presentation state to App, and checks it only
  inside the scheduled callback. Normal View concurrency and manual Find are
  unchanged. Docker image
  `sha256:0dbfcad41a1b686af6d94a677888356d9d2edbf5c9453e485a170a30c56acceb`
  was accepted with both View 100 interval rows temporarily changed from zero
  to one second. Nginx recorded consecutive one-second Data-tab requests, zero
  requests during a 3.1-second Chart-tab window, and exactly one request for a
  Chart-tab manual Find. Both interval rows were restored to zero; a fresh
  `/view100` showed the eight seeded rows and emitted zero requests in another
  2.6-second window. Compose was healthy and all 67 runtime-doctor checks
  passed.
- 2026-07-15: aligned `ViewWithChartController.query` paging. In the old
  chart template, Find calls `querylistdata.query`, which invokes the bound
  `querydata` callback directly and therefore retains the current page. Plain
  `view.jade` Find still calls its local `query`, which resets to page one.
  Vue now applies its existing page reset only to non-chart manual searches;
  scheduled refresh retains its separate page-one reset. No payload, View/Data
  projection, DTO, route, or component changed. Docker image
  `sha256:4d772ca09299a01ba343c80bf840e35a4139225de43844d6b27add795e1b3f84`
  was accepted with three temporary orders and seven temporary order items.
  View 100 Page 2 retained `aria-current=page`, Chart selection, and its 1001
  data point after Find; plain View 101 Page 2 returned to Page 1 after Find.
  All temporary rows were removed, order/item counts returned to 8/4, View
  100's file/interval metadata remained at 990001/0, Compose was healthy, and
  all 67 runtime-doctor checks passed.
- 2026-07-15: aligned `timer.js` registration cadence. The old service owns one
  global one-second ticker, starts a listener counter at zero, and only changes
  registration when Angular observes a different interval. Vue now retains one
  main View timer for an unchanged `AutoFreshTime`, uses the same due-check then
  increment sequence, and leaves that timer running across manual Find,
  pagination, and same-interval responses. View navigation and actual interval
  changes still clear it; hidden Chart panes still consume due ticks without a
  request. No request, payload, DTO, route, or component was added. Docker image
  `sha256:67ee985fe976be13e5c12b573f0716b63b5d5e05b6a12185e7490f014f5a7b02`
  was accepted with View 100 temporarily set to one second. Nginx recorded the
  initial query at `17:54:19.215`, the first tick at `17:54:21.259`, then ticks
  at `22.257`, `23.245`, and `24.245`, matching the old delayed first tick and
  one-second continuation. With the backend paused, Find stayed active and 18
  manual/scheduled requests completed together after resume, proving the ticker
  remained registered. Both interval rows were restored to zero; a subsequent
  2.6-second window recorded zero requests. View/file and row-count state was
  unchanged, Compose was healthy, and all 67 runtime-doctor checks passed.
- 2026-07-15: aligned `querylistdata.js`'s silent HTTP-error path. Its legacy
  `$http.post(...).success(...)` query has no `.error(...)` callback, so a
  network or non-2xx failure leaves the rendered table, pagination, and timer
  state in place without opening shared error feedback; response-backed
  `data.error` still uses the old error UI. The shared Vue API wrapper now
  classifies transport failures separately from business errors, and the main
  View query alone asks `runAction` to suppress that transport presentation.
  Other actions retain existing error behavior, and no View/Data projection,
  payload, route, DTO, or component changed. Frontend tests/build, Compose
  replacement, repository harness, and all 67 runtime-doctor checks pass; the
  deployed image is
  `sha256:17e563ff5095bb9226a916f3c3b61c0664a6dd7f9072498a147cb2369764dc4e`.
  Authorized browser acceptance loaded the eight seeded rows, stopped the
  backend, and clicked Find. Nginx recorded the expected `querydata` `502`,
  while the existing rows/paginator remained, the command became available,
  and no HTTP, network, or shared error appeared. After backend restart, Find
  succeeded in the same session. View 100 remained file `990001` with refresh
  interval zero, and order/item counts remained 8/4.
- 2026-07-15: aligned the detail candidate picker's data-query transport branch.
  `detailView.jade` mounts `QuerylistdataController` inside `#selectdialog`, so
  its Find and paging requests inherit the same success-only `/data/querylist`
  path as the main list. Vue now reuses the existing `silentTransport` action
  policy only for `child-select-data`, preserving candidate rows, record count,
  and page state when the request has a network or non-2xx failure. Candidate
  View metadata loading and response-backed business errors remain unchanged;
  no component, View/Data projection, payload, route, DTO, or abstraction was
  added. All 187 frontend tests, TypeScript/Vite build, and repository harness
  pass. The deployed Docker image is
  `sha256:7179af2aed562a4959a0529e1dfda3cd796b246b3d218501f28dd7ab59a99869`.
  Authorized browser acceptance opened `/view100/1001`, loaded four candidate
  rows from the candidate View, and then stopped the backend. Nginx recorded
  settled `querydata` `502` / `504` failures while the dialog retained four
  Select commands, `共4条记录`, its fixed ten-row table, active Find, and no
  HTTP, network, or shared error. Find succeeded in the same dialog after the
  backend restarted. View metadata and order/item counts remained unchanged,
  Compose was healthy, and all 67 runtime-doctor checks passed.
- 2026-07-15: aligned success-only transport handling across Sudoku panels.
  Root List and grouped List panels use `querylistdata.js`, Group metadata uses
  `groupview.js`, and Map, Item, and line-chart data use `ServerUtil.js`; none
  defines an HTTP error callback. The shared Vue panel loaders now accept one
  optional action policy, and only `useSudokuPanels` supplies the existing
  `silentTransport` policy to its View, list-data, and detail-data requests.
  Failed transport leaves already merged metadata/data/chart state in place,
  while response-backed business errors and non-Sudoku callers remain
  unchanged. No component, route, DTO binding, dependency, or request-specific
  abstraction was added. All 189 frontend tests, TypeScript/Vite build, and
  repository harness pass. The deployed Docker image is
  `sha256:c3280b496c00c46f4a65400c09e87b06a04de513b06320d675a8f43a7a6341a1`.
  Authorized browser acceptance loaded `/view103` with List, Group, chart, map,
  and item content, then stopped the backend and clicked the first List Refresh.
  Nginx recorded `getlistview` as `502`; every loaded panel and both Refresh
  commands remained, with no HTTP, network, or shared error. After restart,
  Refresh succeeded in the same session and advanced the List update time from
  `18:21:12` to `18:22:16`. Compose was healthy, metadata and row counts were
  unchanged, and all 67 runtime-doctor checks passed.
- 2026-07-15: aligned the main View's response-backed error outlet with
  `querylistdata.js` and `showerror.js`. Legacy business errors open the shared
  `发生错误` modal with one `关闭` command; they are not rendered as an inline
  table Message. Vue now uses one small `LegacyErrorDialog` in both main View
  and detail surfaces, removing the duplicated detail modal markup. The main
  panel emits dismissal to the existing shared error state, while the separate
  login error dialog retains its error-code and CAPTCHA lifecycle. Transport
  suppression, response classification, View/Data projection, routes, and DTO
  bindings are unchanged. All 191 frontend tests, TypeScript/Vite build, and
  repository harness pass. The deployed Docker frontend image is
  `sha256:c7d7b6939aae34405b3a6f8dd0f9455f42cb59757923222e7b66c16b33f6d8da`.
  An authorized browser replay of `/view999999` opened one `发生错误` dialog
  containing `发生未知错误` and one `关闭` command; Nginx recorded the backing
  `getlistview` request as HTTP 200. Closing removed the dialog, and `/view100`
  then rendered all 8 seeded records. Compose was healthy, `db-migrate` was
  `Exited (0)`, metadata and row counts were unchanged, and all 67
  runtime-doctor checks passed.
- 2026-07-15: aligned `mkreport.initquery`'s success-only transport path. The
  old report setup opens only inside the successful `/report/mkqview` callback;
  a network or non-2xx failure leaves the main View unchanged. Vue now reuses
  the existing transport classification and `silentTransport` action option,
  closing the still-hidden report component only for transport failures.
  Response-backed business errors, report success behavior, View-derived
  columns, routes, payloads, and DTOs are unchanged. All 192 frontend tests,
  TypeScript/Vite build, and repository harness pass. The deployed Docker
  frontend image is
  `sha256:3dffceff8cd5975770354e62affd317b0107531d9fa23bd73e1cd7f2b4d4f5f8`.
  An authorized browser replay kept all four `/view101` rows visible while the
  backend was stopped; clicking `统计` produced no report or error dialog, and
  Nginx recorded `getmkqview` as `502`. After backend restart, the same command
  returned HTTP 200 and opened `生成报表` with the View-derived Item ID and Item
  Name candidates. Compose was healthy, `db-migrate` was `Exited (0)`, metadata
  and row counts were unchanged, and all 67 runtime-doctor checks passed.
- 2026-07-15: aligned the report-generation transport branches in
  `MakeReportController` and `ShowReportController`. The old initial command
  hides setup before its success-only `mkrpt` request, so transport failure
  leaves both report dialogs closed. Paging uses the same success-only request
  without hiding results, so failure retains the current result. Vue now reuses
  one component-local success-only action helper for report metadata and
  generation requests: initial transport failure closes the hidden component,
  while paging failure preserves result state; neither opens transport feedback.
  Response-backed errors, successful generation, paging boundaries,
  View-derived report state, payloads, routes, and DTOs are unchanged.
  All 193 frontend tests, TypeScript/Vite build, and repository harness pass.
  The deployed Docker frontend image is
  `sha256:08bc0b9824639e4de05f1b782e2774105ec6fd897c0ac8531b3724c05ec014eb`.
  An authorized browser replay loaded `/view101` report setup before stopping
  the backend. Initial generation settled as Nginx HTTP 504 while setup,
  results, and error dialogs all remained absent and the four list rows stayed
  visible. After backend restart, the same workflow selected Item ID / original
  value, returned HTTP 200, and opened `报表结果 共1页 当前第1页` with all four
  values. Compose was healthy, `db-migrate` was `Exited (0)`, metadata and row
  counts were unchanged, and all 67 runtime-doctor checks passed. Paging-failure
  state retention remains covered by the focused source contract because the
  seeded report has one page.
- 2026-07-15: aligned `timer.js` / `message.js` polling concurrency. The old
  one-second ticker invokes the registered message callback whenever its
  15-second interval is due, without checking whether an earlier `$http`
  request is pending. Vue inherited an extra `shellPollInFlight` gate from its
  former combined user/message refresh and therefore skipped ticks during slow
  requests. The gate and its state are now removed; the existing token guard,
  15-second timer, silent transport handling, timer cleanup, first-message
  display, protocol adapters, routes, and DTOs are unchanged. All 193 frontend
  tests, TypeScript/Vite production build, and repository harness pass.
  The deployed Docker frontend image is
  `sha256:a6be14106ff3180e0a96e594a1c55105c2c87df23a7b8cc6e85f18a978afbddc`.
  After an authorized `admin/admin` login, the backend was paused at 03:00:05.
  Frontend Nginx held 2 established backend connections at 03:00:26 and 5 at
  03:00:52. After resume at 03:01:03, its access log settled four accumulated
  `getmsg` requests together as HTTP 200, directly proving overlapping polls;
  the fifth paused connection was an unrelated `initapp`. The page retained
  Admin, showed no error dialog, and a recovery Find returned all 8 rows before
  safe logout. Compose was healthy, `db-migrate` was `Exited (0)`, View refresh
  intervals and row counts were unchanged, and all 67 runtime-doctor checks
  passed.
- 2026-07-15: aligned `operation.js`'s success-only transport path. The old
  `runoperation` service opens `执行结果` only from its `$http.success` callback;
  network and non-2xx failures provide no shared detail error or result dialog.
  Vue now passes the existing `silentTransport` option through the one
  View-derived operation request. Response-backed results, edit-state guards,
  request payloads, operation metadata, routes, DTOs, and result presentation
  are unchanged. All 193 frontend tests, TypeScript/Vite production build, and
  repository harness pass. The deployed Docker frontend image is
  `sha256:233148af80bf9ed10f33e2d83c6a9edff97444aa7973ad0155a0a6dcdaf2a741`.
  An authorized `/view100/1002` replay stopped the backend and invoked the
  enabled View-derived Save operation. After Nginx settled `runoperation` as
  HTTP 502, the detail page retained its heading and Admin user with no
  `发生错误` or `执行结果` dialog. After backend recovery, the same command
  returned HTTP 200 and opened `执行结果 / 操作成功 / 保存成功`; `确定` closed it
  and safe logout returned to the login form. Compose was healthy,
  `db-migrate` was `Exited (0)`, View intervals and order counts were unchanged,
  order 1002 retained its seeded ETH-USDT values, and all 67 runtime-doctor
  checks passed.
- 2026-07-15: aligned `menuinfo.js`'s success-only submenu transport path. The
  old Bootstrap parent opens independently of its `$http.success` callback;
  network and non-2xx failures leave an empty expanded menu without error
  feedback. Vue already matched the expand-before-request and second-click
  collapse timing, and now passes the existing `silentTransport` option through
  `getsubmenu`. Response-backed data, desktop/mobile shared rendering, menu
  metadata, routes, and DTOs are unchanged. All 193 frontend tests,
  TypeScript/Vite production build, and repository harness pass. Docker/browser
  acceptance deployed frontend image
  `sha256:267e07af1258c49319a61bbac4930ea6140fe64a284cb981075c11b70700cfc3`.
  With the backend stopped, clicking Views immediately set `aria-expanded=true`
  while retaining all 8 list rows. After Nginx settled `getsubmenu` as HTTP 502,
  the menu remained expanded and empty with no `发生错误` dialog. After backend
  recovery, collapse then re-expand returned HTTP 200 and rendered `OrderList`;
  selecting it navigated to `/view100`, collapsed the menu, and retained all
  rows. Safe logout returned to login. Compose was healthy, `db-migrate` was
  `Exited (0)`, View intervals and row counts were unchanged, and all 67
  runtime-doctor checks passed.
- 2026-07-15: aligned `menuinfo.js`'s success-only logout transport path. The
  old controller clears the session and reloads the root route only inside its
  `$http.success` callback; network and non-2xx failures leave the authenticated
  page unchanged without feedback. Vue now passes the existing
  `silentTransport` option through logout. Successful route replacement,
  session cleanup, fresh login metadata/CAPTCHA, shell controls, payloads,
  routes, and DTOs are unchanged. All 193 frontend tests, TypeScript/Vite
  production build, and repository harness pass. The deployed Docker frontend
  image is
  `sha256:39b8a37c1696b9122c4832fc4a0538b367d2207625ed2ae603dcb95d47f73781`.
  An authorized `/view100` browser replay stopped the backend and invoked
  `安全退出`. After Nginx settled logout as HTTP 502, the URL, authenticated
  shell, token, and 8 View rows remained with no `发生错误` dialog. After
  backend recovery, the same command returned HTTP 200, replaced the route with
  `/`, cleared the shell, and rendered the fresh login flow. Compose was
  healthy, `db-migrate` was `Exited (0)`, View intervals and row counts were
  unchanged, and all 67 runtime-doctor checks passed.
- 2026-07-15: aligned `setextype.js`'s success-only BusinessObject lookup
  transport path. The old typeahead source handles candidates and
  response-backed errors only inside `$http.success`; network and non-2xx
  failures do not render field feedback. Vue now reuses the existing transport
  classifier in `MetadataFieldEditor` and suppresses only that inline transport
  message. Loading cleanup, the typed input, response-backed lookup errors,
  View-derived identity and parent context, selection behavior, request payload,
  route, and DTO are unchanged. All 193 frontend tests, TypeScript/Vite
  production build, and repository harness pass. The exact implementation
  commit was deployed as tagged Docker image
  `sha256:a15b749f64296711d4b4e8accd4116b162717c7ddb5a5fbfba3e75c692894e69`.
  An authorized `/view100/1002` replay entered edit mode through View-derived
  detail metadata and used its Customer BusinessObject field. With the backend
  stopped, Nginx settled `inputquery` as HTTP 502 while `Ada` remained in the
  field with no inline transport error. After backend recovery, the same query
  returned HTTP 200 and rendered `Ada Capital - 3001`; safe logout then returned
  to login. Compose was healthy, `db-migrate` was `Exited (0)`, View metadata,
  order/detail counts, and order 1002 were unchanged, and all 67 runtime-doctor
  checks passed.
- 2026-07-15: aligned the signed-out `index` / `soway.initapp` transport
  surface. FoolFrame initializes app metadata on the server; its shared
  `postandget` network-error handler only logs and never invokes the route's
  render callback, so no browser business error is produced. Vue now passes the
  existing `silentTransport` option through only its initial `initapp` request.
  The static fallback login shell and request cleanup remain available instead
  of copying the old permanently pending navigation. Successful metadata,
  CAPTCHA/database adapters, login behavior, request payload, route, and DTO are
  unchanged. All 193 frontend tests, TypeScript/Vite production build, and
  repository harness pass. An isolated acceptance deployed frontend image
  `sha256:d1144dc7db08cf0b9c8cbeb636911ee1ff2e5169c06e35fdeab5ea2d932dceb1`
  and served bundle `index-BmTy2PHd.js`. Its authorized browser context returned
  deterministic HTTP 502 from `initapp`; the three visible login inputs remained
  and no `发生错误` or transport text appeared. Removing the interception and
  reloading returned HTTP 200 from `initapp`; a locally read CAPTCHA plus
  `admin/admin` then returned HTTP 200 from `loginv2`, reached `/main`, rendered
  Admin and Order List, and logged out successfully. A final shared-stack replay
  deployed and matched image
  `sha256:86f7f14ea319c50361d3e99c87979596e21e09c80a85d119a0928c3e583dda26`,
  stopped the backend, and recorded real Nginx HTTP 502/504 responses. A fresh
  failed page retained the same three-input fallback, omitted CAPTCHA, disabled
  Login, and showed no error dialog; backend recovery and reload returned HTTP
  200 and restored CAPTCHA, Login, and version metadata. Compose was healthy,
  `db-migrate` was `Exited (0)`, View metadata and row counts were unchanged, and
  all 67 runtime-doctor checks passed.
- 2026-07-15: aligned `setextype.js`'s success-only enum-option transport path.
  The old editor creates its Select and presents response-backed errors only
  inside the `getenum` success callback; network and non-2xx failures do not use
  shared feedback. `useFieldEnums` now reuses the shared
  `WorkflowActionRunner` type instead of its duplicate local signature and
  passes the existing `silentTransport` option for `getenums`. View-derived
  model ids, cache behavior, successful options, response-backed errors, field
  values, components, payloads, routes, and DTOs are unchanged. Focused/full
  frontend tests passed (1/1 focused; 19 files and 193 tests full), as did the
  production build, repository harness, and diff check. Docker deployed the
  exact implementation commit as frontend image
  `sha256:64e38d31bfeb773d97915abb80f968c6c948ba044321dd5efb85aeb07c1b1b38`.
  Authorized `admin/admin` browser acceptance used View 102 metadata, whose
  editable State field is Enum model 102. A deterministic browser interception
  returned HTTP 502 only from `getenums`; `/view102/1001` kept the View-derived
  detail and edit controls without a shared dialog or HTTP/error text. Removing
  the interception and reloading returned HTTP 200 through Nginx, and the State
  Select exposed `Open` and `Filled`; logout also returned HTTP 200. Compose was
  healthy, `db-migrate` was `Exited (0)`, order 1001 and row counts were
  unchanged, and all 67 runtime-doctor checks passed.
- 2026-07-15: aligned the authenticated `index` / `soway.getmain` transport
  surface. FoolFrame's root route renders only from the `getmain` callback;
  shared `postandget` request errors only log and do not replace the current
  browser document. Vue now passes the existing `silentTransport` policy to
  `getmain` and returns before session cleanup when `runAction` leaves its
  freshly cleared error state empty. Response-backed stale-token errors still
  clear local state and prepare the login flow. Successful main metadata,
  View-first loading, payload, route, DTO, components, and login behavior are
  unchanged; no ref, helper, or abstraction was added. Focused/full frontend
  tests passed (83 focused; 19 files and 193 tests full), as did the production
  build, repository harness, and diff check. A clean `c4941f64` archive produced
  deployed image
  `sha256:34e3ed81d17b883b95ce24070d5639cc64e43b3f95777f49e16392f0d8124da8`
  and bundle `index-BWT3CjNo.js`. An authorized isolated browser context logged
  in with a locally read CAPTCHA, then received deterministic HTTP 502 from
  `getmain`: `/main`, its token, and `.app-shell` remained without a login form,
  error dialog, or transport text. Removing the failure returned HTTP 200 and
  restored Admin and Order List. After safe logout, a fake token's HTTP-200
  business error still cleared storage and rendered login. Compose was healthy,
  `db-migrate` was `Exited (0)`, and all 67 runtime-doctor checks passed.
- 2026-07-15: aligned `detailview.js beginsave()` transport handling. The old
  handler opens the non-dismissible `保存中` dialog before `save` / `new` and
  hides it only inside `$http.success`; a network or non-2xx failure therefore
  leaves that dialog visible without shared error feedback. Vue now passes the
  existing `silentTransport` option through `saveobj` and `savenewobj`, and
  closes its save dialog on a failed request only when a response-backed
  business error populated the existing error state. Successful save, staged
  child changes, View-derived fields and identity, `history.back()` navigation,
  payloads, routes, DTOs, and components are unchanged; no new state, helper,
  or abstraction was added. Focused/full frontend tests passed (83 focused; 19
  files and 193 tests full), as did the TypeScript/Vite production build,
  repository harness, and diff check. A clean `ecb8592e` build was deployed as
  tagged frontend image
  `sha256:67ce4a79d9e15cb8de295ac221b0a0479c47a734325915fab5c5eee61baa8f1c`
  with entry bundle `index-CYqoDW3m.js`. An authorized isolated browser context
  read the local CAPTCHA, logged in with `admin/admin`, opened `/view102/1001`,
  and received deterministic HTTP 502 only from `saveobj`; the detail route and
  sole `保存中` dialog remained, while `发生错误` and transport text stayed
  absent. Removing the failure and repeating the unchanged save returned HTTP
  200 and performed the old back navigation to `/main`. A second isolated
  context forced HTTP 502 only from `savenewobj` on `/new102`; the same save
  dialog remained without shared/transport feedback, and the request did not
  reach the backend. Both sessions logged out successfully. Compose was
  healthy, `db-migrate` was `Exited (0)`, order 1001 plus the final
  8-order/4-item counts were unchanged, and all 67 runtime-doctor checks passed.
- 2026-07-15: aligned `detailview.js initQueryView()` transport handling. The
  old candidate flow opens its non-dismissible `加载中` dialog before requesting
  the linked View, and closes it only inside the `$http.success` callback before
  querying candidate data. `WorkflowActionOptions` now has a narrow pending
  retention policy, used only by the child `getlistview` request together with
  silent transport handling. View-first ordering, linked View id, candidate
  columns, later query payload/data, components, routes, and DTOs are unchanged;
  no state owner or helper was added. Focused tests passed (84/84), as did all
  195 frontend tests, the production build, repository harness, and diff check.
  Exact implementation commit `7f73f8cd` produced deployed image
  `sha256:79cabbce9c94555003e6ca015323c1a6277566cf2fa25d695c835c33edbb3965`
  and entry bundle `index-f7c4WPnZ.js`. Authorized `admin/admin` browser
  acceptance forced HTTP 502 only from the child `getlistview`: the request
  sequence contained no `querydata`, the sole `加载中 / 正在加载，请稍后....`
  dialog remained, and no shared/transport error appeared. After removing the
  failure and reloading, `getlistview` returned HTTP 200 before `querydata` 200,
  opened `选择 Items`, and rendered `共4条记录`; logout returned HTTP 200.
  Compose was healthy, `db-migrate` was `Exited (0)`, View/order data was
  unchanged, and all 67 runtime-doctor checks passed.
- 2026-07-15: aligned the server-rendered read-item View metadata transport
  surface. FoolFrame's `/itemview:id`, `/view:id/:objid`, and `/new:id` routes
  render only from their nested `postandget` callbacks; request errors only log
  and never produce shared browser feedback. Vue now passes the existing
  `silentTransport` option through `getreaditemview`, retaining the migration's
  required View-before-`querydatadetail` / `initnew` gate. Successful metadata
  caching, rendered fields, data requests, payloads, routes, DTOs, components,
  and pending state are unchanged; no new state, helper, or abstraction was
  added. The focused workflow tests passed (9/9), as did all 194 frontend tests,
  the TypeScript/Vite production build, repository harness, and diff check.
  Exact implementation commit `72e2e446` produced deployed image
  `sha256:2a25c4acbcb4bec67b765f4a30e29b86b13b38dcc213ef239dfca5c60716208c`
  and entry bundle `index-DbeIIhCC.js`. Authorized `admin/admin` browser
  acceptance forced `getreaditemview` HTTP 502 independently on `/itemview102`,
  `/view102/1001`, and `/new102`; all retained their route and authenticated
  shell without `发生错误` or transport text, while detail/new issued zero
  `querydatadetail` / `initnew` requests. Removing the failure returned View
  HTTP 200 on all three paths, then detail/new data HTTP 200, and restored one
  View-derived panel per path. Logout returned HTTP 200. Compose was healthy,
  `db-migrate` was `Exited (0)`, order 1001 plus the 8-order/4-item counts were
  unchanged, and all 67 runtime-doctor checks passed.
- 2026-07-15: aligned the server-rendered detail/new data transport surface
  after View metadata succeeds. FoolFrame's `getItem` and `initnew` routes call
  `res.render('detailView', ...)` only from their `querydatadetail` / `initnew`
  `postandget` callbacks; request errors only log and leave the render callback
  untouched. Vue now passes the existing `silentTransport` option through both
  data requests. The View-before-data gate, response-backed business errors,
  successful detail/new rendering, payloads, routes, DTOs, components, and
  pending state are unchanged; no helper, state, or abstraction was added.
  Focused payload tests passed (84/84), as did all 195 frontend tests, the
  TypeScript/Vite production build, repository harness, and diff check. Exact
  implementation commit `545ed181` produced deployed image
  `sha256:800448bbf2cd98fed8f701435461415e6a7b9d03dca8784b0e3820ba8e6584b0`
  and entry bundle `index-D4PLU5xf.js`. Authorized `admin/admin` browser
  acceptance loaded `getreaditemview` HTTP 200 before forcing HTTP 502 from
  `querydatadetail` on `/view102/1001` and `initnew` on `/new102`. Both routes
  retained one View-derived panel without `发生错误` or transport text. A
  separate HTTP-200 nonzero-code detail response still rendered the shared
  business-error dialog. Removing the failures returned View/data HTTP 200 in
  order on both paths, restored normal panels, and logout returned HTTP 200.
  Compose was healthy, `db-migrate` was `Exited (0)`, order 1001 plus the
  8-order/4-item counts were unchanged, and all 67 runtime-doctor checks passed.
- 2026-07-15: aligned the server-rendered list View metadata transport surface.
  FoolFrame's authenticated `/`, `/main`, and `/view:id` routes render only
  inside the `getlistview` `postandget` callback; request errors only log and do
  not continue to `querydata` or show shared browser feedback. Vue now passes
  the existing `silentTransport` option through `getlistview`, preserving its
  View-before-data gate. Response-backed business errors, successful list
  rendering, payloads, routes, DTOs, components, and pending state are
  unchanged; no helper, state, or abstraction was added. Focused workflow tests
  passed (9/9), as did all 195 frontend tests, the TypeScript/Vite production
  build, repository harness, and diff check. Exact implementation commit
  `37ef7662` produced deployed image
  `sha256:5994dd9cd2c76931cb767ec15bec1ae95a164036b769eaa28275740ad0f58efa`
  and entry bundle `index-CN0-wUor.js`. Authorized `admin/admin` browser
  acceptance forced `getlistview` HTTP 502 independently on `/`, `/main`, and
  `/view100`; each retained its route and authenticated shell without
  `发生错误` or transport text, and issued zero `querydata` requests. A separate
  HTTP-200 nonzero-code response retained the shared business-error dialog.
  Removing the failure returned `getlistview` HTTP 200 before `querydata` 200,
  restored the order list with BTC-USDT and ETH-USDT rows, and logout returned
  HTTP 200. Compose was healthy, `db-migrate` was `Exited (0)`, order 1001 plus
  the 8-order/4-item counts were unchanged, and all 67 runtime-doctor checks
  passed.
- 2026-07-15: restored `view.jade`'s inert report-save command. The old report
  footer renders `保存报表定义` with `ng-click="saverpt()"`, but
  `mkreport.js` never defines that scope function; clicking the enabled command
  therefore does not call the otherwise-present `/report/saverpt` route or
  present success/failure feedback. Vue now keeps the report-name field and
  visible enabled footer command while removing its invented save handler,
  request, payload name injection, and status messages. Report metadata,
  generation, conditions, output selection, the backend compatibility route,
  DTOs, and other dialog commands are unchanged. The report component shrank
  from 401 to 391 lines. Focused tests passed (88/88), as did all 196 frontend
  tests, the TypeScript/Vite production build, repository harness, and diff
  check. Exact implementation commit `7069f765` produced deployed image
  `sha256:a0ab8a20af2e84b7013de6c50e57346a9b24e0bbcf54f0296914e351f14e4358`
  and entry bundle `index-hTPS1fYg.js`. Authorized `admin/admin` browser
  acceptance opened `/view101`, loaded report metadata with HTTP 200, entered
  a report name, and clicked the visible enabled save command. The dialog,
  name, and route remained unchanged, zero `saverpt` requests were emitted, and
  no success, failure, or shared error text appeared. Cancel closed the dialog
  and logout returned HTTP 200. Compose was healthy, `db-migrate` was
  `Exited (0)`, order 1001 plus the 8-order/4-item counts were unchanged, and
  all 67 runtime-doctor checks passed.
- 2026-07-15: exposed the old Sudoku Item data-route alias. FoolFrame's
  `src/Web/public/javascripts/app/subitem.js` posts `/itemview` with `id`,
  `objid`, and the original `idxep` spelling after the route View has rendered.
  The migrated API already accepted those fields through
  `querydatadetail`, so `/api/v1/data/itemview` now maps to the same controller
  method without adding a DTO, service branch, or duplicate query path.
  Annotation coverage passed (5/5) in the focused Java 17 Maven run, with all
  nine required modules successful; the runtime-doctor compile check,
  repository harness, and diff check also passed. Local Maven could not compile
  Java 17 because the host Maven uses an older JDK (`invalid target release:
  17`), so the focused test ran in `maven:3.9-eclipse-temurin-17`. Exact
  implementation commit `25136a0a` produced deployed backend image
  `sha256:06c8105a09f472a27d221c5d17991870d074b5a9c30231bc3c5dc0a00139773d`.
  All 68 runtime-doctor checks passed, including an authenticated request built
  from loaded View and row identifiers against the legacy alias. Compose was
  healthy, `db-migrate` was `Exited (0)`, and order 1001 plus the
  8-order/4-item counts were unchanged.
- 2026-07-15: restored the static `view.jade` report-dialog draft lifecycle.
  Old Cancel only dismisses the existing modal, while `mkreport.initquery`
  reloads candidate metadata without resetting selected outputs, conditions,
  report name, or active tab. Vue now keeps `ViewReportPanel` mounted for the
  current list View, toggles its visibility separately, and reloads report
  metadata on each open; changing View still destroys the keyed component and
  its draft. No report DTO or business state was lifted into App. Focused tests
  passed (92/92), as did all 206 frontend tests, the TypeScript/Vite production
  build, and diff check. The repository harness remained blocked only by an
  unrelated concurrent `AppManageMigrationTest.java` at 2106 lines against its
  2100-line limit. Exact implementation commit `6d4714b4` produced deployed
  image
  `sha256:ca2a3d4894cc777a7255db647b80bfe30a96ef86b7eda1aa9780957d03dc4b76`,
  entry `index-CvJ-YQFt.js`, report chunk `ViewReportPanel-BHiJuoyK.js`, and
  shared editor chunk `MetadataFieldEditor-BQJAjYF6.js`. Authorized
  `admin/admin` browser acceptance opened `/view101` twice with an HTTP-200
  View-derived report metadata response. The second open issued a fresh
  metadata request while retaining the Save tab, report name, selected output,
  and condition value from the first open. Cancel and logout completed without
  browser errors. Compose was healthy, `db-migrate` was `Exited (0)`, and order
  1001 plus the 8-order/4-item counts were unchanged.
- 2026-07-15: completed the report-reopen output-control boundary after a live
  browser check exposed a gap missed by source-only tests. FoolFrame rebuilds
  `#rpt-candidate` in `mkreport.initquery` but leaves `#rtp-selecttype` and
  `#rtp-selected` untouched. Vue now owns those three control values in the
  still-mounted `ViewReportPanel`, while `ReportOutputSelector` continues to
  own their mutations. The state stays component-local and no App state, DTO,
  store, route, or dependency was added. Focused tests passed 97/97, all 208
  frontend tests and the production build passed, and exact commit `2b7d66fa`
  produced deployed image
  `sha256:fdac83a762b1f6a1d796b9936a32b073e8430ed2a7f9ff960abf41cdae3945a9`.
  Authorized `admin/admin` acceptance on `/view101` selected
  `Item Name / 计数`, added that output, canceled, and reopened. The candidate
  reset to `Item ID`, while `原值/计数`, selected `计数`, selected output index
  zero, and `Item Name[计数]` remained. A second Add produced
  `Item ID[计数]`; login/logout returned HTTP 200, both metadata requests used
  View 101, Compose was healthy, and all 68 runtime-doctor checks passed.
- 2026-07-15: restored the application browser title rendered by
  `layout.jade` / `default.jade`. Vue now derives the signed-out title from
  `initapp.AppName` and the authenticated title from `getmain.App.AppName`,
  using the static HTML title only as a metadata fallback. No View/data DTO,
  route, store, or dependency was added. Focused tests passed (85/85), as did
  all 210 frontend tests and the TypeScript/Vite production build. Exact
  implementation commit `a98e3012` produced deployed image
  `sha256:63d5995451ac3184c16409441f9612c9fb16b0ca2b38c123d710b43ee4f71f67`.
  Authorized `admin/admin` browser acceptance intercepted only the real
  `initapp` application-name fields: the signed-out document title and page
  heading both became `Legacy Login Title`, login returned HTTP 200 and changed
  the title plus shell brand to `Fool Service`, and logout returned HTTP 200 and
  restored the cached signed-out title without another metadata request.
  Compose was healthy, `db-migrate` was `Exited (0)`, and all 68
  runtime-doctor checks passed.
- 2026-07-15: separated the two old report candidate lifecycles.
  `MakeReportController.mkrpt` hides setup and `ShowReportController.back`
  shows that same modal without calling `mkreport.initquery`, so returning from
  results must retain the selected candidate. Clicking `统计` again does call
  `initquery`, rebuilds `#rpt-candidate`, and returns it to the first option
  while leaving output methods and selected outputs untouched. Vue now keeps
  the candidate in View-scoped UI state, resets it only after successful report
  metadata loading, and resolves the native change event before the parent
  model round trip. No App state, business DTO, store, route, dependency, or
  duplicate output builder was added; the selector/panel remain 181/409 lines.
  Focused tests passed (100/100), as did all 211 frontend tests and the
  TypeScript/Vite production build. Implementation commits `e6d22c12` and
  `ef18d38d` produced final deployed image
  `sha256:c9767bf5e725f2918934de8aed2628d3efb289bb72964885417ecfae9e9be2b3`
  with report chunk `ViewReportPanel-D-lrTGwB.js`. Authorized `admin/admin`
  browser acceptance on `/view101` selected `第二列 / 第二计数`, added
  `第二列[第二计数]`, generated one report, and returned without a second
  metadata request; candidate, methods, selected method, and output all
  remained. Cancel/reopen raised the metadata count to two and reset only the
  candidate to `第一列`. Logout returned HTTP 200 with no browser errors. All
  68 runtime-doctor checks passed, Compose was healthy, `db-migrate` was
  `Exited (0)`, and order 1001 plus the 8-order/4-item counts were unchanged.
- 2026-07-15: restored `index.jade` Reset's modal-close sequencing. The old
  `showerror()` opens the shared blank `发生错误` modal and registers CAPTCHA
  refresh on `hidden.bs.modal`; it does not refresh when Reset is clicked.
  Vue now keeps that presentation state inside `LoginPanel`, opens the same
  dialog from Reset, and emits the existing refresh event only when Close is
  pressed. Business login errors still use the parent-owned dismiss path, and
  username, password, hidden database, CAPTCHA request, and DTO ownership are
  unchanged. `LoginPanel.vue` remains 199 lines. Focused tests passed (85/85),
  as did all 212 frontend tests, the TypeScript/Vite production build,
  repository harness, and diff check. Exact implementation commit `5d04def0`
  produced image
  `sha256:f089382d2eef340138511a803e0b7ed7253570432c964bc54be7f24bb56e786b`
  with entry bundle `index-DR8HOYTi.js`. Browser acceptance on the exact image
  filled `admin/admin` and a stale CAPTCHA, clicked Reset, and observed zero
  check-code requests for 500 ms while all fields and the image remained
  unchanged. Close then issued exactly one HTTP-200 check-code request, cleared
  only the CAPTCHA input, and replaced the image. Reading that local code
  completed login to `/main`; login/logout returned HTTP 200, and browser
  console/page errors were empty. All 68 runtime-doctor checks passed, Compose
  was healthy, `db-migrate` was `Exited (0)`, and order 1001 plus the
  8-order/4-item counts were unchanged.
- 2026-07-15: completed the previously source-only report paging-failure state.
  `ShowReportController.next/pre` changes `$rootScope.pageindex` before calling
  the success-only request, and `mkreport` updates only cells and total pages on
  success. A transport failure therefore advances the visible page number while
  retaining the prior table. Vue now renders the requested component page
  directly instead of preferring a stale response `CurrentPage`; the unused
  response-page adapter was removed. No report DTO, request, route, component,
  or fallback was added. Focused tests passed (144/144), as did all 212 frontend
  tests, the TypeScript/Vite production build, repository harness, and diff
  check. Exact implementation commit `b6ea15fd` produced deployed image
  `sha256:62ebb6e74f03da62d9d6e14353da843445006f73cb20b992ea7fe52ffe731caf`
  with report chunk `ViewReportPanel-Bc-_HvrS.js`. Authorized `admin/admin`
  browser acceptance on `/view101` returned a two-page report, then forced the
  page-2 request to HTTP 502. The heading advanced to page 2 while page-1 cells
  remained; another Next emitted no request at the boundary, and Previous
  requested/restored page 1. No shared error dialog or page exception appeared;
  the console contained only the expected browser resource line for HTTP 502.
  Logout returned HTTP 200. All 68 runtime-doctor checks passed, Compose was
  healthy, `db-migrate` was `Exited (0)`, and order 1001 plus the
  8-order/4-item counts were unchanged.
- 2026-07-15: restored the dismissible result-modal lifecycle from
  `view.jade` and Bootstrap 3.3.5. The old report result has default
  `backdrop:true` and `keyboard:true`; hiding it by backdrop or Escape does not
  call `ShowReportController.back`, so its page index remains unchanged, while
  the next `统计` still opens report setup. Vue now clears only its result/setup
  presentation flag when the dialog emits a dismiss event and leaves
  `currentPage` untouched. Explicit `返回` remains the sole page reset. No App
  state, report DTO, route, dependency, or helper abstraction was added; the
  report panel remains 414 lines. Focused tests passed (97/97), as did all 212
  frontend tests and the TypeScript/Vite production build. Exact implementation
  commit `cb2b23d0` produced deployed image
  `sha256:201af77f6aa3cd9762204e91d3ea842bf0904f9d1e2502507916f409ded2a51d`
  with report chunk `ViewReportPanel-eJYK5ZH5.js`. Authorized `admin/admin`
  browser acceptance on `/view101` generated page 1, paged to 2, dismissed the
  result by its mask, and reopened setup. The resulting report request sequence
  was `[1,2,2,1]`: mask/reopen preserved page 2, while explicit Return reset the
  next request to page 1. Logout returned HTTP 200 with no browser errors. All
  68 runtime-doctor checks passed, Compose was healthy, `db-migrate` was
  `Exited (0)`, and order 1001 plus the 8-order/4-item counts were unchanged.
- 2026-07-15: completed a stable-HEAD authenticated frontend matrix after the
  latest login and report lifecycle fixes. Exact HEAD `fd513c53` used frontend
  image
  `sha256:8d8a2cabf1887e8699cb9c2f6c1428e93505d78d2010a3ff16e415403a674d2c`.
  Authorized `admin/admin` acceptance loaded `/view100`, `/view101`,
  `/view102`, `/view103`, `/itemview100`, `/view100/1001`, and `/new100`.
  Every list/detail/new route returned HTTP 200 and loaded View metadata before
  its data request; Sudoku child panels followed the same per-panel boundary.
  The report flow loaded View 101 columns, generated `Item Name[计数] = 4`, and
  returned to setup with the candidate, method, and output intact without a
  second metadata request. At 1440px every route matched document width; at
  390px both Sudoku and detail documents remained exactly 390px while rendering
  their tables, chart, map, item, and group content. Login/logout returned HTTP
  200, all 65 observed API requests succeeded, and browser console/page errors
  were empty. All 212 frontend tests, the production build, repository harness,
  and 68 runtime-doctor checks passed. Compose was healthy, `db-migrate` was
  `Exited (0)`, and the database retained 8 orders, 4 order items, and order
  1001 as `BTC-USDT` / state `0` / customer `3001`.
- 2026-07-15: restored `detailView.jade`'s visible inert candidate `确定`
  command. The old select-existing footer renders `取消` followed by `确定`,
  but only row-level `选择` owns confirmation and the footer command has no
  handler. Vue now renders the missing enabled text command beside the existing
  Cancel action without adding selection state, a request, a DTO branch, or a
  second close path. Exact implementation commit `7da1b4b9` kept the component
  at 448 lines. All 213 frontend tests, the TypeScript/Vite production build,
  repository harness, and 68 runtime-doctor checks passed. Authorized
  `admin/admin` browser acceptance opened the real Items candidate dialog on
  `/view100/1001`; clicking `确定` retained one dialog, the same URL, and all
  three rendered child rows. The Nginx log delta contained only the scheduled
  `getmsg` poll, with no candidate, save, or add request. At 1280px both footer
  commands shared one row; at 390x844 the dialog stayed between x=16 and x=374,
  and document width equaled the 390px viewport. Browser warnings/errors were
  empty, logout returned HTTP 200, and Compose retained `db-migrate` at
  `Exited (0)`. The standard Compose build was downgraded because the local
  Docker builder's registry proxy returned `only one connection allowed`
  while resolving its two already-cached bases; the host-validated `dist` was
  injected into the existing Nginx image for runtime acceptance, producing
  `sha256:671bda982089ffad5c49c81be527d63131a50880dff8ce2c161dead51a93123b`.
- 2026-07-15: removed the Vue-only zero-selection report-merge feedback. Old
  `mkreport.js` emits alerts only for one selected unit or a non-contiguous
  selection; clicking merge with no selected condition reaches its broken
  empty-array path without presenting operator feedback. Vue now keeps that
  visible interaction as a safe silent no-op rather than showing
  `请选择要合并的条件`. Single/non-contiguous feedback and successful grouping
  remain unchanged in the existing shared condition helper. Exact
  implementation commit `317efc30` changed one production line. All 213
  frontend tests, the TypeScript/Vite build, repository harness, and 68
  runtime-doctor checks passed. Authorized `admin/admin` acceptance opened
  `/view101`, loaded report metadata, switched to the empty Conditions tab,
  and clicked merge. The dialog count stayed one, condition rows stayed zero,
  the URL remained `/view101`, no message appeared, and no report request was
  emitted after the initial `getmkqview`; subsequent access-log entries were
  only scheduled `getmsg` polls. Browser warnings/errors were empty and logout
  returned HTTP 200. The clean runtime image was
  `sha256:1678b94c10769ada34071d57b59331c1aa0a54f98e2b2fc76993f09b6cbe47b2`.
- 2026-07-15: restored `item.jade`'s initially inactive child View tabs. Its
  server-rendered tab links and panes have no initial Bootstrap `active` class,
  while Vue previously opened the first child table immediately. Exact commit
  `9716ca50` first initialized an empty tab value for metadata-only
  `/itemview:id`. A follow-up source audit found that `detailView.jade` uses the
  same inactive markup and no legacy script activates a first tab, so the shared
  Vue component now applies the empty initial value to item, detail, and new
  routes. It is 447 lines and gained no DTO, state module, or new component.
  All 214 frontend tests, the TypeScript/Vite production build, repository
  harness, and 68 runtime-doctor checks passed.
  Authorized `admin/admin` acceptance proved `/itemview100` loads only
  `getmain` then `getreaditemview`, starts with `Items` at
  `aria-selected=false` and no visible child table, and reveals only the
  View-derived `Item ID` / `Item Name` / `操作` table after clicking the tab.
  That click emitted zero requests. The first slice proved `/view100/1001` and
  `/new100` still retained the expected View-first `querydatadetail` / `initnew`
  order before the follow-up removed their Vue-only active first tab. At 1280px
  and 390x844, document width matched the viewport; browser
  warnings/errors were empty, logout returned HTTP 200, and the database still
  held 8 orders and 4 order items. Compose remained healthy with `db-migrate`
  at `Exited (0)`. The standard Compose build could not update the local
  Buildx activity file (`operation not permitted`), so the host-validated
  `dist` was injected into the existing Nginx image for runtime acceptance,
  producing
  `sha256:3fef5b753aa96571f252580a25b647baeb54dde35f966b4dc0b4f762bef46e2e`.
- 2026-07-15: completed the shared `detailView.jade` inactive-tab follow-up.
  Source audit found no `detail-tab`, `tabbtn-*`, Bootstrap `tab('show')`, or
  `active` manipulation anywhere in the old application scripts; the new-page
  `beginedit()` callback edits fields only. Exact commit `82dc8eb5` therefore
  removes Vue's first-group default for existing detail and new-object routes,
  reusing the same empty selection already applied to item metadata. The shared
  component shrank to 447 lines. All 214 frontend tests, the TypeScript/Vite
  build, repository harness, and 68 runtime-doctor checks passed. Authorized
  `admin/admin` acceptance proved `/view100/1001`, `/new100`, and
  `/itemview100` all begin with `Items` at `aria-selected=false` and no visible
  child table. Selecting `Items` on existing detail and new-object pages showed
  their View-derived controls/table, retained the URL, and emitted zero
  requests. Initial page loading still ordered `getreaditemview` before
  `querydatadetail` or `initnew`. Desktop 1280px and mobile 390x844 documents
  matched their viewport widths; browser warnings/errors were empty, logout
  returned HTTP 200, and MySQL retained 8 orders and 4 order items. The
  host-built `dist` was injected through the documented Nginx-image downgrade
  because local Buildx still could not write its activity file, producing
  `sha256:77f617bf0d6393b99726172639aa28363415859f4714d788834d129cd5cb3adf`.
- 2026-07-15: restored `setextype.js`'s `limit: 5` BusinessObject suggestion
  behavior. The shared Vue metadata editor now renders at most the first five
  View-derived `inputquery` candidates. This also covers owner `Source`
  collections, whose backend compatibility path can return more than the
  normal paged query's five rows, without changing the protocol response or
  adding a concrete business DTO dependency.
  Exact implementation commit `c43a8354` passed all 214 frontend tests, the
  TypeScript/Vite production build, repository harness, and 68 runtime-doctor
  checks. Its deployed image was
  `sha256:242a1315cae58d1ed78599b27cc8cf540b300b55cd79bc8ca24ae91636a3493a`.
  Authorized `admin/admin` acceptance loaded `/view100/1001` View-first and
  entered its metadata-derived edit mode. A one-request browser interception
  supplied seven protocol candidates without changing MySQL; both 1280px and
  fresh 390px views rendered only Candidate One through Candidate Five plus
  `查找更多`. The desktop menu aligned below the input, while the mobile menu
  correctly flipped above it with a two-pixel gap and document width equal to
  the viewport. Releasing interception restored the real `Ada Capital - 3001`
  candidate. Browser warnings/errors were empty, logout returned HTTP 200,
  Compose was healthy with `db-migrate` at `Exited (0)`, and MySQL retained 8
  orders, 4 order items, and unchanged order 1001 data.
- 2026-07-15: restored the old free-text BusinessObject blur/save behavior.
  `setextype.js` changes the stored id only on `typeahead:select`, while
  `savetext.js` submits the existing element id for any non-empty unmatched
  input and clears it only when the input is empty. Vue therefore no longer
  enables PrimeVue `forceSelection`: unmatched text can remain in the editor
  without clearing the View-derived draft id, and the existing explicit-empty
  branch still emits an empty id. No save builder, protocol DTO, or concrete
  business model was changed.
  Exact implementation commit `d8658f70` passed all 214 frontend tests, the
  TypeScript/Vite build, repository harness, and 68 runtime-doctor checks. Its
  deployed image was
  `sha256:0beb64454641fbd7e9f86c44c4592be81f822f08970e6585b50bffd9ae7e92da`.
  Authorized `admin/admin` acceptance entered the real `/view100/1001` editor.
  `No Such Customer` remained visible after an empty result and blur, while
  the captured Vue save payload retained `customer=3001`; a fresh explicit
  clear produced `customer=""`. The final save requests were answered by a
  page-local transport proxy and did not reach the backend. Browser
  warnings/errors were empty, logout returned HTTP 200, Compose was healthy
  with `db-migrate` at `Exited (0)`, and MySQL retained 8 orders, 4 order
  items, and unchanged order 1001 data.
- 2026-07-15: aligned the Sudoku `GroupViewController` request boundary. The
  old controller posts `/view` for Group metadata and invokes
  `QuerylistdataController` only for each `ListViewType=0` child; it never
  queries list rows for the Group View itself. Vue now loads a Group through
  the existing metadata-only `loadViewById` path before loading its eligible
  child lists, removing the extra `querydata(GroupViewId)` request. List, Map,
  Item, line-chart, child refresh, and success-only transport behavior remain
  unchanged. A focused workflow test guards the metadata-only branch without
  adding component state, a DTO, or a request-specific abstraction. All 215
  frontend tests, the TypeScript/Vite production build, and repository harness
  pass. Exact implementation commit `62f17909` was deployed as frontend image
  `sha256:83fbae3abb39efb5164683cc3445fb96d4af7e84fba57cd3982e0d19a89d94e5`.
  Authorized `admin/admin` acceptance on `/view103` retained all five root
  panels and `Group Orders` at 1280px and 390x844. The captured initial request
  sequence contained `getlistview(104)` and the grouped-child
  `querydata(100,pageSize=5)`, but zero `querydata(104)` calls. Both documents
  matched their viewport widths, browser warnings/errors were empty, logout
  returned HTTP 200, all 68 runtime-doctor checks passed, and MySQL retained 8
  orders, 4 order items, and unchanged order 1001 data.
- 2026-07-15: restored `QuerylistdataController`'s pending timestamp feedback
  for Sudoku List panels. The old controller sets its local `querytime` to
  `更新中..` before every query and replaces it with `FreshTime` only inside the
  successful HTTP callback, so a transport failure leaves the updating text in
  place. Vue now keeps one ViewItem-panel keyed updating record in
  `useSudokuPanels` and reuses the timer's existing identity rule for root and
  Group child panels, including the seeded pair that both use View 100. The
  View component reads that UI state only for active List footers and clears it
  only when the shared loader returns non-null list data; its metadata-preserving
  `{ view, data: null }` transport-failure shell therefore keeps the old pending
  text. Map, line-chart, and Item passive timestamps, concurrent Refresh
  availability, View/data projection, and DTOs remain unchanged. All 218
  frontend tests, the TypeScript/Vite production build, and repository harness
  pass. Exact commits `ca95ce2c`, `8050057a`, and `c2f97b97` respectively add
  the visible state, preserve it for the real `{ view, data: null }` failure
  shell, and isolate panels that share View 100. The final Docker image is
  `sha256:e80d0135d2dc7f565fe37ec34c4e4bee6fa762328dbb7e0d2d00c38ff863defd`.
  Authorized `admin/admin` acceptance on `/view103` paused one real root List
  query: its footer changed from `FreshTime` to `更新中..`, while Group Orders
  retained its own timestamp, then success restored the root time. A second
  mobile query failed in-page with `TypeError: Failed to fetch`; the root
  footer remained `更新中..` and Group Orders again stayed unchanged. Desktop
  1280x800 and mobile 390x844 documents matched their viewport widths with all
  five panels and no browser warnings/errors. Logout returned HTTP 200, all 68
  runtime-doctor checks passed, and MySQL retained 8 orders, 4 order items, and
  unchanged order 1001 data.
- 2026-07-15: connected the imported FoolFrame metadata catalogs to the
  normalized runtime path. Idempotent migrations now project legacy
  `SW_SYS_MODEL` / `SW_SYS_PROPERTY` / `SW_SYS_EMUNVALUE` and `SW_SYS_VIEW` /
  `SW_SYS_VIEW_ITEM` rows without overwriting existing runtime records, and
  infer a missing model id property only from legacy `IdentifyId` metadata.
  Exact commits `d59e6c45`, `5dfedeea`, and `9f1f5005` restore the catalog and
  object-query chain; `129d67d2` sets the backend runtime to `Asia/Shanghai` so
  JDBC DateTime values match the MySQL session instead of drifting by eight
  hours. Replaying all migrations retained 83 legacy / 82 runtime models, 478
  properties, 137 enum values, 118 Views, and 922 legacy / 927 unioned runtime
  ViewItems. The one skipped legacy model is the unreferenced duplicate-name
  `AuthorizedUser` id 182; every model and property referenced by an old View
  resolves. Commit `4aae9ddd` adds a runtime-doctor guard for that invariant.
  Authorized `admin/admin` acceptance on `/view112/1` requested
  `getreaditemview(112)` before `querydatadetail(112,1)`, then rendered all 16
  `User详细` fields, three DateTime controls, and the `Male` enum from View
  metadata. MySQL exposed create time `2026-07-03 12:22:51` and the API emitted
  `2026-07-03 12:22:51.0`; desktop 1280x900 and mobile 390x844 layouts stayed
  within their viewports with no browser errors. All 218 frontend tests, the
  production build, 49 doctor unit tests, repository harness, and 69 Docker
  runtime checks passed after a second migration replay. MySQL retained one
  admin user, 8 orders, 4 order items, and unchanged order 1001 data.
- 2026-07-15: restored readable legacy table sizing for imported Views whose
  `VIEW_ITEM_WIDTH` is zero. Old `view.jade` gives each header its configured
  width and lets the browser's table layout retain intrinsic content width;
  PrimeVue previously compressed all 17 `User列表` columns into the viewport,
  splitting `Male`, dates, and `编辑` vertically. The shared metadata table now
  keeps a positive View width unchanged and otherwise derives one bounded
  96-220px minimum from the View column title, reserves 72px for operations,
  and exposes the existing horizontal scroller. No User DTO, page-specific
  width, or duplicate table was added. Exact commit `645c12be` passed all 219
  frontend tests, the production build, repository harness, and 69 runtime
  checks. Authorized `admin/admin` acceptance loaded `/view113` through
  `getlistview(113)` before `querydata(113)`: the table became 1704px inside
  1198px desktop and 328px mobile viewports, with data columns at 96-132px and
  the action column at 72px. Scrolling reached 506px / 1376px and kept `Male`,
  dates, and `编辑` readable. The row operation preserved
  `getreaditemview(112)` -> `querydatadetail(112,1)` -> `/view112/1`; `新建`
  preserved `getreaditemview(112)` -> `initnew(112)` -> `/new112`. Rechecking
  `/view103` retained all five Sudoku panels and its metadata-only Group request
  boundary. Browser logs were empty, document widths matched 1280px / 390px,
  and MySQL retained one user, 8 orders, 4 order items, and unchanged order
  1001. The host-built dist was injected into image
  `sha256:f8c91559a1c8720a9532f2d18f349bf18a173f014c512066ede2665f271a2bf0`
  because both default and `desktop-linux` Buildx builders could not update
  their activity files.
- 2026-07-15: completed the imported list-to-detail catalog read pass by
  following the old View metadata to runtime data and physical identity
  columns. The old `HandlerQueryData` exposes `ObjectProxy.ID`, while
  `ObjectProxyClass.ID` uses the model id property when present and otherwise
  its internal id; the runtime now preserves explicit and auto `SysId` reads,
  qualifies joined detail identity predicates, reconciles projected id/display
  properties with physical primary keys, and repairs normalized View/Model
  One2Many target columns. The only editable list model lacking both an id
  property and auto id was ApplicationDatabase, so its idempotent migration
  adds a compatible `SysId` instead of introducing page-specific frontend
  logic. Exact commits `6b6b82ce`, `b56521d3`, `2c6f4427`, `6833056e`,
  `c14a7a78`, `08f7dbb4`, `278e5b46`, `160aec00`, and `dad7619f` keep the
  repair in shared model/query/schema paths.
  An authenticated API scan passed all 60 imported list Views. A second scan
  followed the first row from every data-backed list with a default detail
  View and passed 47/47; the previous ApplicationDatabase, View, Property, and
  Model failures now return populated details. Real `admin/admin` row clicks
  also opened those four detail routes plus EventDefinition through their
  metadata-derived object ids, with detail fields/collections rendered and no
  dialog error. The ApplicationDatabase list fit both 1280px and 390x844
  pages without document overflow. Full Maven reactor tests, 50 doctor unit
  tests, repository harness, and all 69 Docker runtime checks passed; Compose
  was healthy with `db-migrate` at `Exited (0)`. The rebuilt backend image is
  `sha256:72885b14ed031835c43f2e5ad7aceda1f709e9ec74696046da741d5d55c25318`.
  Standard Compose Buildx still cannot update its local activity file, so the
  Java-17 container-built JAR was injected through the documented clean image
  fallback. Remaining imported-View work is breadth of interaction coverage:
  classify all 118 old Views by template, then run reversible write acceptance
  only for genuinely editable new/edit/save/child/delete/operation paths.
- 2026-07-15: completed the stable imported-View route matrix from old source
  before following runtime data. `routes/index.js` selects `view`,
  `viewWithChart`, or `Sudoku` only from list View `TempFile`; object, new, and
  schema routes render `detailView.jade` / `item.jade`; the sole type-3 View is
  reached through `Sudoku.jade`'s `includes/Map` item rather than a separate
  top-level template. Commit `173de400` adds a reusable stdlib auditor that
  joins View, template, menu, default-detail, panel-reference, and operation
  metadata, then logs in with the authorized local CAPTCHA and `admin/admin`.
  The generated `docs/migration/foolframe-view-matrix.md` records all 118 rows:
  58 ordinary lists, one `viewWithChart`, one Sudoku, 57 details, and one Map
  panel. Runtime metadata/data checks passed 118/118, pairing
  `getlistview + querydata` for 61 list-like Views and
  `getreaditemview + initnew` for 57 detail Views without persistence. The
  five focused auditor tests and repository harness passed. No template
  dispatch or View/Data binding mismatch was found, so this slice adds no Vue
  branch or DTO. Remaining imported-View work is now the reversible mutation
  matrix for genuinely editable new/edit/save/child/delete/operation paths.
- 2026-07-15: completed real browser new/edit/save acceptance on the imported
  ApplicationDatabase View pair 123/122. FoolFrame `AutoViewFactory` proves the
  56 generated list pairs expose `新建` and `编辑` as result-View navigation,
  while actual persistence remains the shared `detailView.js` new/save path.
  Authorized `admin/admin` acceptance clicked `新建` on `/view123`, entered
  `99150715` / `CDX-NEW` on `/new122`, and sent `savenewobj`; the persisted
  `SysId` was then opened through `/view122/:id` at 390px, edited to
  `CDX-EDIT`, and sent `saveobj`. The observed chain included View-first
  `getlistview/querydata` and `getreaditemview/initnew` before both writes.
  Desktop and mobile document widths matched 1280px and 390px, browser
  runtime/log errors were zero, and screenshots are under
  `artifacts/runs/20260715-legacy-appdb-mutation/`. The dedicated temporary row
  was deleted in cleanup and `DB_AppDB` exactly returned to its original sole
  `App_Id=1, DBNo=01, SysId=1` row. All 69 runtime-doctor checks then passed.
  No page-specific Vue branch or DTO was needed. Remaining mutation work is
  shared child-row add/edit/delete plus the real OrderList model operations.
- 2026-07-15: completed reversible browser acceptance for the shared child
  collection and reconciled the seeded OrderList operation metadata against
  old Web behavior. On `/view102/9915071501`, authorized `admin/admin`
  acceptance opened the inactive `Items` tab, clicked `增加`, loaded candidate
  View 101, queried its rows, selected `CDX-MOVE`, and saved the parent through
  `saveobj`; MySQL then showed item `9915071512` owned by order `9915071501`.
  At 390x844 the same row was staged for deletion and persisted through the
  parent save, leaving only `CDX-KEEP` visible. The old
  `src/Web/public/javascripts/app/querylistdata.js` binds selected-row
  operations only when `ViewId > 0`; OrderList's seeded `删除` and `保存` both
  have `ViewId=0`, so `/view100` correctly rendered two link-colored inert
  names, zero operation buttons, and sent zero `runoperation` requests. The
  observed View-first/data chain included `getreaditemview`, `querydatadetail`,
  `getlistview`, `querydata`, and `saveobj`. Document widths matched their
  1280px and 390px viewports, browser runtime/log errors were zero, and visible
  evidence is under
  `artifacts/runs/20260715-legacy-order-child-mutation/`. Cleanup removed the
  temporary order and both dedicated item ids; the final combined MySQL count
  was zero. All 69 runtime checks, the 118/118 View matrix, and the repository
  harness passed. No source change was needed because the shared Vue path
  already matches the old interaction contract without a business DTO.
- 2026-07-15: restored `setextype.js`'s visible BusinessObject lookup pending
  state. Both old typeahead construction paths render `正在查询....` while
  `inputquery` is unresolved; Vue previously retained the prior candidates and
  showed only PrimeVue's spinner. Exact implementation commit `d8c5c099`
  clears the shared editor's candidate list at request start and reuses its
  existing empty-result slot for `正在查询....`, returning to
  `未找到匹配的选项` after completion. No new component, state module, protocol
  shape, or business DTO was added. All 219 frontend tests, the TypeScript/Vite
  production build, repository harness, and 69 runtime checks passed.
  Authorized `admin/admin` browser acceptance paused real `inputquery`
  requests after View-first `getreaditemview` / `querydatadetail` loading on
  `/view100/1001`. At 1280px and 390px, the deployed overlay displayed the old
  pending text with zero stale suggestions, matched the input's left/right
  bounds, and restored five candidate rows after the intercepted response.
  Document widths matched both viewports, browser runtime/log errors were zero,
  and screenshots are under `artifacts/runs/20260715-lookup-pending/`. MySQL
  retained 8 orders, 4 order items, and unchanged order 1001. Standard Compose
  build remained blocked by the Buildx activity-file permission; the validated
  clean `dist` was injected into Nginx image
  `sha256:70d0de41d699b7e0159f34deee3aa60a6f1356e04faa1207336020bcc8eb67b9`,
  whose `index.html` hash matched the host build.
- 2026-07-15: closed the remaining browser-proof gap for old menu images and
  nested navigation without changing Vue source. The Docker catalog already
  exposes parent menu `Views` (`ViewId=0`) and child `OrderList` (`ViewId=100`),
  but both image columns are normally null. Acceptance temporarily served two
  original 32x32 FoolFrame PNG assets from the running Nginx container and set
  only `SW_APP_AUTH_MENU.AUTH_MENU_IMAGE` for ids 1/2. Authorized
  `admin/admin` browser replay rendered both metadata images at the old 30x30
  `.sw-menuimg` size in the desktop dropdown and mobile Drawer. The stable
  390px Drawer occupied x=0..300 with document width equal to the viewport;
  selecting `OrderList` closed it, navigated to `/view100`, and preserved
  `getlistview` before `querydata`. Desktop document width also matched its
  1280px viewport, browser runtime/log errors were zero, and screenshots are
  under `artifacts/runs/20260715-menu-images/`. Cleanup restored both image
  columns to `NULL` and removed both temporary Nginx files. All 69 runtime
  checks and the repository harness passed afterward. Existing shared
  `LegacyMenuNav.vue` behavior is therefore sufficient; no duplicate menu,
  page-specific branch, or application asset was added.
- 2026-07-15: closed the deferred real-browser proof for a non-empty legacy
  user avatar without changing application source. FoolFrame's `default.jade`
  and `layout.jade` render `UserAvtarUrl`; `soway.css` gives `.avtar` a 50x50
  box and Bootstrap's `.img-circle` makes it circular. Acceptance temporarily
  served original FoolFrame `02.png` (native 32x32) from the running Nginx
  container and set only admin id 1's `SW_AUTH_USER.USER_AVTAR` to that URL.
  Authorized `admin/admin` replay loaded the image on desktop and mobile,
  rendered it at 50x50 with `border-radius: 50%`, and retained `Admin` beside
  it. The desktop avatar occupied x=1165..1215 in a 1280px header; the mobile
  avatar occupied x=14..64 in a 390px header. Both document widths matched
  their viewports and browser runtime/log errors were zero. Screenshots are
  under `artifacts/runs/20260715-user-avatar/`. Cleanup restored
  `USER_AVTAR=NULL` and removed the temporary Nginx file. All 69 runtime checks
  and the repository harness passed afterward. The existing shared auth/shell
  implementation is therefore sufficient; no user DTO, page branch, or
  permanent application asset was added.
- 2026-07-15: completed the missing browser proof for `detailview.js` inline
  child-edit staging without changing application source. The regular Docker
  `Items` collection uses View 101 columns but normally selects existing rows,
  so acceptance temporarily cleared only `fool_sys_view_item` 1204's edit and
  selected View pointers and made item-name column 1102 editable. A dedicated
  order `9915071502` and child `9915071521` avoided touching seeded business
  rows. Authorized `admin/admin` replay entered parent Edit, then row Edit, on
  both desktop and 390px. Row Save changed only the rendered value and emitted
  zero API/write requests while MySQL retained the prior value. Parent Save
  then emitted exactly one `saveobj` whose View-derived `UpdatedList` contained
  the same child id and new item name; MySQL changed only at that boundary.
  Both runs loaded `getreaditemview` before `querydatadetail`, document widths
  matched 1280px / 390px, and browser runtime/log errors were zero. Screenshots
  are under `artifacts/runs/20260715-inline-child-edit/`. Cleanup restored the
  two metadata rows to `1102:0:NULL:NULL:NULL` and `1204:0:101:101:101`, then
  removed the dedicated order and child. All 69 runtime checks and the
  repository harness passed afterward. Existing shared `ViewDetailPanel.vue`
  staging is therefore sufficient; no page branch, concrete DTO, or duplicate
  child editor was added.
- 2026-07-15: completed real-browser proof for `setextype.js`'s Boolean editor
  without changing application source. Imported Model detail View 146 exposes
  `autoSysId` for model 181 as a real type-8 field. Authorized `admin/admin`
  replay loaded `getreaditemview(146)` before `querydatadetail(146,181)` and
  rendered exactly one checked checkbox with an empty editor text wrapper, so
  Vue did not invent a visible `是/否` value. Clicking the checkbox changed it
  only to local unchecked state and emitted zero API or write requests; the
  mobile reload restored checked state from the unchanged database value.
  Desktop and mobile document widths matched their 1280px and 390px
  viewports, browser runtime/log errors were zero, and screenshots are under
  `artifacts/runs/20260715-boolean-editor/`. `SW_SYS_MODEL.MODEL_AUTOID`
  remained `1` before and after the run. All 69 runtime checks and the
  repository harness passed afterward. The existing shared
  `MetadataFieldEditor.vue` checkbox path is therefore sufficient; no fixture,
  concrete DTO, page branch, or duplicate Boolean editor was added.
- 2026-07-15: restored `view.jade`'s create-operation command chrome across the
  56 imported generated list Views. Old list templates render non-select
  operations as a bare Bootstrap `.btn`, whose border is solid but transparent,
  background is transparent, and text is `#333`; Vue instead used a visible
  slate outlined button. The shared `ViewListPanel.vue` now gives only
  metadata create commands a scoped PrimeVue text-button style, leaving Find,
  Statistics, row operations, and navigation state unchanged. Before/after
  computed styles on `/view123` moved from a visible `rgb(71, 85, 105)` border
  and text to a transparent border, transparent background, and
  `rgb(51, 51, 51)` text at both 1280px and 390px. Clicking `新建` still loaded
  `getreaditemview(122)` before `initnew(122)`, navigated to `/new122`, and
  emitted zero write requests. Both document widths matched their viewports,
  browser logs were empty, and screenshots are under
  `artifacts/runs/20260715-create-command/`. All 222 frontend tests, the
  production build, 118/118 View matrix, 69 runtime checks, and repository
  harness passed. No page-specific View id, business DTO, or duplicate toolbar
  was added.

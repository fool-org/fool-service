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
  footer, and resets page state to one when returning to report setup. The old
  markup's inert export buttons remain omitted rather than becoming fake UI.
- 2026-07-12: restored Sudoku panel refresh semantics from old Web
  `includes/List.jade`, `querylistdata.js`, and `groupview.js`. Top-level and
  grouped list panels expose `FreshTime` with a text refresh command; targeted
  refresh merges by ViewId so shared list/chart/item data is retained. Each
  data panel schedules its own stable-key `AutoFreshTime` interval, while View
  changes and component teardown clear all panel timers. The 119-line
  `useSudokuPanels.ts` composable owns loading, merging, and timers, reducing
  `App.vue` from 1153 to 1057 lines. All 132 frontend
  tests, TypeScript/Vite and Compose frontend builds, deployed runtime doctor,
  and repository harness pass. Visible refresh clicking remains in final
  authenticated browser acceptance.
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
  repository harness pass. Visible message replay remains in final
  authenticated browser acceptance.
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
- 2026-07-10: completed the authenticated Vue browser acceptance after explicit
  permission to read the local CAPTCHA and use the Docker `admin/admin`
  account. Desktop and 390x844 checks exercised the default View-first list,
  keyword search, detail selection, metadata new form, report execution, SVG
  chart, message popover, Sudoku list/chart/map/item/group panels, and legacy
  detail/new deep links without console warnings or page-level horizontal
  overflow. The replay exposed one shared-View bug: Sudoku list/chart/item and
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
- 2026-07-10: the Vue shell now renders the signed-in user, 15-second legacy
  message polling, message count/popover, menu notification badges, and logout
  in the topbar instead of separate developer panels. Message targets reuse
  the existing View-first detail/list loaders, non-empty messages remain
  available across later empty polls, and the responsive popover stays inside
  a 390px viewport. The runtime doctor now refreshes its message seed directly
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
- 2026-07-10: backend `inputquery` now accepts the old FoolFrame
  Cloud-Social payload where `soway.inputquery` sends the numeric View id in
  `ViewName`, while nonnumeric business-name `ViewName` remains rejected. The
  Docker runtime doctor proves the shape through the Vue proxy.
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
- 2026-07-09: Vue's `InputQueryRequest` type no longer exposes a
  `viewName` field. The frontend payload builder was already ViewId-driven;
  removing the stale type shortcut keeps lookup callers from reintroducing a
  business-name request path while leaving backend legacy `inputquery`
  protocol compatibility at the DTO boundary.
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
- 2026-07-04: the main Vue View workflow now displays legacy
  `querydata.FreshTime` beside the list paging status, matching FoolFrame's
  list panel update-time surface without adding a custom date parser.
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
  does not attach a `setselect(...)` target. Vue shows those metadata actions
  as disabled buttons, so the rendered page reflects the View metadata without
  inventing a concrete business DTO action or a fake detail View transition.
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
  `/api/v1/message/getnotify` and in the Vue operator console. FoolFrame's
  `DataService.GetNotify` throws `NotImplementedException`, so the migrated
  shell returns an empty `notifies` list until a real legacy count source is
  identified.
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
- A Vue topbar message popover that polls `/api/v1/message/getmsg` every 15
  seconds, uses old Web system-message/view-detail copy, and opens message
  targets through the existing View-first loader
- Vue API types for legacy `getnotify` notification-count payloads
- Vue shell menu badges populated from `/api/v1/message/getnotify`
- Old Web authenticated shell actions for Home, navigation, system messages,
  refresh, and safe logout while server-provided App/user/menu/message text
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

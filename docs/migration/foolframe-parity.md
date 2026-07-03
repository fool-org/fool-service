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
- Full backend Maven tests run inside the Compose network without datasource
  command-line overrides:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false test`
- Smoke routes verified:
  `curl http://localhost:8081/`
  `curl http://localhost:8080/test`
  `curl -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
  `curl -H 'Content-Type: application/json' -d '{"viewId":100}' http://localhost:8080/api/v1/view/getlistview`
  `curl -H 'Content-Type: application/json' -d '{"viewId":100}' http://localhost:8080/api/v1/view/getreaditemview`
  `curl -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list`
  `curl -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":{"orderId":{"values":["1001","1002"]}}}' http://localhost:8080/api/v1/data/query-list`
  `curl -H 'Content-Type: application/json' -d '{"viewId":100,"pageSize":10,"pageIndex":1,"queryFilter":"order_state=\"0\""}' http://localhost:8080/api/v1/data/querydata`
  `curl -H 'Content-Type: application/json' -d '{"viewId":100,"objId":"1001"}' http://localhost:8080/api/v1/data/querydatadetail`
  `curl -H 'Content-Type: application/json' -d '{"viewId":100,"objId":"","IdExp":"$1001"}' http://localhost:8081/api/v1/data/querydatadetail`
  `curl -H 'Content-Type: application/json' -d '{"viewId":100,"objId":"","IdExp":""}' http://localhost:8081/api/v1/data/querydatadetail`
  `curl -H 'Content-Type: application/json' -d '{"ViewId":100,"ParentObjId":"5001"}' http://localhost:8080/api/v1/data/initnew`
  `curl -H 'Content-Type: application/json' -d '{"ViewId":100,"ParentObjId":"5001"}' http://localhost:8081/api/v1/data/initnew`
  `curl -H 'Content-Type: application/json' -d '{"modelId":"102"}' http://localhost:8080/api/v1/data/getenums`
  `curl -H 'Content-Type: application/json' -d '{"viewName":"OrderList","viewItemId":"symbol","text":"BTC"}' http://localhost:8080/api/v1/data/inputquery`
  `curl -H 'Content-Type: application/json' -d '{"saveObj":{"id":"1001","viewID":"100","propertyies":[{"key":"symbol","value":"BTC-USDT"},{"key":"state","value":"0"}]}}' http://localhost:8080/api/v1/data/saveobj`
  `curl -H 'Content-Type: application/json' -d '{"saveObj":{"id":"1001","viewID":"100","propertyies":[{"key":"symbol","value":"BTC-USDT"},{"key":"state","value":"0"}],"itemproperties":[{"key":"items","items":[{"itemId":"2001","isExist":true,"propertyies":[{"key":"itemName","value":"Updated item"}]}],"addedItems":[{"itemId":"2003","isExist":true,"propertyies":[{"key":"itemName","value":"New item"}]}],"delteItems":[{"itemId":"2004","isExist":true,"propertyies":[]}]}]}}}' http://localhost:8080/api/v1/data/saveobj`
  `curl -H 'Content-Type: application/json' -d '{"SaveObj":{"id":"930001","viewID":"100","propertyies":[{"key":"symbol","value":"SOL-USDT"},{"key":"state","value":"0"}],"itemproperties":[]}}' http://localhost:8080/api/v1/data/savenewobj`
  `curl -H 'Content-Type: application/json' -d '{"SaveObj":{"id":"930002","viewID":"100","propertyies":[{"key":"symbol","value":"SOL-USDT"},{"key":"state","value":"0"}],"itemproperties":[]}}' http://localhost:8081/api/v1/data/savenewobj`
  `curl -H 'Content-Type: application/json' -d '{"ViewId":100,"ObjectId":"970731","OperationId":7001}' http://localhost:8080/api/v1/data/runoperation`
  `curl -H 'Content-Type: application/json' -d '{"ViewId":100,"ObjectId":"970732","OperationId":7001}' http://localhost:8081/api/v1/data/runoperation`
  `curl -H 'Content-Type: application/json' -d '{"viewId":100,"currentPage":1,"pageSize":10,"queryFilter":"order_state=\"0\"","reportCols":[{"colName":"Symbol","index":1},{"colName":"State","index":2}]}' http://localhost:8080/api/v1/report/makereport`
  `curl -H 'Content-Type: application/json' -d '{"ViewId":100,"CurrentPage":1,"PageSize":10,"FilterExp":{"Col":{"Name":"order_state"},"CompareOp":{"ID":"1","Name":"等于"},"ValueExp":"0","ValueFmt":"Open"},"ReportCols":[{"ColName":"Symbol","Index":1},{"ColName":"State","Index":2}]}' http://localhost:8080/api/v1/report/makereport`
  `curl -H 'Content-Type: application/json' -d '{"ViewId":100,"CurrentPage":1,"PageSize":10,"FilterExp":{"Col":{"Name":"symbol"},"CompareOp":{"ID":"7","Name":"包含"},"ValueExp":"BTC","ValueFmt":"BTC"},"ReportCols":[{"ColName":"Symbol","Index":1},{"ColName":"State","Index":2}]}' http://localhost:8080/api/v1/report/makereport`
  `curl -H 'Content-Type: application/json' -d '{"ViewId":100,"CurrentPage":1,"PageSize":10,"FilterExp":{"Col":{"ID":"1002"},"CompareOp":{"ID":"7","Name":"包含"},"ValueExp":"BTC","ValueFmt":"BTC"},"ReportCols":[{"ColName":"Symbol","Index":1},{"ColName":"State","Index":2}]}' http://localhost:8080/api/v1/report/makereport`
  `curl -H 'Content-Type: application/json' -d '{"ViewId":100,"CurrentPage":1,"PageSize":10,"FilterExp":{"FirstExp":{"Col":{"Name":"order_state"},"CompareOp":{"ID":"1","Name":"等于"},"ValueExp":"0","ValueFmt":"Open"},"Sequences":[{"BoolOp":{"DBName":"and","ShowName":"与"},"AddedExp":{"Col":{"Name":"symbol"},"CompareOp":{"ID":"7","Name":"包含"},"ValueExp":"BTC","ValueFmt":"BTC"}},{"BoolOp":{"DBName":"or","ShowName":"或"},"AddedExp":{"Col":{"Name":"order_price"},"CompareOp":{"ID":"3","Name":"大于"},"ValueExp":"100","ValueFmt":"100"}}]},"ReportCols":[{"ColName":"Symbol","Index":1},{"ColName":"State","Index":2}]}' http://localhost:8080/api/v1/report/makereport`
  `curl -H 'Content-Type: application/json' -d '{"ViewId":100,"CurrentPage":1,"PageSize":10,"QueryFilter":"order_state=\"0\"","ReportCols":[{"ColName":"Symbol","Index":1},{"ColName":"State","Index":2}]}' http://localhost:8080/api/v1/report/getrpt`
  `curl -H 'Content-Type: application/json' -d '{"ViewId":100}' http://localhost:8080/api/v1/report/getmkqview`
  `curl -H 'Content-Type: application/json' -d '{"ViewId":100}' http://localhost:8080/api/v1/report/mkqview`
  `curl -H 'Content-Type: application/json' -d '{"ViewId":100,"ReportName":"Order Daily","ReportCols":[{"ColName":"Symbol","Index":1}],"FilterExp":{"Col":{"Name":"order_state"},"CompareOp":{"ID":"1","Name":"等于"},"ValueExp":"0","ValueFmt":"Open"}}' http://localhost:8080/api/v1/report/saverpt`
  `curl -H 'Content-Type: application/json' -d '{"ViewId":100}' http://localhost:8081/api/v1/report/getmkqview`
  `curl -H 'Content-Type: application/json' -d '{"ViewId":100,"CurrentPage":1,"PageSize":10,"QueryFilter":"order_state=\"0\"","ReportCols":[{"ColName":"Symbol","Index":1},{"ColName":"State","Index":2}]}' http://localhost:8081/api/v1/report/getrpt`
  `curl -H 'Content-Type: application/json' -d '{"ViewId":100,"ReportName":"Order Daily","ReportCols":[{"ColName":"Symbol","Index":1}],"FilterExp":{"Col":{"Name":"order_state"},"CompareOp":{"ID":"1","Name":"等于"},"ValueExp":"0","ValueFmt":"Open"}}' http://localhost:8081/api/v1/report/saverpt`
  `curl -H 'Content-Type: application/json' -d '{"token":"<login-token>"}' http://localhost:8080/api/v1/message/getmsg`
  `curl -H 'Content-Type: application/json' -d '{"token":"<login-token>"}' http://localhost:8081/api/v1/message/getmsg`
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
  `curl -H 'Content-Type: application/json' -d '{}' http://localhost:8080/api/v1/auth/getcheckcode`
  `curl -H 'Content-Type: application/json' -d '{"key":"<check-key>","code":"<check-code>"}' http://localhost:8080/api/v1/auth/checkcode`
  `curl -H 'Content-Type: application/json' -d '{}' http://localhost:8081/api/v1/auth/getcheckcode`
  `curl -H 'Content-Type: application/json' -d '{"key":"<check-key>","code":"<check-code>"}' http://localhost:8081/api/v1/auth/checkcode`
  `curl -H 'Content-Type: application/json' -d '{"UserId":"admin","PassWord":"admin","DbId":"car_wash","CheckCode":"<check-code>","AppId":"fool-service","AppKey":"fool-service","CheckCodeKey":"<check-key>"}' http://localhost:8080/api/v1/auth/loginv2`
  `curl -H 'Content-Type: application/json' -d '{"UserId":"admin","PassWord":"admin","DbId":"car_wash","CheckCode":"<check-code>","AppId":"fool-service","AppKey":"fool-service","CheckCodeKey":"<check-key>"}' http://localhost:8081/api/v1/auth/loginv2`
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
  `Itemproperties.Items` / `AddedItems` / `DelteItems` payload names. Richer
  field-type widgets remain future work.
- 2026-07-04: added the first Vue select-from-existing child collection path.
  When `querydatadetail.Items[]` marks a group as `selectFromExists`, the
  detail panel can load the configured `selectedView` / `listViewId` through
  legacy `getlistview` + `querydata`, show candidate rows from that View
  metadata, and add the selected row through legacy
  `saveobj.Itemproperties.AddedItems`. Candidate search/pagination remains
  future work.
- 2026-07-04: added metadata-driven enum editors to the default Vue View
  workflow. Detail and child collection fields with `prpType=Enum` and
  `prpModelId` now load options through the migrated `getenums` endpoint and
  render as `<select>` controls while saving the enum DB value through the
  existing `saveobj` / `savenewobj` payloads. Readonly, lookup, and formatted
  field-specific widgets remain future work.
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
  selected order symbol/state through the migrated legacy data APIs. The older
  endpoint panels remain available under `API Tools`.
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
  target object. Richer nested external-model edge cases, WCF/JSON operation
  types, and trigger side effects remain future work.
- 2026-07-03: added legacy `runoperation`
  `BaseOperationType.Assebmly` execution for Java classpath handlers.
  `CommandsType.SetParamValue` and `CommandsType.SetConStrValue` now collect
  method and constructor argument values in command index order, then the
  migrated path instantiates `SW_MODEL_OPERATION_INVOKECLASS` and invokes
  `SW_MODEL_OPERATION_INVOKEMETHOD` with the current `IDynamicData` plus method
  parameters. `SW_MODEL_OPERATION_INVOKEDLL` is intentionally not loaded yet;
  WCF/JSON operation types, richer external-model edge cases, and trigger side
  effects remain future work.
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
  detail data. Auth/context `IdExp` expressions remain future work.
- 2026-07-03: added legacy `querydatadetail` static `IdExp` object-id
  resolution. Blank `objId` requests now map `$...` expressions to the target
  object ID before loading detail data, and the `IdExp` field is preserved from
  controller to service. Auth/context expressions remain future work.
- 2026-07-03: extended legacy `runoperation` to `BaseOperationType.Create`.
  The existing hydrated object and command pipeline now runs through
  `ModelDataService.createData` before returning the legacy operation success
  message. WCF, JSON, richer external-model edge cases, and trigger side
  effects remain future work.
- 2026-07-03: hydrated legacy `SW_SYS_OPERATION` runtime metadata for
  `runoperation`. View-operation loading now carries
  `SW_MODEL_OPERATION_FILTER`, `SW_MODEL_OPERATION_ARGMODEL`,
  `SW_MODEL_OPERATION_ARGFILTER`, `SW_MODEL_OPERATION_INVOKEDLL`,
  `SW_MODEL_OPERATION_INVOKECLASS`, `SW_MODEL_OPERATION_INVOKEMETHOD`, and
  `SW_MODEL_OPERATION_RETURNMODEL` onto the runtime `Operation` object.
  WCF/JSON operation types and richer external-model edge cases remain future
  work.
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
  target property type. Auth/user/app/database context values, owner traversal
  beyond the current object shell, and other command types remain future work.
- 2026-07-03: widened legacy `runoperation` `SetValue` static-value
  conversion. `$...` command expressions now follow the simple scalar
  `GetStaticVlue` branches for Boolean, Byte, Char, DateTime, Int/UInt,
  Long/ULong, Decimal, and Double/Float target properties, while string-like
  property types keep the literal value. Auth/user/app/database context values,
  owner traversal, and other command types remain future work.
- 2026-07-03: extended legacy `runoperation` `SetValue` expression parity.
  FoolFrame-style current-object property expressions such as `.symbol` now
  read from the target object before UPDATE save, and static `$...` values are
  converted for `Int`/`UInt` target properties before assignment.
  Auth/user/app/database context values, owner traversal, and other command
  types remain future work.
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
  `User`, and `App` fields. FoolFrame `CacheStore` app/database session state
  remains future token-context work.
- 2026-07-03: exposed legacy `getapp` at `/api/v1/auth/getapp`
  and in the Vue operator console. It accepts legacy `Token`, validates the
  current token through the existing token service, maps the Docker-seeded
  `SW_APPLICATION` record to legacy `AppInfo`, and returns `Token` plus app
  fields such as `AppName`, `AppVer`, `AppPowerBy`, `AppLogoUrl`,
  `DefaultViewId`, and `AppId`.
- 2026-07-03: exposed legacy `getmain` at `/api/v1/auth/getmain`
  and in the Vue operator console. It accepts the legacy raw token request
  body, returns `Token`, legacy `User`, seeded default `AppInfo`, and
  top-level `TopMenu` items by reusing the migrated user-info, app-info, and
  top-menu paths. Full `loginv2` app-session selection remains future work.
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
| `SCPB01-Soway.Data` | 45 | `fool-common` | 52 | Partial data annotations, legacy tree factory/level-order traversal behavior, dynamic contracts, repeatable legacy column metadata including key-nullability/sql-type/identity flags, table column-prefix metadata, legacy display metadata, legacy entity display metadata, legacy enum-note metadata, legacy dynamic-column metadata, legacy default-owner marker metadata, legacy parent-relation metadata, legacy serial-number attribute metadata, legacy `BasicEnum` value registry without the `enum.txt` debug dump, legacy math-expression operator detection and variable-aware arithmetic evaluation, legacy column generation/encryption enum codes, legacy DB context interface surfaces, legacy controller CRUD/list interface surface, legacy row-backed object interface surface, legacy graphic node/graph helper surface, legacy DS MD5 helper surface, `ObjectWithSubItem<>` marker, legacy `IBusinessObject`/`BusinessObject`/`IItemInterface`/`IItem` parent-assignment and `BO_Id` surface, legacy `PropertyType` codes, legacy `PropertyTypeAdaper` type/default-value mapping, legacy `SubItemList` added/updated/deleted tracking, legacy `BusinesObjectsWithItem` wrapper, and legacy `SerialNoObject` length surface migrated |
| `SCPB02-Soway.DB` | 24 | `fool-dao` | 21 | Partial DAO, mapper, SQL generation, legacy enum code-aware read/write mapping, `Column.noMap` field exclusion, legacy operation type ordinals, SQL operation names, transaction command carrier surface, and `GlobalSqlContext` default/type connection registry migrated |
| `SCPB05-Soway.Model` | 115 | `fool-model` / `fool-view` | 27 / 44 | Partial model/service/sql generator, base-model metadata, enum value metadata for runtime models, DAO rehydration of runtime enum value detail rows, legacy model default-owner metadata, legacy single-row `GetDetail` lookup by model/data ID, legacy simple dynamic row create/update/delete plus simple batch saves, legacy old-id dynamic save lookup, legacy simple dynamic default and collection initialization, legacy DBMaps create/update writes, One2Many child-row create/update/delete-list sync, Many2Many/Recurve relation-table insert/delete-list sync, legacy `savenewobj` new-object creation and owner-collection relation mapping, legacy `SW_SYS_MULTIMAP` DBMaps hydration, multi-column DB map metadata, multi-column DBMaps row-loading for dynamic data, relation-aware collection item query SQL and parent-id bucketing, legacy list-query DBMaps aliases, legacy simple-column and enum row default values, reflective model `ShowProperty` selection, legacy list-query BusinessObject show-property joins, keyword filters, ordering, and count joins, `ColumnAttribute.DefaultValue` metadata and MySQL DDL defaults, `ColumnAttribute.GenerationExp` metadata and MySQL DDL default expressions, legacy model/relation MySQL DDL generation, legacy model type codes, legacy relation type codes, legacy `ConnectionType` codes, legacy load/save type codes, legacy operation-base/operation/command/order type codes, legacy operation command runtime model, legacy model/property trigger type codes, legacy `SW_SYS_CON` connection schema, legacy `SW_SYS_OPERATION`/`SW_SYS_OPERATION_PARAM`/`SW_SYS_COMMANDS` schema with owner columns and Java table mappings, legacy model/property trigger schema with owner columns and app-install Java table mappings, legacy `ItemEditType` and `ViewType` codes for view items, legacy full `SW_SYS_VIEW` schema plus view-file/view/operation-view owner columns and app-install Java table mappings, and legacy default auto-view generation migrated |
| `SWDQ01-Soway.Query` | 46 | `fool-query` | 49 | Partial filter DTO/query components, legacy bool-expression SQL generation for compare/between/in/composite/report filters including `BoolOp` DBName/ShowName tokens, selected-table compare-column `SimpleBoolExpression` SQL/report-parameter/display-string behavior including bracketed identifier normalization, legacy `BoolExpression` wrapper and expression factory create/add orchestration, persisted compare-operation and select-type catalog loading with Spring auto-configuration for runtime consumers, legacy query enum codes, selected column/table state models, selected query table unsupported getter/no-op setter surface, selected column collection duplicate-name/index/read-only/unsupported indexed-setter plus direct insert/remove-at behavior, selected table join-add direction handling, legacy add-table result contract, table/column lookup collection behavior including legacy string-indexer aliases, query instance parameter/result containers, query report definition contract, legacy report-parameter refresh orchestration, `QueryFactory` table lookup normalization plus bidirectional column state-value dictionary surface, legacy table/column/base/paged select SQL builder with named report-parameter binding and bracketed identifier normalization, JDBC paged query executor, and `QueryContext` add/clear/CanJoinSelected/save-unsupported/connection-string-routed/nonpaged-SQL/result orchestration including legacy connection-string result overload with execution-time enum state-value hydration migrated |
| `Soway.Server` | 150 | `fool-view` | 44 | Partial view/data REST surface, legacy list-query column metadata, legacy list-query refresh metadata (`FreshTime`/`AutoFreshTime`), legacy list-query row indexes, paging aliases (`TotalItem`/`TotalPage`/`PageIndex`), `Data` result alias, row `Items`/`ObjValuePair` metadata, typed `ObjValuePair` Date/Time/Enum/BusinessObject formatting, legacy `getlistview` view-id definition API, legacy `getreaditemview` simple read-item API, legacy `getenums` enum-value API, legacy `querydata` view-id/paging/raw-`QueryFilter` API, legacy `querydatadetail` explicit-object, static `IdExp`, and blank-object first-row simple-data API, legacy `initnew` empty-detail initialization, legacy `savenewobj` create-object API, legacy `inputquery` business-object candidate lookup API including existing-object and added-item owner source lists, legacy `saveobj` simple `Propertyies` writeback API, legacy `saveobj` `Itemproperties` update/add/delete request mapping to dynamic collection writeback, legacy `getmkqview`/`mkqview` report model candidate-column API, legacy `makereport`/`getrpt` flat-grid report API with simple and composite `FilterExp` compare mapping, legacy `initapp` app/check-code/db-list surface, legacy `loginv2` check-code/app/db/user login surface, legacy `getapp` default AppInfo API, legacy `getmain` user/default-AppInfo/top-menu shell, legacy `getsubmenu` menu AuthItem API, legacy `getmsg` message polling, and legacy `getnotify` empty notification-count shell, legacy `Type`, `Name`, `ShowType`, `TempFile` empty default, and `DetailViewId`, legacy view-item `ID`, `Name`, `PropertyName` with empty missing-property fallback, `ShowIndex`, `Width`, `Format`, `IsReadOnly`, `EditType`, `PropertyId`, `EditViewId`, `EditExp`, linked-list-view empty defaults, `PropertyType`, `PropertyModel`, and `ViewFile` metadata, ordered list columns/row values, list-view raw `Filter` SQL, global keyword filtering over read-only list items including BusinessObject show properties, default list SQL ordering by the first `ShowIndex` item descending including BusinessObject show properties, and BusinessObject show-property list joins, legacy view operation metadata surface including operation names, IDs, and locations, legacy list row-format handling for `ItemEditType.Format` view items, plus Docker-seeded `OrderList` view/data and collection-write smoke and legacy `SW_SYS_VIEW_FILE`/`SW_SYS_VIEW_ITEM`/`SW_SYS_VIEW_OPERATION`/`SW_SYS_OPERATIONVIEW`/`SW_SYS_OPERATIONVIEW_ITEM` schema migrated |
| `SWUA01-SOWAY.ORM.AUTH` | 5 | `fool-auth` | 37 | Auth API, role/user models, stable MD5-hex password storage, Docker auth base schema, legacy `SW_AUTH_USER` schema and Java table mapping, legacy `Sex` enum codes, legacy `LoginFactory.ToMD5` hash algorithm plus DAO-backed login/register/change-password/update-user behavior, legacy logout token invalidation, legacy `loginv2` user/app/db/check-code wrapper, legacy user-info/app-info/main-info token wrappers, legacy check-code generation/validation, legacy submenu token wrapper, legacy `LoginLog` empty shell, and seeded admin login smoke partially migrated |
| `SWUA02-SOWAY.ORM.AUTH` | 13 | `fool-auth` / `fool-app-manage` | 36 / 48 | Auth/menu concepts, Docker auth menu relation schema, legacy role/company/department/menu/authorized-user scalar table mappings and collection surfaces, legacy company/department/user empty user-list factories, legacy authorized-user detail factory, legacy typed menu factory top/sub menu filtering wired through the `getsubmenu` AuthItem response, legacy `AuthItem` unsupported getter/no-op setter surface, legacy `RoleFactory` empty shell, legacy auth table-prefix metadata, legacy company/department/subdepartment/role-department app-install table mappings, and seeded `OrderList` auth menu smoke partially migrated |
| `SCPB03 -Soway.DB.Manage` | 15 | `fool-db-manage` | 16 | Working database catalog, app/source mappings, connection-string rendering, legacy DES password payloads, `WorkDataBaseFactory` list/create/save/delete behavior, `SqlCon` direct SQL query/nonquery/transaction adapter, and legacy `NotImplementedException` operation surfaces migrated |
| `SCPB07-Soway.AppManage` | 5 | `fool-app-manage` | 22 | App definitions, legacy `AppType` enum codes, store DB model, app lookup, and DAO-backed create-application adapter migrated |
| `SCPB08-Soway.AppManage` | 2 | `fool-app-manage` | 32 | Bootstrap menu/admin-role plan, legacy create-app installer side-effect flow, DAO-backed app creation gateway, creator authorized-user creation, app-system view preparation, root module/model installation records, model default-owner id backfill, menu-record/subitem relation creation, role creation, role-user relation creation, role-menu relation creation, model/relation DDL execution hook, installer-to-model-schema hook wiring, default auto-view hook wiring, App.SysCon/current database DAO routing split, connection-string `DaoService`/`JdbcTemplate` factory with legacy `SqlCon.ToString()` SQL Server string parsing, routed connection reuse boundary, static module-source model expansion with legacy dependency ordering, typed reflective Java module-source model/property discovery including inherited fields, reflective enum value metadata, reflective model `ShowProperty` selection, legacy `SW_SYS_EMUNVALUE` enum metadata persistence for module-source installs, file/jar package scanning, model-reference package traversal, basic collection `One2Many`, self-collection `Recurve`, bidirectional collection `Many2Many`, `ReferToProperyAttrbute`, `MultiTypeAttribute`, legacy `ObjectWithSubItem<>` parent target-property relation generation, legacy `ColumnAttribute` key-group/key-nullability/identity/generation/generation-expression/default-value/format/encryption/no-map/multi-column DBMaps/table column-prefix metadata for reflective module sources, and module-source module/model/property/relation/DBMaps metadata persistence migrated |
| `SCPB09-SOWAY.EVENT` | 20 | `fool-event` | 46 | Event definition/event/message models, legacy enum codes, event query SQL helper including raw DefModel filter SQL and null-filter empty command rendering, model-metadata table-name, explicit object-ID-column, auto-`SYSID` object-ID resolution, case-insensitive object-ID column matching, missing key-column validation, null-`DefModel` object-query empty result, zero-match object-query empty result, matched-object row value capture, message creation, message persistence, generated-message polling plus `Generate` -> `Push` transition through `/api/v1/message/getmsg`, legacy `SW_EVT_DEF` running-definition loading with `EVTDEF_STATE = 0`, direct NotifyUsers/NotifyRoles/NotifyDeps/NotifyCompanies relation loading, role authorized-user expansion, department/company department user expansion, object checks, idempotent event creation, runtime event deal-text from `Operation.Name`, runtime service, recipient expansion logic, All-authorized-user JDBC source, application/database catalog loading, legacy `SqlCon.ToString()` connection-string parsing, scoped system/database JDBC runtime, Spring-managed scheduler lifecycle, application/database polling traversal, and Docker-seeded Order event/message smoke migrated |
| `SWRPT01-Soway.Report` | 31 | `fool-report` | 31 | Report definitions and params, legacy `ReportFactory`/`IReportSource` empty shells, legacy unsupported getter/no-op setter surfaces for `Param`, `ParamInput`, report result/source/audit fields plus `ReportResult`, `ReportResultTable`, and `ReportResultTableColumn`, table/value/static formats, legacy report enum codes, matrix cells, legacy `MatrixHeader` comparison and `StaticCellValue` helper shape, source-row matrix construction, row/column static subtotal calculation cells, nested row/column static subtotal sibling-scope behavior, deep shared-ancestor subtotal scope behavior, legacy cell ordering, `TableHeader` unsupported getter/no-op setter surface, `MatrixResult.Add` unsupported surface, and legacy report grid rendering including flat-row column coordinates wired through `/api/v1/report/makereport` migrated |
| `SCPB07.TESTS` | 2 | module tests | varies | No direct parity target |

Mapping note: the 2026-07-03 `runoperation` slices add persisted
view-operation and command hydration in `fool-view`, wire the legacy
`Soway.Server` CREATE/DELETE/UPDATE surface to migrated simple dynamic create/delete/save
paths, and apply `SetValue` literal, current-object, static-object,
math-expression, time-context, and `Filter` guard commands before the base
operation. Runtime `Operation` metadata now includes legacy filter, argument
model/filter, invoke assembly/class/method, and return-model columns, but those
invoke paths are not executed yet.

## fool-service Module Status

| Module | Java main files | Reactor wired | Notes |
| --- | ---: | --- | --- |
| `fool-common` | 52 | yes | Shared annotations including relation-mapping annotations, legacy tree factory/level-order traversal behavior, dynamic contracts, repeatable legacy column metadata including key-nullability/sql-type/identity flags, table column-prefix metadata, legacy display metadata, legacy entity display metadata, legacy enum-note metadata, legacy dynamic-column metadata, legacy default-owner marker metadata, legacy parent-relation metadata, legacy serial-number attribute metadata, legacy `BasicEnum` value registry without the `enum.txt` debug dump, legacy math-expression operator detection and variable-aware arithmetic evaluation, legacy column generation/generation-expression/default-value/encryption enum codes, legacy DB context interface surfaces, legacy controller CRUD/list interface surface, legacy row-backed object interface surface, legacy graphic node/graph helper surface, legacy DS MD5 helper surface, legacy `ObjectWithSubItem<>` marker base type, legacy `IBusinessObject`/`BusinessObject`/`IItemInterface`/`IItem` parent-assignment and `BO_Id` surface, legacy `PropertyType` codes, legacy `PropertyTypeAdaper` type/default-value mapping, legacy `SubItemList` added/updated/deleted tracking, legacy `BusinesObjectsWithItem` wrapper, and legacy `SerialNoObject` length surface |
| `fool-dao` | 21 | yes | JDBC DAO layer, SQL script generation, legacy enum code-aware read/write mapping, `Column.noMap` field exclusion, legacy operation type ordinals, SQL operation names, transaction command carrier surface, and `GlobalSqlContext` default/type connection registry |
| `fool-auth` | 37 | yes | Auth REST API, Redis token flow including logout token invalidation, legacy `initapp` app/check-code/db-list payload wrapper, legacy `loginv2` check-code/app-key/app-database/user login payload wrapper, legacy `getuserinfo` token/user payload wrapper, legacy `getapp` default AppInfo payload wrapper, legacy `getmain` raw-token user/default-AppInfo/top-menu shell, legacy check-code generation/validation, legacy `getsubmenu` token/menu AuthItem payload wrapper, role/menu models, stable MD5-hex password storage, Docker schema and Java table mapping for legacy `SW_AUTH_USER`, legacy `Sex` enum codes, legacy `LoginFactory.ToMD5` hash algorithm plus DAO-backed login/register/change-password/update-user behavior, legacy `LoginLog` empty shell, legacy `SW_APP_AUTH_ROLE`, `SW_APP_AUTH_COMPANY`, `SW_APP_AUTH_DEPARTMENT`, `SW_APP_AUTH_MENU`, and `SW_APP_AUTH_USER` scalar table mappings and collection surfaces, legacy company/department/user empty user-list factories, legacy authorized-user detail factory, legacy typed menu factory top/sub menu filtering, legacy `AuthItem` unsupported getter/no-op setter surface, legacy `RoleFactory` empty shell, modern `auth_user`, `auth_role`, `auth_item`, `auth_user_role`, and `auth_role_auth`, plus seeded admin auth smoke |
| `fool-dto` | 3 | yes | Common request/response wrappers |
| `fool-error-handler` | 9 | yes | Exception handling/autoconfiguration |
| `fool-log` | 11 | yes | Request logging filter/interceptor; response capture no longer creates a Spring bean cycle |
| `fool-model` | 27 | yes | Dynamic model/relation metadata, base-model metadata, runtime enum value metadata, reflective model `ShowProperty` metadata, DAO rehydration of runtime enum value detail rows, legacy single-row `GetDetail` lookup by model/data ID, legacy simple dynamic row create/update/delete plus simple batch saves, legacy old-id dynamic save lookup, legacy simple dynamic default and collection initialization, legacy DBMaps create/update writes, legacy One2Many child-row create/update/delete-list sync, legacy Many2Many/Recurve relation-table create/update/delete-list sync, legacy `SW_SYS_MULTIMAP` DBMaps hydration, multi-column DB map metadata, `generation_expression`/`default_value` property metadata, service layer with DBMaps row-loading for dynamic business-object values, relation-aware collection item SQL for One2Many/Many2Many/Recurve parent bucketing, legacy list-query DBMaps aliases, joined BusinessObject show-property aliases, simple-column/enum row default values, legacy model type codes, legacy relation type codes, legacy `ConnectionType` codes, legacy load/save type codes, legacy operation-base/operation/command/order type codes, legacy operation command runtime model, legacy model/property trigger type codes, legacy `SW_SYS_CON` connection schema, legacy `SW_SYS_OPERATION`/`SW_SYS_OPERATION_PARAM`/`SW_SYS_COMMANDS` schema with owner columns and app-install Java table mappings, legacy model/property trigger schema with owner columns and app-install Java table mappings, and legacy MySQL DDL generator for model property columns, raw default expressions, literal default values, no-map skips, multi-column DB map columns, primary/unique keys, and One2Many/Many2Many/Recurve relation DDL |
| `fool-query` | 49 | yes | Query filters and request DTOs, including legacy-style compare/between/in/composite/report SQL generation with ordered parameters, report-parameter reuse, and `BoolOp` DBName/ShowName tokens, selected-table compare-column `SimpleBoolExpression` SQL/report-parameter/display-string behavior including bracketed identifier normalization, legacy `BoolExpression` wrapper and `BoolExpressionFactory` create/add orchestration with owner-preserving report parameters, JDBC loading for legacy `SE_COMPARETYPE` compare-operation catalogs and `SE_SELECTEDTYPE` select-type catalogs plus auto-configuration for runtime consumers, legacy query enum codes, selected output-column/table DTOs, selected query table unsupported getter/no-op setter surface, selected column collection duplicate-name/index/read-only/unsupported indexed-setter/direct insert/remove-at behavior, selected table join condition direction handling, non-throwing legacy add-table result contract, legacy-style table/column lookup collections including string-indexer aliases, query instance parameter/result containers including the legacy `QueryResult.GetData` current-page data surface, query report definition contract for SQL, output columns, query parameters, report name, and report number, `QueryInsFac` report-parameter refresh orchestration, `QueryFactory` table lookup normalization, column lookup default, and bidirectional state-value dictionary mapping, a legacy-style table/column/base/paged select SQL builder with join, enum CASE, ROW_NUMBER, WHERE text, GROUP BY, page projection, filter/page parameter payload support, named report-parameter binding for JDBC positional args, and bracketed identifier normalization, a JDBC paged query executor that maps count/page rows into `QueryResult`, and `QueryContext` ownership of add/clear/CanJoinSelected/connection-string-routed/save-unsupported/getSql/getResult orchestration including the legacy connection-string result overload plus legacy enum state-value hydration before SQL generation/execution |
| `fool-view` | 44 | yes | View/data controllers and adapters, legacy list-query `Cols`, `FreshTime`, `AutoFreshTime`, row-index metadata, paging aliases (`TotalItem`/`TotalPage`/`PageIndex`), `Data` result alias, row `Items`/`ObjValuePair` metadata, typed `ObjValuePair` Date/Time/Enum/BusinessObject formatting, legacy `getlistview` view-id definition API, legacy `getreaditemview` simple read-item API, legacy `getenums` enum-value API, legacy `querydata` view-id/paging/raw-`QueryFilter` API, legacy `querydatadetail` explicit-object, static `IdExp`, and blank-object first-row simple-data API, legacy `initnew` empty-detail initialization, legacy `savenewobj` create-object API, legacy `inputquery` business-object candidate lookup API, legacy `saveobj` simple `Propertyies` writeback API, legacy `saveobj` `Itemproperties` update/add/delete request mapping to dynamic collection writeback, legacy `getmkqview`/`mkqview` report model candidate-column API, and legacy `makereport`/`getrpt` flat-grid report API with simple and composite `FilterExp` compare mapping, legacy top-level `Type`/`Name`/`ShowType` aliases and `TempFile` empty default, legacy default `DetailViewId`, `ViewItem.ID`, `Name`, `PropertyName` with empty missing-property fallback, and `ViewItem.ShowIndex` ordering metadata, `ViewItem.Width`, `Format`, `IsReadOnly`, `EditType`, `PropertyId`, `EditViewId`, `EditExp`, linked-list-view empty defaults, `PropertyType`, `PropertyModel`, and `ViewFile` column metadata, raw view `Filter` SQL, global keyword filtering over read-only list items including BusinessObject show properties, default descending SQL order by the first `ShowIndex` item including BusinessObject show properties, and BusinessObject show-property list joins, legacy view operation metadata including operation names, IDs, and locations in view definitions, legacy `ViewType` codes, legacy list row-format behavior for `ItemEditType.Format` view items, legacy default auto-view factory behavior, Docker-seeded `OrderList` view/data and collection-write smoke, and legacy `SW_SYS_VIEW_FILE`/`SW_SYS_VIEW_ITEM`/`SW_SYS_VIEW_OPERATION`/`SW_SYS_OPERATIONVIEW`/`SW_SYS_OPERATIONVIEW_ITEM` schema |
| `fool-app-manage` | 48 | yes | Application/store DB model, legacy `AppType` enum codes, app-key lookup, legacy bootstrap defaults, create-app installer side-effect orchestration, DAO-backed create-application gateway, creator authorized-user model/creation, app-system view preparation, legacy root module/model installation records, menu-record/subitem relation creation, role creation, role-user relation creation, role-menu relation creation, company/department/subdepartment/role-department relation mappings including auth table-prefix metadata, migrated model/relation DDL execution hook, connection-aware metadata/DDL DAO routing with legacy `ConnectionType` codes, connection-string `DaoService`/`JdbcTemplate` factory including legacy `SqlCon.ToString()` SQL Server string parsing, cached single-connection `DaoService` boundary per routed legacy SqlCon, installer wiring for configured model schemas or static/reflective module sources, static module-source model dependency ordering, typed reflective Java module-source model/property discovery including inherited fields, reflective enum value metadata, reflective model `ShowProperty` selection, legacy `SW_SYS_EMUNVALUE` enum metadata persistence for module-source installs, file/jar package scanning, model-reference package traversal, basic collection `One2Many`, self-collection `Recurve`, bidirectional collection `Many2Many`, `ReferToProperyAttrbute`, `MultiTypeAttribute`, legacy `ObjectWithSubItem<>` parent target-property relation generation, legacy `ColumnAttribute` key-group/key-nullability/identity/generation/generation-expression/default-value/format/encryption/no-map/multi-column DBMaps/table column-prefix metadata for reflective module sources, module-source module/model/property/relation/DBMaps metadata persistence, legacy view-file/view/operation-view app-install table mappings, and default auto-view persistence |
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

- Auth login/profile/menu/logout calls with a Docker-seeded admin smoke account
- Legacy `initapp` loading with legacy `AppId` / `AppKey`, app metadata,
  check code, and store database list
- Legacy `loginv2` loading with legacy `UserId`, `PassWord`, `DbId`,
  `CheckCode`, `AppId`, `AppKey`, and `CheckCodeKey`
- Legacy `getuserinfo` user/token payload loading with legacy `Token` alias
- Legacy `getapp` default application metadata loading with legacy `Token`
  alias
- Legacy `getmain` raw-token user/default-AppInfo/top-menu loading
- Legacy `getcheckcode` / `checkcode` captcha loading and validation controls
- Legacy `getsubmenu` top/child menu loading with legacy `ParentAuthCode`
- Auth menu loading
- Vue API type for common token-only auth requests such as logout
- Vue API types for legacy check-code result and validation payloads
- Vue API types for legacy `getsubmenu` AuthItem result payloads
- View definition lookup through `/api/v1/view/get-view` and legacy
  `/api/v1/view/getlistview`
- Data query through `/api/v1/data/query-list`
- Vue API types for view operation names, IDs, operation locations, view operations, list row format, list-query columns, legacy view types/names/show types/temp files, default detail view IDs, and refresh metadata
- Vue API types for legacy list-query row indexes, paging aliases, `Data` result alias, and row `Items`/`ObjValuePair` metadata
- Vue API types for legacy `getenums` enum-value request/result payloads
- A Vue enum-value panel that calls legacy `/api/v1/data/getenums` and renders
  returned enum values
- Vue API types for legacy `getlistview` view-id payloads
- A Vue legacy list-view lookup control that calls `/api/v1/view/getlistview`
  by view ID and reuses the view-definition summary display
- Vue API types for legacy `getreaditemview` read-item payloads
- A Vue read-item view panel that calls `/api/v1/view/getreaditemview` by view
  ID and renders returned field metadata
- Vue API types for legacy `querydata` request payloads
- A Vue legacy query-data panel that calls `/api/v1/data/querydata` by view ID
  with raw `QueryFilter` text and reuses the result-set table display
- Vue API types for legacy `querydatadetail` request/result payloads
- A Vue detail-data panel that calls legacy `/api/v1/data/querydatadetail` and
  renders returned `SimpleData` rows
- Vue API types for legacy `querydatadetail` collection `Items` groups and child
  `DataItem` rows
- The default Vue `OrderList` detail panel renders persisted `Order Items`
  returned by backend `querydatadetail`
- Vue API types for legacy `initnew` request/result payloads
- A Vue init-new-object panel that calls legacy `/api/v1/data/initnew` and
  renders the empty editable `SimpleData` rows
- Vue API types for legacy `inputquery` request/result payloads
- A Vue input candidate lookup panel that calls legacy `/api/v1/data/inputquery`
  with view item, text, existing-object ID, added-item owner ID, and added-item
  mode fields
- Vue API types for legacy `saveobj` request payloads
- A Vue save-object panel that calls legacy `/api/v1/data/saveobj` with view ID,
  object ID, simple `Propertyies`, and `Itemproperties` JSON payloads
- Vue API types for legacy `savenewobj` request payloads
- A Vue save-new-object panel that calls legacy `/api/v1/data/savenewobj` with
  view ID, object ID, simple `Propertyies`, and optional owner collection fields
- Vue API types for legacy `runoperation` request/result payloads
- A Vue run-operation panel that calls legacy `/api/v1/data/runoperation` with
  view ID, operation ID, and object ID
- Vue API types for legacy `makereport` request/result payloads
- A Vue report-grid panel that calls legacy `/api/v1/report/makereport` with
  view ID, page, `QueryFilter`, and report column JSON payloads, then renders
  returned report cells
- The same Vue report-grid panel can call legacy `/api/v1/report/getrpt`
  with the same `MakeReportOption` payload
- Vue API types for legacy `getmkqview` report model candidate payloads
- A Vue report-column panel that calls `/api/v1/report/getmkqview` by view ID
  and renders candidate columns, compare/select catalogs, and enum states
- Vue report-definition save payload support for legacy `ReportName`
- A Vue save-report-definition panel that calls `/api/v1/report/saverpt` with
  view ID, report columns, `QueryFilter`, and report name
- Vue API types for legacy `getmsg` generated-message polling
- A Vue messages panel that calls `/api/v1/message/getmsg` with the current
  token and renders the pushed message metadata
- Vue API types for legacy `getnotify` notification-count payloads
- A Vue notify-count panel that calls `/api/v1/message/getnotify` with the
  current token and renders returned auth/count rows
- Vue API types for legacy view-item `ID`, `Name`, `PropertyName`, `ShowIndex`, `Width`, `Format`, `IsReadOnly`, `EditType`, `PropertyId`, `EditViewId`, `EditExp`, linked-list-view defaults, `PropertyType`, and `PropertyModel` metadata
- Vue API types for legacy view-item `ViewFile` metadata
- Structured visible equality/range filters that emit Spring `QueryValue` payloads, plus advanced JSON filters
- Legacy-style keyword filtering over read-only list columns
- Legacy-style keyword filtering over BusinessObject show-property list columns
- Legacy-style default ordering over BusinessObject show-property list columns
- Seeded `OrderList` metadata and rows for Docker smoke coverage
- A default Vue View workflow that loads View metadata first, queries rows by
  view name, renders columns from `tableColumn`, renders detail fields from
  `querydatadetail.simpleData`, initializes new rows with `initnew`, creates
  them with `savenewobj`, and saves existing rows with `saveobj`
- A default Vue child collection workflow that renders from
  `querydatadetail.Items[].properties` and sends legacy
  `saveobj.Itemproperties.Items`, `AddedItems`, and `DelteItems` payloads
- A default Vue select-from-existing child collection path that loads
  configured candidate Views and adds selected rows through legacy
  `saveobj.Itemproperties.AddedItems`
- Metadata-driven Vue enum editors that call legacy `getenums` for detail and
  child collection fields with enum model metadata
- Metadata-driven Vue readonly editors that display locked detail/child fields
  and omit them from legacy save payloads
- Legacy `EditType.Format` row classes applied from `ListDataItem.rowFmt`
- Metadata-driven Vue lookup editors for BusinessObject fields using legacy
  `inputquery`
- A Vue backend smoke panel that calls `/test` and renders the Docker seed rows
- A migration-map strip showing current server module mapping
- Vite and Nginx proxies for `/api/*` and `/test` to the backend service

## Remaining Migration Work

- Complete concrete `AppInstallGateway` side-effect parity beyond application creation, creator authorized-user creation, app-system view preparation, root module/model installation records, menu/role record creation, menu/role relation creation, the model/relation DDL execution hook, configured model/static/reflective module-source schema wiring, static module-source dependency ordering, connection-aware metadata/DDL routing, connection-string `DaoService`/`JdbcTemplate` factory including legacy `SqlCon.ToString()` SQL Server string parsing and routed connection reuse, module-source module/model/property/relation metadata persistence, default auto-view hook wiring, reflective model-reference package traversal, basic reflective collection `One2Many` relation generation, self-collection `Recurve` relation generation, bidirectional collection `Many2Many` relation generation, `ReferToProperyAttrbute`, `MultiTypeAttribute`, legacy `ObjectWithSubItem<>` parent target-property relation generation, legacy `ColumnAttribute` key-group/key-nullability/identity/generation/generation-expression/default-value/format/encryption/no-map/multi-column DBMaps/table column-prefix metadata, and legacy `SW_SYS_EMUNVALUE` enum metadata persistence: transaction boundaries for routed connection strings, deeper DBMaps query/runtime behavior beyond same-row dynamic loading and list-query alias mapping, and arbitrary classpath dependency enumeration beyond model-type references remain.
- Complete remaining `SCPB05-Soway.Model` runtime data mutations beyond the already-covered simple dynamic row create/update/delete, simple batch saves, old-id dynamic save lookup, DBMaps create/update writes, One2Many child-row create/update/delete-list sync, Many2Many/Recurve relation-table insert/delete-list sync, legacy `saveobj` `Itemproperties` request mapping, legacy `savenewobj` new-object/owner-relation request mapping, model-trigger metadata hydration, and CREATE/DELETE/UPDATE `runoperation` with legacy operation metadata hydration plus `SetValue`, `Filter`, direct/collection property-model method, list-method command slices, Java classpath assembly reflection with constructor/parameter command values, and external-model create/update/delete/detail-fallback execution: richer collection state parity, remaining operation command types, WCF/JSON/external-model edge cases, operation-trigger side-effect execution, and routed-connection transaction behavior remain.
- Complete remaining `SWDQ01-Soway.Query` behavior: saved-query/report execution surfaces and richer query-to-view integration beyond the current compare/between/in/composite/report filter SQL, selected-table compare-column simple bool-expression path, bool-expression factory create/add orchestration, compare-operation/select-type catalogs, selected column/table state and collection surfaces, selected table join-add/result contract, query instance parameter/result container including the `QueryResult.GetData` current-page data surface, query report definition contract, report-parameter refresh orchestration and named binding, `QueryFactory` table/column/state-value dictionary surface, base/paged SQL builder, JDBC paged executor, `QueryContext` add/clear/CanJoinSelected/connection-string-routed/getSql/getResult orchestration, enum state-value hydration surfaces, and richer input-query expression evaluation beyond added-item `#.` owner source lists.
- Complete remaining `SCPB09-SOWAY.EVENT` runtime behavior: fuller dynamic object-query parity beyond the current raw DefModel filter SQL, null-model empty result, zero-row empty result, table, explicit/auto-`SYSID` ID-column, case-insensitive ID-column matching, missing key-column validation, and matched-row value capture.
- Complete remaining `SWRPT01-Soway.Report` behavior: table source adapters, saved report metadata persistence, saved-report execution, and export integration around the rendered report grid. The flat `makereport` REST path now reuses migrated list-query data and `ReportGridRenderer`, with simple and composite `FilterExp` request mapping; the report model candidate-column lookup is exposed through `getmkqview`/`mkqview` with compare/select catalogs and enum states; the legacy `saverpt` no-op success surface is exposed; Matrix static subtotal parity covers nested row/column sibling scope, deep shared-ancestor scope, flat-row grid column coordinates, and the legacy `MatrixHeader`/`StaticCellValue` helper surface; `ReportFactory`/`IReportSource` empty shell parity is covered, but broader report persistence/execution/export wiring remains open.
- Add complete database schema/migration scripts for `car_wash`; Compose currently seeds smoke/order, app-management base tables plus app/store-db relation table, DB-management base tables, event/message base tables plus one Docker `Order` event definition and direct admin notification relation, legacy `SW_AUTH_USER`, modern Vue auth base tables and auth relation tables with admin/menu smoke data, the legacy All-authorized-user source table, direct event NotifyUsers/NotifyRoles/NotifyDeps/NotifyCompanies relation tables, the minimum auth graph tables needed for role/department/company recipient expansion, auth menu/role tables with menu-subitem, role-user, and role-menu relation tables, legacy `SW_SYS_VIEW`/`SW_SYS_VIEW_FILE`/`SW_SYS_VIEW_ITEM`/`SW_SYS_VIEW_OPERATION`/`SW_SYS_OPERATIONVIEW`/`SW_SYS_OPERATIONVIEW_ITEM` schema including collection owner columns, the legacy `SW_SYS_MODULE` table for root and module-source module installation records, the legacy `SW_SYS_MODEL` table for root and module-source model shell records, legacy `SW_SYS_CON` connection schema, legacy `SW_SYS_EMUNVALUE` enum metadata, runtime `fool_sys_model_enum` enum metadata, `SW_SYS_PROPERTY`, `SW_SYS_MULTIMAP`, `SW_SYS_RELATION`, `SW_SYS_OPERATION`, `SW_SYS_OPERATION_PARAM`, `SW_SYS_COMMANDS`, `SW_SYS_MODEL_TRIGGER`, `SW_SYS_MODEL_TRIGGER_COMMANDS`, `SW_SYS_PROPERTY_TRIGGER`, `SW_SYS_PROPERTY_TRIGGER_COMMANDS`, model/property metadata tables needed for event object table and ID-column resolution plus legacy DDL type/key/generation-expression/default-value metadata, legacy `SE_COMPARETYPE` and `SE_SELECTEDTYPE` query catalogs, and `fool_sys_view`/`fool_sys_view_item` metadata including `auto_fresh_interval`, `edit_type`, `show_index`, and `width` for the Vue `OrderList` smoke workflow.

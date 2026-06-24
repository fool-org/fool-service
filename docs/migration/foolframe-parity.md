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
- Smoke routes verified:
  `curl http://localhost:8081/`
  `curl http://localhost:8080/test`
  `curl -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
  `curl -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list`
  `curl -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":{"orderId":{"values":["1001","1002"]}}}' http://localhost:8080/api/v1/data/query-list`
- Vue frontend local and Compose builds pass:
  `cd frontend && npm test && npm run build`
  `docker compose build frontend`
- Project-local Maven resolution uses `.mvn/settings.xml` to mirror Central through Aliyun for repeatable Docker builds on this network.
- Module profile-specific `application-dev.yml` and `application-test.yml`
  files use Spring Boot 2.4+ `spring.config.activate.on-profile`
  activation syntax instead of the deprecated `spring.profiles` document
  selector.

## Server Source Mapping

| FoolFrame project | C# files | fool-service target | Java main files | Status |
| --- | ---: | --- | ---: | --- |
| `SCPB01-Soway.Data` | 45 | `fool-common` | 25 | Partial data annotations/tree/dynamic contracts, repeatable legacy column metadata, table column-prefix metadata, legacy column generation/encryption enums, `ObjectWithSubItem<>` marker, and `PropertyType` enum migrated |
| `SCPB02-Soway.DB` | 24 | `fool-dao` | 17 | Partial DAO, mapper, SQL generation migrated |
| `SCPB05-Soway.Model` | 115 | `fool-model` / `fool-view` | 18 / 26 | Partial model/service/sql generator, base-model metadata, enum value metadata for runtime models, DAO rehydration of runtime enum value detail rows, multi-column DB map metadata, multi-column DBMaps row-loading for dynamic data, legacy simple-column and enum row default values, `ColumnAttribute.DefaultValue` metadata and MySQL DDL defaults, `ColumnAttribute.GenerationExp` metadata and MySQL DDL default expressions, legacy model/relation MySQL DDL generation, and legacy default auto-view generation migrated |
| `SWDQ01-Soway.Query` | 46 | `fool-query` | 48 | Partial filter DTO/query components, legacy bool-expression SQL generation for compare/between/in/composite/report filters, selected-table compare-column `SimpleBoolExpression` SQL/report-parameter behavior, legacy `BoolExpression` wrapper and expression factory create/add orchestration, persisted compare-operation and select-type catalog loading, selected column/table state models, selected query table unsupported getter/no-op setter surface, selected column collection duplicate-name/index/read-only/unsupported indexed-setter behavior, selected table join-add direction handling, legacy add-table result contract, table/column lookup collection behavior, query instance parameter/result containers, query report definition contract, legacy report-parameter refresh orchestration, `QueryFactory` table/column/state-value dictionary surface, legacy table/column/base/paged select SQL builder with named report-parameter binding, JDBC paged query executor, and `QueryContext` add/clear/save-unsupported/nonpaged-SQL/result orchestration with execution-time enum state-value hydration migrated |
| `Soway.Server` | 150 | `fool-view` | 26 | Partial view/data REST surface plus Docker-seeded `OrderList` view/data smoke migrated |
| `SWUA01-SOWAY.ORM.AUTH` | 5 | `fool-auth` | 23 | Auth API and role/user models partially migrated |
| `SWUA02-SOWAY.ORM.AUTH` | 13 | `fool-auth` | 23 | Auth/menu concepts partially migrated |
| `SCPB03 -Soway.DB.Manage` | 15 | `fool-db-manage` | 16 | Working database catalog, app/source mappings, connection-string rendering, legacy DES password payloads, `WorkDataBaseFactory` list/create/save/delete behavior, `SqlCon` direct SQL query/nonquery/transaction adapter, and legacy `NotImplementedException` operation surfaces migrated |
| `SCPB07-Soway.AppManage` | 5 | `fool-app-manage` | 22 | App definitions, store DB model, app lookup, and DAO-backed create-application adapter migrated |
| `SCPB08-Soway.AppManage` | 2 | `fool-app-manage` | 31 | Bootstrap menu/admin-role plan, legacy create-app installer side-effect flow, DAO-backed app creation gateway, creator authorized-user creation, app-system view preparation, root module/model installation records, menu-record/subitem relation creation, role creation, role-user relation creation, role-menu relation creation, model/relation DDL execution hook, installer-to-model-schema hook wiring, default auto-view hook wiring, App.SysCon/current database DAO routing split, connection-string `DaoService`/`JdbcTemplate` factory, static module-source model expansion with legacy dependency ordering, typed reflective Java module-source model/property discovery including inherited fields, reflective enum value metadata, legacy `SW_SYS_EMUNVALUE` enum metadata persistence for module-source installs, file/jar package scanning, model-reference package traversal, basic collection `One2Many`, self-collection `Recurve`, bidirectional collection `Many2Many`, `ReferToProperyAttrbute`, `MultiTypeAttribute`, legacy `ObjectWithSubItem<>` parent target-property relation generation, legacy `ColumnAttribute` key-group/generation/generation-expression/default-value/format/encryption/no-map/multi-column DBMaps/table column-prefix metadata for reflective module sources, and module-source module/model/property/relation metadata persistence migrated |
| `SCPB09-SOWAY.EVENT` | 20 | `fool-event` | 45 | Event definition/event/message models, legacy enum ordinals, event query SQL helper, model-metadata table-name, explicit object-ID-column, auto-`SYSID` object-ID resolution, missing key-column validation, null-`DefModel` object-query empty result, matched-object row value capture, message creation, message persistence, legacy `SW_EVT_DEF` running-definition loading with `EVTDEF_STATE = 0`, direct NotifyUsers/NotifyRoles/NotifyDeps/NotifyCompanies relation loading, role authorized-user expansion, department/company department user expansion, object checks, idempotent event creation, runtime service, recipient expansion logic, All-authorized-user JDBC source, application/database catalog loading, scoped system/database JDBC runtime, Spring-managed scheduler lifecycle, and application/database polling traversal migrated |
| `SWRPT01-Soway.Report` | 31 | `fool-report` | 26 | Report definitions, params, result tables, table/value/static formats, legacy report enum ordinals, matrix cells, source-row matrix construction, row/column static subtotal calculation cells, nested row/column static subtotal sibling-scope behavior, and legacy report grid rendering migrated |
| `SCPB07.TESTS` | 2 | module tests | varies | No direct parity target |

## fool-service Module Status

| Module | Java main files | Reactor wired | Notes |
| --- | ---: | --- | --- |
| `fool-common` | 25 | yes | Shared annotations including relation-mapping annotations, tree/dynamic contracts, repeatable legacy column metadata, table column-prefix metadata, legacy column generation/generation-expression/default-value/encryption enums, legacy `ObjectWithSubItem<>` marker base type, and `PropertyType` enum |
| `fool-dao` | 17 | yes | JDBC DAO layer and SQL script generation |
| `fool-auth` | 23 | yes | Auth REST API, Redis token flow, role/menu models |
| `fool-dto` | 3 | yes | Common request/response wrappers |
| `fool-error-handler` | 9 | yes | Exception handling/autoconfiguration |
| `fool-log` | 11 | yes | Request logging filter/interceptor; response capture no longer creates a Spring bean cycle |
| `fool-model` | 18 | yes | Dynamic model/relation metadata, base-model metadata, runtime enum value metadata, DAO rehydration of runtime enum value detail rows, multi-column DB map metadata, `generation_expression`/`default_value` property metadata, service layer with DBMaps row-loading for dynamic business-object values and simple-column/enum row default values, and legacy MySQL DDL generator for model property columns, raw default expressions, literal default values, no-map skips, multi-column DB map columns, primary/unique keys, and One2Many/Many2Many/Recurve relation DDL |
| `fool-query` | 48 | yes | Query filters and request DTOs, including legacy-style compare/between/in/composite/report SQL generation with ordered parameters and report-parameter reuse, selected-table compare-column `SimpleBoolExpression` SQL/report-parameter behavior, legacy `BoolExpression` wrapper and `BoolExpressionFactory` create/add orchestration with owner-preserving report parameters, JDBC loading for legacy `SE_COMPARETYPE` compare-operation catalogs and `SE_SELECTEDTYPE` select-type catalogs, selected output-column/table DTOs, selected query table unsupported getter/no-op setter surface, selected column collection duplicate-name/index/read-only/unsupported indexed-setter behavior, selected table join condition direction handling, non-throwing legacy add-table result contract, legacy-style table/column lookup collections, query instance parameter/result containers, query report definition contract for SQL, output columns, query parameters, report name, and report number, `QueryInsFac` report-parameter refresh orchestration, `QueryFactory` table lookup, column lookup default, and state-value dictionary mapping from display state to database state, a legacy-style table/column/base/paged select SQL builder with join, enum CASE, ROW_NUMBER, WHERE text, GROUP BY, page projection, filter/page parameter payload support, and named report-parameter binding for JDBC positional args, a JDBC paged query executor that maps count/page rows into `QueryResult`, and `QueryContext` ownership of add/clear/save-unsupported/getSql/getResult orchestration plus legacy enum state-value hydration before SQL generation/execution |
| `fool-view` | 26 | yes | View/data controllers and adapters, legacy default auto-view factory behavior, and Docker-seeded `OrderList` view/data smoke |
| `fool-app-manage` | 31 | yes | Application/store DB model, app-key lookup, legacy bootstrap defaults, create-app installer side-effect orchestration, DAO-backed create-application gateway, creator authorized-user model/creation, app-system view preparation, legacy root module/model installation records, menu-record/subitem relation creation, role creation, role-user relation creation, role-menu relation creation, migrated model/relation DDL execution hook, connection-aware metadata/DDL DAO routing, connection-string `DaoService`/`JdbcTemplate` factory, installer wiring for configured model schemas or static/reflective module sources, static module-source model dependency ordering, typed reflective Java module-source model/property discovery including inherited fields, reflective enum value metadata, legacy `SW_SYS_EMUNVALUE` enum metadata persistence for module-source installs, file/jar package scanning, model-reference package traversal, basic collection `One2Many`, self-collection `Recurve`, bidirectional collection `Many2Many`, `ReferToProperyAttrbute`, `MultiTypeAttribute`, legacy `ObjectWithSubItem<>` parent target-property relation generation, legacy `ColumnAttribute` key-group/generation/generation-expression/default-value/format/encryption/no-map/multi-column DBMaps/table column-prefix metadata for reflective module sources, module-source module/model/property/relation metadata persistence, and default auto-view persistence |
| `fool-db-manage` | 16 | yes | DB app/source mappings, working database connection rendering, legacy password cipher, factory CRUD/list service, direct SQL execution adapter, and explicit unsupported legacy operation stubs |
| `fool-event` | 45 | yes | Event definition/event/message models, message factory, JDBC message persistence, legacy `SW_EVT_DEF` running-definition loading with `EVTDEF_STATE = 0`, direct NotifyUsers/NotifyRoles/NotifyDeps/NotifyCompanies relation loading, role authorized-user expansion, department/company department user expansion, model-metadata object query adapter with null-model empty result, table, explicit ID-column, auto-`SYSID` ID-column resolution, missing key-column validation, and matched row values, idempotent event creation, runtime service, recipient expansion logic, All-authorized-user JDBC source, application/database catalog loading, scoped system/database JDBC runtime, Spring-managed scheduler lifecycle, application/database traversal, and auto-configuration |
| `fool-report` | 26 | yes | Report definition/result DTOs, table formats, matrix table/cell model, cell coordinate rendering, source-row matrix construction, static subtotal calculation cells including nested row/column sibling-scope subtotals, and legacy report grid rendering |
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

- Auth login/profile calls
- Auth menu loading
- View definition lookup through `/api/v1/view/get-view`
- Data query through `/api/v1/data/query-list`
- Structured visible equality/range filters that emit Spring `QueryValue` payloads, plus advanced JSON filters
- Seeded `OrderList` metadata and rows for Docker smoke coverage
- A migration-map strip showing current server module mapping
- Nginx proxy for `/api/*` to the backend service in Compose

## Remaining Migration Work

- Complete concrete `AppInstallGateway` side-effect parity beyond application creation, creator authorized-user creation, app-system view preparation, root module/model installation records, menu/role record creation, menu/role relation creation, the model/relation DDL execution hook, configured model/static/reflective module-source schema wiring, static module-source dependency ordering, connection-aware metadata/DDL routing, connection-string `DaoService`/`JdbcTemplate` factory, module-source module/model/property/relation metadata persistence, default auto-view hook wiring, reflective model-reference package traversal, basic reflective collection `One2Many` relation generation, self-collection `Recurve` relation generation, bidirectional collection `Many2Many` relation generation, `ReferToProperyAttrbute`, `MultiTypeAttribute`, legacy `ObjectWithSubItem<>` parent target-property relation generation, legacy `ColumnAttribute` key-group/generation/generation-expression/default-value/format/encryption/no-map/multi-column DBMaps/table column-prefix metadata, and legacy `SW_SYS_EMUNVALUE` enum metadata persistence: transaction/pooling boundaries for routed connection strings, deeper DBMaps query/runtime behavior beyond same-row dynamic loading, and arbitrary classpath dependency enumeration beyond model-type references remain.
- Complete remaining `SWDQ01-Soway.Query` behavior: saved-query/report execution surfaces and richer query-to-view integration beyond the current compare/between/in/composite/report filter SQL, selected-table compare-column simple bool-expression path, bool-expression factory create/add orchestration, compare-operation/select-type catalogs, selected column/table state and collection surfaces, selected table join-add/result contract, table/column lookup, query instance parameter/result container, query report definition contract, report-parameter refresh orchestration and named binding, `QueryFactory` table/column/state-value dictionary surface, base/paged SQL builder, JDBC paged executor, and `QueryContext` add/clear/getSql/getResult orchestration plus enum state-value hydration surfaces.
- Complete remaining `SCPB09-SOWAY.EVENT` runtime behavior: fuller dynamic object-query parity beyond the current null-model empty result, table, explicit/auto-`SYSID` ID-column, missing key-column validation, matched-row value capture, and legacy filter SQL construction.
- Complete remaining `SWRPT01-Soway.Report` behavior: additional matrix subtotal edge cases beyond the covered nested row/column sibling-scope paths, table source adapters, and query/export integration around the rendered report grid; legacy `ReportFactory` and `IReportSource` are empty shells.
- Add complete database schema/migration scripts for `car_wash`; Compose currently seeds smoke/order, app-management base tables plus app/store-db relation table, DB-management base tables, event/message base tables, the legacy All-authorized-user source table, direct event NotifyUsers/NotifyRoles/NotifyDeps/NotifyCompanies relation tables, the minimum auth graph tables needed for role/department/company recipient expansion, auth menu/role tables with menu-subitem, role-user, and role-menu relation tables, the minimum legacy `SW_SYS_VIEW` table for app-system view preparation, the legacy `SW_SYS_MODULE` table for root and module-source module installation records, the legacy `SW_SYS_MODEL` table for root and module-source model shell records, legacy `SW_SYS_EMUNVALUE` enum metadata, runtime `fool_sys_model_enum` enum metadata, `SW_SYS_PROPERTY`, `SW_SYS_RELATION`, model/property metadata tables needed for event object table and ID-column resolution plus legacy DDL type/key/generation-expression/default-value metadata, legacy `SE_COMPARETYPE` and `SE_SELECTEDTYPE` query catalogs, and `fool_sys_view`/`fool_sys_view_item` metadata for the Vue `OrderList` smoke workflow.
- Make full backend tests self-contained without datasource override; current full Maven verification passes against the Compose MySQL by running Maven on `fool-service_default` with `spring.datasource.url` pointed at `mysql:3306/car_wash`, while the Docker image build still uses `-DskipTests`.

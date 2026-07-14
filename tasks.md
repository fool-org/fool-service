# Tasks

This file is the repo-local work-state surface until an external tracker is
explicitly named as authoritative for fool-service.

## Active: Installation and Initialization

Design source: `docs/installation-and-initialization.md`. Owner: Codex.

- [x] Add one idempotent system initialization coordinator that discovers the
      configured framework model packages, installs legacy metadata, executes
      model/relation DDL, and creates missing default Views in a fixed order.
      Acceptance: focused tests prove ordering and repeat-safe gateway calls.
- [x] Wire the coordinator into Spring Boot startup behind
      `fool.app.initialization.enabled`, enable it for Docker, and fail startup
      when initialization fails. Acceptance: a rebuilt backend adds non-Market
      system modules/models and a second restart does not duplicate them.
- [x] Keep application provisioning on the same installation boundary through
      `AppInstaller.createApp(...)`, and document the split between database
      migrations, system initialization, and application installation.
      Acceptance: design, code, tasks, and delivery evidence agree.

## Active: Agent Configuration Workflow

- [x] Bootstrap the ordered backend agent/session boundary for
      report/query -> form/view -> model -> data-source -> event/automation.
- [x] Add the first agent API surface for capability discovery, session
      creation, message recording, and ordered stage advancement.
- [x] Persist agent session state through a JDBC store when application
      `JdbcTemplate` is available, with in-memory storage only as a fallback.
- [x] Return a low-risk read-only report/query draft shell from the first
      agent stage, including report/query endpoints and a View-scoped payload.
- [x] Wire the report/query agent stage to concrete read-only query/report
      draft generation with actual View/model column hydration against
      `fool-query` and `fool-report`.
- [x] Wire the form/view agent stage to concrete read-only View draft
      generation with actual field, child collection, and operation metadata
      hydration against `fool-view`.
- [x] Wire the model agent stage to concrete read-only model draft generation
      with actual property, relation, operation, and DDL dry-run metadata
      hydration against `fool-model`.
- [ ] Add a runtime read-only preview gate that compares the hydrated agent
      `ReportCols` with `/api/v1/report/getmkqview` before enabling saved
      report definitions.
- [ ] Add a runtime read-only preview gate that compares the hydrated agent
      form/view draft with `/api/v1/view/getlistview` before enabling View
      metadata writes or operation execution.
- [ ] Add a runtime DDL dry-run preview gate that compares the hydrated agent
      model draft with the target table schema before enabling model metadata
      writes.
- [x] Wire the data-source agent stage to concrete connection catalog,
      credential-reference, and routing validation drafts.
- [x] Wire the event/automation agent stage to concrete trigger, recipient,
      idempotency, and audit dry-run drafts.
- [ ] Add frontend workspace entry points after the backend session contract is
      stable enough for browser smoke automation.

## Active: Vue Interface Upgrade

Design source: `docs/frontend/ui-design-system.md`. Owner: Codex. Each checked
item must land with its matching implementation and validation evidence.

- [x] Establish the PrimeVue/Nora design tokens, direct-import dependency
      policy, baseline screenshots, and responsive acceptance contract.
- [x] Upgrade login and the authenticated application shell, including the
      narrow-screen menu Drawer, while preserving auth, menu, message, and
      logout behavior.
- [x] Upgrade the shared View list table, toolbar, tabs, and paginator without
      changing metadata-derived columns or backend pagination.
- [x] Upgrade detail fields, child-row controls, lookup results, report
      builder/results, and View-specific containers without changing legacy
      request values or component events.
- [x] Pass frontend, Docker runtime, repository harness, desktop browser, and
      390x844 browser acceptance with evidence under `artifacts/runs/`.

## Current Focus

- [x] Bootstrap harness and Standard Engine entrypoints.
- [x] Add a repo-local validation matrix and checker.
- [x] Add runtime evidence bundles for Docker/browser smoke checks after the
      Docker stack stabilizes.
- [x] Revalidate the current signed-out login page at desktop and 390x844:
      controls remain inside the viewport, and Refresh preserves credentials
      while requesting a new CAPTCHA and clearing only the CAPTCHA input.
      Layout evidence is under
      `artifacts/runs/20260712-login-layout-current/`.
- [x] Revalidate the current authenticated desktop and 390x844 View-first
      workflow after fresh authorization to read and fill the current local
      CAPTCHA. Evidence is under
      `artifacts/runs/20260713-authenticated-view-parity/`.
- [x] Make the Docker runtime doctor fail when core legacy model/view/operation
      schema columns required by the View-first workflow drift.
- [x] Hydrate legacy View and View-item template file metadata from
      `SW_SYS_VIEW_FILE` into `getlistview` responses.
- [x] Extend the Docker runtime doctor to prove legacy View `TempFile` and
      item `ViewFile` render metadata through the Vue proxy before data
      binding.
- [x] Render Vue `viewWithChart` pages from loaded `TempFile` metadata as
      legacy data/chart panes, deriving chart rows only from FoolFrame chart
      `EditType` item metadata.
- [x] Resolve legacy recursive `#` owner expressions through the shared
      operation-command resolver and attach parent rows to dynamic collection
      children during load/write.
- [x] Preserve legacy Long/ULong static command values as Java `Long` through
      the shared operation-command resolver.
- [x] Preserve legacy IdentifyId static command values as Java `Long` through
      the shared operation-command resolver.
- [x] Accept legacy date-only DateTime static command values through the shared
      operation-command resolver.
- [x] Recheck legacy command-type and AppInstall remaining-work wording against
      FoolFrame sources and current tests, trimming stale remaining items.
- [x] Select legacy DBMaps columns when loading collection item rows through
      generated item SQL.
- [x] Roll back legacy model parent and owned collection writes together
      through the public `ModelDataService` mutation entrypoints.
- [x] Create missing target rows before writing legacy Many2Many/Recurve
      relation rows through the shared dynamic create path.
- [x] Recheck legacy `runoperation` model-trigger side effects against
      FoolFrame and remove the stale remaining-work item after focused
      runoperation/persistence trigger tests.
- [x] Recheck legacy `runoperation` WCF/JSON base-operation behavior against
      FoolFrame and remove the stale remaining-work marker for that no-op
      success surface.
- [x] Execute legacy trigger direct property-model and list-method command
      slices through the shared `ModelDataService` trigger path.
- [x] Execute legacy trigger external-model update/map command checks through
      the shared `ModelDataService` trigger path.
- [x] Execute legacy runoperation `ArgModel` operations against the target
      model selected by `ArgFilter`, matching FoolFrame object operation invoke
      behavior without binding to concrete DTOs.
- [x] Bind legacy runoperation `Filter` commands to the current object with a
      `SYSID` fallback when the model has no explicit id property.
- [x] Bind legacy model save/update mutations and model-trigger `Filter`
      commands to `SYSID` when metadata has no explicit id property.
- [x] Execute legacy model-trigger assembly handlers through the shared Java
      classpath assembly invoker with trigger constructor/parameter commands.
- [x] Execute legacy property and collection trigger assembly handlers through
      the same shared Java classpath assembly invoker.
- [x] Execute legacy property and collection trigger create/update/delete base
      operations through the shared trigger base-operation path.
- [x] Map legacy `saveobj.Itemproperties[].Items[]` child updates into
      `SubItemList.UpdatedList`, keeping `AddedItems[]` as added state and
      `DelteItems[]` as delete state.
- [x] Render persisted `Order Items` in the Vue `OrderList` detail workflow via
      backend `querydatadetail` collection rows.
- [x] Replace the hand-shaped Vue `OrderList` screen with View metadata-driven
      list/detail/child-row rendering.
- [x] Add basic select-from-existing child collection support for configured
      Vue detail groups.
- [x] Align select-from-existing child interaction with `detailView.jade`:
      `SelectFromExists` groups open a modal metadata-driven candidate table
      instead of showing that picker beside the manual child-create form.
- [x] Match the old child-picker command chrome: keep candidate lookup,
      previous/next paging, and Close as text-only commands while leaving the
      separate child Add plus icon intact.
- [x] Render enum detail fields as metadata-driven selects in the Vue View
      workflow.
- [x] Render readonly View fields as locked controls and skip them from legacy
      save payloads.
- [x] Apply legacy formatted row classes from View data in the Vue View
      workflow.
- [x] Add metadata-driven lookup editors for BusinessObject fields.
- [x] Restore old `setextype.js` BusinessObject typeahead interaction with the
      existing PrimeVue AutoComplete: query after one typed character, show
      View-derived `Text` / `Id` candidates, and save only the selected id.
- [x] Restore BusinessObject current-value and clear interaction: initialize
      lookup text from `FmtValue` and save an empty id when the user clears it.
- [x] Seed and prove a live BusinessObject lookup path for the Vue View
      workflow.
- [x] Auto-load the default Vue View workflow so the first screen is usable
      without opening API tools or pressing a setup button.
- [x] Add candidate search/pagination for select-from-existing child collection
      dialogs without frontend SQL/filter construction.
- [x] Expose configured child list/select View IDs from backend View metadata
      and prove the Docker Vue detail workflow uses them for existing-item
      candidates.
- [x] Preserve existing object state during legacy partial `saveobj` and prove
      Vue select-existing child rows save through `AddedItems`.
- [x] Accept the old FoolFrame Web `/data/save` and `/data/new` protocol
      shapes as `/api/v1/data/save` and `/api/v1/data/new`, including the
      `obj` / `ownerviewid` / `ownerid` / `prpid` payload from `detailview.js`.
- [x] Drive the main Vue workflow through legacy `getlistview(viewId)` then
      `querydata(viewId)` so the rendered page follows View metadata instead
      of the newer business-name query shortcut.
- [x] Accept the old FoolFrame Web `/data/querylist` protocol shape at the
      backend data boundary as `/api/v1/data/querylist`, including
      `viewid` / `filter` / `page` / `pagesize` / `orderitem` / `ordertype`
      aliases.
- [x] Keep Vue detail/initnew rendering tied to loaded read-item View metadata;
      raw `querydatadetail` DTO fields and child groups can no longer define
      page structure when the View metadata is missing or empty.
- [x] Preserve `initnew.Data.ObjId` for new-object identity and use the local
      id generator only when the generic View response leaves that value empty.
- [x] Preserve detail `Data.Name` as the legacy save object's `ViewID`, with
      numeric View-id fallback only when the generic detail response omits it.
- [x] Preserve detail `Data.ParentId` as the BusinessObject lookup owner for
      standalone child detail/new pages, with the parsed route owner fallback.
- [x] Restore existing child detail `Data.ParentId` from generic
      `Model.default_owner` and relation metadata, without binding View output
      to an Order/OrderItem DTO.
- [x] Drop `querydatadetail` child groups that are not declared by the loaded
      read-item View `DetailViews`, so DTO-only groups cannot define Vue child
      sections or columns.
- [x] Read FoolFrame Pascal `querydatadetail` child-group aliases in shared
      Vue helpers so `SelectedView` / `ListViewId` still drive select-existing
      child workflows after matching the rendered read-item View.
- [x] Render select-existing child controls through a shared
      `SelectFromExists`/`selectFromExists` helper instead of a camel-only
      template field read.
- [x] Route Vue child group labels, row iteration, item ids, and child-item
      save payloads through shared helpers so Pascal detail DTO aliases do not
      leak into the template or payload builders.
- [x] Cache Vue read-item View metadata by rendered `ViewId` so detail,
      init-new, and manual read-item panels do not bind data DTO values to a
      stale or wrong read View shape.
- [x] Render legacy `getmain/getsubmenu` menu entries in the Vue shell and
      open menu `ViewId` entries through the existing
      `getlistview(ViewId)` -> `querydata(ViewId)` workflow, including stale
      stored-token recovery before first-screen rendering.
- [x] Preserve the old Web menu hierarchy in desktop and mobile navigation:
      keep `getmain.TopMenu` visible while `getsubmenu.Items` expands beneath
      its parent, and keep the mobile Drawer open until a concrete View opens.
- [x] Restore Bootstrap dropdown toggle behavior: a second click on the open
      parent collapses its submenu, with desktop/mobile `aria-expanded` bound
      to the same shared expansion state.
- [x] Restore shell route history semantics: menu and message View/detail
      targets update `/view{id}` paths, and browser back/forward reloads the
      matching existing View-first route without adding a router dependency.
- [x] Clear transient shell navigation on every actual route transition:
      direct menu, Home, message target, and browser history close the mobile
      Drawer and dropdown through one shared helper.
- [x] Restore the old Web desktop shell layout: move AppInfo branding,
      Home/TopMenu/SubMenu navigation, and user actions into the top header,
      while reusing the same menu component vertically in the mobile Drawer.
- [x] Restore old Web authenticated shell copy for Home, navigation, system
      messages, refresh/detail actions, signed-in fallback, and safe logout
      while preserving server-provided user/menu/message text.
- [x] Restore old `/` and `/main` Home behavior: desktop/mobile Home and the
      desktop brand return to `App.DefaultViewId`, while an App without a
      default View shows the original configuration guidance.
- [x] Replace the invented Indigo/gradient shell theme with old Bootstrap
      primary states (`#337ab7`, `#286090`, `#204d74`) across PrimeVue
      controls, the shell brand mark, and map markers.
- [x] Remove the remaining invented indigo color from detail child links and
      restore Bootstrap link/hover colors without changing their routes or
      edit/detail interactions.
- [x] Accept the old FoolFrame Web `/user/getmenu` submenu protocol shape as
      `/api/v1/auth/getmenu`, including the `authcode` payload from
      `menuinfo.js`.
- [x] Tighten the Docker runtime doctor so every `querydata` row must expose
      View-column-matched `Items`; DTO-only `values` rows no longer satisfy the
      view-first smoke contract.
- [x] Load the Vue first-screen View id from legacy `getmain/getapp`
      `App.DefaultViewId` before `getlistview`, so the default page follows
      FoolFrame shell configuration instead of a Docker seed DTO shortcut.
- [x] Require legacy `getapp` / `getmain` AppInfo shell aliases used by
      `layout.jade` and `default.jade` through the Docker runtime doctor.
- [x] Render authenticated shell branding from `getmain.AppName/AppVer` and
      remove the Vue-only Docker/MySQL/Redis status strip from the user-facing
      header.
- [x] Seed legacy View/query Chinese metadata as UTF-8 in Docker so Vue
      renders operation labels like `删除` / `保存` instead of mojibake.
- [x] Hydrate legacy `SW_SYS_OPERATIONVIEW_ITEM` operation parameter metadata
      into View operation DTOs for both list-view and detail-view responses.
- [x] Keep operation parameter metadata behind shared `Params`/`ParamName`
      protocol helpers without inventing visible parameter labels; old Web
      `operation.js` executes detail operations with View/object/operation ids.
- [x] Restore the old detail view/edit/save state machine from
      `detailView.jade`: existing records start read-only, server `CanEdit`
      controls Edit access, new records start editable, and View operations
      and child write controls stay blocked while the main detail is editing.
- [x] Restore the old detail heading format: show `视图名 -对象ID` or
      `视图名 -新建` inline, remove the invented rounded ID tag, and localize
      the transient no-View fallback.
- [x] Restore the old detail action layout by keeping Edit, Save, and
      metadata-defined View operation buttons in one wrapping toolbar; keep
      Edit/Save mounted for existing records and switch their disabled state
      instead of shifting the toolbar during edit.
- [x] Match `detailView.jade` main-toolbar icon semantics: retain the pencil
      for Edit and use the old shared check mark for Save and metadata View
      operations instead of invented save/bolt icons.
- [x] Restore old Web Chinese detail interaction copy for edit/save,
      child add/select/page/edit/delete, empty states, confirmation prompts,
      and local validation while preserving metadata and server ReturnMsg text.
- [x] Honor child-group `DetailViewId`: keep zero-detail groups inline editable
      and route configured child rows through the old
      `/view{DetailViewId}/{DataID}` deep-detail path.
- [x] Keep select-existing child paging protocol-only like `detailView.jade`:
      remove editable page/page-size inputs, retain fixed 10-row requests,
      keyword reset, and previous/next navigation.
- [x] Persist dynamic `BusinessObject` fields through their foreign-key ids so
      legacy `runoperation` update saves stay metadata-driven and do not bind
      to concrete business DTOs.
- [x] Render View metadata operation buttons in the main Vue workflow and
      execute them through legacy `runoperation`.
- [x] Remove invented Vue `New Row` and default `Open` list commands so main
      list actions come only from View `Operations`; keep the child candidate
      table's old explicit Select action as an opt-in shared-table behavior.
- [x] Remove the invented editable main-list page size, restore
      `view.jade`'s 10-row request size, and let the search/report/metadata
      operation toolbar wrap when operation counts vary.
- [x] Restore old Web list action order and static copy: input condition,
      search, report, then metadata create operations, with Chinese operation,
      empty, paging, and refresh labels while preserving metadata text.
- [x] Restore Bootstrap 3 panel chrome for list/detail pages: 4px corners,
      light gray bordered headings, 15px body spacing, and the old subtle
      one-pixel shadow instead of modern floating-card styling.
- [x] Expose FoolFrame Pascal `runoperation` result aliases (`Value`,
      `IsSuccess`, `ReturnObjId`, `ReturnViewId`, `ReturnMsg`) and make Vue
      operation refresh logic read success through a shared protocol helper.
- [x] Restore old Web operation-result feedback: show `ReturnMsg` with
      success/error severity, retain the result after a successful View/detail
      refresh, and clear stale results when navigation changes context.
- [x] Extend the Docker runtime doctor to prove `runoperation` result aliases
      through the Vue proxy without executing a mutating View operation.
- [x] Accept the old FoolFrame Web `/data/exoperation` protocol shape at the
      backend data boundary as `/api/v1/data/exoperation`, including
      `objid` / `viewid` / `opid` aliases, while reusing the shared
      `runoperation` service path.
- [x] Accept the old FoolFrame Web `/report/mkrpt` report execution protocol
      shape at the backend report boundary as `/api/v1/report/mkrpt`,
      including `viewid` / `cols` / `pagesize` / `pageindex` / `exp` aliases.
- [x] Prove the old FoolFrame Web `/report/mkqview` candidate-column protocol
      shape through `/api/v1/report/mkqview` with the lower-case `viewid`
      payload from `mkreport.js`.
- [x] Remove the Vue workspace's visible business-name `get-view/query-list`
      shortcuts so View rendering and data loading stay `viewId` driven.
- [x] Add a repository harness guard for oversized source files.
- [x] Add repository harness checks for Java package boundaries and FoolFrame
      migration parity marker drift.
- [x] Accept legacy Pascal `inputquery` request fields at the protocol DTO
      boundary without introducing concrete business DTO binding.
- [x] Accept legacy Web `inputquery` lower-case request fields from
      `setextype.js` at the same protocol DTO boundary.
- [x] Accept the old FoolFrame Cloud-Social `inputquery` payload where numeric
      `ViewName` carries the View id, while keeping business-name-only
      `ViewName` rejected.
- [x] Remove the unused Vue business-name `query-list` payload builder so the
      frontend keeps a single view-id driven data-loading path.
- [x] Move reusable Vue child-group View workflow helpers out of `App.vue`
      into `viewWorkflow.ts`.
- [x] Move report-grid cell matrix rendering into a tested Vue workflow helper
      and keep `App.vue` below 2000 lines.
- [x] Tighten the repository source-size harness limit from 2200 to 2100 lines.
- [x] Accept FoolFrame Pascal `getlistview` / `querydata` request fields at
      the generic View/data protocol boundary without binding to business DTOs.
- [x] Accept the old FoolFrame Web `/view` get-list payload shape at
      `/api/v1/view/getlistview`, including the `id` alias at the shared
      View request DTO boundary.
- [x] Remove visible `OrderList` / trading-field defaults from the Vue View
      workflow so first-screen rendering stays `viewId` and View metadata
      driven.
- [x] Resolve report `ReportCols[].ColId` through View/Model metadata so
      legacy `getrpt` can render selected columns without frontend business
      DTO assumptions.
- [x] Project report `ReportCols[].ColId` values into custom `ColName` aliases
      so selected report columns render data even when the output header differs
      from the View property name.
- [x] Build report `getmkqview` candidate columns from configured View items
      when present, so report column selection follows the rendered View
      ordering instead of exposing every Model property.
- [x] Support Docker-seeded single-argument `SE_SELECTEDEXP` query select-type
      expressions so report `SelectedTypeId` execution can reuse the shared
      query SQL builder instead of controller-side special cases.
- [x] Auto-fill Vue report `ReportCols` JSON from `getmkqview` metadata so the
      report tool can run from loaded View columns instead of hand-written
      column DTOs.
- [x] Align report `getmkqview` / `getrpt` column identity with FoolFrame's
      View-query model: candidate `ColId` values now come from View-derived
      property keys, while report/filter/selected-type resolution reuses one
      View metadata context before reading data rows.
- [x] Route report `ReportCols.OrderType` through View metadata into the
      `querydata` SQL order path instead of sorting rendered row DTO maps;
      `OrderType=2` falls back to the first selected report column ASC.
- [x] Preserve multiple report `ReportCols.OrderType` entries by selected
      column order in the View/data SQL query path, matching FoolFrame report
      ORDER BY generation without binding reports to rendered row DTO maps.
- [x] Align `QueryFactory.getStateStr` with FoolFrame's enum DB-value to
      display-name lookup, including empty string for missing values.
- [x] Remove stale saved-report persistence/execution language from remaining
      report migration work after confirming FoolFrame `HandlerSaveReport` is
      an empty no-op success handler.
- [x] Remove the frontend generic record-map table helpers and `ListDataItem`
      `values` typing so rendered tables cannot bind directly to concrete
      business DTO maps.
- [x] Let lookup `inputquery` resolve the current View by `ViewId` before
      falling back to legacy `ViewName`, and have Vue metadata editors pass
      the loaded View id so lookup follows the rendered View context.
- [x] Make backend `inputquery` combine a configured `ViewItem.SelectedView`
      filter with the text lookup when querying target-model candidates.
- [x] Remove the Vue metadata lookup component's `viewName` fallback so
      BusinessObject candidate lookup stays tied to the rendered View id.
- [x] Correct lookup View identity against FoolFrame: hydrate collection
      `Name` from its linked list View, then send the rendered detail or child
      View name through legacy `inputquery`, with numeric ViewId only as a
      metadata-missing fallback and no business DTO shortcut.
- [x] Render detail collection `Properties` and row `Values` from the linked
      List View's ordered ViewItems, including labels, editability, types, and
      formats, instead of exposing every property from the child model DTO.
- [x] Keep `ItemEditType.Format` ViewItems in detail `SimpleData` and
      collection groups like FoolFrame `DataFormator`; restrict the row-class
      exclusion behavior to list-table rendering only.
- [x] Add a Docker runtime doctor for the stable Vue/View workflow so
      backend, frontend proxy, View metadata, data query, inputquery, and
      report model smoke checks are repeatable.
- [x] Make generic `get-view` / `query-list` and the Vue main detail/save
      refresh path prefer the loaded `ViewId`, keeping `ViewName` only as
      compatibility fallback.
- [x] Require `ViewId` on the generic backend `get-view` / `query-list`
      compatibility endpoints so data queries cannot start from a business-name
      DTO shortcut.
- [x] Treat legacy `OperationBaseType.NULL` runoperation as a successful no-op
      without create/update/delete persistence, matching FoolFrame handler
      behavior.
- [x] Accept FoolFrame Pascal `SaveObj` and nested save payload fields at the
      generic legacy `saveobj` DTO boundary without binding saves to concrete
      business DTOs.
- [x] Accept legacy `getenums` model-id spellings from both FoolFrame Web
      proxy forms without changing the enum lookup contract.
- [x] Accept the old FoolFrame Web `/model/getenum` route shape as
      `/api/v1/data/getenum`, including the lowercase `modelid` payload.
- [x] Expose legacy `getenums` response aliases (`EnumValues`, `Name`,
      `Value`) while keeping the Vue camel-case enum contract.
- [x] Expose legacy `querydatadetail` response aliases for detail payloads
      while keeping the Vue camel-case detail contract.
- [x] Expose FoolFrame Pascal `getmkqview` report model aliases (`Cols`,
      `ID`, `Name`, `QueryTypes`, `CompareTypes`, `States`) and make Vue
      report-column consumers read them through shared View workflow helpers.
- [x] Route Vue View summary name/title/type/input-count rendering through
      shared View workflow helpers instead of direct page-level metadata field
      reads.
- [x] Expose FoolFrame Pascal report grid aliases (`Cells`, `Col`, `Row`,
      `ColSpan`, `RowSpan`, `FmtValue`) and make Vue report-grid rendering read
      cells through shared helpers.
- [x] Tighten the Vue main View workflow layout so loaded View rows and
      `querydatadetail` detail fields render in the first usable screen on
      desktop and remain scrollable on narrow screens.
- [x] Execute legacy model SAVE trigger `SET_VALUE` side effects through
      `ModelDataService` writes, reusing the shared operation command value
      resolver instead of adding a second expression parser.
- [x] Execute legacy trigger `FILTER` commands for model/property/collection
      triggers through the shared trigger-command path, stopping later trigger
      commands when the current persisted row does not match.
- [x] Make the Vue API-tools `Query Data` path load the selected View
      definition before calling `querydata`, so shared row state stays bound to
      the rendered View context instead of a standalone business DTO shortcut.
- [x] Make the Vue current View refresh path verify the rendered
      `getlistview(viewId)` metadata before `querydata(viewId)`, preventing
      page rendering from starting at a data/business DTO fallback.
- [x] Split the Vue results panel and static migration map out of `App.vue`,
      bringing the file back under the 2000-line target while keeping View
      row tables on the shared metadata renderer.
- [x] Move Vue field enum option loading into `useFieldEnums` so metadata
      editors share the same cached model-id lookup without growing `App.vue`.
- [x] Keep Vue child collection editor bindings stable when DetailView
      metadata renders before child draft maps are synchronized.
- [x] Move Vue child add/update draft state into `useChildDrafts`, reusing
      shared View workflow draft helpers while keeping `App.vue` below the
      frontend root budget.
- [x] Execute legacy property `SET` trigger `SET_VALUE` side effects through
      dynamic `ModelDataService` create/save writes without introducing
      concrete business DTO binding.
- [x] Execute legacy collection `ItemsAdd` / `ItemsDelete` property trigger
      `SET_VALUE` side effects during dynamic collection writes.
- [x] Accept legacy Pascal `querydatadetail` `ViewId` / `ObjId` fields at the
      generic detail-data protocol boundary and cover it in the Docker runtime
      doctor.
- [x] Pass legacy `querydatadetail` request `Token` through the controller and
      detail service View lookup boundary without adding a cross-module auth
      dependency.
- [x] Pass legacy `runoperation` request `Token` through operation-command
      expression evaluation so `@userid` / `@username` values reuse the shared
      context service without binding commands to business DTOs.
- [x] Require the Vue main list and detail editor to have rendered View
      metadata before using `querydata` / `querydatadetail` payloads, so data
      DTO rows cannot define page columns or detail form fields.
- [x] Remove the remaining Vue list-column fallback to `querydata.Cols` and
      first-row `Items`, so main and child candidate tables only take columns
      from loaded View metadata.
- [x] Render detail View operation buttons from the loaded `querydatadetail`
      operations payload instead of the list View metadata, keeping detail
      actions bound to the rendered detail View context.
- [x] Render list View create operations from metadata and use their target
      `ViewId` for the Vue `initnew` / `savenewobj` flow.
- [x] Render list row operations with target `ViewId` and keep selected detail
      load/save/lookup/operation execution bound to the active detail View.
- [x] Render list row operations without a target `ViewId` as inert metadata
      text without a button role or selection handler, matching old Web.
- [x] Render View workflow paging controls from legacy `querydata` totals and
      keep page navigation on the loaded `ViewId`.
- [x] Reset the main View workflow search/load action to page 1 before
      re-querying, matching FoolFrame list search behavior.
- [x] Use the rendered View's `DetailViewId` for default Vue row open and
      fallback new-row initialization, so detail data stays bound to the View
      metadata context instead of the list View or a concrete business DTO.
- [x] Stop deriving the main Vue list columns from row `values` DTO keys; when
      View columns are absent, the page keeps columns empty instead of
      inventing them from data DTO fields.
- [x] Hydrate persisted legacy `SW_SYS_VIEW.VIEW_DEFAULT` into
      `getlistview.DetailViewId` and prove runtime detail loading uses that
      loaded View metadata instead of a fixed business DTO/detail id.
- [x] Prefer legacy row `Items` metadata over `row.values` DTO fallbacks for
      Vue row identity and selected-existing child item payloads.
- [x] Remove `row.values` from Vue View row identity, cell rendering, and
      selected-existing child save mapping so rendered pages stay View/Items
      driven instead of business DTO driven.
- [x] Send the main Vue View workflow search text as legacy
      `querydata.QueryFilter`, matching FoolFrame's list page filter box.
- [x] Extend the Docker runtime doctor to prove `querydata.QueryFilter`
      returns filtered legacy row `Items`.
- [x] Expose FoolFrame Pascal `querydata` response aliases and make Vue list
      helpers consume legacy `Data` / row `Items` without binding rendering to
      concrete business DTO fields.
- [x] Expose FoolFrame Pascal `getlistview` response aliases and make Vue View
      helpers consume legacy `Items` / `Operations` metadata without binding
      page rendering to concrete business DTO fields.
- [x] Expose FoolFrame Pascal `getreaditemview` response aliases and make the
      Vue detail editor load read-item View metadata before merging
      `querydatadetail` values.
- [x] Populate legacy `getreaditemview.DetailViews` collection metadata from
      configured child edit Views via the existing View data service, and make
      Vue child tables use that View metadata before binding
      `querydatadetail` row values.
- [x] Extend the Docker runtime doctor to fail when
      `getreaditemview.DetailViews` child metadata disappears from the loaded
      detail View path.
- [x] Reuse the shared Vue `ListDataTable` renderer for main, child-candidate,
      and API-tool View row tables so row cells keep one View/Items-driven path.
- [x] Honor legacy `querydata.AutoFreshTime` in the main Vue View workflow with
      a minimal timer that refreshes the loaded View/data path.
- [x] Prefer legacy `querydata.Cols` as the Vue fallback table header source
      before deriving columns from row `Items`.
- [x] Display legacy `querydata.FreshTime` in the main Vue View workflow
      status row.
- [x] Format shared main/Sudoku `FreshTime` values with the browser locale like
      old `querylistdata.js`, supporting both current ISO and legacy Date text.
- [x] Keep report-grid row projection sourced from legacy row `Items` instead
      of `row.values` DTO maps.
- [x] Extend the Docker runtime doctor to prove legacy `getrpt` returns
      report-grid cells through the Vue proxy.
- [x] Extend the Docker runtime doctor to prove legacy `saverpt` returns the
      FoolFrame no-op success surface through the Vue proxy.
- [x] Prove the old FoolFrame Web `/report/saverpt` report-definition payload
      shape through `/api/v1/report/saverpt`, including `viewid` / `cols` /
      `exp` / `reportname`.
- [x] Accept the old FoolFrame Web `/itemview` detail-data payload shape at
      `/api/v1/data/querydatadetail`, including `id` / `objid` / `idexp`
      aliases at the shared DTO boundary.
- [x] Remove hard-coded seed object IDs from Vue API-tool defaults and make
      runtime detail smoke use the object id returned by `querydata`.
- [x] Extend the Docker runtime doctor to prove the legacy auth first-hop
      path through the Vue proxy: `initapp`, `getcheckcode` / `checkcode`,
      `loginv2`, and `getuserinfo`.
- [x] Accept the old FoolFrame Web login field names from `login.js` at the
      shared `loginv2` DTO boundary: `name`, `pwd`, `dbid`, `chk`, and
      `chkid`, while keeping explicit `AppId` / `AppKey`.
- [x] Expose the old FoolFrame Web login response flag `IsLogin` from the
      shared `loginv2` result DTO and prove it through the Vue proxy.
- [x] Extend the Docker runtime doctor to prove the logged-in legacy auth
      shell path through the Vue proxy: `getapp`, `getmain`, `getsubmenu`,
      and `logout`.
- [x] Make the Docker runtime doctor derive `querydata`, `inputquery`, and
      report checks from the loaded View metadata instead of hard-coded
      Docker order DTO fields.
- [x] Expose FoolFrame Pascal message aliases for `getmsg` / `getnotify`
      while keeping the Vue message panel on the existing compatibility
      contract.
- [x] Expose the old FoolFrame Web root `POST /getmsg` shape as
      `/api/v1/getmsg`, reusing the migrated message polling service path.
- [x] Prove a non-empty `getmsg` response exposes the legacy fields consumed
      by `message.js`, including `/Date(ms)/` `GernerationTime`.
- [x] Expose FoolFrame Pascal `getcheckcode` / `initapp` response aliases and
      make the runtime doctor require those legacy fields before auth login.
- [x] Accept the old FoolFrame Web `/user/getchk` route shape as
      `/api/v1/auth/getchk` while reusing the migrated check-code generator.
- [x] Make Vue auth first-hop controls consume FoolFrame Pascal `initapp` and
      `getcheckcode` fields through shared protocol helpers.
- [x] Remove the Docker seed operation id from the Vue manual `runoperation`
      tool default so operation execution starts from View metadata or user
      input.
- [x] Make Vue SubMenu, message, notify, and enum tool panels consume
      FoolFrame Pascal aliases through shared protocol helpers.
- [x] Make Vue `querydatadetail` / `initnew` tool tables consume legacy
      `Data.SimpleData` / `Data.Items` aliases through shared View workflow
      helpers instead of binding templates to camel-case DTO fields.
- [x] Expose FoolFrame Pascal `inputquery` response aliases (`Items`, `Id`,
      `Text`) and make Vue lookup consumers read them through shared protocol
      helpers.
- [x] Remove stale `SCPB09-SOWAY.EVENT` object-query items from remaining
      migration work after confirming the same cases are already covered by
      the module map and event tests.
- [x] Make focused Docker Maven module tests work without repeating
      `-DfailIfNoTests=false`, using the root Surefire config instead of
      adding Maven profile scaffolding.
- [x] Add a repository harness guard that fails Vue main render paths if they
      infer table columns or cells from business DTO `row.values` instead of
      loaded View metadata and row `Items`.
- [x] Remove the Docker runtime doctor's seeded `ViewId=100` fallback so runtime
      proof must start from the legacy app shell default View before loading
      View metadata and data.
- [x] Implement the legacy `QueryResult.GetData` current-page reload surface in
      `fool-query` so changing `CurrentPage` can re-query the current page
      through the existing paged SQL executor.
- [x] Expose the legacy `SelectedColCollection.CopyTo` collection surface in
      `fool-query` without changing selected-column index behavior.
- [x] Import the FH_JAVA legacy `market_symbols` schema into Docker and make the
      runtime doctor fail if the exchange/filter precision columns are missing.
- [x] Remove seeded `ViewId=100` bootstrap defaults from the Vue workflow so
      data queries only run after a View has been loaded from app/view metadata.
- [x] Remove the Vue `/test` seed-data panel and frontend proxy so the browser
      workspace proves data loading through View metadata and `querydata`
      instead of the backend `Order` smoke DTO.
- [x] Bootstrap fresh Vue Docker browser sessions through the legacy
      `initapp` / `loginv2` / `getmain` path before `getlistview`, so the
      first screen loads the app-shell default View instead of stopping at
      `ViewId=0`.
- [x] Initialize Vue child add-row drafts immediately after `getreaditemview`
      metadata loads so the first-screen detail panel renders without
      `undefined.itemId` runtime errors.
- [x] Persist AppInstall-generated default Views through legacy
      `SW_SYS_VIEW` / `SW_SYS_VIEW_ITEM` records, preserving View model/default
      View and item read-only/edit-type metadata without binding to business
      DTOs.
- [x] Backfill legacy model/property ids during module-source install and use
      them when persisting generated default View items and View operations.
- [x] Persist module-source model operations and operation commands into
      legacy `SW_SYS_OPERATION` / `SW_SYS_COMMANDS` records.
- [x] Persist module-source operation params into legacy
      `SW_SYS_OPERATION_PARAM` records.
- [x] Persist module-source custom View operation metadata through legacy
      `SW_SYS_VIEW_OPERATION` / `SW_SYS_OPERATIONVIEW` records.
- [x] Keep Vue fallback row identity and table keys tied to rendered View
      columns before raw `querydata.Items` order.
- [x] Reuse parsed legacy enum values for AppInstall `SW_SYS_EMUNVALUE`
      duplicate checks.
- [x] Wrap routed AppInstall module-source/default-View metadata and schema
      DDL work in target `DaoService` transaction boundaries.
- [x] Keep Vue lookup/rendering and legacy view endpoints on ViewId-only flow:
      load View metadata first, then query data from that loaded View.
- [x] Require shared Vue row tables to have rendered View columns before
      drawing data rows or row actions, so `querydata` DTO rows cannot define
      a page when View metadata is empty.
- [x] Remove the remaining Vue API-tool seed defaults for enum model and
      new-object ID so manual tools no longer start from concrete business DTO
      identifiers.
- [x] Align `QueryFactory.getTable` with FoolFrame `GetTable` by resolving
      tables from trimmed/case-insensitive DBName only and failing missing
      tables instead of accepting rendered ShowName labels or returning null.
- [x] Remove the generic View/data request DTO `viewName` shortcut from
      `get-view`, `getlistview`, `getreaditemview`, and `query-list`, so
      stray `ViewName` JSON is ignored and those endpoints still require
      `ViewId`.
- [x] Require service-level View/data lookups to receive numeric `ViewId`
      before DAO lookup, removing direct `ViewName` shortcuts from
      `ViewDataService.getViewData` and `DataQueryService.queryViewDataList`.
- [x] Remove the stale frontend `InputQueryRequest.viewName` type shortcut so
      Vue lookup payloads stay typed around loaded `ViewId` context.
- [x] Evaluate legacy `querydatadetail.IdExp` through the shared
      `OperationCommandValueResolver` before detail data lookup, keeping
      empty-`objId` loading View-first and avoiding a second expression parser.
- [x] Resolve legacy `querydatadetail.IdExp` `@userid` / `@username`
      expressions through a token-backed shared context service without adding
      a direct View-to-auth module dependency, while pinning detail lookup to
      load View metadata before View-model data.
- [x] Let legacy `inputquery` owner source-list expressions (`#.`) work for
      existing child rows as well as added rows, and pass Vue child lookup
      owner context through the shared metadata field editor.
- [x] Render Vue metadata scalar fields with native date, time, and numeric
      inputs from View field type metadata instead of a one-size text input.
- [x] Render Vue DateTime metadata fields with native `datetime-local` inputs
      from View field type metadata, normalizing legacy display strings only
      at the editor boundary and not from business DTO field names.
- [x] Keep Vue scalar editor control selection on FoolFrame's detail render
      path: `PrpType` / `PropertyType` selects native inputs first, while
      `EditType` picker names only fill missing property-type metadata.
- [x] Keep Vue readonly detection on FoolFrame's detail render path:
      explicit `ReadOnly` / `readOnly` wins before `EditType=ReadOnly`
      compatibility fallback.
- [x] Route Vue enum option selection through the shared `fieldModelId`
      helper so shell code no longer reads `prpModelId` / `PrpModelId`
      aliases directly.
- [x] Accept numeric `PrpType` / `PropertyType` codes in Vue metadata editors
      for enum, lookup, Boolean, date, time, datetime, and numeric controls;
      keep numeric `ItemEditType` only as a compatibility fallback or
      view-item state such as readonly/rich text.
- [x] Keep `RichTextBox` / numeric `ItemEditType=5` as a native textarea
      fallback only when `PrpType` / `PropertyType` metadata is absent.
- [x] Render Vue Boolean / CheckBox metadata fields as native checkboxes from
      View field metadata and coerce legacy Boolean string save values by
      `PropertyType.Boolean` during dynamic persistence.
- [x] Align shared metadata editor display with old `setextype.js`: render
      Boolean labels as `是` / `否`, keep BusinessObject ids protocol-only,
      and use a Chinese lookup-failure fallback.
- [x] Keep Vue input-query item display helpers stable for empty legacy
      candidate entries so the first-screen View workflow does not throw an
      `itemId` render error while remaining metadata-driven.
- [x] Treat legacy WCF / JSONPOST / JSONGET `runoperation` base types as
      successful no-ops, matching FoolFrame handler semantics without adding
      speculative external client code.
- [x] Move Vue View/data loading into a reusable View-first workflow helper so
      list rendering loads View metadata before `querydata` and avoids concrete
      business DTO column binding.
- [x] Add a stricter harness line budget for `frontend/src/App.vue` so future
      Vue migration work keeps extracting workflow helpers instead of growing
      the root component.
- [x] Normalize loaded list View ids through shared `ViewId` / `ID` helpers so
      Vue queries data from the rendered View response instead of a request-form
      value or concrete business DTO fallback.
- [x] Build existing child-row update payload fields from rendered child group
      View columns instead of `querydatadetail` data DTO values.
- [x] Initialize existing child-row draft state from rendered child group View
      columns, merging data values by property key so DTO-only fields cannot
      enter the Vue editor state.
- [x] Map select-existing child rows from rendered candidate View columns before
      same-key row items, preventing DTO-only values from overriding
      `AddedItems` payload fields.
- [x] Reuse rendered child group View columns for child update fallback drafts
      when the Vue child draft map is missing during save.
- [x] Trim child delete payloads to item id plus empty `propertyies`, matching
      FoolFrame's delete-by-`ItemId` behavior and avoiding DTO-only values.
- [x] Stop Vue data queries when the loaded View has no renderable columns, so
      page data cannot fall back to concrete business DTO rows when the View
      contract is missing.
- [x] Remove stale `SWDQ01-Soway.Query` / `SWRPT01-Soway.Report` remaining-work
      wording after re-checking the current module map, focused tests, and
      legacy empty-shell report sources.
- [x] Restrict explicit query/report ordering to rendered View items so hidden
      Model properties cannot bypass loaded View metadata.
- [x] Preserve the legacy `loginv2` selected App id in token session state so
      `getapp` and `getmain` return token-selected AppInfo instead of the first
      configured app.
- [x] Preserve the legacy `loginv2` selected `DbId` in token session state so
      `@appcon` and `@datacon` resolve from token-selected App/store database
      connection strings.
- [x] Recheck AppInstall package scanning, referenced model package traversal,
      DBMaps coverage, and routed transaction tests against FoolFrame
      `AssemblyModelFactory`, trimming stale remaining-work wording without
      adding speculative classpath APIs.
- [x] Complete the legacy `SW_SYS_MODEL` parent/id-property/type/is-view,
      default-format/default-view schema and `AppInstalledModel` mapping, and
      guard those columns in the Docker runtime doctor.
- [x] Add idempotent Docker repair blocks and runtime-doctor coverage for the
      legacy `SW_SYS_PROPERTY` model/filter/source/format/sqlcon/owner columns
      used by migrated View/data/AppInstall metadata.
- [x] Extend the legacy `SW_SYS_PROPERTY` Docker repair and runtime-doctor
      coverage to the remaining mapped property scalar columns: connection type,
      collection, DB/property names, multi-map, key/check/generation, and
      nullable/get/set flags.
- [x] Align the `fool-view` runoperation scalar-conversion test with the shared
      `PropertyType.Long` -> Java `Long` resolver semantics, restoring the
      broader `fool-app-manage -am` Maven gate.
- [x] Add idempotent Docker repair blocks and runtime-doctor coverage for legacy
      `SW_SYS_RELATION` collection relation columns and `SW_SYS_MULTIMAP`
      DBMaps columns used by Model/AppInstall paths.
- [x] Extend Docker runtime-doctor coverage to legacy operation, operation
      parameter, command, model-trigger, and property-trigger columns used by
      migrated runoperation, trigger hydration, and AppInstall paths.
- [x] Extend Docker runtime-doctor coverage to the full legacy View render
      schema used by the Vue View-first workflow: View, ViewFile, ViewItem,
      ViewOperation, OperationView, and OperationViewItem columns.
- [x] Deduplicate the runtime-doctor schema test fixture so schema guard tests
      reuse `LEGACY_CORE_SCHEMA_COLUMNS` instead of carrying a second full copy.
- [x] Extend Docker runtime-doctor coverage to the auth/app shell schema used
      before Vue loads View metadata: `auth_user`, `SW_AUTH_USER`,
      `SW_APPLICATION`, `SW_STOREDB`, and `SW_APP_AUTH_*` menu relations.
- [x] Extend Docker runtime-doctor coverage to the remaining AppManage mapped
      schema columns for installed modules, model metadata, enum values,
      trigger/operation primary keys, and company/department auth graph tables.
- [x] Extend Docker runtime-doctor coverage to the legacy event/message schema
      used by the migrated scheduler and message polling path:
      `SW_EVT_DEF`, `SW_EVT_EVENT`, and `SW_SYS_MSG`.
- [x] Extend Docker runtime-doctor coverage to event definition recipient
      relation tables used by notification expansion.
- [x] Extend Docker runtime-doctor coverage to legacy query catalog tables used
      by report/query metadata loading.
- [x] Extend the Docker runtime doctor to prove legacy `getenums` using an enum
      model id discovered from loaded View metadata.
- [x] Extend Docker runtime-doctor schema coverage to `fool_sys_model_enum`
      columns used by View-derived enum loading.
- [x] Extend Docker runtime-doctor schema coverage to the legacy `SW_SYS_CON`
      connection table seeded for routed runtime paths.
- [x] Move runtime-doctor schema guard catalogs into a reusable module so the
      executable Docker smoke script stays smaller while migration coverage
      grows.
- [x] Extend the Docker runtime doctor to prove legacy `initnew` through the
      Vue proxy using the loaded detail View metadata.
- [x] Extend the Docker runtime doctor to prove legacy
      `querydatadetail.IdExp` through the Vue proxy using the loaded detail
      View metadata and row id.
- [x] Let dynamic create/save omit absent scalar fields so legacy
      `savenewobj` can use database defaults for missing BusinessObject
      foreign keys instead of writing null.
- [x] Extend the Docker runtime doctor to prove legacy `savenewobj` through
      the Vue proxy using loaded detail View fields and cleanup for the fixed
      smoke id.
- [x] Extend the Docker runtime doctor to prove legacy `saveobj` through the
      Vue proxy by updating a temporary detail-View object and reading the
      changed field back.
- [x] Extend the Docker runtime doctor to prove legacy
      `saveobj.Itemproperties[].AddedItems[]` through the Vue proxy using
      loaded detail child metadata and cleanup for the fixed smoke id.
- [x] Extend the Docker runtime doctor to prove legacy
      `saveobj.Itemproperties[].Items[]` update and `DelteItems[]` delete
      through the Vue proxy using loaded detail child metadata.
- [x] Refactor runtime doctor tests to reuse shared fake legacy API responses
      while keeping saveobj/savenewobj proof assertions scoped.
- [x] Add a repository harness guard that checks Docker init SQL declares every
      runtime-doctor legacy schema column, including FH_JAVA `market_symbols`.
- [x] Let reflective AppInstall module sources include declared dependency
      packages as legacy module dependencies without scanning the whole
      classpath.
- [x] Execute legacy `ItemsSet` collection property triggers when saving
      existing owned child rows through `ModelDataService`.
- [x] Guard modern `fool_sys_model` / property / View metadata columns in the
      Docker runtime doctor schema contract used by the Vue View-first flow.
- [x] Guard DB-management base schema columns in the Docker runtime doctor
      schema contract: `DB_App`, `WorkDataBase`, `DB_AppDB`, and
      `DS_DataSourceSet`.
- [x] Preserve FoolFrame no-op semantics for missing legacy trigger
      `ExuteListMethod` list methods while keeping real Java list invocation.
- [x] Remove stale WCF/JSON base-operation wording from remaining migration
      work after rechecking the current no-op runtime/test evidence.
- [x] Extract the Vue View-first detail and child-collection panel into a
      reusable component so `App.vue` stays under the repo line budget while
      preserving metadata-driven save, lookup, operation, and child-row flows.
- [x] Fall back to FoolFrame `SYSID` for View/data protocol object ids when a
      migrated dynamic model has no explicit id property.
- [x] Let legacy `inputquery` lookup target models omit explicit id metadata
      without passing null properties into the model query path.
- [x] Hydrate legacy owned collections for dynamic parent models that rely on
      `SYSID` instead of explicit id metadata.
- [x] Let legacy `inputquery` source-list owner/current lookups use
      FoolFrame `SYSID` when model metadata has no explicit id property.
- [x] Return FoolFrame `SYSID` from `DbMysqlDynamic.getId()` when a dynamic
      model has no explicit id property.
- [x] Write legacy `saveobj` / `savenewobj` request ids into FoolFrame `SYSID`
      when dynamic model metadata has no explicit id property.
- [x] Insert FoolFrame `SYSID` during dynamic creates when model metadata has
      no explicit id property.
- [x] Use the old FoolFrame `SYSID` when saving dynamic rows whose model
      metadata has no explicit id property.
- [x] Seed and prove FoolFrame chart `EditType` values `11` through `14` for
      `viewWithChart`, so Vue chart rows come from loaded View row `Items`
      instead of concrete business DTO fields.
- [x] Recognize legacy Vue `TempFile=Sudoku` and render child panel shells from
      View item `ViewFile` metadata before adding any child-panel data binding.
- [x] Dispatch the old repository's complete top-level template set (`view`,
      `viewWithChart`, `Sudoku`) through explicit Vue render paths and block
      unknown custom `TempFile` values from falling through to list/data/detail
      rendering.
- [x] Load Vue Sudoku child panels through each panel `ListViewId`
      `getlistview` -> `querydata` chain, skipping root View `querydata` so
      panel rows stay bound to child View metadata.
- [x] Render top-level `viewWithChart` and Vue Sudoku `linechart` panels through
      one responsive SVG chart component, reusing `legacyChartData` for line,
      bar, and scatter `EditType` metadata.
- [x] Match `viewWithChart.jade` data/chart navigation as text-only tabs,
      removing invented table/chart icons and their tab-only spacing rule.
- [x] Reset the shared chart tab to old `viewWithChart.jade`'s data-first state
      when the active View id or template changes, without resetting it during
      search, paging, or data refresh.
- [x] Align the shared SVG chart presentation with old `swchartLine.js`:
      restore the legacy ECharts default color order, keep legends metadata-
      name-only, and remove English series-type/debug fallback text.
- [x] Render Vue Sudoku `Map` panels from child row map `EditType` metadata
      (`16` longitude, `17` latitude, `18` title) on a lazy-loaded interactive
      Leaflet map without binding to concrete business DTO fields.
- [x] Align map fallback display with old `mapview.js`: derive an absent marker
      title from the first metadata info item and use Chinese location/error/
      accessibility copy instead of fixed English UI text.
- [x] Seed and prove a real Docker `CustomerMap` child View for Vue Sudoku
      `Map` panels, using the panel `ListViewId` before querying legacy map
      `EditType` row data.
- [x] Render Vue Sudoku `Item` panels through the legacy detail path:
      `getlistview(ListViewId)` then `querydatadetail(ListViewId, ObjId="")`,
      showing `SimpleData` without reading list-row DTO maps.
- [x] Render Vue Sudoku `Group` panels as one-level child tabs, loading
      `ListViewType=0` child lists through their own `ListViewId` and keeping
      `ListViewType=1` simple children as explicit placeholders.
- [x] Repair Sudoku presentation drift against `Sudoku.jade`: honor each root
      panel's 12-column `Width`, render Group children as actual tabs, move
      update/refresh controls below data, and remove type/load-count badges.
- [x] Hydrate backend `ListViewType` from each child `ListViewId` View type
      and prove Docker-seeded Sudoku `Group` metadata exposes both list and
      detail/simple child surfaces.
- [x] Browser-verify Docker-rendered Vue Sudoku panels and render child panel
      titles from ViewItem `Name` metadata instead of only template kind text.
- [x] Align Vue Sudoku `ListViewType=1` group child placeholder text with the
      legacy FoolFrame `GroupViewController` simple-item branch.
- [x] Restore legacy Sudoku list/group panel refresh behavior: expose each
      loaded panel's `FreshTime`, support targeted refresh, schedule its own
      `AutoFreshTime`, preserve shared ViewId data, and clear timers on View
      changes and unmount.
- [x] Start Vue from the old FoolFrame Web `/view:id` list route by applying
      the path View id before loading app-default View metadata.
- [x] Start Vue from the old FoolFrame Web detail/new routes by reusing
      `queryDetail` and `startNewObject` with parsed path View/object/owner
      context.
- [x] Render the old FoolFrame Web `/itemview:id` route from
      `getreaditemview` metadata only, without querying an empty object or
      binding an arbitrary first business row.
- [x] Restore `item.jade`'s `DetailViews` tab interaction on `/itemview:id`:
      reuse the detail collection tabs and metadata field table while hiding
      object-data Add, picker, and mutation controls.
- [x] Restore top-level command availability by old View template: keep
      `view.jade` search/report/create commands, limit `viewWithChart.jade` to
      search, and render no invented top toolbar or stale report on `Sudoku`.
- [x] Guard old FoolFrame Web Vue deep links in the Docker runtime doctor.
- [x] Guard the current Docker `car_wash` init SQL slice set in the repository
      harness.
- [x] Guard key Docker `car_wash` seed markers needed by the Vue/runtime smoke
      workflow.
- [x] Guard Docker runtime seed rows in the runtime doctor, including the
      query catalogs that old MySQL volumes can miss.
- [x] Guard the old FoolFrame Web root entry `/` in the Docker runtime doctor.
- [x] Render the Vue detail panel title from loaded read-item View metadata
      instead of a hard-coded generic heading.
- [x] Guard Docker query catalog relation seed rows in the runtime doctor and
      repository harness so report column `CompareTypes` / `QueryTypes`
      cannot silently lose their property-type bindings.
- [x] Prove `getmkqview` and legacy `mkqview` return query catalog
      `CompareTypes` / `QueryTypes` through the Docker Vue proxy.
- [x] Guard old FoolFrame Web static `/about` and `/contact` GET routes in the
      Docker runtime doctor through the Vue frontend fallback.
- [x] Expose old FoolFrame Web `getchk` response aliases `chkkey` / `chkimg`
      from the shared check-code DTO and prove them through the Docker Vue
      proxy.
- [x] Expose legacy `querydatadetail` child `Properties[].Name` aliases from
      the shared list-data value DTO and prove them through the Docker Vue
      proxy.
- [x] Move the old FoolFrame list-page report workflow out of the developer
      console
      and into the rendered View page, with metadata-driven output selection,
      output types, ordering, structured AND/OR conditions, paging, and no
      raw report JSON or SQL filter inputs.
- [x] Make old report condition grouping functional: group consecutive
      conditions, nest complete groups, split the innermost group, and emit a
      recursive `FirstExp` / `Sequences` tree with backend parenthesis proof.
- [x] Align report condition-group rendering with old `mkreport.js`: keep
      internal group ids protocol-only, show nested legacy-colored group rails,
      and restore the `拆分分组` action copy.
- [x] Restore old report condition-row initialization: add an empty field and
      operator, delay the value editor until a View field is chosen, keep the
      first relation cell blank, and use the old `与/或` labels.
- [x] Restore `mkreport.js` / `setextype.js` report-condition value controls:
      derive enum, Boolean, date/time, numeric, and BusinessObject editors from
      the selected View column while preserving lookup id/display filter values.
- [x] Restore the compact old report condition table layout: icon-only add and
      group controls, operation columns before data fields, visible field
      headers, and both top and bottom add-condition entry points.
- [x] Restore the old save-report form: start with a blank report name, show
      `输入报表信息以保存该报表`, and use the full dialog content width.
- [x] Restore the old report-result interaction: render every matrix row as a
      striped/hoverable table row, use `前一页` / `下一页`, keep only `返回` in
      the result footer, and reset report paging when returning to setup.
- [x] Restore the old report dialog interaction: open report setup as a modal
      with output, condition, and save-definition tabs, then switch successful
      runs into a separate paged result state with a back action.
- [x] Restore old `mkreport.js` output selection as candidate/output-method/
      selected three-column lists, including duplicate field output methods,
      add, reorder, delete, ascending, descending, and cancel-sort actions.
- [x] Keep report page size at old `mkreport.js`'s protocol-only value of 10;
      remove the invented output-tab page-size editor and reload command.
- [x] Restore `view.jade` Chinese report copy for dialog/result titles,
      output/condition/save tabs, column/order controls, grouping, paging, and
      footer actions while preserving protocol values and metadata labels.
- [x] Match `view.jade` report tabs exactly as text labels by removing the
      invented output/filter/save icons and their tab-only spacing rule.
- [x] Match `view.jade` report footer commands as text-only buttons by removing
      invented return, cancel, run, and save icons while preserving state and
      handlers.
- [x] Restore `view.jade`'s compact right-aligned list toolbar: keep the query
      prompt inside the search field, align actions on one desktop row, and
      let only the input occupy a full row on narrow screens.
- [x] Match `view.jade` list toolbar commands as text-only controls: remove
      invented search, chart, and create icons while retaining metadata names,
      order, targets, and disabled states.
- [x] Restore the old scrolling list operation column: remove PrimeVue's
      frozen-right behavior, left-align row actions, and keep candidate
      selection as a text-only `选择` command.
- [x] Preserve View-provided list header text like old Bootstrap tables:
      restore normal 14px dark headers with zero letter spacing and remove
      forced uppercase transformation.
- [x] Keep the active report ViewId protocol-only: remove the invented
      `视图 {id}` dialog subtitle while retaining the id for report model/run/
      save requests.
- [x] Collapse unknown View-template handling into one Chinese warning: stop
      the data workflow without duplicating an App-level error, preserve the
      metadata template name, and remove English loading/unsupported copy.
- [x] Replace the main Vue View toolbar's editable View ID and raw
      `QueryFilter` controls with a View-metadata-safe keyword search; raw
      compatibility filters remain protocol-only and are not exposed in Vue.
- [x] Move legacy shell user, message polling, and logout into a responsive Vue
      topbar, reusing the View-first
      detail/list loaders for message targets.
- [x] Restore old `message.js` delivery behavior: each non-empty 15-second
      poll immediately opens the first generated message in a `系统消息` dialog
      with time, content, detail navigation, and confirmation controls.
- [x] Match `tbar.jade` by removing the invented system-message bell, manual
      refresh, and history Popover; retain only old `message.js` automatic
      delivery and its View-first detail action.
- [x] Match `tbar.jade`'s text-only `安全退出` command by removing the invented
      sign-out icon while preserving logout behavior and pending state.
- [x] Match `menuinfo.js` logout navigation: explicit sign-out replaces the
      current deep-link URL with `/`, while stale-token recovery preserves its
      requested View/detail/new path for login resumption.
- [x] Restore `tbar.jade` navigation order by moving `安全退出` after the
      metadata menu; keep the right shell area limited to the old avatar and
      user-name presentation, with a matching mobile Drawer action.
- [x] Restore old top/sub-menu `ImageUrl` rendering: adapt Pascal/camel aliases
      once, show metadata-provided images at the old 30x30 size, and allocate
      no icon slot when the value is empty.
- [x] Keep legacy `getnotify` protocol-only like the old Web implementation:
      remove the invented 15-second UI request and menu-count badges while
      retaining API aliases, helpers, backend route, and runtime coverage.
- [x] Restore `default.jade` user avatar rendering end to end: hydrate
      `UserAvtarUrl` from `SW_AUTH_USER.USER_AVTAR`, adapt the compatibility
      response in `viewWorkflow`, and retain an icon fallback for empty values.
- [x] Match `soway.css`'s fixed 50x50 user-avatar dimensions instead of the
      invented 40x40 Vue shell size.
- [x] Match `tbar.jade` menu state behavior by removing the invented current
      View `active` highlights; retain only the old dropdown-open equivalent.
- [x] Restore the old Bootstrap navbar/dropdown palette and geometry: neutral
      gray states, 50px top actions, 160px dropdown minimum, and list-group
      child rows replace the invented indigo rounded navigation treatment.
- [x] Restore `default.jade`'s actual `h2 > small` brand typography: 21px
      application name with a 65% version replaces the invented 24px/12px pair.
- [x] Restore `default.jade` user-area metrics: the name uses the old 10px blue
      link treatment, and the empty-avatar fallback keeps the old 50x50 slot.
- [x] Restore `detailview.js`'s edit-state operation guard: business-operation
      buttons remain clickable while editing and show `请先保存当前信息` without
      issuing the operation request.
- [x] Restore the old detail-page error dialog outlet so edit guards, validation
      failures, and detail API errors are visible and explicitly dismissible.
- [x] Restore `operation.js` result feedback as an `执行结果` modal with the
      old `操作成功`/`操作失败` summary, return message, and `确定` command.
- [x] Restore `operation.js` response timing by showing the result immediately;
      remove Vue-only success refreshes of both list and detail data.
- [x] Restore `detailview.js` child-delete staging: hide deleted rows locally,
      merge same-group `DelteItems`, and submit them only with the parent Save;
      remove the Vue-only confirmation and immediate save/query cycle.
- [x] Restore `detailview.js` inline child-edit staging: parent edit keeps child
      rows read-only until their own Edit action, one row edits at a time, and
      row Save upserts `Items` locally for the parent Save without a request.
- [x] Restore `detailview.js` child-add persistence timing: stage manual and
      selected-existing rows in `AddedItems`, discard unsaved rows locally,
      query candidates by `ListViewId`, and route `SelectedView` child creation.
- [x] Restore `AddedItems.IsExist` semantics: manual rows use false so the model
      creates their IDs, while selected-existing rows preserve their real IDs.
- [x] Restore `detailview.js` inline child-add interaction: replace the invented
      persistent add form with a blank table row that immediately enters its
      row editor and stages only when the row Save action runs.
- [x] Restore `detailView.jade` child-action availability: keep Add, Edit, and
      Delete visible, while preserving `edititem`'s parent-edit early return.
- [x] Restore new-parent child tabs and `detailview.js`'s exact add guard:
      render View metadata, issue no candidate/save request, and show the old
      `操作提示` modal before child creation.
- [x] Restore `detailView.jade`'s `DetailViewId` action matrix: condition deep
      Edit on non-select groups, restore the separate Details link, and keep
      table operation spans aligned with rendered actions.
- [x] Restore `detailview.js beginsave()` feedback and timing: show the old
      non-dismissible saving modal and navigate back only after a successful
      save closes it; failed saves remain on the detail page.
- [x] Restore select-existing View-first timing: open with candidate metadata
      and runtime `记录未知 请查询`, then query rows only after Find or paging.
- [x] Restore `initQueryView` picker-open ordering: show the old loading modal,
      await candidate View metadata, and open the picker only on success.
- [x] Restore candidate `NavbarController` feedback and placement: show exact
      runtime unknown/total-record text below results and keep paging after the
      table.
- [x] Restore candidate query page timing: Find resets to page 1 while Navbar
      previous/next requests preserve the selected target page.
- [x] Restore the compact text-only Bootstrap navbar brand: remove the invented
      42px initial tile, render App name/version inline, and reduce the desktop
      shell header to the old 50px scale while reusing it in the mobile Drawer.
- [x] Replace default `admin/admin` auto-login and developer auth controls with
      a signed-out Vue `initapp` / captcha / `loginv2` page that resumes legacy
      View/detail/new deep links after authentication.
- [x] Align the signed-out Vue page with old `index.jade`: use the application
      image/title, narrow unframed form, Chinese placeholders, captcha refresh,
      stacked login/reset actions, footer metadata, and reset-time code refresh.
- [x] Replay the ordered idempotent Docker MySQL init catalog on every Compose
      startup and block backend startup when existing-volume migration fails.
- [x] Audit all 25 old Web `app.js` routes and the Jade templates, then remove
      the Vue `API Tools`, migration-map, raw response, and manual DTO panels so
      production navigation only exposes the shared View-first workflows.
- [x] Remove the deleted console's remaining Vue request-state and JSON-string
      staging: detail, create, save, child mutation, and operation requests now
      consume current rendered View state and typed payload arrays directly.
- [x] Delete the unused Java `SelectStream` placeholder after proving it has no
      call sites and no FoolFrame counterpart; use the JDK `Stream` API for any
      future stream workflow instead of maintaining a second empty proxy.
- [x] Remove the duplicate compile-scoped `spring-jdbc` declaration from
      `fool-dao` and prove the Java 17 module reactor and tests still pass.
- [x] Extract the metadata-driven list View presentation into one reusable
      Vue component without duplicating request or workflow state; keep
      search, paging, operations, chart, and Sudoku rendering View-first.
- [x] Restore the old Web list/detail page boundary: list pages no longer
      auto-open the first row or render a side-by-side detail DTO, and View
      operations/new commands navigate through `/view{id}/{obj}` and
      `/new{id}` before rendering the standalone detail page.
- [x] Restore the old single View heading: remove duplicate desktop/mobile
      topbar titles and protocol `ViewName` text, leaving the View-defined title
      in the list/detail panel while the mobile topbar only opens navigation.
- [x] Restore `default.jade`'s authenticated `AppPowerBy` footer from loaded
      `getmain.App` metadata without adding fixed company text or another API.
- [x] Align standalone detail simple-field layout with `detailView.jade`:
      reuse one metadata-driven field grid for read/edit states, render two
      field groups per desktop row, and collapse to one group on mobile.
- [x] Align standalone detail collection layout with `detailView.jade`: render
      each metadata `Items[]` group as a scrollable tab and its dynamic child
      fields/rows/actions as a reusable legacy-style table.
- [x] Match `detailView.jade` child tab labels exactly: render only metadata
      `ItemName` text and remove the invented row-count badge and its styles.
- [x] Match `itemreadonly.js` page-entry state across SPA detail transitions:
      each object/new context starts on its first metadata child tab and closes
      any prior select-existing dialog.
- [x] Match `viewWithChart.jade` page-entry state when Home, menu, or browser
      history re-enters the same View: reset to the Data tab without resetting
      the tab during search, paging, or automatic data refresh.
- [x] Restore `detailView.jade`'s left-aligned select-existing child Add command
      instead of pushing that single-button toolbar to the right edge.
- [x] Restore `detailView.jade`'s two-column child operation layout: span the
      operation heading across edit/save and delete cells, and keep empty-row
      colspan aligned with the metadata and operation columns.
- [x] Keep detail child object ids protocol-only like `detailView.jade`: render
      only metadata `Properties[]` and operation columns while retaining each
      `DataID` for save/delete/detail actions and stable row keys.
- [x] Reconcile the vague remaining Model runtime mutation wording against
      FoolFrame, the 37-case Java 17 Model service suite, and live Docker
      `MODEL_CON` state; close speculative routed-connection work until a real
      migrated model declares a separate connection.
- [x] Complete authenticated desktop/mobile browser acceptance for the
      View-first Vue workflow; preserve Sudoku row data when list/chart/item
      panels share a View id and keep both mobile shell navigation controls
      inside the 390px viewport.
- [x] Treat legacy zero argument-model IDs as unset for same-model operations;
      prove the seeded detail Save reaches its update path, returns the old
      success result dialog, and leaves the idempotent target row unchanged.
- [x] Restore `navbar.js` main-list paging: show record totals above Previous,
      up to seven direct page links, and Next; remove Vue-only First/Last and
      main-list `FreshTime`, then prove page 2 queries and cleanup at runtime.
- [x] Restore `viewWithChart.jade`'s left-aligned search form and full-width
      Data/Chart tabs without changing normal `view.jade`'s right-aligned
      toolbar; verify desktop and 390px geometry in the deployed frontend.
- [x] Restore Sudoku `Map.jade` presentation: keep location details in marker
      popups, remove the Vue-only coordinate list, and preserve the old fixed
      200px map height on desktop and mobile.
- [x] Restore Sudoku `linechart.jade`'s fixed 200px partial without shrinking
      the shared `viewWithChart` page; adapt the compact SVG viewBox and mobile
      label density to the rendered panel geometry.
- [x] Restore `Sudoku.js`'s one-time maximum flow-control height: wait for all
      View-derived panel data, lock every grid row to the natural maximum, and
      keep that height stable across Group tab switches and panel refreshes.
- [x] Match `Sudoku.jade`'s root rendering boundary: remove the shared Vue root
      data table after the child panel grid while preserving normal-list and
      `viewWithChart` root tables.
- [x] Restore `subitem.js`'s Sudoku Item matrix: render two View-derived fields
      per row, pad to six rows, and isolate the table in a small reusable child
      component without binding a business DTO.
- [x] Match `Sudoku.jade`'s heading boundary: start directly with child panels
      instead of showing a Vue-only root View title, while preserving normal
      list and chart View headings.
- [x] Match `detailView.jade`'s exact select-existing initial feedback:
      `记录数未知,请查询` before the first candidate Find, while preserving
      metadata-first loading and queried total counts.
- [x] Match `detailView.jade`'s candidate form submission: pressing Enter in
      the select-existing query input runs the same metadata-driven search as
      the Find button and preserves existing result/paging state.
- [x] Restore the candidate dialog's `navbar.updateNavbar` interaction: reuse
      the main list's extracted seven-link Previous/Page/Next paginator and
      remove the Vue-only `第 x / y 页` control without duplicating page state.
- [x] Match `detailView.jade`'s functional candidate footer action: label the
      dismiss command `取消`, preserve no-change close behavior, and do not
      restore the old inert `确定` placeholder.
- [x] Match `detailView.jade`'s candidate query-form placement: reuse the main
      list's compact right-aligned input/Find layout, remove the Vue-only
      visible label, and preserve responsive and Enter-query behavior.
- [x] Restore `view.jade` / `detailView.jade` plain-text query controls: use
      `type="text"` for main and select-existing inputs without changing Enter,
      Find, keyword payloads, or View-first data loading.
- [x] Restore `querylistdata.js`'s fixed page-row layout through the shared
      metadata table: pad main/chart/candidate pages to 10 rows and Sudoku
      List/Group List partials to 5 without adding actions or DTO state.
- [x] Restore ViewItem `Width` rendering in the shared metadata table: apply
      positive camel/Pascal widths to list/candidate columns while preserving
      automatic layout for zero and avoiding business-row DTO coupling.
- [x] Restore `querylistdata.js` `EditType=10` row-format presentation: keep
      View-derived row classes and supply the old Bootstrap contextual
      backgrounds and hover states through the shared metadata table.
- [x] Restore the old shared `.table-hover` feedback for normal, chart,
      candidate, and Sudoku metadata tables with the Bootstrap `#f5f5f5`
      background while retaining contextual row hover colors.
- [x] Restore template-specific Bootstrap striping: use exact first-row
      `#f9f9f9` stripes for normal/chart/candidate tables and disable striping
      for Sudoku List/Group List through the shared table prop.
- [x] Restore template-specific Bootstrap table density through the shared
      renderer: 5px for condensed normal/chart tables and 8px for candidate
      and Sudoku tables without duplicating markup or data state.
- [x] Restore Bootstrap table-cell geometry in the shared renderer: 14px/20px
      typography, bold bottom-aligned headers with 2px `#ddd`, and top-aligned
      body cells with 1px `#ddd` borders.
- [x] Restore Bootstrap `nav-tabs` presentation through one reusable class for
      chart, detail collection, report, and Sudoku Group tabs while preserving
      each existing Vue interaction state.
- [x] Restore `navbar.js` Bootstrap pagination presentation through the shared
      list/candidate component: `«` / `»`, contiguous 34px page links, and old
      active/hover/disabled colors without changing the seven-page workflow.
- [x] Restore the old default-command hierarchy for shared list Find buttons:
      remove the invented primary emphasis while preserving normal/chart
      placement, Enter submission, and View-first search behavior.
- [x] Restore Bootstrap query-control geometry in the shared list/candidate
      toolbars: 34px controls with 6x12px padding and 14/20px typography while
      preserving responsive placement and search events.
- [x] Restore old dialog close-command availability: remove invented header
      Close actions from message/feedback/result dialogs while retaining the
      footer commands and the real candidate/report header close paths.
- [x] Restore Bootstrap header close presentation for candidate/report setup:
      reuse one 21px bold `×` slot and shared borderless opacity states while
      preserving PrimeVue close events and accessibility.
- [x] Restore shared Bootstrap modal chrome for migrated dialogs: 6px outer
      frame, 15px head/body/footer spacing, old dividers/shadow, and 18px titles
      without changing component widths, commands, or state.
- [x] Restore Bootstrap modal-footer command hierarchy and geometry: outlined
      default dismiss/message/feedback actions, 34px controls, and 5px spacing
      while preserving report primary commands and existing close events.
- [x] Restore `view.jade`'s report-save command hierarchy: render
      `保存报表定义` as the distinct information action while keeping Cancel
      default, Confirm primary, and the existing View-derived save workflow.
- [x] Restore `view.jade` report-result paging presentation: keep functional
      Previous/Next commands in one right-aligned extra-small button group and
      keep the old eventless export placeholders as a separate parity slice.
- [x] Restore `view.jade` report-output toolbar grouping: separate
      move/remove commands from sort commands with compact reusable groups and
      preserve all existing output-column mutations.
- [x] Restore `detailView.jade` detail command hierarchy: group Edit, Save, and
      View operations as contiguous default commands while preserving edit
      state, save enablement, and operation dispatch.
- [x] Restore `detailView.jade` child-row link presentation: use lightweight
      Edit/Save and Delete commands while preserving inline staging, danger
      emphasis, and parent-save persistence.
- [x] Restore the child collection Add command's default group geometry by
      reusing the shared 34px detail command contract without changing its
      metadata-driven add path.
- [x] Restore the candidate table's default Select action as a default outlined
      command without changing shared metadata rows, View operations, or
      select-existing staging.
- [x] Restore condensed report-condition control geometry: 34px Select/Input
      controls with shared form padding and typography while preserving field,
      comparison, value, grouping, and mobile-scroll behavior.
- [x] Restore `view.jade`'s report-output Add command: use a right-aligned
      secondary outlined 34px arrow and keep it visible above the mobile dialog
      footer without changing output selection or deduplication.
- [x] Restore `mkreport.js` report-output toolbar interaction: keep move,
      remove, and sort commands available at empty/boundary states with no-op
      handlers, and append sort labels without an extra space.
- [x] Restore `mkreport.js` output-type metadata behavior: do not synthesize an
      `原值` option when `QueryTypes` is empty, while retaining the old
      single-type candidate-change shortcut and Add-command no-op.
- [x] Restore `mkreport.initquery` output-method activation: keep the first
      candidate selected with an empty output-method list until an actual
      candidate change, then retain multi-type explicit Add, single-type
      auto-add, and zero-type no-op behavior.
- [x] Restore `view.jade`'s empty report-condition structure: retain the header
      and trailing Add row without the Vue-only explanatory empty-state copy.
- [x] Restore the report-condition merge glyph with the installed `pi-list`
      equivalent of old `glyphicon-list` without changing grouping behavior.
- [x] Restore `mkreport.js` condition-merge availability and feedback: keep the
      command actionable, report single/non-contiguous selections, and preserve
      successful contiguous grouping through the shared condition validator.
- [x] Restore old report-result paging boundaries: keep Previous/Next available
      and move first/last-page no-op checks into one local page-change handler.
- [x] Restore `mkreport.js` grouped-condition selection: show one representative
      checkbox per top-level group and apply it to the whole group without
      duplicating grouping or filter state.
- [x] Restore `view.jade`'s fixed report-result table for zero data: retain the
      empty table body and remove the Vue-only empty-state sentence.
- [x] Restore `detailView.jade`'s empty child-collection rendering: retain the
      metadata-defined tab and heading-only table after staged row removal,
      remove the Vue-only empty copy, and omit the panel when no group exists.
- [x] Restore `querylistdata.js`'s zero-result candidate table: mount the
      shared metadata table after the first query even with no matches, retain
      ten filler rows and record count, and remove the Vue-only empty sentence.
- [x] Restore `view.jade`'s zero-field report-output structure: retain all three
      metadata selection controls, remove the Vue-only empty sentence, and do
      not invent an output type before a candidate column exists.
- [x] Restore `viewWithChart.js`'s zero-series chart surface: retain the chart
      axes after a zero-row query, remove the Vue-only empty sentence, and draw
      horizontal labels only when legacy chart-axis metadata supplies them.
- [x] Apply the zero-series chart contract to Sudoku `linechart` partials:
      mount the shared compact chart before rows exist and avoid the generic
      Vue empty-state fallback without changing panel loading or refresh.
- [x] Restore `subitem.js`'s zero-field Sudoku Item matrix: always mount the
      shared two-fields-per-row table and retain six padded rows instead of
      replacing it with a Vue-only empty-state sentence.
- [x] Restore `mapview.js`'s empty Sudoku Map lifecycle: always mount the shared
      map, use the legacy Beijing center at zoom 18 when no valid points exist,
      enable wheel zoom, and remove Vue-only empty/error copy.
- [x] Restore `Group.jade` / `groupview.js` empty-content behavior: keep empty
      Group bodies and unknown child-type tab panels blank instead of inserting
      Vue-only empty-state copy.
- [x] Remove Vue-only detail initialization copy from `/itemview:id` and the
      standalone detail panel while preserving View-only schema rendering and
      avoiding an empty-object data query.
- [x] Restore the shared old-page table lifecycle: always retain an empty table
      shell, remove generic Vue empty/loading copy, and keep rows suppressed
      until View columns exist.
- [x] Restore `Sudoku.jade`'s unmatched child `ViewFile` behavior by leaving the
      panel content empty instead of displaying a Vue-only generic data message.
- [x] Restore select-existing candidate initialization: show View-derived table
      headings before the first query without an automatic empty row, then keep
      ten-row query padding and local-only candidate staging.
- [x] Restore `setextype.js` numeric input behavior: use text inputs for legacy
      numeric metadata and apply the original digit/decimal filtering plus
      four/eight-character limits for property types 1-6.
- [x] Restore `setextype.js` BusinessObject suggestion presentation: render
      each result as `Text - Id` and retain the fixed `查找更多` footer without
      changing View-driven query or selection state.
- [x] Restore `detailview.js` readonly edit rendering: keep readonly View fields
      as plain text while editable fields become controls, preserve empty-line
      geometry, and keep readonly values out of save payloads.
- [x] Restore `setextype.js` Boolean editor presentation: render the checkbox
      alone without Vue-only `是` / `否` copy and preserve legacy boolean save
      values through the shared metadata editor.
- [x] Restore `detailView.jade` empty simple-value rendering: retain a
      non-breaking text line for empty View fields without adding placeholder
      data to shared values, drafts, or save payloads.
- [x] Restore `view.jade` report-footer command availability: keep Confirm and
      Save Report Definition actionable at empty/partial setup states while
      retaining request-pending duplicate protection and View-driven payloads.
- [x] Restore `view.jade` / `mkreport.js` report-output control availability:
      keep the output-method list and Add arrow actionable without a selected
      candidate while retaining the existing no-op and duplicate guards.
- [x] Restore `view.jade` / `mkreport.js` report-condition Add availability:
      keep both Add commands actionable with zero View fields and create one
      empty condition row through the existing View-driven editor.
- [x] Restore `menuinfo.js` non-navigable submenu rendering: show `ViewId=0`
      children as plain list text instead of disabled buttons while preserving
      View-backed child navigation in the shared desktop/mobile component.
- [x] Restore `mkreport.js` dialog timing: wait for report View metadata before
      opening setup, then hide setup while generating and reopen with results.
- [x] Restore `mkreport.js` output-selection stability: select the first added
      output once, then preserve the current selection on later/duplicate adds.
- [x] Restore `initQueryView` candidate headings: render only loaded View field
      labels and keep the row-level Select action without an added header label.
- [x] Restore `swchartLine.js` series presentation in the shared chart renderer:
      smooth and fill line series, hide their point symbols, and show the
      bar/scatter metadata-series value labels.
- [x] Restore `swchartLine.js` horizontal-axis naming: carry the first row's
      `EditType=11` View field name through shared chart data and render it at
      the axis end in top-level and Sudoku charts.
- [x] Restore `swchartLine.js` category-axis boundaries: place multi-category
      first/last centers on the horizontal-axis ends, keep a single category
      centered, and prevent the end-axis name from overlapping the final label.
- [x] Restore `swchartLine.js getOption` bar width: cap top-level chart bars at
      15 rendered pixels across responsive sizes while preserving the compact
      realtime chart branch that had no legacy `barMaxWidth` setting.
- [x] Restore both `swchartLine.js` chart layouts: reserve the legacy 20% right
      grid space and move the horizontal metadata legend to the right-middle,
      stacking only on narrow screens to keep the plot readable.
- [x] Restore ECharts 3.1.7's default legend selection: toggle by metadata
      series name, remove hidden series from drawing and domain/bar grouping,
      retain the inactive `#ccc` treatment, and share the behavior across
      top-level and compact charts.
- [x] Restore `swchartLine.js` axis-trigger tooltip behavior: select the nearest
      View-derived category on mouse movement, draw the default axis pointer,
      list only selected metadata series, suppress all-hidden content, and
      reuse the bounded overlay across top-level and compact charts.
- [x] Restore `LineChartController` realtime data semantics for Sudoku
      `linechart`: load its child View before `querydatadetail`, initialize the
      metadata series as a 100-point zero window, append one detail sample per
      refresh, and schedule from detail `AutoFreshTime` without changing the
      list-backed top-level chart.
- [x] Restore Sudoku partial footer interaction boundaries: keep List and Group
      List refresh commands active, render linechart/Map refresh copy as the
      old inert anchors with blank update times, retain linechart's timer, and
      stop scheduling refreshes for the one-shot Map controller.
- [x] Restore `LineChartController`'s compact chart title from the configured
      child View name, reserving space above the realtime plot while leaving
      top-level `viewWithChart` without an inner title.
- [x] Restore both `swchartLine.js` branches' value-axis
      `boundaryGap: [0, '50%']`: expand the upper bound from the raw data span
      before crossing zero, retain legend-filtered ranges, and preserve the
      empty/all-hidden `0..1` fallback through shared tested geometry.
- [x] Restore ECharts 3.1.7 value-axis nicifying after the boundary gap: choose
      the old 1/2/3/5/10 decimal interval, round extents outward, and render the
      resulting variable tick count across top-level and compact charts.
- [x] Restore realtime `LineChartController` `stack: 'a'` coordinates: apply
      same-sign cumulative values to compact line/bar/scatter series, retain
      bar-only positive/negative bases and smooth line-area bases, recompute
      after legend filtering, and leave normal unique-name series independent.
- [x] Restore ECharts 3.1.7's default zero-value bar geometry: remove the
      Vue-only one-pixel minimum so zero values stay invisible while nonzero
      metadata values keep their natural scale in top-level and compact charts.
- [x] Restore `swchartLine.js` stack ids across both chart paths: use each
      View-derived metadata name for top-level stacks, retain realtime `a`,
      group matching bar slots, and collapse duplicate legend names.
- [x] Restore `querylistdata.js` non-target row-operation presentation: keep
      metadata names visible as inert link-colored text without disabled button
      semantics, while target View operations retain navigation buttons.
- [x] Restore `mapview.js`'s async marker lifecycle: redraw the shared Leaflet
      marker layer when child View data arrives and bind popups only when the
      View-derived title or information fields exist.
- [x] Restore `mapview.js` info-window geometry: keep a 240x100 View-derived
      popup body and group metadata details two items per row across desktop
      and mobile map panels.
- [x] Restore template-specific Sudoku headers: use static active tabs for
      List/Map/linechart, the Item panel heading and inert Detail link, and no
      Vue-only parent heading above Group child tabs.
- [x] Restore `subitem.js`'s Item footer: render blank update time and passive
      Refresh text after the six-row View-derived matrix without adding a
      third executable Sudoku refresh command.
- [x] Restore `groupview.js`'s simple-child presentation: render the legacy
      plain `这是简单项` line at the Group tab's top-left instead of centering it
      in a Vue empty-state surface.
- [x] Restore ECharts 3.1.7's default square bar corners: remove the Vue-only
      fixed SVG radius from the shared top-level/Sudoku renderer while keeping
      View-derived series geometry and responsive width behavior unchanged.
- [x] Match the old plain chart mounting surfaces: remove the Vue-only border,
      radius, and padding from the shared top-level/Sudoku chart pane while
      retaining the legacy 200px compact height and existing chart behavior.
- [x] Restore `viewWithChart.js`'s first-data chart-height lock: measure the
      rendered data pane once per View entry, reuse that height in the shared
      chart, and retain readable ECharts-style category-label sampling.
- [x] Restore ECharts 3.1.7's default axis presentation: use its `#333` 1px
      axis, `#333` 12px labels, and `#ccc` 1px split lines in the shared
      top-level/Sudoku renderer and exercise the CSS contract in Vitest.
- [x] Restore ECharts 3.1.7 legend-item presentation: render type-specific
      25x14 bar/line/scatter glyphs with normal 12px text and keep dynamic
      View-derived names outside the plot at narrow widths.
- [x] Restore ECharts 3.1.7 legend hover linking: set and clear one shared
      View-series emphasis state on legend enter/leave while retaining click
      selection and the existing top-level/Sudoku renderer.
- [x] Restore ECharts 3.1.7 scatter-symbol defaults: render metadata scatter
      points at 10 actual pixels with 0.8 opacity across responsive viewBox
      sizes and expand legend-linked emphasis to the old 13-pixel target.
- [x] Restore ECharts 3.1.7 item hover targets: keep the axis-tooltip hit
      surface below bar/scatter graphics, route plot movement through the
      shared SVG, and apply the old direct item emphasis without losing tooltip.
- [x] Restore ECharts 3.1.7 line-area opacity: use the old `LineView` 0.7
      default for both View-backed chart entrypoints while retaining series
      color, legend selection, and responsive geometry.
- [x] Restore ECharts 3.1.7 symbol-less line label behavior: omit value labels
      when `swchartLine.js` sets `symbol: 'none'`, while retaining bar/scatter
      labels and axis-trigger tooltip values in both shared chart entrypoints.
- [x] Restore ECharts 3.1.7 bar/scatter value-label defaults: center 12px white
      labels inside each bar or scatter symbol and retain scatter's 0.8
      opacity across top-level and compact shared chart entrypoints.
- [x] Restore ECharts 3.1.7 chart value-text precision: retain View `FmtValue`
      beside numeric geometry, use raw text for bar labels, numeric text for
      scatter labels, and preserve all decimals in comma-formatted tooltips.
- [x] Restore ECharts 3.1.7 axis-tooltip coordinate bounds: clear the shared
      pointer and tooltip above, below, or beside the actual plot while keeping
      nearest-category movement inside top-level and compact charts.
- [x] Restore ECharts 3.1.7 tooltip refresh after chart resize: replay the last
      chart-local point through shared bounds/category logic so responsive
      top-level and compact charts reposition or clear stale tooltip state.
- [x] Restore ECharts 3.1.7's default tooltip hide delay: keep one cancellable
      100ms timer for plot exit/mouseleave while explicit legend and no-data
      cleanup remains immediate in the shared renderer.
- [x] Restore ECharts 3.1.7 tooltip box and movement defaults: use its block
      layout, 21px rows, 20px pointer gap, nowrap/high-layer surface, and 0.4s
      left/top easing across shared top-level and compact charts.
- [x] Restore `view.jade`'s complete report-result command group: append the
      eventless `导出当前页` / `导出全部` controls after paging without adding a
      request, download path, DTO state, or handler absent from `mkreport.js`,
      and wrap the intact group below the page summary on narrow screens.
- [x] Restore `showerror.js` no-target message-detail availability: keep
      `查看详细` enabled like the old `href='#'` anchor, but leave the current
      dialog and URL unchanged when the message has no target View.
- [x] Restore `message.js` generated-time handling: parse current camel
      LocalDateTime and legacy Pascal `/Date(ms)/` through one shared adapter,
      render local `yyyy-MM-dd hh:mm:ss`, and preserve invalid server text.
- [x] Restore old shell initialization and polling boundaries: render user
      aliases from `getmain`, avoid an immediate post-login message request,
      and let the 15-second timer call only `getmsg` without invented repeated
      `getuserinfo` requests.
- [x] Restore old no-default-View route behavior: successful login replaces
      the path with `/main`, that route keeps `Sudoku.jade` guidance, and Home
      `/` uses `main.jade`'s shorter configuration text through the existing
      `App.DefaultViewId` metadata branch.
- [x] Restore `login.js` Reset state semantics: preserve username, password,
      and database selection while reusing the existing CAPTCHA refresh event;
      omit the old blank error-modal intermediary as a presentation cleanup.
- [x] Restore `login.js` failure feedback timing: adapt legacy error code/message
      before rendering, open the old `发生错误` footer-close dialog, preserve
      login fields, and refresh the CAPTCHA only after dialog dismissal;
      browser-verify the lifecycle at desktop and 390px without overflow.
- [x] Restore `login.js`'s empty HTTP-error callback: suppress the shared
      transport exception after a response-less login attempt while preserving
      fields, CAPTCHA, and response-backed business-error dialog behavior;
      browser-verify the stopped-backend path and restore Compose afterward.
- [x] Restore `index.jade`'s single-database login binding: remove the invented
      multi-database Select and hard-coded `car_wash` frontend fallback, and
      submit an initapp database id only when the View received exactly one;
      browser-verify the backend success request, `/main`, and logout state.
- [x] Restore `index.jade`'s server-owned login validation: remove browser
      `required` constraints from username, password, and CAPTCHA so empty
      submits reach the existing response-backed legacy error lifecycle;
      browser-verify the server request, dialog, and dismissal refresh order.
- [x] Restore `login.js`'s raw input contract: submit username, password, and
      CAPTCHA without frontend trimming, and remove the Vue-only CAPTCHA
      maxlength so server validation receives the operator's exact strings;
      browser-verify the exact backend request and dismissal state.
- [x] Restore `login.js`'s always-available controls: keep the Login label fixed
      and leave Login, Refresh, and Reset enabled during requests while retaining
      only the initialization guard for a missing CAPTCHA key; browser-verify
      all three controls during a real paused-backend login request.
- [x] Restore `login.js`'s silent CAPTCHA-refresh HTTP-error path: suppress only
      response-less `getcheckcode` transport errors while retaining the current
      image, fields, successful replacement, and login business-error dialog;
      browser-verify the settled Nginx `502` path and restored backend.
- [x] Restore `index.jade`'s external vendor-link intent: prefix bare `AppUrl`
      metadata with `http://` while retaining current absolute HTTP(S) values,
      so the footer never resolves a legacy host as an application-relative
      path; browser-verify both forms and restore the Docker metadata row.
- [x] Guard `index.jade`'s implicit Enter login: retain the form submit handler
      and default submit button so Enter fires the old `ng-click="hello()"`
      equivalent; browser-verify that Enter reaches `loginv2` before closing
      this parity slice.
- [x] Restore `tbar.jade`'s always-available shell navigation: keep Home,
      metadata menus, submenus, and both `安全退出` controls actionable while
      unrelated requests are pending; browser-verify the paused-backend path
      without widening the change to View/detail/report controls.
- [x] Restore `view.jade` / `viewWithChart.jade` top-command availability:
      leave Find, Report, and metadata create commands active during unrelated
      View requests while retaining pending protection in row actions, paging,
      Sudoku, detail, and report internals; browser-verify the in-flight state.
- [x] Restore `detailView.jade`'s always-active existing-item picker: keep its
      Find, Select, pagination, close, mask-dismiss, and Cancel interactions
      available while a candidate query is pending; browser-verify against a
      paused-backend `querydata` request without changing View/Data DTOs.
- [x] Restore `detailView.jade`'s always-active child collection commands:
      leave metadata Add, inline Edit/Save, Delete, and existing detail links
      available while unrelated requests are pending; retain the edit-state
      guard and browser-verify without persisting staged child changes.
- [x] Restore `detailView.jade` parent-detail command availability: keep Edit,
      Save, View operations, and metadata lookup editors active during
      unrelated requests while retaining edit-state and own-save protection;
      browser-verify without persisting detail changes.
- [x] Restore `view.jade` row-navigation and `navbar.js` paging availability:
      keep list row links and page commands active during unrelated requests,
      preserve page-boundary and Sudoku refresh guards, and browser-verify with
      temporary local rows that are removed afterward.
- [x] Restore `view.jade` / `mkreport.js` report-command availability: keep
      setup fields and commands active during unrelated requests, and keep
      result paging, Return, and mask dismissal visible during page requests;
      retain initial-generation hiding and the no-close-button result header.
- [x] Restore `GroupViewController` Sudoku refresh availability: keep root and
      grouped-list Refresh commands plus panel auto-refresh active during other
      requests, while retaining map passive-refresh and existing panel timers;
      browser-verify the two visible Refresh commands on `/view103`.
- [x] Restore `timer.js` main View auto-refresh concurrency: let scheduled
      `querylistdata` refresh run during other requests while retaining timer
      cleanup, page reset, and the existing metadata-driven interval; verify
      overlapping requests with a temporary one-second View interval.
- [x] Restore `querylistdata.js`'s visible-table timer gate: pause only the
      scheduled `viewWithChart` refresh while its Data pane is hidden, while
      keeping manual Find active on the Chart pane and preserving normal View
      timer concurrency; browser-verify with a temporary one-second interval.
- [x] Restore `ViewWithChartController.query` paging: keep a chart View's
      current page when Find directly invokes `querylistdata.query`, while
      retaining page-one reset for plain View Find and scheduled refresh;
      browser-verify on a temporary second page and remove the seed rows.
- [x] Restore `timer.js` registration cadence: keep one one-second ticker for
      an unchanged `AutoFreshTime`, preserve its old first-tick counter, and do
      not restart it after manual Find, paging, or same-interval responses;
      browser-verify initial and repeated one-second timestamps.
- [x] Restore `querylistdata.js`'s silent HTTP-error path: keep existing rows,
      pagination, and timer state without showing Vue's shared error message
      when a main View query has a network/non-2xx failure, while retaining
      response-backed business errors; browser-verify with the backend stopped.
- [x] Restore the detail candidate picker's shared `querylistdata.js` silent
      HTTP-error path: keep candidate rows, record count, and paging without a
      shared error on data-query transport failure, while retaining View-load
      and response-backed business errors; browser-verify with backend stopped.
- [x] Restore success-only Sudoku panel transport handling across legacy
      `querylistdata.js`, `groupview.js`, and `ServerUtil.js` paths: preserve
      loaded List/Group/Map/Item/chart content without a shared transport error,
      retain business errors, and browser-verify a stopped-backend Refresh.
- [x] Restore `querylistdata.js` response-backed error presentation: replace the
      main View's inline Message with the shared `发生错误` / `关闭` dialog, reuse
      it for detail errors, retain silent transport handling, and browser-verify
      an HTTP-200 nonzero-code View failure plus dismissal.
- [x] Restore `mkreport.initquery`'s success-only transport behavior: keep the
      report setup closed without shared or local error feedback when its
      View-derived column request has a network/non-2xx failure, then
      browser-verify failure and recovery without changing report DTOs.
- [x] Restore `mkreport` generation's success-only transport behavior: keep
      setup/result dialogs hidden after an initial report failure, retain the
      current result during paging failure, suppress transport feedback, and
      browser-verify initial failure plus successful recovery.
- [x] Restore `timer.js` / `message.js` polling concurrency: let each 15-second
      tick start `getmsg` even while an earlier poll is pending, retain silent
      transport failures and session timer cleanup, and browser-verify
      overlapping requests against a paused backend.
- [x] Restore `operation.js`'s success-only transport path: suppress shared
      detail errors and keep the result dialog closed when `runoperation` has a
      network/non-2xx failure, then browser-verify failure and successful
      recovery without changing the View-derived operation payload.
- [x] Restore `menuinfo.js`'s success-only submenu transport path: keep the
      metadata parent expanded without a shared error when `getsubmenu` has a
      network/non-2xx failure, then browser-verify empty expansion and
      successful child recovery without changing menu metadata or routes.
- [x] Restore `menuinfo.js`'s success-only logout transport path: retain the
      authenticated View, token, and URL without a shared error when logout has
      a network/non-2xx failure, then browser-verify successful retry returns to
      `/` and refreshes the login flow.
- [x] Restore `setextype.js`'s success-only BusinessObject lookup transport
      path: keep the View-derived lookup input without inline error feedback on
      network/non-2xx failure, retain response-backed errors, and browser-verify
      successful candidate recovery without changing the lookup DTO.
- [x] Restore the signed-out `index` / `soway.initapp` transport surface: keep
      Vue's static login shell without a browser error dialog when initial app
      metadata has a network/non-2xx failure, then browser-verify real Nginx
      HTTP 502/504 and live backend reload recover metadata/CAPTCHA/login.
- [x] Restore `setextype.js`'s success-only enum-option transport path: keep the
      View-derived detail editor without a shared error dialog when `getenums`
      has a network/non-2xx failure, retain response-backed enum errors/cache,
      and browser-verify a successful retry after recovery.
- [x] Restore the authenticated `index` / `soway.getmain` transport surface:
      preserve the current token, URL, and shell without an error dialog on
      network/non-2xx failure, retain stale-token return to login, and
      browser-verify HTTP 502 plus both success and business-error recovery.
- [x] Restore `detailview.js beginsave()` transport handling: keep the
      non-dismissible `保存中` dialog and suppress shared transport feedback
      when `saveobj` / `savenewobj` has a network/non-2xx failure, retain
      response-backed business errors, and browser-verify both failure branches
      plus successful back-navigation recovery without data drift.
- [x] Restore `detailview.js initQueryView()` transport handling: keep the
      non-dismissible `加载中` dialog open without shared transport feedback
      when the child candidate `getlistview` request has a network/non-2xx
      failure, then browser-verify View-first recovery before candidate data.
- [x] Restore the server-rendered read-item View metadata transport surface:
      suppress shared feedback when `getreaditemview` has a network/non-2xx
      failure on item, detail, or new routes, retain the View-before-data gate,
      and browser-verify all three failures plus successful recovery.
- [x] Restore the server-rendered detail/new data transport surface: suppress
      shared feedback when `querydatadetail` or `initnew` has a network/non-2xx
      failure after View metadata succeeds, retain business errors, and
      browser-verify both failures plus successful recovery.
- [x] Restore the server-rendered list View metadata transport surface:
      suppress shared feedback when `getlistview` has a network/non-2xx failure
      on `/`, `/main`, or `/view:id`, retain business errors and the
      View-before-data gate, and browser-verify all failures plus recovery.
- [x] Restore `view.jade`'s inert report-save command: keep its report-name
      field and visible enabled footer button, remove the Vue-only `saverpt`
      dispatch and status feedback, and browser-verify a click emits no request.
- [x] Restore the static `view.jade` report draft lifecycle: retain selected
      outputs, conditions, report name, and active tab across Cancel/reopen,
      reload candidate metadata on each open, and reset the draft only when
      navigating to another View.
- [x] Preserve `mkreport.initquery`'s untouched output controls across report
      reopen: keep the output-method options, selected method, and selected
      output index while the candidate list rebuilds to its first View column.
- [x] Expose the old Sudoku Item `POST /itemview` data route as
      `/api/v1/data/itemview`, reusing `querydatadetail` and accepting the real
      `id` / `objid` / `idxep` request; guard it in runtime doctor.
- [x] Restore `layout.jade` / `default.jade` application browser-title
      rendering: use `initapp.AppName` while signed out and
      `getmain.App.AppName` after authentication, without deriving page
      metadata from View/data DTOs.
- [x] Separate old report candidate lifecycles: preserve the selected candidate
      across result Return, rebuild it to the first View column only on a new
      metadata load, and keep output controls untouched in both paths.
- [x] Restore default Bootstrap dismissal for report results: reopen setup after
      mask/Escape close without resetting the report page, while keeping
      explicit Return as the only reset to page one.
- [x] Restore `ShowReportController` paging-failure page state: advance the
      visible requested page before transport settles, retain prior result
      cells on failure, and keep next/previous boundaries on that page index.

## Backlog
- Close the unchecked browser-acceptance items above, then continue from
  `docs/migration/foolframe-parity.md` remaining migration work.

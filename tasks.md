# Tasks

This file is the repo-local work-state surface until an external tracker is
explicitly named as authoritative for fool-service.

## Current Focus

- [x] Bootstrap harness and Standard Engine entrypoints.
- [x] Add a repo-local validation matrix and checker.
- [x] Add runtime evidence bundles for Docker/browser smoke checks after the
      Docker stack stabilizes.
- [x] Render persisted `Order Items` in the Vue `OrderList` detail workflow via
      backend `querydatadetail` collection rows.
- [x] Replace the hand-shaped Vue `OrderList` screen with View metadata-driven
      list/detail/child-row rendering.
- [x] Add basic select-from-existing child collection support for configured
      Vue detail groups.
- [x] Render enum detail fields as metadata-driven selects in the Vue View
      workflow.
- [x] Render readonly View fields as locked controls and skip them from legacy
      save payloads.
- [x] Apply legacy formatted row classes from View data in the Vue View
      workflow.
- [x] Add metadata-driven lookup editors for BusinessObject fields.
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
- [x] Drive the main Vue workflow through legacy `getlistview(viewId)` then
      `querydata(viewId)` so the rendered page follows View metadata instead
      of the newer business-name query shortcut.
- [x] Load the Vue first-screen View id from legacy `getmain/getapp`
      `App.DefaultViewId` before `getlistview`, so the default page follows
      FoolFrame shell configuration instead of a Docker seed DTO shortcut.
- [x] Hydrate legacy `SW_SYS_OPERATIONVIEW_ITEM` operation parameter metadata
      into View operation DTOs for both list-view and detail-view responses.
- [x] Persist dynamic `BusinessObject` fields through their foreign-key ids so
      legacy `runoperation` update saves stay metadata-driven and do not bind
      to concrete business DTOs.
- [x] Render View metadata operation buttons in the main Vue workflow and
      execute them through legacy `runoperation`.
- [x] Remove the Vue workspace's visible business-name `get-view/query-list`
      shortcuts so View rendering and data loading stay `viewId` driven.
- [x] Add a repository harness guard for oversized source files.
- [x] Accept legacy Pascal `inputquery` request fields at the protocol DTO
      boundary without introducing concrete business DTO binding.
- [x] Remove the unused Vue business-name `query-list` payload builder so the
      frontend keeps a single view-id driven data-loading path.
- [x] Move reusable Vue child-group View workflow helpers out of `App.vue`
      into `viewWorkflow.ts`.
- [x] Move report-grid cell matrix rendering into a tested Vue workflow helper
      and keep `App.vue` below 2000 lines.
- [x] Tighten the repository source-size harness limit from 2200 to 2100 lines.
- [x] Accept FoolFrame Pascal `getlistview` / `querydata` request fields at
      the generic View/data protocol boundary without binding to business DTOs.
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
- [x] Let lookup `inputquery` resolve the current View by `ViewId` before
      falling back to legacy `ViewName`, and have Vue metadata editors pass
      the loaded View id so lookup follows the rendered View context.
- [x] Remove the Vue metadata lookup component's `viewName` fallback so
      BusinessObject candidate lookup stays tied to the rendered View id.
- [x] Add a Docker runtime doctor for the stable Vue/View workflow so
      backend, frontend proxy, View metadata, data query, inputquery, and
      report model smoke checks are repeatable.
- [x] Make generic `get-view` / `query-list` and the Vue main detail/save
      refresh path prefer the loaded `ViewId`, keeping `ViewName` only as
      compatibility fallback.
- [x] Accept FoolFrame Pascal `SaveObj` and nested save payload fields at the
      generic legacy `saveobj` DTO boundary without binding saves to concrete
      business DTOs.
- [x] Accept legacy `getenums` model-id spellings from both FoolFrame Web
      proxy forms without changing the enum lookup contract.
- [x] Expose legacy `getenums` response aliases (`EnumValues`, `Name`,
      `Value`) while keeping the Vue camel-case enum contract.
- [x] Expose legacy `querydatadetail` response aliases for detail payloads
      while keeping the Vue camel-case detail contract.
- [x] Tighten the Vue main View workflow layout so loaded View rows and
      `querydatadetail` detail fields render in the first usable screen on
      desktop and remain scrollable on narrow screens.
- [x] Execute legacy model SAVE trigger `SET_VALUE` side effects through
      `ModelDataService` writes, reusing the shared operation command value
      resolver instead of adding a second expression parser.
- [x] Make the Vue API-tools `Query Data` path load the selected View
      definition before calling `querydata`, so shared row state stays bound to
      the rendered View context instead of a standalone business DTO shortcut.
- [x] Execute legacy property `SET` trigger `SET_VALUE` side effects through
      dynamic `ModelDataService` create/save writes without introducing
      concrete business DTO binding.
- [x] Execute legacy collection `ItemsAdd` / `ItemsDelete` property trigger
      `SET_VALUE` side effects during dynamic collection writes.
- [x] Accept legacy Pascal `querydatadetail` `ViewId` / `ObjId` fields at the
      generic detail-data protocol boundary and cover it in the Docker runtime
      doctor.
- [x] Render detail View operation buttons from the loaded `querydatadetail`
      operations payload instead of the list View metadata, keeping detail
      actions bound to the rendered detail View context.
- [x] Render list View create operations from metadata and use their target
      `ViewId` for the Vue `initnew` / `savenewobj` flow.
- [x] Render list row operations with target `ViewId` and keep selected detail
      load/save/lookup/operation execution bound to the active detail View.
- [x] Render list row operations without a target `ViewId` as disabled
      metadata actions instead of dropping them from the View-rendered page.
- [x] Render View workflow paging controls from legacy `querydata` totals and
      keep page navigation on the loaded `ViewId`.
- [x] Reset the main View workflow search/load action to page 1 before
      re-querying, matching FoolFrame list search behavior.
- [x] Use the rendered View's `DetailViewId` for default Vue row open and
      fallback new-row initialization, so detail data stays bound to the View
      metadata context instead of the list View or a concrete business DTO.
- [x] Stop deriving the main Vue list columns from row `values` DTO keys; when
      View columns are absent, fallback columns now come from legacy row
      `Items` metadata.
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
- [x] Keep report-grid row projection sourced from legacy row `Items` instead
      of `row.values` DTO maps.
- [x] Extend the Docker runtime doctor to prove legacy `getrpt` returns
      report-grid cells through the Vue proxy.
- [x] Extend the Docker runtime doctor to prove legacy `saverpt` returns the
      FoolFrame no-op success surface through the Vue proxy.
- [x] Remove hard-coded seed object IDs from Vue API-tool defaults and make
      runtime detail smoke use the object id returned by `querydata`.
- [x] Extend the Docker runtime doctor to prove the legacy auth first-hop
      path through the Vue proxy: `initapp`, `getcheckcode` / `checkcode`,
      `loginv2`, and `getuserinfo`.
- [x] Extend the Docker runtime doctor to prove the logged-in legacy auth
      shell path through the Vue proxy: `getapp`, `getmain`, `getsubmenu`,
      and `logout`.

## Backlog

- [ ] Add focused Maven module profiles once module boundaries are cleaned up.
- [ ] Add contract checks for package boundaries and migration drift.

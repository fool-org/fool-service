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

## Backlog

- [ ] Add focused Maven module profiles once module boundaries are cleaned up.
- [ ] Add contract checks for package boundaries and migration drift.
- [ ] Add a browser/runtime doctor when the frontend and backend API workflow
      becomes stable enough for repeatable smoke automation.

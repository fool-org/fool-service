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

## Backlog

- [ ] Add candidate search/pagination for select-from-existing child collection
      dialogs.
- [ ] Add richer metadata-specific editors for enum, readonly, lookup, and
      formatted field types.
- [ ] Add focused Maven module profiles once module boundaries are cleaned up.
- [ ] Add contract checks for oversized files, package boundaries, and migration
      drift.
- [ ] Add a browser/runtime doctor when the frontend and backend API workflow
      becomes stable enough for repeatable smoke automation.

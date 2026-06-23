# Tasks

This file is the repo-local work-state surface until an external tracker is
explicitly named as authoritative for fool-service.

## Current Focus

- [x] Bootstrap harness and Standard Engine entrypoints.
- [x] Add a repo-local validation matrix and checker.
- [ ] Add runtime evidence bundles for Docker/browser smoke checks after the
      Docker stack stabilizes.

## Backlog

- [ ] Add focused Maven module profiles once module boundaries are cleaned up.
- [ ] Add contract checks for oversized files, package boundaries, and migration
      drift.
- [ ] Add a browser/runtime doctor when the frontend and backend API workflow
      becomes stable enough for repeatable smoke automation.

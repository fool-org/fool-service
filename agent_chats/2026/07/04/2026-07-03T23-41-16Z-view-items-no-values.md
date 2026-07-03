# View Items Without Values Fallbacks

## Prompt

The rendered page should load View metadata first, then query data from that
View context. Binding the page to concrete business DTO fields is wrong.

## Scope

- Remove `row.values` from Vue View row identity, table-cell rendering, and
  selected-existing child save draft mapping.
- Keep generic response DTO shapes intact for protocol compatibility.

## Changes

- `rowObjectId` now uses only the protocol row id or legacy row `Items`.
- Added `rowRenderKey` so Vue table keys can fall back to row index without
  treating that index as an object id for detail queries.
- `rowValue` resolves cells by matching loaded View columns to legacy
  `querydata.Items`, including `propertyName` / `property` aliases.
- `buildDraftsFromRow` maps selected-existing child rows from row `Items` by
  key/column/index and no longer falls back to DTO `values`.
- Updated Vue helper tests for conflicting `values` payloads.

## Validation

- Passed: `cd frontend && npm test`.
- Passed: `cd frontend && npm run build`.
- Passed: `python3 scripts/check_repo_harness.py`.
- Passed: `git diff --check`.

## Runtime Evidence

- Rebuilt and restarted the Compose frontend:
  `docker compose up -d --no-deps --build frontend`.
- Passed: `python3 scripts/runtime_doctor.py`.

## Risks

- If a non-legacy query response omits `Items`, the View table will not infer
  columns or cells from DTO `values`; that is intentional for the migrated View
  workflow.

## Follow-ups

- Rebuild/restart the Compose frontend and run the runtime doctor before
  delivery.

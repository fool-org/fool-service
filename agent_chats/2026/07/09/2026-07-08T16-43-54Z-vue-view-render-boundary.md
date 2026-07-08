# Vue View Render Boundary

## Prompt

User noted that the frontend should render from View first, then query data
from that View, and that binding rendering to concrete business DTOs is wrong.

## Scope

- Tightened Vue table column rendering so columns come only from loaded View
  metadata.
- Removed the remaining fallback from `querydata.Cols` and first-row `Items`
  for main list and child candidate tables.
- Kept data payloads as row/value sources after View metadata is loaded.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Red: `cd frontend && npm test` failed on the new expectation that empty View
  columns must not be filled from `querydata.Cols`.
- Green: `cd frontend && npm test` passed after removing the fallback.

## Runtime Artifacts

None. This slice changes frontend render-boundary logic and tests only.

## Risks

- A malformed or incomplete View definition now renders no columns instead of
  using data DTO fallbacks. That is intentional for view-first migration
  parity, but it means bad View metadata is visible immediately.

## Follow-ups

- Continue AppInstall/View metadata parity so runtime Views always provide the
  columns the Vue workflow needs.

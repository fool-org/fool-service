# Vue Detail View Boundary

## Prompt

User pointed out that the page should render from View metadata first, then
query data through that View, and binding rendered UI to concrete business DTOs
is wrong.

## Scope

- Removed the frontend fallback where empty or missing read-item View metadata
  let raw `querydatadetail` DTO fields define detail fields.
- Removed the frontend fallback where raw DTO child groups/properties could
  define child view columns.
- Rendered API-tool detail/initnew tables through read-item View metadata
  merged with data values, matching the main View workflow.
- Updated parity docs and repo task state.

## Changed Files

- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Red: `npm test -- --run src/viewWorkflow.test.ts` failed because
  `renderedDetailFields({ Items: [] }, dataFields)` and
  `renderedDetailGroups({ DetailViews: [] }, dataGroups)` still returned raw
  DTO data.
- Red: `npm test -- --run src/payload.test.ts` failed because `App.vue` still
  rendered API-tool detail rows from `detailDataRows`.
- Green: `npm test -- --run src/viewWorkflow.test.ts` passed, 27 tests.
- Green: `npm test -- --run src/payload.test.ts` passed, 53 tests.
- Frontend: `npm test` passed, 4 files / 83 tests.
- Frontend: `npm run build` passed.
- Harness: `python scripts/check_repo_harness.py` passed.
- Whitespace: `git diff --check` passed.

## Runtime Artifacts

None. This was a Vue helper/render-boundary change with automated coverage.

## Risks

- A malformed or incomplete read-item View now renders no detail fields or child
  columns instead of falling back to raw data DTO shape. That is intentional for
  View-first parity, but it makes missing metadata visible.

## Follow-ups

- Keep moving remaining manually driven API-tool surfaces toward the same
  `get...view(ViewId)` before data-query pattern.

# Select Existing View Columns

## Prompt

Continue the View-first frontend migration and avoid binding child workflows to
concrete business DTO fields.

## Scope

- Keep select-existing child row mapping tied to the rendered candidate View
  columns.
- Do not change backend candidate lookup or save semantics in this slice.

## Changes

- Added a regression test where a candidate row has both a same-key DTO item and
  a rendered candidate View column value.
- Changed `buildDraftsFromRow` so candidate View column matches win before
  same-key raw row items when building child `AddedItems` drafts.
- Updated migration parity docs and task state.

## Validation

- RED: `npm test -- --run src/viewWorkflow.test.ts` failed because `itemId`
  came from the same-key DTO row item instead of the candidate View `id` column.
- GREEN: `npm test -- --run src/viewWorkflow.test.ts`.
- PASS: `npm test`.
- PASS: `npm run build`.
- PASS: `python scripts/check_repo_harness.py`.
- PASS: `git diff --check`.
- PASS: `docker compose build frontend`.
- PASS: `docker compose up -d frontend`.
- PASS: `python scripts/runtime_doctor.py`.

## Runtime Evidence

- `curl -fsS http://localhost:8081/` loaded frontend bundle
  `index-BBi4GJzx.js`.
- `docker compose ps` showed backend, rebuilt frontend, MySQL, and Redis
  running; MySQL and Redis were healthy.

## Risks

- This is frontend mapping scope only. If a configured candidate View omits a
  column, the helper still preserves the previous same-key and row-index
  fallbacks.

## Follow-ups

- Continue the remaining migration work from `docs/migration/foolframe-parity.md`.

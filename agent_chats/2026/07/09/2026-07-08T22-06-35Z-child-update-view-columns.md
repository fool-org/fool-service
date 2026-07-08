# Child Update View Columns

## Prompt

Continue the migration toward a View-first Vue frontend, avoiding concrete
business DTO binding and keeping code reuse tight.

## Scope

- Keep existing child-row update save payloads tied to rendered child View
  metadata.
- Do not change backend save semantics or child delete behavior in this slice.

## Changes

- Added a regression test proving existing child updates use rendered child
  group columns instead of `querydatadetail` row DTO values.
- Changed `buildUpdatedItemProperty` to build `Propertyies` from
  `groupColumns(group)`.
- Updated migration parity docs and task state.

## Validation

- RED: `npm test -- viewWorkflow.test.ts` failed because `dtoOnly=leak` was
  emitted and the rendered `itemName` field was missing.
- GREEN: `npm test -- viewWorkflow.test.ts`.
- PASS: `npm test`.
- PASS: `npm run build`.
- PASS: `python scripts/check_repo_harness.py`.
- PASS: `git diff --check`.
- PASS: `docker compose build frontend && docker compose up -d frontend`.
- PASS: `python scripts/runtime_doctor.py`.

## Runtime Evidence

- `curl -fsS http://localhost:8081/` loaded frontend bundle
  `index-A9NJnekA.js`.
- Frontend proxy `POST /api/v1/view/getlistview` with `{"ViewId":100}`
  returned `"ViewId":100`.
- `docker compose ps` showed backend, frontend, MySQL, and Redis running.

## Risks

- This slice only changes existing child-row update payloads. Delete payloads
  still follow the previous minimal id/data path because backend deletion only
  needs the child item id for the current migration surface.

## Follow-ups

- Continue the remaining migration work from `docs/migration/foolframe-parity.md`.

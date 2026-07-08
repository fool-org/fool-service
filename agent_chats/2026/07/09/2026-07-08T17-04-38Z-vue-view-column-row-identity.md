# Vue View Column Row Identity

## Prompt

Continue the Docker/Vue/FoolFrame migration, keeping the frontend View-driven
instead of binding row rendering to business DTO shape.

## Scope

- Changed Vue list row fallback identity to prefer loaded View columns before
  raw `querydata.Items` order when the legacy `querydata.Id` value is absent.
- Passed the rendered columns into `ListDataTable` row keys so selection and
  DOM identity share the same View-driven fallback.
- Updated migration parity and task state.

## Changed Files

- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/ListDataTable.vue`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Red: `npm test -- src/viewWorkflow.test.ts` failed with
  `expected 'wrong-first-item' to be 'view-column-id'`.
- Green: `npm test -- src/viewWorkflow.test.ts` passed.
- Frontend: `npm test` passed.
- Frontend build: `npm run build` passed.

## Runtime Artifacts

None. This is a frontend helper/component behavior fix.

## Risks

- If a legacy `querydata` row omits both top-level `Id` and any View-column
  matched value ids, the fallback remains the first formatted row value.

## Follow-ups

- Continue removing remaining View/data coupling as concrete legacy parity
  gaps are found.

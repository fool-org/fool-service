# Vue Child Group Pascal Aliases

## Prompt

Continue the FoolFrame Docker/Vue migration, keep commits atomic, maximize
reuse, and keep View rendering ahead of data/DTO binding.

## Scope

- Compared the frontend child-group helpers with the backend
  `QueryDataDetailResult.PropertyDataItems` Pascal aliases.
- Added Pascal child-group aliases to the Vue API type surface.
- Reused the existing `firstList` / helper flow so `PrpId`, `Name`,
  `ItemName`, `ListViewId`, `SelectedView`, `Items`, and `Properties` work
  without component-level special cases.
- Updated migration docs and task state.

## Changed Files

- `frontend/src/api.ts`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Red: `npm test -- viewWorkflow.test.ts` failed because Pascal
  `SelectedView` returned `0` instead of the target child View id.
- Green: `npm test -- viewWorkflow.test.ts` passed.
- Frontend: `npm test` passed.
- Build: `npm run build` passed.
- Harness: `python scripts/check_repo_harness.py` passed.
- Whitespace: `git diff --check` passed.

## Runtime Evidence

`docker compose ps` showed MySQL, Redis, backend, and frontend up. No Docker
runtime doctor was used for this slice. The change is covered by the frontend
View workflow helper test and does not require a backend container rebuild.

## Risks

- This keeps camel-case fields preferred when both aliases are present. That
  matches the existing helper pattern and avoids changing current payload
  precedence.

## Follow-ups

- Continue reducing remaining Vue protocol edges through shared helpers rather
  than page/component-specific branches.

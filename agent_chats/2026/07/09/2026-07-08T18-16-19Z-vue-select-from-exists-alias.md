# Vue Select From Exists Alias

## Prompt

Continue the FoolFrame Docker/Vue migration, keep commits atomic, maximize
reuse, and keep View rendering ahead of data/DTO binding.

## Scope

- Found the Vue child collection template still reading camel-only
  `group.selectFromExists`.
- Added a shared `groupSelectFromExists` helper that reads camel or FoolFrame
  Pascal `SelectFromExists`.
- Routed the template through the helper so select-existing controls are not
  hidden for legacy Pascal `querydatadetail` payloads.
- Updated migration docs and task state.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Red: `npm test -- viewWorkflow.test.ts` failed because
  `groupSelectFromExists` was missing.
- Red: `npm test -- payload.test.ts` failed because the template still read
  `group.selectFromExists`.
- Green: `npm test -- viewWorkflow.test.ts` passed.
- Green: `npm test -- payload.test.ts` passed.
- Frontend: `npm test` passed.
- Build: `npm run build` passed.
- Harness: `python scripts/check_repo_harness.py` passed.
- Whitespace: `git diff --check` passed.

## Runtime Evidence

`docker compose ps` showed MySQL, Redis, backend, and frontend up. No Docker
runtime doctor was used for this slice. The change is covered by the frontend
View workflow helper and payload source tests and does not require a backend
container rebuild.

## Risks

- The helper keeps camel-case `selectFromExists` preferred when both aliases
  are present, matching the existing alias precedence pattern.

## Follow-ups

- Continue moving remaining protocol alias reads into shared helpers instead
  of Vue template expressions.

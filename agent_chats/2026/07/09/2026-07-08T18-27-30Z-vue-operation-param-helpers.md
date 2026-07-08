# Vue Operation Parameter Helpers

## Prompt

Continue the FoolFrame Docker/Vue migration, keep commits atomic, maximize
reuse, and keep View rendering ahead of data/DTO binding.

## Scope

- Found detail View operation parameter rendering still reading
  `operation.params` and `param.paramName` directly in `App.vue`.
- Added Pascal operation parameter aliases to the Vue protocol type.
- Added shared helpers for operation params, parameter keys, and parameter
  labels.
- Routed the detail operation parameter template through those helpers.
- Updated migration docs and task state.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/api.ts`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Red: `npm test -- viewWorkflow.test.ts` failed because `operationParams`
  was missing.
- Red: `npm test -- payload.test.ts` failed because `App.vue` still rendered
  operation parameter fields directly.
- Green: `npm test -- viewWorkflow.test.ts` passed.
- Green: `npm test -- payload.test.ts` passed.
- Frontend: `npm test` passed.
- Build: `npm run build` passed.
- Harness: `python scripts/check_repo_harness.py` passed.
- Whitespace: `git diff --check` passed.

## Runtime Evidence

- `docker compose ps` showed MySQL, Redis, backend, and frontend up.
- `curl http://localhost:8081/` returned HTTP 200.
- `curl http://localhost:8080/test` returned HTTP 200.
- No Docker rebuild was used for this slice because only Vue source,
  protocol types, and migration evidence changed.

## Risks

- Parameter key fallback uses the render index only after protocol ids/names,
  matching Vue's need for a stable-enough key when legacy metadata is sparse.

## Follow-ups

- Continue removing remaining page-level direct protocol alias reads where a
  shared helper already exists or can be added without widening the design.

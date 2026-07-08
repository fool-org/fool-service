# Vue Child Group Shared Helpers

## Prompt

Continue the FoolFrame Docker/Vue migration, keep commits atomic, maximize
reuse, and keep View rendering ahead of data/DTO binding.

## Scope

- Found Vue child group rendering and child-item save logic still reading
  group/item DTO fields directly.
- Added shared helpers for child group title, child group rows, and item data
  ids.
- Routed the Vue detail child group template and child-item save payload
  builders through the shared helpers.
- Updated migration docs and task state.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Red: `npm test -- viewWorkflow.test.ts` failed because `groupTitle` was
  missing.
- Red: `npm test -- payload.test.ts` failed because `App.vue` still rendered
  child group fields directly.
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
- No Docker rebuild was used for this slice because only frontend source and
  migration evidence changed.

## Risks

- `groupKey` remains the fallback identity when child group metadata is
  incomplete, matching the existing helper behavior.

## Follow-ups

- Continue moving remaining protocol alias reads into shared helpers instead
  of Vue template expressions.

# Frontend InputQuery ViewId Type

## Prompt

Continue the FoolFrame migration with Vue on the View-first path: render View
metadata first, then query data/lookup candidates by that View context.

## Scope

- Removed the stale `viewName` field from Vue's `InputQueryRequest` type.
- Added a frontend guard test so the API type cannot reintroduce a
  `ViewName` lookup shortcut while payload builders stay ViewId-driven.
- Left backend legacy `inputquery` request DTO compatibility untouched.

## Changed Files

- `frontend/src/api.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-08T19-20-30Z-frontend-inputquery-viewid-type.md`

## Validation

- RED: `npm test -- payload.test.ts`
  - Failed because `InputQueryRequest` still declared `viewName?: string`.
- GREEN: `npm test -- payload.test.ts`
  - `57` focused tests passed.
- GREEN: `npm run build`
  - `vue-tsc --noEmit && vite build` succeeded.
- GREEN: `npm test`
  - `92` frontend tests passed.
- GREEN: `python scripts/check_repo_harness.py`
  - Repository harness validation passed.
- GREEN: `git diff --check`

## Risks

- Frontend code that was manually constructing `InputQueryRequest.viewName`
  now fails TypeScript checks. That matches the migrated Vue contract: lookup
  requests should use the rendered View id.

# Remove Query-List Builder

## Prompt

- Continue the migration while keeping the frontend view-first and data-second.
- Avoid binding the Vue workflow to concrete business DTOs.
- Keep the change small and remove residual code instead of adding abstractions.

## Scope

- Removed the unused `buildQueryRequest` helper for the newer
  business-name `/api/v1/data/query-list` request shape.
- Removed the unused `QueryRequestInput`, `VisibleFilterInput`, `QueryRequest`,
  `parseFilter`, `buildVisibleFilter`, and `compactValues` code.
- Removed tests that only exercised the deleted dead helper.
- Preserved `buildInputQueryRequest`, because FoolFrame lookup remains keyed by
  the selected View and View item.

## Changed Files

- `frontend/src/payload.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/04/2026-07-03T19-29-03Z-remove-query-list-builder.md`

## Validation

- `cd frontend && npm test && npm run build`
  passed with 3 Vitest files / 42 tests and a successful Vite production build.
- `python3 scripts/check_repo_harness.py`
  passed.
- `git diff --check`
  passed with no output.
- `rg -n "buildQueryRequest|QueryRequestInput|VisibleFilterInput|/api/v1/data/query-list|/api/v1/view/get-view" frontend/src --glob '!**/node_modules/**'`
  only found `inputquery` names and negative test assertions for the removed
  routes/builder.

## Risks / Follow-ups

- The backend still exposes `get-view` and `query-list` for compatibility and
  smoke coverage; this slice only removes the unused Vue request builder so the
  operator workflow does not drift back to the shortcut.

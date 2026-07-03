# View Workflow Helper Extraction

## Prompt

- Continue the Docker/FoolFrame/Vue migration.
- Keep file size under control and reuse existing code.
- Do not add speculative abstractions.

## Scope

- Moved child-group View workflow helper logic from `App.vue` to
  `viewWorkflow.ts`.
- Reused the existing `displayValue` helper instead of keeping a local
  `formatValue` wrapper.
- Added one focused Vitest case for group key, selected child View id, and
  empty child draft behavior.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/04/2026-07-03T19-33-14Z-view-workflow-helper-extraction.md`

## Validation

- `cd frontend && npm test && npm run build`
  passed with 3 Vitest files / 43 tests and a successful Vite production build.
- `wc -l frontend/src/App.vue frontend/src/viewWorkflow.ts frontend/src/viewWorkflow.test.ts`
  showed `App.vue` at 2001 lines after the extraction.
- `python3 scripts/check_repo_harness.py`
  passed.
- `git diff --check`
  passed with no output.
- `rg -n "function groupKey|function selectedChildViewId|function emptyGroupDraft|formatValue" frontend/src/App.vue frontend/src/viewWorkflow.ts frontend/src/viewWorkflow.test.ts`
  showed the child-group helpers only in `viewWorkflow.ts`.

## Risks / Follow-ups

- This is a small extraction only. `App.vue` is still too large; keep moving
  only obvious View workflow helpers/components out as they become shared or
  independently testable.

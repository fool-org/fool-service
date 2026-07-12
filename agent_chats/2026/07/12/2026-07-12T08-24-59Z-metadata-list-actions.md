# Metadata List Actions

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with the old
  FoolFrame Web pages, using an atomic commit for each change.

## Scope

- Remove the fallback `New Row` command when View metadata declares no create
  operation.
- Remove the shared table's default `Open` action from main lists.
- Keep child existing-item selection as an explicit shared-table opt-in.

## Changed Files

- `frontend/src/ViewListPanel.vue`
- `frontend/src/ListDataTable.vue`
- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T08-24-59Z-metadata-list-actions.md`

## Validation

- `cd frontend && npm test && npm run build`
  - 131 tests passed.
  - Vue TypeScript checking and Vite production build passed.
- `python scripts/check_repo_harness.py`
  - Repository harness passed.
- `git diff --check` passed.

## Runtime Evidence

- No Docker/browser artifact was produced because this slice only removes
  undeclared frontend fallback commands; View operation request behavior did
  not change.

## Risks And Follow-Ups

- Views with no declared operation now intentionally expose no create or row
  command, matching the old Web metadata contract.
- Child `DetailViewId` navigation and operation-result feedback remain the next
  independent parity slices.

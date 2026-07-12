# Restore Inline Child Add

## Prompt

Keep old interaction logic aligned while allowing visual modernization, keep
files focused, reuse existing state, and commit each behavior atomically.

## Scope

- Remove the Vue-only persistent child-add form.
- Insert a metadata-shaped blank row and enter its row editor on Add.
- Stage the previously edited row before adding another inline row.
- Add the new row to `AddedItems` only when row Save runs.
- Remove the now-unused separate new-child draft state and CSS.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/useChildDrafts.ts`
- `frontend/src/useChildDrafts.test.ts`
- `frontend/src/usePendingChildChanges.ts`
- `frontend/src/usePendingChildChanges.test.ts`
- `frontend/src/payload.test.ts`
- `frontend/src/style.css`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T12-51-00Z-restore-inline-child-add.md`

## Validation

- `cd frontend && npm test -- --run`: 10 files, 151 tests passed, including
  unstaged blank-row rendering/discard, previous-row staging, added-row lookup
  context, and source-level removal of the persistent add form.
- `cd frontend && npm run build`: TypeScript and Vite production build passed.
- `docker compose build frontend`: passed.
- `docker compose up -d --no-deps frontend`: current frontend deployed on port
  8081.
- `python scripts/runtime_doctor.py`: all checks passed, including
  `saveobj-addeditems` and surrounding View-first detail routes.
- `python scripts/check_repo_harness.py`: passed.
- `docker compose ps -a`: backend/frontend are up, MySQL/Redis are healthy, and
  `db-migrate` is `Exited (0)`.

## Source Evidence

- `detailview.js additem(...)` appends a blank `data-operation="new"` row and
  immediately calls `edititem(..., 'new')`.
- `edititem(...)` stages the previously edited row before switching rows and
  adds the new row to `AddedItems` only when its row action changes to Save.
- `beginsave()` remains the only parent persistence request.

## Risks And Follow-Ups

- Add controls remain limited to parent edit mode; old Jade renders the link
  before parent edit even though inline editing itself requires edit mode.
- Authenticated browser acceptance needs a fresh CAPTCHA confirmation.

# Stage Child Additions Until Parent Save

## Prompt

Keep old interaction logic aligned while allowing visual modernization, reuse
the existing pending item-property flow, and commit each behavior atomically.

## Scope

- Stage manual and selected-existing children in `AddedItems` until parent Save.
- Render pending additions locally without querying detail data again.
- Discard an unsaved child locally instead of sending `DelteItems`.
- Separate candidate `ListViewId` from child-create `SelectedView`.
- Restore the owner-aware `/new{view}/{parent}&{ownerView}&{property}` route.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/usePendingChildChanges.ts`
- `frontend/src/usePendingChildChanges.test.ts`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T12-44-00Z-stage-child-additions.md`

## Validation

- `cd frontend && npm test -- --run`: 10 files, 151 tests passed, including
  unchanged-count add/delete rendering, unsaved-add discard, View-id separation,
  owner-aware routes, and source-level no-request staging checks.
- `cd frontend && npm run build`: TypeScript and Vite production build passed.
- `docker compose build frontend`: passed.
- `docker compose up -d --no-deps frontend`: current frontend deployed on port
  8081.
- `python scripts/runtime_doctor.py`: all checks passed, including
  `saveobj-addeditems`, owner-aware new paths, and surrounding detail routes.
- `python scripts/check_repo_harness.py`: passed.
- `docker compose ps -a`: backend/frontend are up, MySQL/Redis are healthy, and
  `db-migrate` is `Exited (0)`.

## Source Evidence

- `detailview.js additem(...)` stores selected and inline children in
  `obj.Itemproperties[*].AddedItems`; only `beginsave()` posts the parent.
- Candidate selection loads `ListViewId`, while non-inline child creation uses
  `SelectedView` and the parent/owner/property route context.
- Inline child-row presentation remains a separate parity slice.

## Risks And Follow-Ups

- The manual-add editor still uses the current inline form instead of creating
  the old blank table row and immediately entering its row editor.
- Authenticated browser acceptance needs a fresh CAPTCHA confirmation.

# Child Drafts Hook

## Prompt

Continue the Docker/Vue FoolFrame migration, keep the frontend View-first, and
control file size and code reuse.

## Scope

- Moved child add/update draft state and read/write helpers out of `App.vue`
  into `useChildDrafts`.
- Reused existing `viewWorkflow` draft helpers for missing-value reads,
  default add-row initialization, persisted child item edits, and child-group
  synchronization.
- Kept child collection editors tied to rendered `getreaditemview.DetailViews`
  metadata; this does not add a concrete business DTO binding path.
- Reduced `App.vue` to 1926 lines while keeping the extracted hook at 65 lines.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/useChildDrafts.ts`
- `frontend/src/useChildDrafts.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-08T20-47-37Z-child-drafts-hook.md`

## Validation

- GREEN: `cd frontend && npm test`
  passed 7 files / 102 tests.
- GREEN: `cd frontend && npm run build`
  produced `/assets/index-g5vOVxmd.js`.
- GREEN: `docker compose build frontend >/tmp/fool_frontend_build.log 2>&1 && docker compose up -d frontend >/tmp/fool_frontend_up.log 2>&1`
- GREEN: `python scripts/runtime_doctor.py`
- GREEN: `python scripts/check_repo_harness.py`
- GREEN: `git diff --check`
- GREEN: `curl -s http://localhost:8081/ | sed -n '1,12p'`
  served `/assets/index-g5vOVxmd.js`.
- GREEN: `wc -l frontend/src/App.vue frontend/src/useChildDrafts.ts frontend/src/useChildDrafts.test.ts`
  returned 1926 / 65 / 36 lines.

## Risks

- This is a frontend reuse and file-size control slice. It does not expand the
  remaining FoolFrame business parity surface.

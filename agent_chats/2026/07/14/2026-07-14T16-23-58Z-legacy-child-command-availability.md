# Legacy Child Command Availability

## Prompt

Continue aligning the old page layout, style, and interactions in atomic
commits without coupling Vue components to concrete business DTOs.

## Scope

- Compared the child collection commands in `detailView.jade` and
  `detailview.js` with `ViewDetailPanel.vue`.
- Removed the Vue-only global pending lock from metadata Add, inline Edit/Save,
  and Delete.
- Kept the old edit-state guard, existing detail links, candidate loading,
  main detail save, View operations, and report boundaries unchanged.
- Reused the current View metadata and child staging flow; added no state,
  request path, DTO binding, or duplicate component.

## Changed Files

- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/ViewDetailPanel.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T16-23-58Z-legacy-child-command-availability.md`

## Validation

- `cd frontend && npm test` passed: 15 files, 181 tests.
- `cd frontend && npm run build` passed.
- `python scripts/check_repo_harness.py` passed.
- `docker compose build frontend` passed.
- `docker compose up -d --no-deps --force-recreate frontend` passed.
- `docker compose ps -a` showed backend/frontend/MySQL/Redis running and
  `db-migrate` at `Exited (0)`.
- Backend `/test` passed through the runtime doctor.
- `python scripts/runtime_doctor.py` passed all 67 checks.
- `ViewDetailPanel.vue` shrank from 464 to 463 lines; its focused interaction
  contract remains 29 lines.
- Deployed frontend image:
  `sha256:76920c26a2a12fae917dd3da1bc01dfd46245fac73d1b9b6fd393575929ecaa4`.

## Risks And Follow-ups

- Browser acceptance must pause the backend during an unrelated detail request,
  verify Add, inline Edit/Save, Delete, and detail links stay actionable, avoid
  saving staged changes, and restore the backend afterward.
- `docs/superpowers/` is unrelated untracked work and remains untouched.

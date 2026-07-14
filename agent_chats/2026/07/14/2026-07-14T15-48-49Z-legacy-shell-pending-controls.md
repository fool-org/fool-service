# Legacy Shell Pending Controls

## Prompt

Continue aligning the old page layout, style, and interactions in atomic
commits.

## Scope

- Compared `tbar.jade` and `menuinfo.js` with the shared Vue desktop/mobile
  shell before changing component bindings.
- Removed the Vue-only global pending disable from Home-adjacent metadata
  menus, submenu View actions, and both `安全退出` commands.
- Retained request protection inside View, detail, save, and report workflows.
- Removed the now-unused `LegacyMenuNav.disabled` prop instead of adding state
  or a second shell component.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/LegacyMenuNav.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T15-48-49Z-legacy-shell-pending-controls.md`

## Validation

- `cd frontend && npm test` passed: 14 files, 179 tests.
- `cd frontend && npm run build` passed.
- `python scripts/check_repo_harness.py` passed.
- `docker compose build frontend` passed.
- `docker compose up -d --no-deps --force-recreate frontend` passed.
- `docker compose ps -a` showed backend/frontend/MySQL/Redis running and
  `db-migrate` at `Exited (0)`.
- Backend `/test` and frontend `/` smoke passed.
- `python scripts/runtime_doctor.py` passed all 67 checks.
- Deployed frontend image:
  `sha256:588a1016381c2b8e7d192c7a85c54690bd3013ab5ad0eec25092263f08f48a75`.
- Used the authorized local CAPTCHA with `admin/admin` and reached `/main` on
  the deployed bundle.
- Paused the backend, clicked Find to hold a real `querydata` request in flight,
  and inspected the shell at 800ms: Home, Views, and `安全退出` were enabled;
  the View-local Find command remained disabled and no dialog opened.
- Unpaused the backend; the query completed, Find re-enabled, the shell controls
  stayed enabled, the URL remained `/main`, and browser console errors were
  empty.
- Backend `/test`, Compose state, and all 67 runtime-doctor checks passed after
  backend restoration.

## Risks And Follow-ups

- No unresolved risk remains for this interaction slice.
- `docs/superpowers/` is unrelated untracked work and remains untouched.

# Frontend Production Shell Cleanup

## Prompt
- Continue the Docker/FoolFrame/Vue migration, keep the goal on a usable
  View-first frontend, maximize reuse, and control file size.

## Scope
- Audited all 25 routes declared in `../FoolFrame/src/Web/app.js` and the Jade
  templates against the current Spring/Vue route and workflow surfaces.
- Confirmed `Group.jade` is only an empty document shell and the placeholder
  `item.jade` is covered by the metadata-only item View.
- Removed the production `API Tools` and migration-map navigation and panels.
- Removed manual View IDs, raw filters, JSON DTO inputs, raw response output,
  and API-only state and actions from `App.vue`.
- Kept auth, menus, View/data, detail/new/save, child collection, operation,
  report, message, and notification behavior on the existing shared workflows.
- Deleted the unused `MigrationMap.vue` and `ResultsPanel.vue` components and
  their dead CSS.
- Removed the obsolete raw-query branch from `useViewDataWorkflow` and updated
  tests to guard the production-only View surface.

## Changed Files
- `frontend/src/App.vue`
- `frontend/src/MigrationMap.vue` (deleted)
- `frontend/src/ResultsPanel.vue` (deleted)
- `frontend/src/payload.test.ts`
- `frontend/src/style.css`
- `frontend/src/useViewDataWorkflow.test.ts`
- `frontend/src/useViewDataWorkflow.ts`
- `frontend/src/viewShell.ts`
- `scripts/check_repo_harness.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/10/2026-07-10T05-23-25Z-frontend-production-shell-cleanup.md`

## Validation
- `cd frontend && npm test -- --run` passed: 7 files, 130 tests.
- `cd frontend && npm run build` passed.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.
- `docker compose build frontend` passed.
- `docker compose up -d --force-recreate --no-deps frontend` replaced the
  running frontend.
- `python scripts/runtime_doctor.py` passed all Compose, schema, seed, deep-link,
  auth, View/data, operation, report, message, and logout checks.

## Runtime Evidence
- `docker compose ps -a` showed backend/frontend running, MySQL/Redis healthy,
  and `db-migrate` at `Exited (0)`.
- The rebuilt `/view100` signed-out page rendered only the application login
  workflow; no API console or migration navigation was present.
- `App.vue` is 1207 lines, down from 1769 before this cleanup.
- The frontend code/test diff removes 897 lines with 38 insertions.

## Risks
- The old raw compatibility request shapes remain intentionally available in
  backend routes and payload builders, but no longer have a production Vue
  control surface.
- Signed-in desktop/mobile browser inspection still requires explicit approval
  to submit the local captcha-backed Docker admin login.

## Follow-ups
- Complete signed-in desktop/mobile browser inspection after captcha approval.
- Continue only concrete remaining AppInstall, rich collection, or routed
  transaction parity gaps identified by a migrated module.

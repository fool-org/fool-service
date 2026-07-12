# Reset Shell Navigation On Route

## Prompt

Continue aligning old route interaction and transient layout state while
keeping shared code small and committing every behavior atomically.

## Scope

- Close the mobile Drawer and expanded submenu on every actual route change.
- Cover direct menu Views, Home, message targets, browser history, and session
  cleanup.
- Keep parent-menu expansion inside the Drawer open until a child is selected.
- Replace repeated state clearing with one shared helper.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T15-49-20Z-reset-shell-navigation-on-route.md`

## Validation

- `cd frontend && npm test -- --run src/payload.test.ts`: 82 tests passed.
- `cd frontend && npm test -- --run && npm run build`: 153 tests passed and
  the production build completed.
- `docker compose build frontend && docker compose up -d --no-deps frontend`:
  final frontend image built and restarted; manifest
  `sha256:6d46094ec0458dff1fa5d21066430679636a77ad6cd138de22e903f918111482`.
- `python scripts/runtime_doctor.py`: passed, including all shell routes and the
  complete auth/View/data/report/message smoke surface.
- `python scripts/check_repo_harness.py`: passed.
- `docker compose ps -a`: frontend/backend/MySQL/Redis running; `db-migrate`
  exited successfully with code 0.
- `git diff --check`: passed.

## Source Evidence

- Old menu, message, Home, and browser route transitions reload the document,
  naturally clearing Bootstrap dropdown state.
- Vue previously preserved the Drawer/dropdown for message and history-driven
  transitions even after page data changed.
- `closeShellNavigation` now owns the three transient shell values; parent
  expansion does not invoke it, preserving the mobile parent-to-child flow.

## Risks And Follow-Ups

- Current-build authenticated mobile Drawer and history visual confirmation
  remains pending fresh authorization to read and fill the current CAPTCHA.

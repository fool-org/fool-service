# Shell Action Copy

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with the old
  FoolFrame Web pages, using an atomic commit for each change.

## Scope

- Restore old Web Chinese Home and navigation labels on desktop and Drawer.
- Restore system-message, refresh, view-detail, close, empty-state, signed-in
  fallback, and safe-logout labels in the shared shell actions component.
- Preserve server-provided AppInfo, user name, menu text, message content, and
  message target identifiers.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/ShellActions.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T09-32-00Z-shell-action-copy.md`

## Validation

- `cd frontend && npm test && npm run build`
  - 139 tests passed.
  - Vue TypeScript checking and Vite production build passed.
- `git diff --check` passed.

## Runtime Evidence

- `docker compose build frontend && docker compose up -d --no-deps frontend`
  rebuilt and restarted the Vue frontend successfully.
- `python scripts/runtime_doctor.py` passed all Compose, auth, View, data,
  detail, child, report, message, chart, and Sudoku checks.
- `python scripts/check_repo_harness.py` passed.

## Skipped Or Downgraded Checks

- Authenticated browser interaction remains gated by a fresh CAPTCHA without
  current solve permission.

## Risks And Follow-Ups

- Final authenticated desktop and 390x844 browser acceptance must prove header
  labels, message popover copy/target action, Drawer, and logout.

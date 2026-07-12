# Shell Top Navigation

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with the old
  FoolFrame Web pages, using an atomic commit for each change.

## Scope

- Remove the desktop sidebar and restore the old Web top-header composition.
- Place AppInfo branding, Home, horizontal TopMenu/dropdown SubMenu, and
  existing user/message/logout actions in the desktop header.
- Add a horizontal presentation mode to the shared `LegacyMenuNav` without
  duplicating menu loading, expansion, notification, or View routing state.
- Keep the same component vertical in the mobile Drawer and rename the fixed
  root action from the invented Views label to old Web Home semantics.
- Update the repository responsive design contract from the superseded
  sidebar/two-column layout.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/LegacyMenuNav.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `docs/frontend/ui-design-system.md`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T09-22-00Z-shell-top-navigation.md`

## Validation

- `cd frontend && npm test && npm run build`
  - 139 tests passed.
  - Vue TypeScript checking and Vite production build passed.
- `docker compose build frontend && docker compose up -d --no-deps frontend`
  - Rebuilt frontend deployed on port 8081.
- `python scripts/runtime_doctor.py`
  - Full runtime checks passed, including getmain/getsubmenu and View routing.
- `python scripts/check_repo_harness.py`
  - Repository harness passed.
- `git diff --check` passed.

## Runtime Evidence

- Docker serves the rebuilt top-navigation shell at `http://localhost:8081`.
- Runtime doctor proved menu metadata and View routing remain healthy after the
  shell layout change.

## Skipped Or Downgraded Checks

- Authenticated browser interaction remains gated by a fresh CAPTCHA without
  current solve permission.

## Risks And Follow-Ups

- Final authenticated desktop and 390x844 browser acceptance must prove
  dropdown placement, menu expansion, Home/detail return, Drawer behavior,
  header wrapping, and no horizontal page overflow.

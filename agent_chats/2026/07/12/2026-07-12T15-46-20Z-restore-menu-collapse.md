# Restore Menu Collapse

## Prompt

Continue aligning old shell layout and interaction behavior, reuse shared
components, and commit every behavior atomically.

## Scope

- Compare the shared Vue parent-menu interaction with old Bootstrap dropdowns.
- Collapse an open parent when it is clicked a second time.
- Bind desktop/mobile `aria-expanded` to the existing shared expansion state.
- Preserve direct View navigation and mobile Drawer behavior.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/LegacyMenuNav.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T15-46-20Z-restore-menu-collapse.md`

## Validation

- `cd frontend && npm test -- --run src/payload.test.ts`: 82 tests passed.
- `cd frontend && npm test -- --run && npm run build`: 153 tests passed and
  the production build completed.
- `docker compose build frontend && docker compose up -d --no-deps frontend`:
  final frontend image built and restarted; manifest
  `sha256:8b523fdf940c507d10fcacd79060ae24738833632193d4e4cb6c70786336f680`.
- `python scripts/runtime_doctor.py`: passed, including top/submenu metadata,
  View navigation, auth, data, report, and message routes.
- `python scripts/check_repo_harness.py`: passed.
- `docker compose ps -a`: frontend/backend/MySQL/Redis running; `db-migrate`
  exited successfully with code 0.
- `git diff --check`: passed.

## Source Evidence

- Old `tbar.jade` marks metadata parents with Bootstrap
  `data-toggle="dropdown"`, whose second activation closes the open dropdown.
- Current Vue previously assigned the same `subMenuParentAuthCode` and issued
  another request on every click, so it could never collapse.
- The fix clears the existing parent/response state and adds no new menu store
  or duplicate desktop/mobile implementation.

## Risks And Follow-Ups

- Current-build authenticated mouse/touch collapse confirmation remains pending
  fresh authorization to read and fill the current CAPTCHA.

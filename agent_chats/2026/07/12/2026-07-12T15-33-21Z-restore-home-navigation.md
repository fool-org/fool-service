# Restore Home Navigation

## Prompt

Continue aligning old page layout, style, and interaction behavior with atomic
commits and metadata-first View loading.

## Scope

- Compare old authenticated `/` and `/main` routing with the current SPA Home
  controls.
- Make desktop/mobile Home and the desktop application brand return to the
  loaded App's `DefaultViewId` from any current list/detail route.
- Preserve explicit `/view:id` route precedence.
- Restore the old no-default-View configuration guidance and clear that state
  when another View/detail/new route opens.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T15-33-21Z-restore-home-navigation.md`

## Validation

- `cd frontend && npm test -- --run src/payload.test.ts`: 82 tests passed.
- `cd frontend && npm test -- --run && npm run build`: 153 tests passed and
  the production build completed.
- `docker compose build frontend && docker compose up -d --no-deps frontend`:
  final frontend image built and restarted; manifest
  `sha256:bbfe6cb311ed3476d648132d3bf5f8202c2d3c3acd15a68c2cf6c1d6499e69a0`.
- `python scripts/runtime_doctor.py`: passed, including root/main/deep-link
  routes, `getmain.App.DefaultViewId`, View metadata, and data queries.
- `python scripts/check_repo_harness.py`: passed.
- `docker compose ps -a`: frontend/backend/MySQL/Redis running; `db-migrate`
  exited successfully with code 0.
- `git diff --check`: passed.

## Source Evidence

- Old `routes/index.js` handles authenticated `/` and `/main` by loading
  `maindata.App.DefaultViewId`, then `getlistview`, or rendering the unconfigured
  home guidance when no default View exists.
- Old `default.jade` makes the desktop application brand a root link and
  exposes a separate `首页` command.
- Vue now reuses `legacyAppDefaultViewId`, `applyRequestedViewId`, and
  `loadViewWorkflow`; it updates browser history to `/` without adding a router
  dependency or binding Home to a business DTO.

## Risks And Follow-Ups

- Current-build authenticated desktop/mobile clicking and URL confirmation
  remains pending fresh authorization to read and fill the current CAPTCHA.

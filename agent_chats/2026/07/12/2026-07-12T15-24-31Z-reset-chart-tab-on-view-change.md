# Reset Chart Tab On View Change

## Prompt

Continue aligning Vue interaction behavior with FoolFrame while preserving the
metadata-first View/data boundary and committing each behavior atomically.

## Scope

- Compare `viewWithChart.jade`'s entry state with the reused Vue list panel.
- Restore the `数据` pane when SPA navigation loads another View id or template.
- Preserve the selected data/chart pane during search, paging, automatic
  refresh, and row-data updates within the same View.

## Changed Files

- `frontend/src/ViewListPanel.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T15-24-31Z-reset-chart-tab-on-view-change.md`

## Validation

- `cd frontend && npm test -- --run src/payload.test.ts`: 81 tests passed.
- `cd frontend && npm test -- --run && npm run build`: 152 tests passed and
  the production build completed.
- `docker compose build frontend && docker compose up -d --no-deps frontend`:
  final frontend image built and restarted; manifest
  `sha256:181ff0b0aa84647c3f3b9542bcf063c7c554cc3dcb71d88ef2aa5654111077b6`.
- `python scripts/runtime_doctor.py`: passed, including chart metadata/data and
  the complete auth, View, detail, report, and message smoke surface.
- `python scripts/check_repo_harness.py`: passed.
- `docker compose ps -a`: frontend/backend/MySQL/Redis running; `db-migrate`
  exited successfully with code 0.
- `git diff --check`: passed.

## Source Evidence

- `viewWithChart.jade` marks `数据` active and initializes the table pane first
  on each page load.
- The Vue panel instance survives menu navigation, so its local `activePane`
  previously leaked a chart selection into the next chart View.
- The watcher depends only on existing `currentViewId` and `templateKind`
  computed values; it does not watch data DTOs or request state.

## Risks And Follow-Ups

- Current-build authenticated desktop/mobile interaction replay remains pending
  fresh authorization to read and fill the current local CAPTCHA.

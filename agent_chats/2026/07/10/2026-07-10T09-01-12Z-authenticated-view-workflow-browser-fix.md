# Authenticated View Workflow Browser Fix

## Prompt

Use the local CAPTCHA and Docker `admin/admin` account to complete the signed-in
Vue browser verification for the FoolFrame migration.

## Scope

- Exercise the View-first list, detail, new, report, chart, message, Sudoku,
  and legacy deep-link workflows in the deployed Docker frontend.
- Check desktop and 390x844 layouts, console errors, and horizontal overflow.
- Fix only browser-visible migration regressions found during that replay.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/style.css`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/10/2026-07-10T09-01-12Z-authenticated-view-workflow-browser-fix.md`

## Result

- Login, default View loading, keyword search, detail selection, metadata new
  form, report execution, SVG chart, message popover, and legacy detail/new
  deep links render through the authenticated Vue shell.
- Sudoku panels that share `ListViewId=100` now retain both list/query data and
  item-detail data instead of the later item response replacing earlier rows.
- Group child lists skip their query only when row data is already available.
- At 390px, the primary and FoolFrame View navigation controls wrap onto one
  complete row instead of clipping the second control.
- No business DTO, duplicate request state, or new dependency was added.

## Validation

- `cd frontend && npm test`: 130 tests passed.
- `cd frontend && npm run build`: TypeScript and Vite production build passed.
- `docker compose build frontend`: production frontend image built.
- `docker compose up -d --force-recreate --no-deps frontend`: deployed the
  rebuilt frontend.
- `python scripts/runtime_doctor.py`: all Docker/auth/View/data/report/message
  and legacy route checks passed.
- `python scripts/check_repo_harness.py`: passed.
- `git diff --check`: passed.

## Runtime Evidence

- `artifacts/browser/2026-07-10-authenticated-view-workflow/order-list-desktop-1280x720.png`
- `artifacts/browser/2026-07-10-authenticated-view-workflow/order-list-mobile-390x844.png`
- `artifacts/browser/2026-07-10-authenticated-view-workflow/order-chart-desktop-1280x720.png`
- `artifacts/browser/2026-07-10-authenticated-view-workflow/sudoku-desktop-1280x720.png`
- Desktop and mobile document `scrollWidth` matched the viewport width.
- Sudoku settled with seven rendered panel sections, three SVGs, four loaded
  map tiles, three panel tables, and only the intentional `简单项` placeholder.
- Browser console warning/error collection was empty.

## Risks

- Browser verification did not submit a create, update, delete, operation, or
  report-definition save because those mutate Docker seed data. Their API
  contracts remain covered by the runtime doctor and focused frontend tests.

## Follow-ups

- No concrete browser blocker remains. Continue only from a newly identified
  FoolFrame parity gap or a real migrated module requirement.

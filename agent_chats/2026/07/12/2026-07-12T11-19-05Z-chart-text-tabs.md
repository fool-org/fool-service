# Chart Text Tabs

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with old
  FoolFrame, with every behavior change committed atomically.

## Scope

- Compare data/chart navigation with old `viewWithChart.jade`.
- Preserve the old data-first state and tab order.
- Remove invented table/chart icons and their spacing rule.
- Preserve chart metadata, rendering, and paging behavior.

## Changed Files

- `frontend/src/ViewListPanel.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T11-19-05Z-chart-text-tabs.md`

## Validation

- `cd frontend && npm test -- --run`: 9 files, 147 tests passed.
- `cd frontend && npm run build`: TypeScript and Vite production build passed;
  `ViewListPanel` decreased from 18.03 kB to 17.95 kB.
- `docker compose build frontend`: passed.
- `python scripts/runtime_doctor.py`: all checks passed.
- `python scripts/check_repo_harness.py`: passed.
- `git diff --check`: passed.

## Runtime Evidence

- Rebuilt frontend image deployed at `http://localhost:8081`.
- `docker compose ps -a`: backend/frontend up, MySQL/Redis healthy, and
  `db-migrate` exited successfully with code 0.

## Skipped Or Downgraded Checks

- Authenticated chart-tab visual inspection requires a fresh CAPTCHA
  authorization.

## Risks And Follow-Ups

- Final browser acceptance should verify data/chart switching and narrow-width
  tab layout with a View-provided chart template.

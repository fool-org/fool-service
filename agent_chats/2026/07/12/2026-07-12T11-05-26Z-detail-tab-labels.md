# Detail Tab Labels

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with old
  FoolFrame, with every behavior change committed atomically.

## Scope

- Compare child collection tabs with old `detailView.jade`.
- Render only the metadata-backed child group title in each tab.
- Remove the invented row-count badge and its dedicated styles.

## Changed Files

- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T11-05-26Z-detail-tab-labels.md`

## Validation

- `cd frontend && npm test -- --run`: 9 files, 147 tests passed.
- `cd frontend && npm run build`: TypeScript and Vite production build passed;
  `ViewDetailPanel` decreased from 70.31 kB to 70.27 kB.
- `docker compose build frontend`: passed.
- `python scripts/runtime_doctor.py`: all checks passed.
- `python scripts/check_repo_harness.py`: passed.
- `git diff --check`: passed.

## Runtime Evidence

- Rebuilt frontend image deployed at `http://localhost:8081`.
- `docker compose ps -a`: backend/frontend up, MySQL/Redis healthy, and
  `db-migrate` exited successfully with code 0.

## Skipped Or Downgraded Checks

- Authenticated detail-tab visual inspection requires a fresh CAPTCHA
  authorization.

## Risks And Follow-Ups

- Final browser acceptance should verify long metadata titles and multiple
  child tabs at desktop and mobile widths.

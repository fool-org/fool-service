# List Operation Column

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with old
  FoolFrame, with every behavior change committed atomically.

## Scope

- Compare shared list operations with `view.jade` and `querylistdata.js`.
- Keep the operation column in the table's normal horizontal scroll flow.
- Restore left-aligned metadata operation commands.
- Remove the invented arrow from the candidate `选择` command.

## Changed Files

- `frontend/src/ListDataTable.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T11-15-10Z-list-operation-column.md`

## Validation

- `cd frontend && npm test -- --run`: 9 files, 147 tests passed.
- `cd frontend && npm run build`: TypeScript and Vite production build passed;
  the shared table chunk decreased from 298.68 kB to 298.60 kB.
- `docker compose build frontend`: passed.
- `python scripts/runtime_doctor.py`: all checks passed.
- `python scripts/check_repo_harness.py`: passed.
- `git diff --check`: passed.

## Runtime Evidence

- Rebuilt frontend image deployed at `http://localhost:8081`.
- `docker compose ps -a`: backend/frontend up, MySQL/Redis healthy, and
  `db-migrate` exited successfully with code 0.

## Skipped Or Downgraded Checks

- Authenticated list and candidate-table visual inspection requires a fresh
  CAPTCHA authorization.

## Risks And Follow-Ups

- Final browser acceptance should verify horizontal scrolling and multiple
  row operations at desktop and mobile widths.

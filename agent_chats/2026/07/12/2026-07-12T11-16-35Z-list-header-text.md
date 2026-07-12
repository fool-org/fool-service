# List Header Text

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with old
  FoolFrame, with every behavior change committed atomically.

## Scope

- Compare shared table headings with old `view.jade` Bootstrap tables.
- Restore normal 14px dark header text on white.
- Remove forced uppercase and nonzero letter spacing.
- Preserve metadata-provided column names exactly.

## Changed Files

- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T11-16-35Z-list-header-text.md`

## Validation

- `cd frontend && npm test -- --run`: 9 files, 147 tests passed.
- `cd frontend && npm run build`: TypeScript and Vite production build passed.
- `rg -n -U "\\.metadata-data-table \\.p-datatable-thead[^}]*\\}"
  frontend/src/style.css`: confirmed 14px text, zero letter spacing, and no
  uppercase transformation.
- `docker compose build frontend`: passed.
- `python scripts/runtime_doctor.py`: all checks passed.
- `python scripts/check_repo_harness.py`: passed.
- `git diff --check`: passed.

## Runtime Evidence

- Rebuilt frontend image deployed at `http://localhost:8081`.
- `docker compose ps -a`: backend/frontend up, MySQL/Redis healthy, and
  `db-migrate` exited successfully with code 0.

## Skipped Or Downgraded Checks

- Authenticated metadata-header visual inspection requires a fresh CAPTCHA
  authorization.
- Vitest transforms direct CSS text imports to an empty module in this setup,
  so ineffective CSS assertions were removed and replaced by the explicit
  source scan above.

## Risks And Follow-Ups

- Final browser acceptance should verify mixed-case and long metadata labels
  in main, Sudoku, and candidate tables.

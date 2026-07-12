# Bootstrap Panel Chrome

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with the old
  FoolFrame Web pages, using an atomic commit for each change.

## Scope

- Compare shared list/detail panels with Bootstrap 3 panel CSS.
- Restore 4px corners, `#ddd` borders, and the subtle one-pixel shadow.
- Restore the light gray bordered heading band and 15px content spacing.
- Remove the invented floating-card shadow and large corner radius.

## Changed Files

- `frontend/src/style.css`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T10-57-37Z-bootstrap-panel-chrome.md`

## Validation

- `cd frontend && npm test -- --run`
  - 146 tests passed across 9 files.
- `cd frontend && npm run build`
  - Vue TypeScript checking and Vite production build passed.
- `rg -n "border-radius: 12px|0 12px 34px" frontend/src/style.css`
  - Returned no matches.
- `python scripts/check_repo_harness.py` passed.

## Runtime Evidence

- `docker compose build frontend` rebuilt the Vue image successfully.
- `docker compose up -d --no-deps frontend` recreated the frontend at
  `http://localhost:8081`.
- `python scripts/runtime_doctor.py` passed all Compose, auth, View, data,
  detail, child, report, message, chart, and Sudoku checks.
- `docker compose ps -a` shows frontend/backend running, MySQL/Redis healthy,
  and `db-migrate` at `Exited (0)`.

## Skipped Or Downgraded Checks

- Authenticated list/detail visual inspection requires a fresh captcha.
- A CSS source unit test was not retained because this Vitest configuration
  transforms global CSS imports to an empty module; build plus source scan is
  the non-browser gate.

## Risks And Follow-Ups

- Browser acceptance must prove heading/body spacing and narrow-screen fit on
  list, existing detail, create detail, and schema-only pages.

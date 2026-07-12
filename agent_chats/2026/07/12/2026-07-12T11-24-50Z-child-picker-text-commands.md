# Child Picker Text Commands

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with old
  FoolFrame, with every behavior change committed atomically.

## Scope

- Compare select-existing child dialog commands with old `detailView.jade`.
- Remove invented search, pagination, and Close icons.
- Preserve immediate row selection and omit the old inert confirm placeholder.
- Keep the separate child Add plus icon.

## Changed Files

- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T11-24-50Z-child-picker-text-commands.md`

## Validation

- `cd frontend && npm test -- --run`: 9 files, 147 tests passed.
- `cd frontend && npm run build`: TypeScript and Vite production build passed;
  `ViewDetailPanel` decreased from 70.25 kB to 70.14 kB.
- `docker compose build frontend`: passed.
- `python scripts/runtime_doctor.py`: all checks passed.
- `python scripts/check_repo_harness.py`: passed.
- `git diff --check`: passed.

## Runtime Evidence

- Rebuilt frontend image deployed at `http://localhost:8081`.
- `docker compose ps -a`: backend/frontend up, MySQL/Redis healthy, and
  `db-migrate` exited successfully with code 0.

## Skipped Or Downgraded Checks

- Authenticated child-picker visual inspection requires a fresh CAPTCHA
  authorization.

## Risks And Follow-Ups

- Final browser acceptance should verify search, paging, row selection, and
  close behavior at desktop and mobile widths.

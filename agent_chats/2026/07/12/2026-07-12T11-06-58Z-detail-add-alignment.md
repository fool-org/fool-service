# Detail Add Alignment

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with old
  FoolFrame, with every behavior change committed atomically.

## Scope

- Compare child Add command placement with old `detailView.jade`.
- Keep the distinct inline-editor and select-existing interaction paths.
- Restore the select-existing toolbar's default left alignment.

## Changed Files

- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T11-06-58Z-detail-add-alignment.md`

## Validation

- `cd frontend && npm test -- --run`: 9 files, 147 tests passed.
- `cd frontend && npm run build`: TypeScript and Vite production build passed;
  `ViewDetailPanel` remained 70.27 kB.
- `docker compose build frontend`: passed.
- `python scripts/runtime_doctor.py`: all checks passed.
- `python scripts/check_repo_harness.py`: passed.
- `git diff --check`: passed.

## Runtime Evidence

- Rebuilt frontend image deployed at `http://localhost:8081`.
- `docker compose ps -a`: backend/frontend up, MySQL/Redis healthy, and
  `db-migrate` exited successfully with code 0.

## Skipped Or Downgraded Checks

- Authenticated child-tab visual inspection requires a fresh CAPTCHA
  authorization.

## Risks And Follow-Ups

- Final browser acceptance should check Add placement with a
  `SelectFromExists` metadata group at desktop and mobile widths.

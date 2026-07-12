# Legacy Avatar Size Parity

## Prompt

Continue aligning the Vue page with FoolFrame layout, style, and interaction,
with every behavior change committed atomically.

## Scope

- Compare the old `default.jade` avatar and `soway.css` dimensions with the Vue
  shell.
- Restore the exact old 50x50 avatar dimensions without changing its data or
  fallback behavior.

## Changed Files

- `frontend/src/ShellActions.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T11-49-20Z-legacy-avatar-size.md`

## Validation

- `cd frontend && npm test -- --run`: 9 files, 147 tests passed.
- `cd frontend && npm run build`: TypeScript and Vite production build passed;
  `ShellActions` remains 1.41 kB.
- `docker compose build frontend`: passed.
- `docker compose up -d --no-deps frontend`: rebuilt frontend deployed on port
  8081.
- `python scripts/runtime_doctor.py`: all Compose, auth, View/data, report,
  message, and logout checks passed.
- `python scripts/check_repo_harness.py`: passed.
- `docker compose ps -a`: backend/frontend are up, MySQL/Redis are healthy, and
  `db-migrate` is `Exited (0)`.

## Source Evidence

- `../FoolFrame/src/Web/views/default.jade` renders the user image with the
  `.avtar` class.
- `../FoolFrame/src/Web/public/stylesheets/soway.css` fixes `.avtar` at 50px
  wide and 50px high.

## Risks And Follow-Ups

- The Docker admin avatar value is empty, so runtime smoke covers the existing
  icon fallback; the non-empty image dimensions are source- and test-covered.
- Fresh authenticated visual acceptance with a non-empty avatar remains useful.

# Bootstrap Brand Typography Parity

## Prompt

Continue aligning the Vue page with FoolFrame layout, style, and interaction,
with every behavior change committed atomically.

## Scope

- Compare the old `default.jade` brand markup and Bootstrap heading rules with
  the Vue shell.
- Restore the `h2 > small` structure and exact typography.
- Reuse the same brand markup in the responsive Drawer.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T12-02-04Z-bootstrap-brand-typography.md`

## Validation

- `cd frontend && npm test -- --run`: 9 files, 148 tests passed.
- `cd frontend && npm run build`: TypeScript and Vite production build passed;
  the main application chunk decreased from 257.36 kB to 257.34 kB.
- Production CSS scan confirms the final bundle contains the exact 21px/500/1.1
  brand and 65%/400/1 version rules.
- `docker compose build frontend`: passed.
- `docker compose up -d --no-deps frontend`: rebuilt frontend deployed on port
  8081.
- `python scripts/runtime_doctor.py`: all Compose, auth, menu, View/data,
  report, message, and logout checks passed.
- `python scripts/check_repo_harness.py`: passed.
- `docker compose ps -a`: backend/frontend are up, MySQL/Redis are healthy, and
  `db-migrate` is `Exited (0)`.

## Source Evidence

- `../FoolFrame/src/Web/views/default.jade` renders the application name as an
  `H2` with a nested `small` version.
- `../FoolFrame/src/Web/public/stylesheets/bootstrap.css` defines that heading
  as 21px, weight 500, line-height 1.1, margins 14px/7px, and its `small` as
  65%, normal weight, line-height 1.

## Risks And Follow-Ups

- Authenticated visual acceptance still needs a fresh CAPTCHA authorization.

# Bootstrap User Area Parity

## Prompt

Continue aligning the Vue page with FoolFrame layout, style, and interaction,
with every behavior change committed atomically.

## Scope

- Trace the old avatar/name markup through Bootstrap's body, link, nav, and
  pull-right rules.
- Restore the old user-name typography and padding without making display text
  falsely interactive.
- Keep the icon fallback in the old fixed 50x50 avatar slot.

## Changed Files

- `frontend/src/ShellActions.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T12-04-43Z-bootstrap-user-area.md`

## Validation

- `cd frontend && npm test -- --run`: 9 files, 148 tests passed.
- `cd frontend && npm run build`: TypeScript and Vite production build passed;
  `ShellActions` remains 1.45 kB JavaScript and 0.76 kB CSS.
- `docker compose build frontend`: passed.
- `docker compose up -d --no-deps frontend`: rebuilt frontend deployed on port
  8081.
- `python scripts/runtime_doctor.py`: all Compose, auth, menu, View/data,
  report, message, and logout checks passed.
- `python scripts/check_repo_harness.py`: passed.
- `docker compose ps -a`: backend/frontend are up, MySQL/Redis are healthy, and
  `db-migrate` is `Exited (0)`.

## Source Evidence

- `default.jade` renders `.avtar` followed by an `a.pull-right` user name.
- `soway.css` fixes `.avtar` at 50x50.
- Bootstrap defines the page font as 10px normal-weight, links as `#337ab7`,
  and `.nav > li > a` padding as 10px 15px.

## Risks And Follow-Ups

- Vue keeps the name as non-interactive text because the old anchor has no
  destination or click handler; only its presentation is retained.
- Authenticated visual acceptance still needs a fresh CAPTCHA authorization.

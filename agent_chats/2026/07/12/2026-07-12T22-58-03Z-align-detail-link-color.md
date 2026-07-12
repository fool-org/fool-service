# Align Detail Link Color

## Prompt

Keep the improved Vue presentation, but align interaction logic with
FoolFrame; visual polish may build on the old page, and each behavior must be
committed atomically.

## Scope

- Replace the remaining invented indigo child Edit/Detail link color with the
  Bootstrap 3 link and hover colors used by FoolFrame.
- Preserve all existing metadata-derived routes and interaction handlers.

## Changed Files

- `frontend/src/style.css`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T22-58-03Z-align-detail-link-color.md`

## Validation

- `cd frontend && npm test -- --run`: 10 files, 152 tests passed.
- `cd frontend && npm run build`: passed; Vue type-check and Vite production
  build completed.
- `rg -n "#4338ca|#337ab7|#23527c" frontend/src/style.css`: the indigo literal
  is absent; the detail link and hover colors are present.
- `docker compose build frontend`: passed.
- `docker compose up -d --no-deps frontend`: rebuilt frontend deployed on port
  8081.
- `python scripts/runtime_doctor.py`: all Compose, auth, View/data, detail/new,
  save, child, lookup, report, and message checks passed.
- `python scripts/check_repo_harness.py`: passed, including source-size checks.
- `docker compose ps -a`: backend/frontend are up, MySQL/Redis are healthy,
  and `db-migrate` is `Exited (0)`.
- Authenticated browser color inspection was not run because the current local
  CAPTCHA was refreshed after the earlier one-time authorization; a fresh
  authorization is required before reading or filling it.

## Source Evidence

- FoolFrame loads Bootstrap 3, where normal links use `#337ab7` and hover
  links use `#23527c`.
- The Vue detail links retained `#4338ca`, the only remaining indigo literal in
  frontend source.

## Risks

- Visual-only change; route targets, event handlers, metadata, and payloads are
  unchanged.

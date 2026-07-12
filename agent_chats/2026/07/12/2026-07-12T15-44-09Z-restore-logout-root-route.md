# Restore Logout Root Route

## Prompt

Continue aligning old shell interaction behavior, preserve deep-link login
recovery, and commit each behavior atomically.

## Scope

- Compare explicit logout navigation with old `menuinfo.js`.
- Replace the current View/detail/new path with `/` after successful logout.
- Keep stale-token recovery route-neutral so a user can authenticate and resume
  the originally requested deep link.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T15-44-09Z-restore-logout-root-route.md`

## Validation

- `cd frontend && npm test -- --run src/payload.test.ts`: 82 tests passed.
- `cd frontend && npm test -- --run && npm run build`: 153 tests passed and
  the production build completed.
- `docker compose build frontend && docker compose up -d --no-deps frontend`:
  final frontend image built and restarted; manifest
  `sha256:ad7ca4d06f8cc0e80d975c05a365c76eda9ee1d6e7bf9582d6b037ea10689583`.
- `python scripts/runtime_doctor.py`: passed, including deep-link routes,
  login/logout, App default View metadata, and the full runtime smoke surface.
- `python scripts/check_repo_harness.py`: passed.
- `docker compose ps -a`: frontend/backend/MySQL/Redis running; `db-migrate`
  exited successfully with code 0.
- `git diff --check`: passed.

## Source Evidence

- Old `menuinfo.js` handles successful `/user/logout` by replacing the current
  location with the root route before reloading.
- Vue now calls `replaceLegacyPath("/")` only inside successful explicit
  `logout()`.
- `clearLegacySession()` remains path-neutral, preserving the established stale
  token deep-link login flow.

## Risks And Follow-Ups

- Current-build authenticated logout URL and subsequent default-View login
  confirmation remains pending fresh CAPTCHA authorization.

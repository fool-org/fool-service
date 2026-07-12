# Protocol-only Getnotify

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with old
  FoolFrame, with every behavior change committed atomically.

## Scope

- Trace `getnotify` through old Web templates/scripts and the server contract.
- Remove the invented Vue polling request, response state, menu-count prop,
  badge markup, and badge styles.
- Preserve API types, protocol helpers, backend compatibility route, and
  runtime-doctor coverage.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/LegacyMenuNav.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T11-47-17Z-protocol-only-getnotify.md`

## Validation

- Old source proof: no Web script calls `getnotify`; server
  `DataService.GetNotify` throws `NotImplementedException`.
- `cd frontend && npm test -- --run`: 9 files, 147 tests passed.
- `cd frontend && npm run build`: TypeScript and Vite production build passed.
- Source scan confirms no `getnotify`, `notifyResponse`, `notifyCount`, or
  `nav-count` remains in the Vue shell/navigation/CSS.
- `docker compose build frontend`: passed.
- `python scripts/runtime_doctor.py`: all checks passed, including the retained
  protocol-only `message:getnotify` route.
- `python scripts/check_repo_harness.py`: passed.
- `git diff --check`: passed.

## Runtime Evidence

- Rebuilt frontend image deployed at `http://localhost:8081`.
- Runtime doctor proves the compatibility route still returns `Notifies` while
  the deployed frontend no longer polls it.
- `docker compose ps -a`: backend/frontend up, MySQL/Redis healthy, and
  `db-migrate` exited successfully with code 0.

## Skipped Or Downgraded Checks

- Authenticated visual inspection requires a fresh CAPTCHA authorization.

## Risks And Follow-Ups

- None for old-Web parity; a future real notification feature should start
  from a new requirement rather than reusing this unimplemented legacy stub.

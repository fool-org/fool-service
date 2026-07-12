# Restore Shell Route History

## Prompt

Continue aligning old page interaction and route behavior, keep View-first
loading, and commit each behavior atomically.

## Scope

- Compare old `menuinfo.js` and message-detail links with SPA shell navigation.
- Synchronize top/submenu View targets and message View/detail targets to their
  canonical old `/view{id}` URLs.
- Replay the existing route loaders on browser back/forward navigation.
- Share path generation and avoid adding Vue Router.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T15-38-49Z-restore-shell-route-history.md`

## Validation

- `cd frontend && npm test -- --run src/viewWorkflow.test.ts src/payload.test.ts`:
  130 tests passed.
- `cd frontend && npm test -- --run && npm run build`: 153 tests passed and
  the production build completed.
- `docker compose build frontend && docker compose up -d --no-deps frontend`:
  final frontend image built and restarted; manifest
  `sha256:f6438903a5a65b3e3e2e3c606c6601dab0d4f755f701b71be930062351221973`.
- `python scripts/runtime_doctor.py`: passed, including root/main/View/detail
  deep-link routes and the complete auth/View/data/report/message smoke surface.
- `python scripts/check_repo_harness.py`: passed.
- `docker compose ps -a`: frontend/backend/MySQL/Redis running; `db-migrate`
  exited successfully with code 0.
- `git diff --check`: passed.

## Source Evidence

- Old `menuinfo.js` navigates any positive menu `ViewId` to `/view{id}`.
- Old `showerror.js` sets message detail links to `/view{id}` or
  `/view{id}/{objectId}`.
- Vue now pushes those same paths before calling existing loaders and registers
  one `popstate` listener that reuses `loadInitialRoute`; listener cleanup runs
  on component unmount. Initial/popstate Home loading suppresses path pushes so
  route replay cannot create a new history entry.

## Risks And Follow-Ups

- Current-build authenticated menu/message/back-forward browser confirmation
  remains pending fresh authorization to read and fill the current CAPTCHA.

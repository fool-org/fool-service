# Restore Child Action Availability

## Prompt

Keep old interaction logic aligned while allowing visual modernization and
commit each behavior atomically.

## Scope

- Keep child Add, inline Edit, and Delete controls visible like old Jade.
- Preserve `edititem(...)`'s no-op guard until parent Edit is active.
- Keep existing local Add/Delete staging and modern PrimeVue button styling.

## Changed Files

- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T12-54-00Z-restore-child-action-availability.md`

## Validation

- `cd frontend && npm test -- --run`: 10 files, 151 tests passed, including
  always-visible child actions and the parent-edit no-op guard.
- `cd frontend && npm run build`: TypeScript and Vite production build passed.
- `docker compose build frontend`: passed.
- `docker compose up -d --no-deps frontend`: current frontend deployed on port
  8081.
- `python scripts/runtime_doctor.py`: all checks passed across detail routes and
  child collection save surfaces.
- `python scripts/check_repo_harness.py`: passed.
- `docker compose ps -a`: backend/frontend are up, MySQL/Redis are healthy, and
  `db-migrate` is `Exited (0)`.

## Source Evidence

- `detailView.jade` renders Add, inline Edit, and Delete links without an
  `isedit` template condition.
- `detailview.js edititem(...)` immediately returns when `$scope.isedit` is
  false; `additem(...)` and `deleteitem(...)` do not share that guard.

## Risks And Follow-Ups

- Child collections remain hidden on parent-new routes; old add attempts show
  `请先保存当前内容，再新建子项`, which remains a separate parity slice.
- Authenticated browser acceptance needs a fresh CAPTCHA confirmation.

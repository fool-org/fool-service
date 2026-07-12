# Restore Child Detail Actions

## Prompt

Keep old interaction logic aligned while allowing visual modernization and
commit each behavior atomically.

## Scope

- Condition deep child Edit on `SelectFromExists == false`.
- Restore the separate Details action for every `DetailViewId` group.
- Reuse one metadata-derived child detail href.
- Align operation and empty-state column spans with rendered actions.

## Changed Files

- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T13-01-00Z-restore-child-detail-actions.md`

## Validation

- `cd frontend && npm test -- --run`: 10 files, 151 tests passed, including the
  select/deep-action matrix, shared href, and dynamic operation spans.
- `cd frontend && npm run build`: TypeScript and Vite production build passed.
- `docker compose build frontend`: passed.
- `docker compose up -d --no-deps frontend`: current frontend deployed on port
  8081.
- `python scripts/runtime_doctor.py`: all checks passed across detail and child
  collection routes.
- `python scripts/check_repo_harness.py`: passed.
- `docker compose ps -a`: backend/frontend are up, MySQL/Redis are healthy, and
  `db-migrate` is `Exited (0)`.

## Source Evidence

- `detailView.jade` shows inline/deep Edit only when `SelectFromExists` is
  false, then independently renders Details when `DetailViewId` is present.
- Both deep actions navigate to the same child detail route.

## Risks And Follow-Ups

- Authenticated browser acceptance needs a fresh CAPTCHA confirmation.

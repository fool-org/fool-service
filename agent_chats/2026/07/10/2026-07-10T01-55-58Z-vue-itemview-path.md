# Delivery Evidence: Vue ItemView Legacy Path

## Prompt

Continue the Docker/FoolFrame/Vue migration goal with atomic commits, reuse,
and View-first rendering.

## Scope

- Compared old FoolFrame Web `/itemview:id` startup in
  `../FoolFrame/src/Web/app.js` and `routes/index.js`.
- Added Vue startup parsing for `/itemview:id`.
- Reused the existing `queryDetail` flow with an empty object id; no router
  dependency or alternate DTO render path was added.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/10/2026-07-10T01-55-58Z-vue-itemview-path.md`

## Validation

- `cd frontend && npm test -- src/viewWorkflow.test.ts src/payload.test.ts`
  - 2 files passed, 117 tests passed.
- `cd frontend && npm test`
  - 7 files passed, 129 tests passed.
- `git diff --check`
  - Passed with no whitespace errors.
- `cd frontend && npm run build`
  - `vue-tsc --noEmit` and `vite build` passed.
- `python scripts/check_repo_harness.py`
  - Repository harness validation passed.
- `docker compose build frontend`
  - Frontend image built with `/assets/index-u1026L26.js`.
- `docker compose up -d --no-deps --force-recreate frontend`
  - Frontend container recreated against the latest static bundle.
- `curl http://localhost:8081/itemview100`
  - Returned `200 text/html` with `/assets/index-u1026L26.js`.
- `python scripts/runtime_doctor.py`
  - All Docker/runtime smoke checks passed.
- `docker compose ps`
  - Backend, frontend, MySQL, and Redis are running; MySQL and Redis are
    healthy.

## Skipped Checks

- No checks skipped for this frontend/runtime slice.

## Risks

- This slice maps only the old read-item startup path. It does not add a
  complete browser automation pass for every legacy read-item interaction.

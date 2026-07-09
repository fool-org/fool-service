# Delivery Evidence: Vue Detail/New Legacy Paths

## Prompt

Continue the Docker/FoolFrame/Vue migration goal with atomic commits, reuse,
and View-first rendering.

## Scope

- Compared old FoolFrame Web detail/new route semantics in
  `../FoolFrame/src/Web/routes/index.js`,
  `public/javascripts/app/querylistdata.js`, and
  `public/javascripts/app/detailview.js`.
- Added Vue startup parsing for:
  - `/view:id/:objid`
  - `/new:id`
  - `/new:id/:objid&:ownerviewid&:prpid`
- Reused existing `queryDetail` and `startNewObject` flows; no router
  dependency or alternate DTO render path was added.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/10/2026-07-09T18-13-12Z-vue-detail-new-paths.md`

## Validation

- `cd frontend && npm test`
  - 7 files passed, 129 tests passed.
- `python scripts/check_repo_harness.py`
  - Repository harness validation passed.
- `git diff --check`
  - Passed with no whitespace errors.
- `cd frontend && npm run build`
  - `vue-tsc --noEmit` and `vite build` passed.
- `docker compose up -d --build frontend`
  - Backend and frontend images built; backend container recreated.
- `docker compose up -d --no-deps --force-recreate frontend`
  - Frontend container recreated against the latest static bundle.
- `curl http://localhost:8081/view100/1001`
  - Returned `200 text/html` with `/assets/index-sHcPb3Al.js`.
- `curl http://localhost:8081/new200`
  - Returned `200 text/html` with `/assets/index-sHcPb3Al.js`.
- `curl 'http://localhost:8081/new200/1001&100&items'`
  - Returned `200 text/html` with `/assets/index-sHcPb3Al.js`.
- `python scripts/runtime_doctor.py`
  - All Docker/runtime smoke checks passed.
- `docker compose ps`
  - Backend, frontend, MySQL, and Redis are running; MySQL and Redis are
    healthy.

## Skipped Checks

- No checks skipped for this frontend/runtime slice.

## Risks

- This slice only maps legacy startup paths into the existing Vue detail/new
  workflows. It does not add browser automation for every old Web deep-link
  action.

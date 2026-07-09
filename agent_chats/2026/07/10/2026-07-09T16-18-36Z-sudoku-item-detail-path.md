# Sudoku Item Detail Path

## Prompt

- Continue the FoolFrame migration with Docker running and Vue as the frontend.
- Keep the migration View-first, avoid binding pages to concrete business DTOs,
  and control file size/reuse.
- Recheck progress and finish the active Sudoku `Item` panel gap.

## Scope

- Aligned Vue Sudoku `Item` panels with FoolFrame `includes/Item.jade` and
  `public/javascripts/app/subitem.js`.
- The panel now loads `getlistview(ListViewId)` first, then calls
  `querydatadetail(ListViewId, ObjId="")` and renders detail `SimpleData`.
- Non-Item Sudoku panels keep their existing View-first `querydata` flow.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/SudokuPanels.vue`
- `frontend/src/useViewDataWorkflow.ts`
- `frontend/src/useViewDataWorkflow.test.ts`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/payload.test.ts`
- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/10/2026-07-09T16-18-36Z-sudoku-item-detail-path.md`

## Validation

- `cd frontend && npm test -- viewWorkflow.test.ts useViewDataWorkflow.test.ts payload.test.ts`
  - 119 tests passed.
- `python scripts/runtime_doctor_test.py`
  - 35 tests passed.
- `cd frontend && npm test && npm run build`
  - 125 tests passed; Vite production build passed.
- `docker compose up -d --build frontend`
  - Frontend image built; Compose also rebuilt the backend image with Maven
    `-DskipTests package`, reactor build succeeded.
- `docker compose up -d --no-deps --force-recreate frontend`
  - Frontend container recreated from the latest image.
- `python scripts/runtime_doctor.py`
  - All checks passed, including `data:querydatadetail-sudoku-item`.
- `python scripts/check_repo_harness.py`
  - Passed.
- `git diff --check`
  - Passed.

## Runtime Artifacts

- Backend: `http://localhost:8080`
- Frontend: `http://localhost:8081`
- Docker services: backend, frontend, MySQL, and Redis are running; MySQL and
  Redis are healthy.

## Skipped Checks

- No full `mvn test` was run for this slice; backend Java code was not changed.
  The Docker image rebuild did run Maven package with tests skipped, matching
  the Compose build path.

## Risks And Follow-Ups

- Full FoolFrame Web parity is still not complete. Remaining work is tracked in
  `docs/migration/foolframe-parity.md`, mainly full DB schema/migration scripts
  and optional deeper legacy surfaces when real migrated modules require them.

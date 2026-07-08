# Vue Read View Cache

## Prompt

User pointed out that rendering should start from View metadata, then query data
from that View, and binding rendered UI to concrete business DTOs is wrong.

## Scope

- Kept `getreaditemview` results keyed by rendered `ViewId` in the Vue workflow.
- Made detail, init-new, and manual read-item panels read their own View
  metadata instead of sharing the last loaded read-item View response.
- Set the target detail `ViewId` before `initnew` data is merged for create
  operations, and cleared stale detail data before loading a different detail
  View.
- Updated parity docs and repo task state.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Red: `cd frontend && npm test -- viewWorkflow.test.ts` failed because
  `rememberReadView` did not exist.
- Red: `cd frontend && npm test -- payload.test.ts` failed because `App.vue`
  still used the single `readItemViewResponse` slot for rendered detail state.
- Green: `cd frontend && npm test -- viewWorkflow.test.ts` passed, 28 tests.
- Green: `cd frontend && npm test -- payload.test.ts` passed, 54 tests.
- Frontend: `cd frontend && npm test` passed, 4 files / 85 tests.
- Frontend: `cd frontend && npm run build` passed.
- Harness: `python scripts/check_repo_harness.py` passed.
- Whitespace: `git diff --check` passed.
- Runtime: `python scripts/runtime_doctor.py` passed against the running Docker
  stack.

## Runtime Evidence

- `docker compose ps` showed backend, frontend, MySQL, and Redis all running.
- Runtime doctor passed View-first checks for `getlistview`, `querydata`,
  `querydatadetail`, `getreaditemview.DetailViews`, `inputquery`, and report
  routes.

## Risks

- The frontend now keeps multiple read-item Views in memory for the session.
  This is intentionally small and per-page; add eviction only if real View
  counts make that necessary.

## Follow-ups

- Continue migrating remaining backend runtime side effects and report/export
  surfaces from the parity backlog.

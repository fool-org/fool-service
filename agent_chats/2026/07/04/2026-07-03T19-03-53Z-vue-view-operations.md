# Vue View Operations

## Prompt

- Continue the Docker/FoolFrame/Vue migration.
- Keep View rendering first, then use View metadata to drive data and
  operation behavior.
- Avoid binding operation execution to concrete business DTOs.
- Keep the change small and commit atomically.

## Scope

- Compared FoolFrame `runoperation`: the request carries only
  `ViewId`, `ObjectId`, and `OperationId`; operation metadata comes from the
  View.
- Reused the existing Vue `runOperation()` path and rendered operation buttons
  from `viewResponse.data.operations` in the primary View workflow.
- Displayed operation parameter names from `operation.params` so the previously
  hydrated `OperationViewItem` metadata is visible in the main workflow.
- Kept the manual API-tools `Run Operation` panel unchanged.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Red test:
  `cd frontend && npm test -- payload.test.ts`
  - Failed as expected because `View Operations` was not present in the main
    workflow.
- Frontend:
  `cd frontend && npm test && npm run build`
  - Passed: 3 test files, 44 tests.
  - Passed: `vue-tsc --noEmit` and Vite production build.

## Runtime Evidence

- Rebuilt and restarted Docker frontend:
  `docker compose build --quiet frontend`
  `docker compose up -d --no-deps --force-recreate frontend`
- Frontend root:
  `curl http://localhost:8081/`
  - Returned the Vue shell HTML.
- Live View metadata:
  `POST /api/v1/view/getlistview` with `{"viewId":100}`
  - Returned operation `7002` with `params[0].paramName=remark`.
- Served frontend bundle:
  - Contains `View Operations`.
- Compose status:
  `docker compose ps`
  - backend and frontend were up on ports `8080` and `8081`.
  - MySQL and Redis were healthy.

## Skipped Checks

- Backend tests were not rerun because this slice only touched Vue and docs.
- Browser-click automation was not added; the runtime proof covered the served
  bundle and the live View operation metadata source.

## Risks

- Operation parameter values are still not collected by the UI. FoolFrame's
  `runoperation` DTO does not carry them, so this slice only surfaces the
  metadata and executes the legacy operation request.

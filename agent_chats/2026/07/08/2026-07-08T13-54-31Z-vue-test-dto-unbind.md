# Vue Test DTO Unbind

## Prompt

User flagged that the page should render from View first, then query data from
that View, and should not bind to a concrete business DTO.

## Scope

- Removed the Vue workspace panel that fetched backend `/test` seed rows.
- Removed Vite and Nginx frontend proxies for `/test`.
- Kept backend `/test` untouched as the Docker backend health check.
- Updated migration/task notes so frontend proof points at
  `getlistview(ViewId)` then `querydata(ViewId)`.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `frontend/vite.config.ts`
- `frontend/nginx.conf`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Red: `cd frontend && npm test -- payload.test.ts` failed on the new checks
  because `App.vue` still exposed `Backend Smoke` and Vite still proxied
  `"/test"`.
- Green: `cd frontend && npm test -- payload.test.ts` passed 46 tests.
- `cd frontend && npm test` passed 74 tests.
- `cd frontend && npm run build` passed.
- `python scripts/check_repo_harness.py` passed.
- `docker compose up -d --build frontend` rebuilt and restarted the frontend;
  Compose also rebuilt the backend dependency image.
- `python scripts/runtime_doctor.py` passed all compose, schema, backend
  `/test`, and frontend-proxied `/api` checks.

## Skipped Checks

- No known skipped checks for this slice.

## Risks

- Users who relied on the Vue Tools tab to inspect `/test` seed rows must now
  use the backend health route directly. The migrated browser data path remains
  the View-driven workflow.

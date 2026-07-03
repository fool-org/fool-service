# Vue getlistview control

## Prompt

Continue the active migration goal:

1. Bring the environment up with Docker.
2. Complete migration against `../FoolFrame`.
3. Use Vue for the frontend.
4. Commit atomically and promptly.

## Scope

- Added a typed Vue payload builder for the legacy `/api/v1/view/getlistview`
  request shape.
- Added a View ID control and Legacy List View action to the existing Vue
  View Definition panel.
- Reused the existing view-definition response summary/table metadata display.
- Updated the migration parity document.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`

## Validation

- RED: `cd frontend && npm test`
  - Failed before implementation with `buildLegacyListViewRequest is not a
    function`.
- GREEN: `cd frontend && npm test`
  - Passed: 1 file, 8 tests.
- `cd frontend && npm run build`
  - Passed `vue-tsc --noEmit` and Vite production build.
- `docker compose up -d --build frontend`
  - Attempted twice; both failed while resolving Docker Hub metadata with
    `net/http: TLS handshake timeout`.
- `DOCKER_BUILDKIT=0 docker build -t fool-service-frontend ./frontend`
  - Passed using already-present local `node:20-alpine` and `nginx:1.27-alpine`
    base images.
- `docker compose up -d --no-build --force-recreate frontend`
  - Recreated the frontend container from the new local image.
- `curl http://localhost:8081/`
  - Returned `/assets/index-BSua5iGz.js`.
- `curl http://localhost:8081/assets/index-BSua5iGz.js | rg -o "Legacy List View|getlistview|legacy-list-view"`
  - Found the new Vue legacy list-view UI strings in the running asset.
- `curl -H 'Content-Type: application/json' -d '{"viewId":100}' http://localhost:8080/api/v1/view/getlistview`
  - Returned `OrderList` with `Order ID`, `Symbol`, and `State` columns.
- `docker compose ps`
  - Backend, frontend, MySQL, and Redis are running; MySQL and Redis are
    healthy.
- `python scripts/check_repo_harness.py`
  - Passed.

## Skipped Checks

- No browser screenshot was captured; the slice was verified through the built
  and running frontend asset plus the backend legacy endpoint response.

## Risks

- This exposes legacy list-view loading by ID only. Legacy read-item view
  loading is still a separate Vue surface.

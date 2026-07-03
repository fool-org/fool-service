# Vue getreaditemview panel

## Prompt

Continue the active migration goal:

1. Bring the environment up with Docker.
2. Complete migration against `../FoolFrame`.
3. Use Vue for the frontend.
4. Commit atomically and promptly.

## Scope

- Added a typed Vue payload builder for the legacy
  `/api/v1/view/getreaditemview` request shape.
- Added a Read Item View panel to the existing Vue migration console.
- Rendered returned read-item field metadata in a compact table.
- Updated the migration parity document.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`

## Validation

- RED: `cd frontend && npm test`
  - Failed before implementation with `buildLegacyReadItemViewRequest is not a
    function`.
- GREEN: `cd frontend && npm test`
  - Passed: 1 file, 9 tests.
- `cd frontend && npm run build`
  - Passed `vue-tsc --noEmit` and Vite production build.
- `DOCKER_BUILDKIT=0 docker build -t fool-service-frontend ./frontend`
  - Passed using already-present local `node:20-alpine` and `nginx:1.27-alpine`
    base images.
- `docker compose up -d --no-build --force-recreate frontend`
  - Recreated the frontend container from the new local image.
- `curl http://localhost:8081/`
  - Returned `/assets/index-CXLgp2zx.js`.
- `curl http://localhost:8081/assets/index-CXLgp2zx.js | rg -o "Read Item View|getreaditemview|read-item-view|Load Read Items"`
  - Found the new Vue read-item UI strings in the running asset.
- `curl -H 'Content-Type: application/json' -d '{"viewId":100}' http://localhost:8080/api/v1/view/getreaditemview`
  - Returned `OrderList` read items for `Order ID`, `Symbol`, and `State`.
- `docker compose ps`
  - Backend, frontend, MySQL, and Redis are running; MySQL and Redis are
    healthy.
- `python scripts/check_repo_harness.py`
  - Passed.

## Skipped Checks

- No browser screenshot was captured; the slice was verified through the built
  and running frontend asset plus the backend legacy endpoint response.

## Risks

- This panel displays read-item metadata only. It does not yet compose a full
  editable detail form from `querydatadetail` plus read-item metadata.

# Delivery Evidence: Vue `/view:id` Startup Route

## Prompt

Continue the Docker/FoolFrame/Vue migration goal with atomic commits, reuse,
and View-first rendering.

## Scope

- Compared old FoolFrame Web routes in `../FoolFrame/src/Web/app.js` and
  `../FoolFrame/src/Web/routes/index.js`.
- Added the smallest Vue startup bridge for the old list route `/view:id`.
- Kept detail/new deep-link routes out of this slice; they need separate
  detail/new workflow handling.

## Changed Files

- `frontend/src/viewWorkflow.ts`
- `frontend/src/App.vue`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- `cd frontend && npm test -- src/viewWorkflow.test.ts src/payload.test.ts`
- `cd frontend && npm test`
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py`
- `git diff --check`
- `docker compose up -d --build frontend`
- `docker compose up -d --no-deps --force-recreate frontend`
- `curl -sS -o /tmp/fool-view100.html -w '%{http_code} %{content_type}\n' http://localhost:8081/view100`
- `python scripts/runtime_doctor.py`
- `docker compose ps`

## Skipped Checks

- No full browser automation was added for this slice; route parsing is covered
  by Vue unit tests and the rebuilt Nginx container serves `/view100` as the
  Vue entrypoint.

## Risks

- This slice only maps old list paths like `/view100`; old detail/new paths
  such as `/view100/1001` and `/new100` are deliberately not treated as list
  routes.

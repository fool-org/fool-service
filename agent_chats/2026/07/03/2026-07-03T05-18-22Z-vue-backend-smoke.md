# Vue Backend Smoke Panel

## Prompt

- Active goal: bring the environment up with Docker, complete the migration against
  `../FoolFrame`, use Vue for the frontend, and keep timely atomic commits.
- User status question: "百分比达到多少".

## Scope

- Added a Vue operator-console panel for the Docker backend `/test` seed-data
  smoke route.
- Kept the route same-origin by wiring both Vite dev-server and Compose Nginx
  `/test` proxies to the backend.
- Updated migration parity notes for the newly visible backend smoke workflow.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `frontend/vite.config.ts`
- `frontend/nginx.conf`
- `docs/migration/foolframe-parity.md`

## Validation

- RED: `cd frontend && npm test` failed with the new App smoke test because
  `Backend Smoke` was not present.
- RED: `cd frontend && npm test` failed with the new proxy test because
  `vite.config.ts` did not contain `"/test"`.
- GREEN: `cd frontend && npm test` passed 14 tests.
- GREEN: `cd frontend && npm run build` passed.
- Runtime browser check: opened `http://127.0.0.1:5173/`, confirmed the
  `Backend Smoke` panel, clicked `Load Seed Data`, and verified the page showed
  both seeded `/test` rows with no frontend error.
- GREEN: `python scripts/check_repo_harness.py` passed.
- GREEN: `git diff --check` passed.
- GREEN: `curl -sS http://localhost:8080/test` returned the two seeded rows.
- GREEN: `curl -sS http://127.0.0.1:5173/test` returned the two seeded rows
  through the Vite proxy.
- GREEN: `docker compose up -d --build frontend` completed; it also rebuilt the
  backend image with `mvn -DskipTests package`.
- GREEN: `docker compose up -d --force-recreate --no-deps frontend` replaced the
  running frontend container.
- GREEN: `curl -sS http://localhost:8081/test` returned the two seeded rows
  through the Compose Nginx proxy.
- GREEN: `docker compose ps` showed backend, frontend, MySQL, and Redis running;
  MySQL and Redis were healthy.

## Skipped Checks

- Full backend Maven tests were not rerun for this UI/proxy-only change; the
  Compose build did run backend packaging with tests skipped.

## Risks And Follow-ups

- The `/test` panel is a Docker smoke visibility aid, not a new legacy
  FoolFrame business workflow.

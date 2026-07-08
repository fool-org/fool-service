# Default View shell wiring

## Prompt

Keep the FoolFrame migration focused on View-first rendering: load the View
page first, query data from that View, and avoid concrete business DTO binding.

## Scope

- Added FoolFrame Pascal response aliases for the migrated legacy auth shell:
  `App`, `TopMenu`, `Items`, and App/Menu/User field aliases including
  `DefaultViewId`.
- Made the Vue first-screen workflow read the default View id from
  `loginv2` / `getmain` / `getapp` shell payloads before calling
  `getlistview`, then reuse the existing `querydata` path for rows.
- Updated the runtime doctor to carry the shell default View id into the
  View/data smoke path.
- Updated `tasks.md` and `docs/migration/foolframe-parity.md`.

## Validation

- `cd frontend && npm test`: passed, 68 tests.
- `cd frontend && npm run build`: passed.
- `python scripts/runtime_doctor_test.py`: passed, 12 tests.
- `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-auth -am -DfailIfNoTests=false -Dtest=LoginControllerLogoutTest test`:
  passed.
- `docker compose up -d --build`: local images built successfully earlier in
  the slice. A later retry hit Docker Hub DNS while resolving
  `node:20-alpine`, so the containers were recreated from the already-built
  local `fool-service-backend:latest` and `fool-service-frontend:latest`
  images with `docker compose up -d --no-build backend frontend`.
- `python scripts/runtime_doctor.py`: passed against the recreated Docker
  stack.
- Live JSON probe through `http://localhost:8081`: `loginv2`, `getmain`, and
  `getapp` expose both camel-case and Pascal legacy shell fields; observed
  `App.DefaultViewId=100` and `TopMenu[0].AuthNo`.
- `python scripts/check_repo_harness.py`: passed.
- `git diff --check`: passed.

## Runtime Evidence

- Before the change, Docker `getmain` / `getapp` returned only camel-case App
  fields such as `app.defaultViewId`, while FoolFrame routes read
  `maindata.App.DefaultViewId`.
- The final Docker probe showed `main.App.DefaultViewId=100` and
  `main.TopMenu[0].AuthNo`, then `runtime_doctor` used that App default View id
  for `getlistview` before calling `querydata`.

## Risks

- Full `loginv2` app-session selection is still broader future work; this
  change uses the migrated default AppInfo returned by the current token path.

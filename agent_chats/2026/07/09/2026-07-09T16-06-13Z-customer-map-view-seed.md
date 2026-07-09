# Customer Map View Seed

## Prompt

- Continue the Docker/Vue FoolFrame migration.
- Keep the workflow View-first: load rendered View metadata, then load data
  from the View's configured child View IDs.
- Avoid concrete business DTO shortcuts and keep file growth controlled.

## Scope

- Added Docker `market_customer` longitude/latitude columns and seeded values.
- Added Customer model properties and a real `CustomerMap` View in both
  compatibility and legacy `SW_SYS_*` metadata.
- Pointed the Sudoku `Map` panel at `CustomerMap` through `ListViewId`.
- Preserved legacy map item edit-type codes in Java and Vue:
  `16` longitude, `17` latitude, `18` title.
- Extended the runtime doctor to read the Sudoku Map panel `ListViewId` before
  querying map rows.

## Changed Files

- `docker/mysql/init/001-market-order.sql`
- `docker/mysql/init/006-view.sql`
- `fool-view/src/main/java/org/fool/framework/view/model/ItemEditType.java`
- `fool-view/src/test/java/org/fool/framework/view/model/ViewTypeMigrationTest.java`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T16-06-13Z-customer-map-view-seed.md`

## Validation

- Red: `docker run --rm -v "$PWD":/workspace -w /workspace --network fool-service_default maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=ViewTypeMigrationTest test`
  failed before implementation because `MapLongitude`, `MapLatitude`, and
  `MapTitle` were missing.
- Red: `cd frontend && npm test -- viewWorkflow.test.ts` failed before the Vue
  helper recognized enum-name map edit types.
- Green: `python scripts/runtime_doctor_test.py`
- Green: `cd frontend && npm test -- viewWorkflow.test.ts`
- Green: `docker run --rm -v "$PWD":/workspace -w /workspace --network fool-service_default maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -Dtest=ViewTypeMigrationTest test`
- Green: applied `docker/mysql/init/001-market-order.sql` and
  `docker/mysql/init/006-view.sql` to the running MySQL volume.
- Green: `docker compose up -d --build backend frontend`
- Green: `docker compose up -d --no-deps --force-recreate frontend`
- Green: `python scripts/runtime_doctor.py`
- Green: `cd frontend && npm test`
- Green: `cd frontend && npm run build`
- Green: `python scripts/check_repo_harness.py`
- Green: `git diff --check`

## Runtime Evidence

- `scripts/runtime_doctor.py` passed with backend, frontend, MySQL, and Redis
  up.
- The new `data:querydata-map-items` check passed by reading Sudoku Map panel
  metadata first and then querying the child View rows for map edit types.

## Skipped Checks

- Chrome DevTools page smoke was attempted but Chrome remote debugging was not
  available on `127.0.0.1:9222`.

## Risks

- The Vue `Map` panel still renders a marker table, not a map SDK widget. That
  matches the current migration slice and avoids adding a new dependency.

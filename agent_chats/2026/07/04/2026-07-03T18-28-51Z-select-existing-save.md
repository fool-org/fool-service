# Select Existing Save

## Prompt

- Continue the Docker/FoolFrame/Vue migration.
- Keep the View-driven flow: render from View metadata, query data through the
  selected View, and avoid binding the page to business DTO fields.
- Keep the diff small and commit atomically.

## Scope

- Compared FoolFrame select-existing child item save behavior:
  `HandlerSaveObj` loads the existing object first, `ObjUpdateToProxy` applies
  posted fields, then `context.Save` persists it.
- Fixed migrated legacy `saveobj` to load existing dynamic data before applying
  partial `Propertyies` and `Itemproperties`.
- Kept ordinary Vue child add/update readonly filtering intact.
- Changed only the Vue selected-existing path to include readonly candidate
  fields in `AddedItems.Propertyies`, matching FoolFrame selected-row payloads.
- Added Docker seed candidate `2002 / 1002 / Existing fee` for repeatable
  select-existing proof.

## Changed Files

- `docker/mysql/init/006-view.sql`
- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceSaveObjTest.java`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Red first:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=DataQueryServiceSaveObjTest#saveLegacyObjectPreservesUnpostedExistingProperties test`
  - Failed before implementation: expected `symbol=BTC-USDT`, got `null`.
- Backend focused:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=DataQueryServiceSaveObjTest test`
  - Passed: 6 tests.
- Frontend focused:
  `cd frontend && npm test -- viewWorkflow.test.ts`
  - Passed: 7 tests.
- Frontend full:
  `cd frontend && npm test && npm run build`
  - Passed: 3 test files, 43 tests.
  - Passed: `vue-tsc --noEmit` and Vite production build.

## Runtime Evidence

- Applied updated seed SQL:
  `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/006-view.sql`
- Rebuilt/recreated runtime containers:
  - `docker compose build --quiet backend && docker compose up -d --no-deps --force-recreate backend`
  - `docker compose build --quiet frontend && docker compose up -d --no-deps --force-recreate frontend`
- Initial DB state before browser select:
  - `2001 / 1001 / Legacy item`
  - `2002 / 1002 / Existing fee`
  - `2004 / 1001 / Delete me`
- Browser target: `http://localhost:8081/`.
- Browser plugin was available. `domSnapshot()` is still unusable in this
  environment (`incrementalAriaSnapshot is not a function`), so proof used body
  text, DOM reads, scoped interaction, console logs, a Browser screenshot, and
  DB queries.
- Browser interaction:
  - Opened order `1001 / BTC-USDT`.
  - In Items, searched candidate keyword `Existing`.
  - Candidate table rendered `2002 / Existing fee / Select`.
  - Clicked `Select`.
  - Detail area changed to `Items / 4 rows` and included child row `2002`.
  - Console warnings/errors: none.
- DB proof after select:
  - `2002 / 1001 / Existing fee`.
- Seed reset after proof:
  - Reapplied `docker/mysql/init/006-view.sql`.
  - Final DB state restored `2002 / 1002 / Existing fee`.

## Skipped Checks

- Full backend `mvn test` was not run; this slice touched `fool-view`
  `saveobj`, Docker seed data, and Vue payload helpers, so focused backend
  tests plus frontend full test/build and Docker browser proof were used.

## Risks

- Select-existing for One2Many currently reassigns the selected child row to
  the new parent, which matches the migrated owned-relation write path. Other
  relation types should be proven separately when their Docker seed views
  exist.

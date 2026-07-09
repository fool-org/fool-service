# Legacy Web querylist Alias

## Prompt

Continue the FoolFrame migration with Docker, Vue, atomic commits, and maximum
reuse. Keep the frontend and backend on the View-first protocol path.

## Scope

- Compared `../FoolFrame/src/Web/public/javascripts/app/querylistdata.js`,
  `mapview.js`, and `../FoolFrame/src/Web/routes/index.js`.
- Reused the existing Java `querydata` service path for the old Web
  `/data/querylist` protocol under the migrated `/api/v1/data` prefix.
- Added DTO aliases for legacy Web list-query payload fields: `viewid`,
  `filter`, `page`, `pagesize`, `orderitem`, and `ordertype`.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/dto/LegacyQueryDataRequest.java`
- `fool-view/src/main/java/org/fool/framework/view/api/DataController.java`
- `fool-view/src/test/java/org/fool/framework/view/api/DataControllerLegacyQueryDataTest.java`
- `scripts/runtime_doctor.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- `git diff --check`
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=DataControllerLegacyQueryDataTest test`
  - Passed: `DataControllerLegacyQueryDataTest`, 7 tests.
- `python scripts/runtime_doctor_test.py`
  - Passed: 35 tests.
- `docker compose up -d --build backend`
  - Passed: backend image rebuilt and `fool-service-backend-1` restarted.
- `python scripts/runtime_doctor.py`
  - Passed, including `data:querylist-legacy-web-payload`.

## Skipped Checks

- None yet.

## Risks

- The raw old Express route `/data/querylist` was not added. The migrated alias
  is `/api/v1/data/querylist`, matching the current Vue/Nginx `/api/*` runtime
  boundary.
- `orderitem` / `ordertype` are accepted at the DTO boundary but still follow
  the existing `querydata` behavior until list-query ordering is wired as its
  own focused migration slice.

# Legacy Web inputquery Aliases

## Prompt

Continue the FoolFrame migration with Docker, Vue, atomic commits, and maximum
reuse. Keep the frontend and backend on the View-first protocol path.

## Scope

- Compared `../FoolFrame/src/Web/public/javascripts/app/setextype.js` and
  `../FoolFrame/src/Web/routes/index.js`.
- Reused the existing Java `inputquery` service path.
- Added DTO aliases for legacy Web lookup payload fields: `viewid`, `itemid`,
  `text`, `objid`, `ownerid`, `newadd`, plus lower-case `modelid`.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/dto/InputQueryRequest.java`
- `fool-view/src/test/java/org/fool/framework/view/api/DataControllerInputQueryTest.java`
- `scripts/runtime_doctor.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- `git diff --check`
- Docker Maven focused test:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=DataControllerInputQueryTest test`
- `python scripts/runtime_doctor_test.py`
- `docker compose up -d --build backend`
- `python scripts/runtime_doctor.py`
- `python scripts/check_repo_harness.py`

## Skipped Checks

- Full `mvn test` was not rerun for this DTO-alias-only slice; focused
  `fool-view` tests and Docker runtime doctor covered the changed behavior.

## Risks

- The raw old Express route `/data/inputquery` was not added. The migrated
  route remains `/api/v1/data/inputquery`, matching the current Vue/Nginx
  `/api/*` runtime boundary.

# Legacy Cloud-Social inputquery ViewName Id

## Prompt

Continue the FoolFrame migration with Docker, Vue, atomic commits, maximum
reuse, and View-first data flow.

## Scope

- Compared `../FoolFrame/src/Web/Cloud-Social/soway.js` and
  `../FoolFrame/src/Web/routes/index.js` for the old `inputquery` payload.
- Reused the existing `/api/v1/data/inputquery` service path and shared View
  lookup boundary.
- Accepted only numeric `ViewName` as a legacy View id fallback; nonnumeric
  business-name-only `ViewName` remains rejected.
- Added Docker runtime-doctor coverage for the Cloud-Social `ViewName`
  payload through the Vue proxy.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceInputQueryTest.java`
- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=DataQueryServiceInputQueryTest test`
- `python scripts/runtime_doctor_test.py`
- `python scripts/check_repo_harness.py`
- `git diff --check`
- `docker compose up -d --build backend`
- `python scripts/runtime_doctor.py`

## Skipped Checks

- Full root `mvn test` was not rerun because this slice is limited to
  `inputquery` View-id compatibility and focused `fool-view -am` coverage.
- Frontend tests were not rerun because no frontend source changed.

## Risks

- Low; this keeps lookup View resolution numeric-id driven and does not
  reintroduce business-name View rendering.

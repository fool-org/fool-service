# Legacy Web view Payload

## Prompt

Continue the FoolFrame migration with Docker, Vue, atomic commits, maximum
reuse, and View-first data flow.

## Scope

- Compared `../FoolFrame/src/Web/routes/index.js` and
  `../FoolFrame/src/Web/Cloud-Social/soway.js` for the old `/view` route.
- Reused the existing `/api/v1/view/getlistview` endpoint and
  `ViewDataRequest` DTO boundary.
- Added Docker runtime-doctor coverage for the old Web view payload: `id`.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/dto/ViewDataRequest.java`
- `fool-view/src/test/java/org/fool/framework/view/api/ViewControllerLegacyGetListViewTest.java`
- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=ViewControllerLegacyGetListViewTest test`
- `python scripts/runtime_doctor_test.py`
- `python scripts/check_repo_harness.py`
- `git diff --check`
- `docker compose up -d --build backend`
- `python scripts/runtime_doctor.py`

## Skipped Checks

- Full root `mvn test` was not rerun because this slice is limited to
  `getlistview` request aliases and focused `fool-view -am` coverage.
- Frontend tests were not rerun because no frontend source changed.

## Risks

- Low; this only widens request aliases at the shared View DTO boundary and
  keeps View lookup numeric-id driven.

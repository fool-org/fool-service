# Legacy Web mkqview Payload

## Prompt

Continue the FoolFrame migration with Docker, Vue, atomic commits, maximum
reuse, and View-first data flow.

## Scope

- Compared `../FoolFrame/src/Web/app.js`,
  `../FoolFrame/src/Web/routes/index.js`,
  `../FoolFrame/src/Web/Cloud-Social/soway.js`, and
  `../FoolFrame/src/Web/public/javascripts/app/mkreport.js`.
- Reused the existing `/api/v1/report/mkqview` alias and
  `MakeReportRequest.viewid` DTO alias.
- Added Docker runtime-doctor coverage for the old Web lower-case
  `/report/mkqview` payload through the Vue proxy.

## Changed Files

- `fool-view/src/test/java/org/fool/framework/view/api/ReportControllerTest.java`
- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=ReportControllerTest test`
- `python scripts/runtime_doctor_test.py`
- `python scripts/check_repo_harness.py`
- `git diff --check`
- `python scripts/runtime_doctor.py`

## Skipped Checks

- Full root `mvn test` was not rerun for this test/runtime-doctor slice.
- `docker compose up -d --build` was not rerun because no backend runtime code
  changed; the current Docker stack was verified by `runtime_doctor.py`.

## Risks

- No business implementation was changed; this slice guards existing shared
  report routing and DTO alias behavior.

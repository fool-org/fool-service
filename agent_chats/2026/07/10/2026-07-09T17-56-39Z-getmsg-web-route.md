# Legacy Web getmsg Route

## Prompt

Continue the FoolFrame migration with Docker, Vue, atomic commits, maximum
reuse, and View-first data flow.

## Scope

- Compared `../FoolFrame/src/Web/app.js`, `routes/index.js`, and
  `Cloud-Social/soway.js` for message polling.
- Reused the existing migrated message polling service path.
- Added the old FoolFrame Web root `POST /getmsg` shape as `/api/v1/getmsg`
  without duplicating controller logic.
- Added Docker runtime-doctor coverage for the route through the Vue proxy.

## Changed Files

- `business-application/src/main/java/org/fool/framework/application/api/MessageController.java`
- `business-application/src/test/java/org/fool/framework/application/api/MessageControllerTest.java`
- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl business-application -am -Dtest=MessageControllerTest test`
- `python scripts/runtime_doctor_test.py`
- `python scripts/check_repo_harness.py`
- `git diff --check`
- `docker compose up -d --build backend`
- `python scripts/runtime_doctor.py`

## Skipped Checks

- Full root `mvn test` was not rerun because this slice is limited to one
  message-route alias and focused module coverage.
- Frontend tests were not rerun because no frontend source changed.

## Risks

- Low; this adds one route alias to the existing controller method.

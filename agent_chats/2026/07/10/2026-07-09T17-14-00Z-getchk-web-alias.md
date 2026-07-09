# Legacy Web getchk Alias

## Prompt

Continue the FoolFrame migration with Docker, Vue, atomic commits, maximum
reuse, and View-first data flow.

## Scope

- Compared `../FoolFrame/src/Web/app.js`,
  `../FoolFrame/src/Web/routes/index.js`, and
  `../FoolFrame/src/Web/Cloud-Social/soway.js`.
- Reused the existing `getcheckcode` generator.
- Added `/api/v1/auth/getchk` as a migrated alias for the old Web
  `/user/getchk` wrapper.

## Changed Files

- `fool-auth/src/main/java/org/fool/framework/auth/api/LoginController.java`
- `fool-auth/src/test/java/org/fool/framework/auth/api/LoginControllerLogoutTest.java`
- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-auth -am -Dtest=LoginControllerLogoutTest test`
- `python scripts/runtime_doctor_test.py`
- `python scripts/check_repo_harness.py`
- `git diff --check`
- `docker compose up -d --build backend`
- `python scripts/runtime_doctor.py`

## Skipped Checks

- Full root `mvn test` was not rerun for this alias-only auth route slice.

## Risks

- The raw Express `/user/getchk` path was not mounted. Compatibility stays
  under the migrated `/api/v1/auth/getchk` boundary.

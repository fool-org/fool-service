# Legacy Web loginv2 Payload

## Prompt

Continue the FoolFrame migration with Docker, Vue, atomic commits, maximum
reuse, and View-first data flow.

## Scope

- Compared `../FoolFrame/src/Web/routes/index.js`,
  `../FoolFrame/src/Web/Cloud-Social/soway.js`, and
  `../FoolFrame/src/Web/public/javascripts/app/login.js`.
- Reused the existing `/api/v1/auth/loginv2` legacy login flow.
- Added DTO aliases for old Web login fields: `name`, `pwd`, `dbid`, `chk`,
  and `chkid`.
- Kept `AppId` / `AppKey` explicit instead of adding a new host/default-app
  selection branch.

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
- `docker compose up -d --build backend`
- `git diff --check`
- `python scripts/runtime_doctor.py`

## Skipped Checks

- Full root `mvn test` was not rerun for this DTO-boundary auth slice.

## Risks

- This does not recreate the raw Node `/user/login` wrapper. Compatibility is
  at the migrated `/api/v1/auth/loginv2` boundary with explicit app identity.

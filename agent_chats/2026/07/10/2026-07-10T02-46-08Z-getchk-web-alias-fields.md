# GetChk Web Alias Fields

## Prompt
- Continue the Docker/FoolFrame/Vue migration with atomic commits and maximum
  reuse.

## Scope
- Added `chkkey` / `chkimg` response aliases to the shared check-code DTO.
- Tightened the Docker runtime doctor so `/api/v1/auth/getchk` must expose the
  old FoolFrame Web `login.js` field names through the Vue proxy.
- Updated migration parity and task-state docs.

## Changed Files
- `fool-auth/src/main/java/org/fool/framework/auth/business/service/CheckCodeService.java`
- `fool-auth/src/test/java/org/fool/framework/auth/api/LoginControllerLogoutTest.java`
- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/10/2026-07-10T02-46-08Z-getchk-web-alias-fields.md`

## Validation
- `python scripts/runtime_doctor_test.py` passed: 46 tests.
- `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-auth -am -Dtest=LoginControllerLogoutTest test` passed.
- `docker compose up -d --build backend` rebuilt and restarted the backend.
- `python scripts/runtime_doctor.py` passed against Docker.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.
- Direct Vue-proxy proof for `POST http://localhost:8081/api/v1/auth/getchk`
  returned truthy `Key`, `ChkCodeImg`, `chkkey`, and `chkimg`.
- Host Maven was not used as the final gate because the local host Java is 8
  and this repo targets Java 17; the focused Maven gate was run in the Docker
  Java 17 image instead.

## Runtime Evidence
- Before the patch, `/api/v1/auth/getchk` returned `Key` / `ChkCodeImg` plus
  camel-case fields, but not the exact old Web `chkkey` / `chkimg` names.

## Risks
- This expands serialized check-code responses; it does not change validation
  or login behavior.

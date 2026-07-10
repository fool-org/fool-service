# Login IsLogin Alias

## Prompt
- Continue the Docker/FoolFrame/Vue migration with atomic commits, maximum
  reuse, and View-first protocol checks.

## Scope
- Matched the old FoolFrame Web `user/login` wrapper and `login.js` consumer,
  which read `IsLogin` after `soway.login` returned `LoginSucess`.
- Added the `IsLogin` response alias to the existing shared `loginv2`
  `LegacyLoginResult` DTO.
- Tightened the Docker runtime doctor so the legacy Web login payload check
  also requires `IsLogin` through the Vue proxy.
- Updated the Vue API type and migration/task-state docs.

## Changed Files
- `fool-auth/src/main/java/org/fool/framework/auth/api/LoginController.java`
- `fool-auth/src/test/java/org/fool/framework/auth/api/LoginControllerLogoutTest.java`
- `frontend/src/api.ts`
- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/10/2026-07-10T03-04-30Z-login-islogin-alias.md`

## Validation
- `python scripts/runtime_doctor_test.py` passed: 46 tests.
- `cd frontend && npm test` passed: 129 tests.
- `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-auth -am -Dtest=LoginControllerLogoutTest test` passed: 14 tests.
- `cd frontend && npm run build` passed.
- `docker compose up -d --build backend` rebuilt and restarted the backend.
- `python scripts/runtime_doctor.py` passed against Docker; the
  `auth:loginv2-legacy-web-payload` check now requires `IsLogin`.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.
- Direct Vue-proxy login proof returned:
  `{"LoginSucess": true, "IsLogin": true, "Token": "<uuid>"}`

## Runtime Evidence
- Old `../FoolFrame/src/Web/routes/index.js` maps successful login to
  `{ IsLogin: true, Token: data.Token }`.
- Old `../FoolFrame/src/Web/public/javascripts/app/login.js` checks
  `data.IsLogin` before saving `data.Token`.

## Risks
- This only expands the serialized login response. It does not change check-code
  validation, token creation, app/database lookup, or Vue rendering flow.

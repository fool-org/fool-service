# Legacy User Avatar

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with old
  FoolFrame, with every behavior change committed atomically.

## Scope

- Compare `default.jade` avatar rendering with the migrated auth responses and
  Vue shell.
- Hydrate `UserAvtarUrl` from `SW_AUTH_USER.USER_AVTAR` for `loginv2`,
  `getmain`, and `getuserinfo`.
- Read the compatibility response through `viewWorkflow` and render a circular
  avatar when present, with the existing user icon as the empty-value fallback.

## Changed Files

- `fool-auth/src/main/java/org/fool/framework/auth/business/service/AuthService.java`
- `fool-auth/src/main/java/org/fool/framework/auth/api/LoginController.java`
- `fool-auth/src/test/java/org/fool/framework/auth/business/service/AuthServiceLegacyMenuTest.java`
- `fool-auth/src/test/java/org/fool/framework/auth/api/LoginControllerLogoutTest.java`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/App.vue`
- `frontend/src/ShellActions.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T11-36-34Z-legacy-user-avatar.md`

## Validation

- `docker run --rm --network fool-service_default ... maven:3.9-eclipse-temurin-17 mvn -pl fool-auth -am test`:
  12-module dependency reactor passed; `fool-auth` ran 57 tests with no
  failures or errors.
- `cd frontend && npm test -- --run`: 9 files, 147 tests passed.
- `cd frontend && npm run build`: TypeScript and Vite production build passed;
  `ShellActions` is 12.54 kB.
- `docker compose build backend frontend`: both current images built.
- `python scripts/runtime_doctor.py`: all checks passed, including loginv2,
  getuserinfo, getmain, messages, and View/data/report workflows.
- `python scripts/check_repo_harness.py`: passed.
- `git diff --check`: passed.

## Runtime Evidence

- Current backend and frontend images deployed at `http://localhost:8081`.
- Backend image timestamp: `2026-07-12T11:35:50Z`; frontend image timestamp:
  `2026-07-12T11:35:00Z`.
- `docker compose ps -a`: backend/frontend up, MySQL/Redis healthy, and
  `db-migrate` exited successfully with code 0.

## Skipped Or Downgraded Checks

- Docker admin has a null `USER_AVTAR`; live runtime covered the icon fallback,
  while the non-empty avatar path is covered by backend and frontend tests.
- Authenticated visual inspection requires a fresh CAPTCHA authorization.
- Host `mvn` was not used because its JDK rejects target 17; the documented
  Temurin 17 container test passed instead.

## Risks And Follow-Ups

- Final browser acceptance should verify a real non-empty avatar URL at desktop
  and mobile widths when migrated user data supplies one.

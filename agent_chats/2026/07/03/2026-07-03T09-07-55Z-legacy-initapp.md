# Legacy initapp

## Prompt

Continue the active goal: keep Docker running, migrate against `../FoolFrame`,
use Vue for the frontend, and commit in atomic slices.

## Scope

- Compared the legacy `Soway.Server` `initapp` contract and old web
  `Cloud-Social/soway.js` bootstrap call.
- Exposed `POST /api/v1/auth/initapp` with legacy `AppId` / `AppKey` aliases.
- Returned app title/name/image/version/power/url, a migrated check code, and
  store database rows from `SW_APPLICATION_SW_STOREDB`.
- Added the action to the Vue operator console.
- Updated the migration parity document.

## Changed Files

- `fool-auth/src/main/java/org/fool/framework/auth/api/LoginController.java`
- `fool-auth/src/main/java/org/fool/framework/auth/business/service/AuthService.java`
- `fool-auth/src/test/java/org/fool/framework/auth/api/LoginControllerLogoutTest.java`
- `fool-auth/src/test/java/org/fool/framework/auth/business/service/AuthServiceLegacyMenuTest.java`
- `frontend/src/App.vue`
- `frontend/src/api.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/03/2026-07-03T09-07-55Z-legacy-initapp.md`

## Validation

- RED backend:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-auth -am -DfailIfNoTests=false -Dtest=LoginControllerLogoutTest,AuthServiceLegacyMenuTest test`
  failed before implementation because `LegacyInitAppRequest`,
  `LegacyInitAppInfo`, `LegacyStoreBaseInfo`, `getLegacyInitAppInfo`, and
  `LegacyInitAppResult` did not exist.
- RED frontend:
  `npm test -- --run payload.test.ts` failed before implementation because the
  Vue console did not expose `Init App`, `/api/v1/auth/initapp`, or
  `initAppResponse`.
- GREEN backend:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-auth -am -DfailIfNoTests=false -Dtest=LoginControllerLogoutTest,AuthServiceLegacyMenuTest test`
  passed: 15 tests, 0 failures, 0 errors.
- GREEN frontend:
  `npm test` passed: 31 tests.
- GREEN frontend build:
  `npm run build` passed.
- Docker runtime smoke:
  `POST /api/v1/auth/initapp` passed through both
  `http://localhost:8080` and `http://localhost:8081`, returning app
  `Fool Service`, version `1.0.0`, a check-code key/code, and database
  `car_wash`.

## Skipped Checks

- Did not add a root-level legacy `initapp` route; this repository keeps legacy
  auth surfaces under `/api/v1/auth/*`.
- Did not reimplement FoolFrame session `CacheStore` state here; `loginv2`
  token-context work remains tracked as future migration work.

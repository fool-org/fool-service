# App Installer Authorized User Migration

## Scope

- Compared legacy `../FoolFrame/src/Server/SCPB08-Soway.AppManage/AppManager.cs`.
- Compared legacy `../FoolFrame/src/Server/SWUA02-SOWAY.ORM.AUTH/AuthorizedUser.cs`.
- Migrated the creator authorized-user side effect from the legacy create-app flow.

## Legacy Mapping

- Legacy `AppManager.CreatApp` creates the application, installs system/auth modules, then creates a `SOWAY.ORM.AUTH.AuthorizedUser`.
- Legacy `AuthorizedUser` maps to `SW_APP_AUTH_USER` with prefix `APP_AUTH_`.
- Legacy `AuthorizedUser.User` writes user fields into `APP_AUTH_USERID` and `APP_AUTH_USERLOGINNAME`.
- Java now has an `AuthorizedUser` model for `SW_APP_AUTH_USER` and `DaoAppInstallGateway.createAuthorizedUser` writes it through `DaoService`.

## Changes

- Added `AuthorizedUser` Java model:
  - `APP_AUTH_ID`
  - `APP_AUTH_USERID`
  - `APP_AUTH_USERLOGINNAME`
  - `APP_AUTH_DEP`
- Updated `DaoAppInstallGateway.createAuthorizedUser` to create the mapped model instead of throwing unsupported.
- Kept remaining installer side effects explicitly unsupported.
- Updated README and migration parity docs.

## TDD Evidence

- Red:
  - `docker run --rm -v "$HOME/.m2":/root/.m2 -v "$PWD":/workspace -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest -DfailIfNoTests=false test`
  - Failed compiling because `AuthorizedUser` did not exist.
- Green:
  - same command passed after adding the mapped model and DAO-backed gateway method.
  - `Tests run: 5, Failures: 0, Errors: 0, Skipped: 0`

## Verification

- `docker run --rm -v "$HOME/.m2":/root/.m2 -v "$PWD":/workspace -w /workspace maven:3.9-eclipse-temurin-17 mvn -DskipTests package`
  - Full 15-module reactor passed.
  - `fool-app-manage` compiled 14 main source files.
- `python scripts/check_repo_harness.py`
  - Repository harness validation passed.
- `git diff --check`
  - Passed with no whitespace errors.
- `docker compose up -d --build backend`
  - Rebuilt backend image successfully.
  - Restarted `fool-service-backend-1` on Java 17.
- `docker compose ps --all`
  - `backend`, `frontend`, `mysql`, and `redis` are running.
- `curl --retry 5 --retry-connrefused --retry-delay 2 -i --max-time 10 http://localhost:8080/test`
  - HTTP 200, body `[]`.
- `curl -I --max-time 10 http://localhost:8081/`
  - HTTP 200 from nginx frontend.

## Remaining App Installer Gaps

- Module installation remains unsupported.
- Menu creation remains unsupported.
- Role creation remains unsupported.
- App-system view preparation remains unsupported.
- The current DAO-backed authorized-user write uses the configured datasource; dynamic `App.SysCon` datasource routing still needs a concrete adapter.

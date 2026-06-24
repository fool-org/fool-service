# App Installer Menu Records Migration

## Scope

- Compared legacy `../FoolFrame/src/Server/SCPB08-Soway.AppManage/AppManager.cs`.
- Compared legacy `../FoolFrame/src/Server/SWUA02-SOWAY.ORM.AUTH/MenuItem.cs`.
- Compared legacy relation naming in `../FoolFrame/src/Server/SCPB05-Soway.Model/AssemblyModelFactory.cs`.
- Migrated the menu-record creation part of the legacy create-app flow.

## Legacy Mapping

- Legacy `AppManager.CreatApp` creates two top-level menus: `系统管理` and `人员及权限`.
- Legacy `MenuItem` maps to `SW_APP_AUTH_MENU` with prefix `AUTH_MENU_`.
- Legacy menu columns include `ID`, `TEXT`, `SHORTCUTKEY`, `IMAGE`, `VISIABLE`, `ENABLE`, `VIEWID`, `TEMPLATEFILE`, and `INDEX`.
- Java now has an `AuthMenuItem` model for `SW_APP_AUTH_MENU` and `DaoAppInstallGateway.createMenu` recursively creates records for a bootstrap menu tree.

## Changes

- Added `AuthMenuItem` Java model:
  - `AUTH_MENU_ID`
  - `AUTH_MENU_TEXT`
  - `AUTH_MENU_SHORTCUTKEY`
  - `AUTH_MENU_IMAGE`
  - `AUTH_MENU_VISIABLE`
  - `AUTH_MENU_ENABLE`
  - `AUTH_MENU_VIEWID`
  - `AUTH_MENU_TEMPLATEFILE`
  - `AUTH_MENU_INDEX`
- Updated `DaoAppInstallGateway.createMenu` to create menu records instead of throwing unsupported.
- Added `SW_APP_AUTH_MENU` to Docker MySQL init schema.
- Updated README and migration parity docs.

## TDD Evidence

- Red:
  - `docker run --rm -v "$HOME/.m2":/root/.m2 -v "$PWD":/workspace -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest -DfailIfNoTests=false test`
  - Failed compiling because `AuthMenuItem` did not exist.
- Green:
  - same command passed after adding the mapped model and DAO-backed gateway method.
  - `Tests run: 5, Failures: 0, Errors: 0, Skipped: 0`

## Remaining App Installer Gaps

- Menu parent-child relation rows remain unimplemented.
- Module installation remains unsupported.
- Role creation and role-menu/user relation rows remain unsupported.
- App-system view preparation remains unsupported.
- Dynamic `App.SysCon` datasource routing still needs a concrete adapter.

## Verification

- Focused test:
  - `docker run --rm -v "$HOME/.m2":/root/.m2 -v "$PWD":/workspace -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest -DfailIfNoTests=false test`
  - `Tests run: 5, Failures: 0, Errors: 0, Skipped: 0`
- Schema apply/check:
  - `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/004-event.sql`
  - `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SHOW COLUMNS FROM SW_APP_AUTH_MENU;"`
  - Confirmed `AUTH_MENU_ID`, `AUTH_MENU_TEXT`, `AUTH_MENU_SHORTCUTKEY`, `AUTH_MENU_IMAGE`, `AUTH_MENU_VISIABLE`, `AUTH_MENU_ENABLE`, `AUTH_MENU_VIEWID`, `AUTH_MENU_TEMPLATEFILE`, and `AUTH_MENU_INDEX`.
- Full build:
  - `docker run --rm -v "$HOME/.m2":/root/.m2 -v "$PWD":/workspace -w /workspace maven:3.9-eclipse-temurin-17 mvn -DskipTests package`
  - 15-module reactor `BUILD SUCCESS`.
- Harness:
  - `python scripts/check_repo_harness.py`
  - Passed.
- Whitespace:
  - `git diff --check`
  - Passed.
- Docker rebuild/smoke:
  - `docker compose up -d --build backend`
  - Backend image built with 15-module reactor `BUILD SUCCESS`; `backend` restarted.
  - `docker compose ps --all` showed backend/frontend up and MySQL/Redis healthy.
  - `docker compose logs --tail=80 backend` showed Spring Boot started and `/test` handled in 165 ms.
  - `curl --retry 5 --retry-connrefused --retry-delay 2 -i --max-time 10 http://localhost:8080/test` returned `HTTP/1.1 200` with `[]`.
  - `curl -I --max-time 10 http://localhost:8081/` returned `HTTP/1.1 200 OK`.

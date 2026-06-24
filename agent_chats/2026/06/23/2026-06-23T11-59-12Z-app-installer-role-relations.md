# App Installer Role And Menu Relation Migration

## Scope

- Compared legacy `../FoolFrame/src/Server/SWUA02-SOWAY.ORM.AUTH/Role.cs`.
- Compared legacy `../FoolFrame/src/Server/SWUA02-SOWAY.ORM.AUTH/MenuItem.cs`.
- Compared legacy relation naming in `../FoolFrame/src/Server/SCPB05-Soway.Model/AssemblyModelFactory.cs`.
- Migrated the app-installer role and menu relation part of the legacy create-app flow.

## Legacy Mapping

- Legacy `Role` maps to `SW_APP_AUTH_ROLE` with prefix `AUTH_ROLE_`.
- Legacy `Role.AuthUsers` maps to `SW_APP_AUTH_ROLE_SW_APP_AUTH_USER`.
- Legacy `Role.Items` maps to sorted many-to-many table `SW_APP_AUTH_MENU_SW_APP_AUTH_ROLE`.
- Legacy recursive `MenuItem.SubItems` maps to `SW_APP_AUTH_MENU_SubItems`.

## Changes

- Added `AuthRole`, `AuthRoleAuthorizedUserRelation`, `AuthRoleMenuItemRelation`, and `AuthMenuSubItemRelation`.
- Added `BootstrapMenuItem.persistedId` so installer-created menu records can be used by later relation rows.
- Updated `DaoAppInstallGateway.createMenu` to write menu-subitem relation rows.
- Updated `DaoAppInstallGateway.createRole` to create the role, link it to the creator authorized user, and link it to persisted menu items.
- Added `SW_APP_AUTH_MENU_SubItems` and `SW_APP_AUTH_MENU_SW_APP_AUTH_ROLE` to the Docker MySQL init schema.
- Updated README and migration parity docs.

## TDD Evidence

- Red:
  - `docker run --rm -v "$HOME/.m2":/root/.m2 -v "$PWD":/workspace -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest -DfailIfNoTests=false test`
  - Failed compiling because `AuthMenuSubItemRelation`, `AuthRole`, `AuthRoleAuthorizedUserRelation`, `AuthRoleMenuItemRelation`, and `BootstrapMenuItem.getPersistedId()` did not exist.
- Green:
  - same command passed after adding the mapped models and DAO-backed gateway behavior.
  - `Tests run: 5, Failures: 0, Errors: 0, Skipped: 0`

## Remaining App Installer Gaps

- Module installation remains unsupported.
- App-system view preparation remains unsupported.
- Dynamic `App.SysCon` datasource routing still needs a concrete adapter.

## Verification

- Focused test:
  - `docker run --rm -v "$HOME/.m2":/root/.m2 -v "$PWD":/workspace -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest -DfailIfNoTests=false test`
  - `Tests run: 5, Failures: 0, Errors: 0, Skipped: 0`
- Schema apply/check:
  - `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/004-event.sql`
  - `SHOW COLUMNS FROM SW_APP_AUTH_MENU_SubItems` confirmed `SW_APP_AUTH_MENU_SubItemsAUTH_MENU_ID` and `SW_APP_AUTH_MENU_SUBITEMS_ITEM`.
  - `SHOW COLUMNS FROM SW_APP_AUTH_MENU_SW_APP_AUTH_ROLE` confirmed `SW_APP_AUTH_MENU_ID` and `SW_APP_AUTH_ROLE_ID`.
  - `SHOW COLUMNS FROM SW_APP_AUTH_ROLE_SW_APP_AUTH_USER` confirmed `SW_APP_AUTH_ROLE_ID` and `SW_APP_AUTH_USER_ID`.
- Full build:
  - `docker run --rm -v "$HOME/.m2":/root/.m2 -v "$PWD":/workspace -w /workspace maven:3.9-eclipse-temurin-17 mvn -DskipTests package`
  - 15-module reactor `BUILD SUCCESS`.
- Docker rebuild/smoke:
  - `docker compose up -d --build backend`
  - Backend image built with 15-module reactor `BUILD SUCCESS`; `backend` restarted.
  - `docker compose ps --all` showed backend/frontend up and MySQL/Redis healthy.
  - `docker compose logs --tail=80 backend` showed Spring Boot started and `/test` handled in 203 ms.
  - `curl --retry 5 --retry-connrefused --retry-delay 2 -i --max-time 10 http://localhost:8080/test` returned `HTTP/1.1 200` with `[]`.
  - `curl -I --max-time 10 http://localhost:8081/` returned `HTTP/1.1 200 OK`.

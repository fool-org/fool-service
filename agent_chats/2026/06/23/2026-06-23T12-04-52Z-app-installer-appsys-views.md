# App Installer AppSys View Preparation Migration

## Scope

- Compared legacy `../FoolFrame/src/Server/SCPB08-Soway.AppManage/AppManager.cs`.
- Compared legacy `../FoolFrame/src/Server/SCPB05-Soway.Model/View/View.cs`.
- Compared legacy `../FoolFrame/src/Server/SCPB05-Soway.Model/View/ViewFactory.cs`.
- Migrated the app-installer `prepareAppSystemView` part of the legacy create-app flow.

## Legacy Mapping

- Legacy create-app gets each default view with `AutoViewFactory.GetView(viewName)`.
- It sets `view.ConnectionType = ConnectionType.AppSys`.
- It saves the view and uses `view.ID` as the menu item's `ViewID`.
- Legacy view table is `SW_SYS_VIEW`; this slice maps the minimum columns needed by the app installer: `VIEW_ID`, `VIEW_NAME`, and `VIEW_CONTYPE`.

## Changes

- Added `AppSystemView` mapped to `SW_SYS_VIEW`.
- Implemented `DaoAppInstallGateway.prepareAppSystemView`.
- The gateway now looks up a view by `VIEW_NAME`, marks it as AppSys, saves it, and returns `VIEW_ID`; if the view is not present yet, it creates a minimal AppSys view shell and returns the generated ID.
- Added minimal `SW_SYS_VIEW` schema to Docker MySQL init.
- Updated README and migration parity docs.

## TDD Evidence

- Red:
  - `docker run --rm -v "$HOME/.m2":/root/.m2 -v "$PWD":/workspace -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest -DfailIfNoTests=false test`
  - Failed compiling because `AppSystemView` did not exist.
- Green:
  - same command passed after adding the mapped model and DAO-backed gateway behavior.
  - `Tests run: 5, Failures: 0, Errors: 0, Skipped: 0`

## Remaining App Installer Gaps

- Module installation remains unsupported.
- Dynamic `App.SysCon` datasource routing still needs a concrete adapter.

## Verification

- Focused test:
  - `docker run --rm -v "$HOME/.m2":/root/.m2 -v "$PWD":/workspace -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest -DfailIfNoTests=false test`
  - `Tests run: 5, Failures: 0, Errors: 0, Skipped: 0`
- Schema apply/check:
  - `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/004-event.sql`
  - `SHOW COLUMNS FROM SW_SYS_VIEW` confirmed `VIEW_ID`, `VIEW_NAME`, and `VIEW_CONTYPE`.
- Full build:
  - `docker run --rm -v "$HOME/.m2":/root/.m2 -v "$PWD":/workspace -w /workspace maven:3.9-eclipse-temurin-17 mvn -DskipTests package`
  - 15-module reactor `BUILD SUCCESS`.
- Docker rebuild/smoke:
  - `docker compose up -d --build backend`
  - Backend image built with 15-module reactor `BUILD SUCCESS`; `backend` restarted.
  - `docker compose ps --all` showed backend/frontend up and MySQL/Redis healthy.
  - `docker compose logs --tail=80 backend` showed Spring Boot started and `/test` handled in 183 ms.
  - `curl --retry 5 --retry-connrefused --retry-delay 2 -i --max-time 10 http://localhost:8080/test` returned `HTTP/1.1 200` with `[]`.
  - `curl -I --max-time 10 http://localhost:8081/` returned `HTTP/1.1 200 OK`.

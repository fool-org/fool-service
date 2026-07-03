# Default Detail View Metadata

## Prompt

The user pointed out that the migration should render the page from View
metadata first, then query data through that View context, and that binding the
flow to concrete business DTOs is wrong.

## Scope

- Keep the Vue/data workflow View-driven.
- Fix missing persisted legacy metadata behind `getlistview.DetailViewId`.
- Keep runtime proof generic: derive the detail View from `getlistview`, then
  call `querydatadetail`.

## Changes

- `ViewDataService` now reads `SW_SYS_VIEW.VIEW_DEFAULT` for a persisted View
  and attaches a lightweight `defaultDetailView` before `ViewAdapter` builds
  `ListViewInfo.detailViewId`.
- Docker seed data now includes the default detail View metadata and assigns
  `SW_SYS_VIEW(100).VIEW_DEFAULT = 102`.
- `runtime_doctor` now stores the loaded `DetailViewId` from
  `getlistview(ViewId=100)` and uses that value for `querydatadetail`, instead
  of hard-coding the detail id in the check.
- `tasks.md` and `docs/migration/foolframe-parity.md` record the completed
  parity increment.

## Validation

- Passed: `python3 scripts/runtime_doctor_test.py`.
- Passed: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=ViewDataServiceTest test`.
- Local `mvn -pl fool-view -am test -Dtest=ViewDataServiceTest` was not usable
  because the shell Java is 1.8 and the project targets Java 17.

## Runtime Evidence

- Applied the idempotent seed to the running MySQL volume:
  `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/006-view.sql`.
- Confirmed DB metadata:
  `SW_SYS_VIEW(100).VIEW_DEFAULT = 102` and `SW_SYS_VIEW(102).VIEW_TYPE = 1`.
- Rebuilt and restarted backend:
  `docker compose build --quiet backend`
  `docker compose up -d --no-deps --force-recreate backend`.
- Passed: `python3 scripts/runtime_doctor.py`.
- Frontend-proxied `getlistview(ViewId=100)` returned `detailViewId:102`.

## Risks

- The Docker smoke seed still uses `OrderList` as the default sample View, but
  runtime verification is no longer tied to the fixed detail id; it follows the
  loaded View metadata.

## Follow-ups

- Continue replacing remaining smoke-only assumptions with View-metadata-driven
  checks when each path is migrated.

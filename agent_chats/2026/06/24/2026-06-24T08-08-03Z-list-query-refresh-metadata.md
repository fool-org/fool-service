# List Query Refresh Metadata Compatibility

## Prompt

- Continue the `/goal`: run the Docker environment, migrate against `../FoolFrame`,
  keep the frontend on Vue, and make timely atomic commits.
- Retry after permissions were granted.

## Scope

- Compared `../FoolFrame/src/Server/Soway.Server/Querylist/ResultQuery.cs`,
  `Querylist/HandlerQueryData.cs`, `ListView/ViewData.cs`, and
  `ListView/HandlerGetListView.cs`.
- Legacy list-query responses include `FreshTime` and `AutoFreshTime`; legacy
  list-view definitions include `AutoFreshTime` from the view auto-refresh
  interval.
- Migrated the refresh metadata surface into Java view DTOs/adapters, the
  persisted `View` model, Docker MySQL schema, app installer view lookup, and
  Vue API types.

## Changed Files

- `docs/migration/foolframe-parity.md`
- `docker/mysql/init/006-view.sql`
- `fool-app-manage/src/main/java/org/fool/framework/app/DaoAppInstallGateway.java`
- `fool-view/src/main/java/org/fool/framework/view/adapter/ViewAdapter.java`
- `fool-view/src/main/java/org/fool/framework/view/adapter/ViewDataAdapter.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/ListViewInfo.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/ListViewResult.java`
- `fool-view/src/main/java/org/fool/framework/view/model/View.java`
- `fool-view/src/test/java/org/fool/framework/view/adapter/ViewAdapterTest.java`
- `fool-view/src/test/java/org/fool/framework/view/adapter/ViewDataAdapterTest.java`
- `frontend/src/api.ts`
- `agent_chats/2026/06/24/2026-06-24T08-08-03Z-list-query-refresh-metadata.md`

## Validation

- Red test first:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -Dtest=ViewDataAdapterTest,ViewAdapterTest -DfailIfNoTests=false test`
  - Failed as expected with `View should expose legacy autoFreshInterval`.
- Focused green:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -Dtest=ViewDataAdapterTest,ViewAdapterTest -DfailIfNoTests=false test`
- Affected module compile/tests:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view,fool-app-manage -am -Dtest=ViewDataAdapterTest,ViewAdapterTest,LegacyAutoViewFactoryTest -DfailIfNoTests=false test`
- Frontend checks:
  - `cd frontend && npm test`
  - `cd frontend && npm run build`
- Harness/checks:
  - `python scripts/check_repo_harness.py`
  - `git diff --check`
- Docker rebuild:
  - `docker compose up -d --build`

## Runtime Evidence

- Current Compose MySQL volume was migrated:
  - `ALTER TABLE fool_sys_view ADD COLUMN auto_fresh_interval int NOT NULL DEFAULT 0 AFTER filter`
  - `SHOW COLUMNS FROM fool_sys_view LIKE "auto_fresh_interval"` returned the
    `auto_fresh_interval` column.
- Docker status after rebuild:
  - backend and frontend containers are running.
  - MySQL and Redis containers are healthy.
- Backend `/test` smoke:
  - `curl http://localhost:8080/test`
  - Returned the seeded order rows.
- View definition smoke:
  - `curl ... /api/v1/view/get-view`
  - Returned `{"code":0,"viewName":"OrderList","autoFreshTime":0,"operations":0}`.
- List-query smoke:
  - `curl ... /api/v1/data/query-list`
  - Returned `autoFreshTime: 0`, a string `freshTime`, three legacy columns
    `["Order ID","Symbol","State"]`, and two seeded rows.
- Frontend smoke:
  - `curl http://localhost:8081/`
  - Returned the Vue HTML shell.

## Skipped Checks

- Full root `mvn test` without datasource override was not run; the affected
  Maven modules were validated, and the Docker rebuild compiled the full backend
  reactor with tests skipped as configured by the Dockerfile.

## Risks And Follow-Ups

- Existing MySQL volumes do not rerun Docker init scripts automatically; apply
  the `auto_fresh_interval` column to the current Compose database before
  runtime smoke. This was done for the current local volume; future fresh
  volumes pick it up from `docker/mysql/init/006-view.sql`.

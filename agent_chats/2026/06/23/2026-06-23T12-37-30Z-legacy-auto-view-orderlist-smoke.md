# Legacy Auto View Factory And OrderList Smoke

## Scope

- Compared legacy `../FoolFrame/src/Server/SCPB05-Soway.Model/View/ViewFactory.cs`.
- Migrated the default list/detail view generation rules into `fool-view`.
- Added Docker MySQL schema and seed metadata for the Vue `OrderList` workflow.

## Legacy Mapping

- `CreateDefaultListView(model)` creates a list view named `model.Name + "列表"`.
- List views include non-array model properties as read-only items.
- List views create `新建` and `编辑` operations for class-like models.
- If the model has a delete operation, list views add a `删除` command with the legacy confirm and success messages.
- `CreateDefaultItemView(model)` creates a detail view named `model.Name + "详细"` and includes all properties as editable items.

## Changes

- Added `LegacyAutoViewFactory` in `fool-view`.
- Added minimal operation fields to `fool-model` and `fool-view`.
- Marked legacy factory-only view/model fields as `transient` and updated DAO `Mapper` to ignore `static`/`transient` fields.
- Added `docker/mysql/init/006-view.sql` with:
  - `fool_sys_view`
  - `fool_sys_view_item`
  - `Order` model metadata
  - `OrderList` view metadata
  - two `market_order` smoke rows
- Updated README and FoolFrame parity docs.

## TDD Evidence

- Red:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=LegacyAutoViewFactoryTest -DfailIfNoTests=false test`
  - Failed compiling because `LegacyAutoViewFactory`, model operations, view operations, and legacy view fields did not exist.
- Green:
  - same command passed after implementing the factory and minimal fields.
  - `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`

## Verification

- Schema apply:
  - `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/006-view.sql`
  - exited 0.
- Backend rebuild:
  - `docker compose up -d --build backend`
  - 15-module Docker Maven reactor `BUILD SUCCESS`; backend container recreated and started.
- Docker smoke:
  - `curl --retry 5 --retry-connrefused --retry-delay 2 -sS -i --max-time 10 http://localhost:8080/test`
  - returned `HTTP/1.1 200` with seeded order rows.
  - `curl --retry 5 --retry-connrefused --retry-delay 2 -sS -i --max-time 10 -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
  - returned `HTTP/1.1 200` and `{"code":0,"message":"success"}` with three columns: `orderId`, `symbol`, `state`.
  - `curl --retry 5 --retry-connrefused --retry-delay 2 -sS -i --max-time 10 -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list`
  - returned `HTTP/1.1 200` and `{"code":0,"message":"success"}` with two rows.
  - `curl -I --max-time 10 http://localhost:8081/`
  - returned `HTTP/1.1 200 OK`.
- Compose status:
  - `docker compose ps --all`
  - backend, frontend, mysql, and redis are up; mysql and redis are healthy.
- Harness:
  - `python scripts/check_repo_harness.py`
  - `Repository harness validation passed.`
- Diff check:
  - `git diff --check`
  - exited 0 with no output.

## Remaining Gaps

- The app installer still needs to feed real installed module metadata into the default-view factory.
- Full property/relation/table DDL generation from installed modules is still pending.

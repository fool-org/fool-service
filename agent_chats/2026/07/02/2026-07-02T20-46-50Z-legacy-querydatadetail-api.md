# Legacy querydatadetail API

## Prompt

- Continue the active migration goal:
  1. run the environment with Docker;
  2. complete migration against `../FoolFrame`;
  3. use Vue for the frontend;
  4. make timely atomic commits.

## Scope

- Added legacy-compatible `POST /api/v1/data/querydatadetail` for explicit
  object detail lookup by `viewId` and `objId`.
- Added detail result DTOs for `Data`, `SimpleData`, `AutoFreshTime`,
  `CanEdit`, and `Operations`.
- Reused model data lookup and view item formatting for legacy
  `ObjValuePair` detail rows.
- Fixed Docker `Order` smoke metadata so the model ID property points at the
  `orderId` property.
- Fixed dynamic MySQL IDs so numeric primary keys are returned through
  `IDynamicData.getId()` as strings instead of throwing a cast error.
- Added Vue API types for the legacy detail request/result payloads.
- Updated migration parity notes for the new endpoint and runtime smoke route.

## Changed Files

- `docker/mysql/init/006-view.sql`
- `fool-model/src/main/java/org/fool/framework/model/model/DbMysqlDynamic.java`
- `fool-model/src/test/java/org/fool/framework/model/service/MapperDbMapsTest.java`
- `fool-view/src/main/java/org/fool/framework/view/api/DataController.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/LegacyQueryDataDetailRequest.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/QueryDataDetailResult.java`
- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `fool-view/src/main/java/org/fool/framework/view/adapter/ViewDataAdapter.java`
- `fool-view/src/test/java/org/fool/framework/view/api/DataControllerLegacyQueryDataDetailTest.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceDetailTest.java`
- `fool-view/src/test/java/org/fool/framework/view/adapter/ViewDataAdapterTest.java`
- `frontend/src/api.ts`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/02/2026-07-02T20-46-50Z-legacy-querydatadetail-api.md`

## Validation

- RED detail API:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=DataControllerLegacyQueryDataDetailTest,DataQueryServiceDetailTest,ViewDataAdapterTest#detailResultIncludesLegacySimpleDataAndOperations test`
  failed because `LegacyQueryDataDetailRequest` and `QueryDataDetailResult`
  were missing.
- GREEN detail API:
  same command passed with 3 tests.
- RED Long ID regression:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-model -am -Dtest=MapperDbMapsTest#mapsLegacyMultiDbMapBusinessObjectFromCurrentRowColumns test`
  failed with `ClassCastException: class java.lang.Long cannot be cast to class java.lang.String`.
- GREEN Long ID regression:
  same command passed with 1 test.
- Backend:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am test`
  passed with 45 `fool-view` tests.
- Frontend:
  `cd frontend && npm test && npm run build` passed.
- Harness:
  `python scripts/check_repo_harness.py` passed.
- Whitespace:
  `git diff --check` passed.
- Docker:
  `docker compose up -d --build backend frontend` passed.

## Runtime Evidence

- `docker compose ps` showed backend, frontend, MySQL, and Redis running.
- `curl http://localhost:8080/test` returned seeded order rows.
- Current Docker volume was repaired with:
  `UPDATE fool_sys_model SET id_property=1001, auto_sys_id=0 WHERE name='Order';`
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SELECT id,name,id_property,auto_sys_id FROM fool_sys_model WHERE name='Order';"`
  returned `100 Order 1001 0`.
- Initial detail smoke failed with `Unknown column 'SYSID' in 'where clause'`;
  the root cause was missing `Order.id_property` metadata in the seed data.
- After the metadata repair, detail smoke failed with `Long cannot be cast to
  String`; the root cause was `DbMysqlDynamic.getId()` assuming every ID value
  was already a `String`.
- Final smoke:
  `curl -H 'Content-Type: application/json' -d '{"viewId":100,"objId":"1001"}' http://localhost:8080/api/v1/data/querydatadetail`
  returned `code:0`, `objId:"1001"`, and `SimpleData` values for `orderId`,
  `symbol`, and `state`.

## Skipped Checks

- No browser screenshot check; this change adds API compatibility and Vue API
  types, not a visible Vue UI flow.

## Risks / Follow-ups

- `idExp`, blank-`objId` first-row fallback, collection `Items`, and detail
  edit/save flows are not fully migrated in this slice.
- `querydatadetail` currently covers the explicit-object simple-data path and
  operation metadata surface.

# Legacy SaveObj Itemproperties Mapping

## Prompt

- Continue the FoolFrame migration with Docker runtime, Vue frontend, and timely atomic commits.

## Scope

- Migrated the view-layer mapping for legacy `SaveObj.Itemproperties`.
- Reused existing `ModelDataService` dynamic collection writeback instead of adding a second persistence path.

## Changes

- `DataQueryService.saveLegacyObject` now maps legacy `Items`, `AddedItems`, and `DelteItems` into `SubItemList<DbMysqlDynamic>`.
- Existing and deleted items keep `ItemId` as the child model ID.
- Added items keep `ItemId` only when legacy `IsExist` is true, matching the FoolFrame `DataFormator.ObjUpdateToProxy` branch.
- Added a focused service test covering update/add/delete item-property mapping.
- Updated `docs/migration/foolframe-parity.md` to move `saveobj Itemproperties` request mapping into migrated scope while keeping deeper collection/runtime smoke gaps open.

## Validation

- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=DataQueryServiceSaveObjTest test`
  - Failed with `saveLegacyObjectWritesItemPropertiesToDynamicSubItems` before production code mapped collections.
- GREEN: same focused command passed with 2 tests, 0 failures, 0 errors.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am test`
  - Passed: `fool-view` 53 tests, 0 failures, 0 errors.
- `cd frontend && npm test && npm run build`
  - Passed: 3 Vitest tests, Vue typecheck, Vite production build.
- `python scripts/check_repo_harness.py`
  - Passed.
- `git diff --check`
  - Passed.
- `docker compose up -d --build backend frontend`
  - Passed: backend and frontend images built, backend recreated and started.

## Runtime Evidence

- `docker compose ps`
  - `backend`, `frontend`, `mysql`, and `redis` running; MySQL and Redis healthy.
- `curl -fsS http://localhost:8080/test`
  - Returned the seeded order test JSON.
- `curl -fsS -H 'Content-Type: application/json' -d '{"saveObj":{"id":"1001","viewID":"100","propertyies":[{"key":"symbol","value":"BTC-USDT"},{"key":"state","value":"OPEN"}]}}' http://localhost:8080/api/v1/data/saveobj`
  - Returned `{"code":0,"message":"success","data":null}`.
- `curl -fsS -H 'Content-Type: application/json' -d '{"viewId":100,"objId":"1001"}' http://localhost:8080/api/v1/data/querydatadetail`
  - Returned `symbol` as `BTC-USDT` and `state` as `OPEN`.
- `curl -fsS http://localhost:8081/`
  - Returned the Vue app HTML shell.

## Risks

- Docker seed data still lacks a collection-write `saveobj` smoke fixture.
- Broader collection parity still excludes operation-trigger side effects and routed-connection transaction behavior.

## Follow-ups

- Add a seeded collection write fixture for `saveobj Itemproperties`.
- Continue the remaining AppInstallGateway, query/report/event parity work.

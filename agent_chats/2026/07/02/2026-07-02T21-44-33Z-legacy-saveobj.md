# Legacy SaveObj Simple Writeback

## Prompt

- Continue the FoolFrame migration with Docker runtime, Vue frontend, and timely atomic commits.

## Scope

- Migrated the legacy `Soway.Server` `/data/saveobj` simple `SaveObj.Propertyies` writeback path.
- Kept legacy `Itemproperties` collection writeback out of this slice.

## Changes

- Added `SaveObjRequest` DTO matching the legacy request shape.
- Added `POST /api/v1/data/saveobj` to `DataController`.
- Added `DataQueryService.saveLegacyObject` to resolve the view/model and call `ModelDataService.saveData`.
- Added focused controller/service tests for the legacy writeback path.
- Added Vue API types for `saveobj` payloads.
- Updated `docs/migration/foolframe-parity.md` with the migrated route, Vue type coverage, smoke command, and remaining collection gap.

## Validation

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=DataControllerSaveObjTest,DataQueryServiceSaveObjTest test`
  - RED first failed because `SaveObjRequest` did not exist.
  - GREEN passed after the minimal DTO/controller/service implementation.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am test`
  - Passed: reactor through `fool-view`, 52 tests in `fool-view`, 0 failures, 0 errors.
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

- Legacy `Itemproperties` collection update/add/delete behavior remains open.
- Operation-trigger side effects and routed-connection transaction behavior remain open in the broader model mutation parity.

## Follow-ups

- Migrate legacy `DataFormator.ObjUpdateToProxy` collection branch for `Itemproperties`.
- Add runtime smoke data covering collection writes before claiming full `saveobj` parity.

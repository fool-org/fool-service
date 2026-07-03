# BusinessObject Save Runoperation

## Prompt

- Continue the Docker/FoolFrame/Vue migration.
- Keep View rendering first, then load data from View metadata.
- Avoid binding data execution to concrete business DTOs.
- Keep the fix small and reusable.

## Scope

- Reproduced the live `runoperation(7002)` failure against Docker:
  `ModelDataService.saveData` tried to bind a `DbMysqlDynamic` customer object
  directly into `market_order.order_customer_id`.
- Added a focused model-layer regression test for saving ordinary
  `BusinessObject` foreign-key columns.
- Fixed the shared dynamic-save column conversion so non-collection,
  non-DBMap `BusinessObject` properties persist the referenced object's id.
- Left View rendering/data lookup ownership unchanged: View metadata still
  comes from `getlistview` / `querydatadetail`, and dynamic data save stays in
  `fool-model`.

## Changed Files

- `fool-model/src/main/java/org/fool/framework/model/service/ModelDataService.java`
- `fool-model/src/test/java/org/fool/framework/model/service/ModelDataServiceBusinessObjectSaveTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Red test before the fix:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-model -am -Dtest=ModelDataServiceBusinessObjectSaveTest test`
  - Failed as expected with `NotSerializableException:
    org.fool.framework.model.model.DbMysqlDynamic`.
- Green focused test after the fix:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-model -am -Dtest=ModelDataServiceBusinessObjectSaveTest test`
  - Passed: 1 test.
- Related backend regression:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-model,fool-view -am -Dtest=ModelDataServiceBusinessObjectSaveTest,ModelDataServiceTest,DataQueryServiceRunOperationTest,ViewDataServiceTest,ViewAdapterTest,ViewDataAdapterTest test`
  - Passed: 55 tests.
- Harness:
  `python3 scripts/check_repo_harness.py`
  - Passed.
- Whitespace:
  `git diff --check`
  - Passed.

## Runtime Evidence

- Rebuilt and restarted backend:
  `docker compose build --quiet backend`
  `docker compose up -d --no-deps --force-recreate backend`
- Backend smoke:
  `curl http://localhost:8080/test`
  - Returned Docker seed order rows.
- Live operation:
  `curl -H 'Content-Type: application/json' -d '{"ViewId":100,"ObjectId":"1001","OperationId":7002}' http://localhost:8080/api/v1/data/runoperation`
  - Returned `success=true` and `returnMsg=保存成功`.
- DB proof:
  `SELECT order_id, order_symbol, order_state, order_customer_id FROM market_order WHERE order_id=1001`
  - Returned `1001`, `BTC-USDT`, `1`, `3001`.
- Seed-state restore:
  `UPDATE market_order SET order_state=0 WHERE order_id=1001`
  - Restored `1001`, `BTC-USDT`, `0`, `3001` for the default Docker view
    smoke path after proof.
- Compose status:
  `docker compose ps`
  - backend and frontend were up on ports `8080` and `8081`.
  - MySQL and Redis were healthy.

## Skipped Checks

- Frontend tests/build were not rerun because this slice did not touch
  frontend files.
- Full backend `mvn test` was not run; focused `fool-model` / `fool-view`
  regression plus live Docker API proof covered the changed behavior.

## Risks

- The current operation parameter UI is still not implemented; the live save
  operation uses existing request defaults.
- Remaining `runoperation` command types and trigger side effects are still
  tracked in the migration backlog.

# SaveObj Collection Smoke

## Prompt

- Continue the FoolFrame migration with Docker runtime, Vue frontend, and timely atomic commits.

## Scope

- Closed the seeded Docker smoke gap for legacy `saveobj Itemproperties`.
- Kept the change on the existing dynamic save path; no new persistence path.

## Changes

- `DataQueryService.saveLegacyObject` now loads save metadata through `ModelDataService.getModel`, so collection relations are available during writeback.
- `ModelDataService.getModel` now attaches legacy `SW_SYS_RELATION` rows for the model's source properties.
- `SaveObjRequest.Item` accepts legacy JSON `isExist`, fixing the add-item branch for HTTP requests.
- Docker seed now includes `market_order_item`, `OrderItem`, `Order.items`, One2Many relation metadata, and repeatable child smoke rows.
- Migration parity docs now list the collection-write runtime smoke and remove that item from remaining work.

## Validation

- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=DataQueryServiceSaveObjTest,ModelDataServiceTest#getModelRehydratesLegacyRelationsForCollectionProperties test`
  - Failed before relation hydration existed.
- GREEN: same focused command passed.
- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=SaveObjRequestTest test`
  - Failed on unrecognized JSON field `isExist`.
- GREEN: same DTO command passed after the alias fix.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am test`
  - Passed: 55 tests, 0 failures, 0 errors.
- `cd frontend && npm test`
  - Passed: 3 Vitest tests.
- `cd frontend && npm run build`
  - Passed: Vue typecheck and Vite production build.
- `python scripts/check_repo_harness.py`
  - Passed.
- `git diff --check`
  - Passed.
- `docker compose up -d --build backend`
  - Passed: backend image built and container restarted.

## Runtime Evidence

- Applied current seed scripts to the running MySQL volume:
  - `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/001-market-order.sql`
  - `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/006-view.sql`
- Metadata check showed:
  - `Order` model id `100`, `OrderItem` model id `101`.
  - `Order.items` property id `1004`, `property_model=101`, `is_collection=1`.
  - `SW_SYS_RELATION_SOURCEPROPERTY=1004`, type `0`, table `market_order_item`, target column `order_id`.
- `curl -fsS http://localhost:8080/test`
  - Returned seeded order JSON.
- `curl -fsS -H 'Content-Type: application/json' -d '{"saveObj":{"id":"1001","viewID":"100","propertyies":[{"key":"symbol","value":"BTC-USDT"},{"key":"state","value":"OPEN"}],"itemproperties":[{"key":"items","items":[{"itemId":"2001","isExist":true,"propertyies":[{"key":"itemName","value":"Updated item"}]}],"addedItems":[{"itemId":"2003","isExist":true,"propertyies":[{"key":"itemName","value":"New item"}]}],"delteItems":[{"itemId":"2004","isExist":true,"propertyies":[]}]}]}}}' http://localhost:8080/api/v1/data/saveobj`
  - Returned `{"code":0,"message":"success","data":null}`.
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e 'SELECT item_id,order_id,item_name FROM market_order_item WHERE order_id = 1001 ORDER BY item_id;'`
  - Returned `2001 / 1001 / Updated item` and `2003 / 1001 / New item`; `2004` was gone.

## Risks

- Collection state parity is still limited to current dynamic writeback behavior.
- Operation-trigger side effects and routed-connection transaction behavior remain open migration work.

## Follow-ups

- Continue AppInstallGateway, query/report/event, and deeper model runtime parity.

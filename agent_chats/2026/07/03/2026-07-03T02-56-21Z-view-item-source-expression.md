# ViewItem Source Expression

## Prompt

- Continue the FoolFrame migration with Docker runtime, Vue frontend, and timely atomic commits.

## Scope

- Migrated runtime `ViewItem.sourceExpression` metadata for `VIEW_ITEM_SOURCEEXP` parity into the modern `fool_sys_view_item` surface.
- Made `inputquery` prefer the view-item source expression before falling back to `Property.source` when resolving an existing-object source list.
- Kept the added-item owner-context branch open because the current Java `Model` runtime has no migrated `Owner` surface.

## Changes

- `ViewItem` now maps `sourceExpression` to `fool_sys_view_item.source_expression`.
- Docker MySQL init DDL now creates and backfills `source_expression`.
- The running Docker MySQL volume was patched with the same column.
- `DataQueryService.inputQuery` now resolves the source list name from `ViewItem.sourceExpression` first and `Property.source` second.
- Migration parity docs now mark view-item source-expression runtime lookup as migrated and narrow the remaining `inputquery` gap to added-item owner-context.

## Validation

- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=ModelDaoMappingTest#viewItemMapsLegacySourceExpressionMetadata test`
  - Failed before implementation with `java.lang.AssertionError` because `ViewItem.sourceExpression` was not mapped.
- GREEN: same focused mapper command passed after adding the field.
- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=DataQueryServiceInputQueryTest#inputQueryUsesViewItemSourceExpressionBeforePropertySource test`
  - Failed before implementation with `expected:<2> but was:<0>` because `Property.source` was used instead.
- GREEN: same focused input-query command passed after adding source-expression preference.
- Runtime DB patch: `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "... SHOW COLUMNS FROM fool_sys_view_item LIKE 'source_expression';"`
  - Passed and showed `source_expression text`.
- Module regression: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am test`
  - Passed with `BUILD SUCCESS`; reactor through `fool-view` ran 58 tests.
- Repo harness: `python scripts/check_repo_harness.py`
  - Passed with `Repository harness validation passed.`
- Whitespace: `git diff --check`
  - Passed with no output.
- Docker runtime: `docker compose up -d --build backend`
  - Rebuilt the backend image and started `fool-service-backend-1`.
- HTTP smoke: `curl --retry 20 --retry-delay 2 --retry-all-errors -fsS http://localhost:8080/test`
  - Passed and returned seeded order rows.
- HTTP smoke: `curl -fsS -H 'Content-Type: application/json' -d '{"viewName":"OrderList","viewItemId":"symbol","text":"BTC"}' http://localhost:8080/api/v1/data/inputquery`
  - Passed with `{"code":0,"message":"success","data":{"items":[]}}`.
- Runtime status: `docker compose ps`
  - Backend, frontend, MySQL, and Redis were up; MySQL and Redis were healthy.

## Skipped Checks

- Frontend tests were not rerun because this change did not touch `frontend/`.

## Risks

- Added-item owner-context handling remains open.
- The `source_expression` column is runtime metadata only; legacy `SW_SYS_VIEW_ITEM.VIEW_ITEM_SOURCEEXP` install-table mapping already existed separately.

## Follow-ups

- Migrate `Model.Owner`/owner-context semantics before implementing the `IsAdded + OwnerId` `inputquery` source branch.

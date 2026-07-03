# Operation View Params

## Prompt

- Continue the Docker/FoolFrame/Vue migration.
- Keep View rendering metadata first, then use that metadata to drive data and
  operation behavior.
- Keep code small and commit atomically.

## Scope

- Compared FoolFrame `OperationView` / `OperationViewItem`:
  `ViewOperation.Operation` points to an `OperationView`, and `OperationView`
  owns `Params: List<OperationViewItem>`.
- Hydrated migrated operation-view parameter rows from
  `SW_SYS_OPERATIONVIEW_ITEM` through `ViewDataService`.
- Corrected the runtime join to follow FoolFrame's legacy chain:
  `SW_SYS_VIEW_OPERATION -> SW_SYS_OPERATIONVIEW -> SW_SYS_OPERATION`, so the
  View operation stores the OperationView ID and the API still returns the
  underlying model operation ID.
- Joined the underlying `SW_SYS_OPERATION_PARAM` row so API callers receive
  item id/name/index plus param id/name/view/filter/value.
- Exposed operation params in `OperationInfo.params` for both list-view
  (`getlistview`) and detail-view (`querydatadetail`) responses.
- Added a Docker seed operation parameter for the save operation.

## Changed Files

- `docker/mysql/init/006-view.sql`
- `fool-view/src/main/java/org/fool/framework/view/model/OperationViewParam.java`
- `fool-view/src/main/java/org/fool/framework/view/model/ViewOperation.java`
- `fool-view/src/main/java/org/fool/framework/view/model/PersistedViewOperation.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/OperationParamInfo.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/OperationInfo.java`
- `fool-view/src/main/java/org/fool/framework/view/service/ViewDataService.java`
- `fool-view/src/main/java/org/fool/framework/view/adapter/ViewAdapter.java`
- `fool-view/src/main/java/org/fool/framework/view/adapter/ViewDataAdapter.java`
- `fool-view/src/test/java/org/fool/framework/view/service/ViewDataServiceTest.java`
- `fool-view/src/test/java/org/fool/framework/view/adapter/ViewAdapterTest.java`
- `fool-view/src/test/java/org/fool/framework/view/adapter/ViewDataAdapterTest.java`
- `frontend/src/api.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Focused backend:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=ViewDataServiceTest,ViewAdapterTest,ViewDataAdapterTest test`
  - Passed: 36 tests.
- Frontend:
  `cd frontend && npm test && npm run build`
  - Passed: 3 test files, 43 tests.
  - Passed: `vue-tsc --noEmit` and Vite production build.
- `git diff --check`
  - Passed.

## Runtime Evidence

- Applied seed SQL:
  `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/006-view.sql`
- Rebuilt backend:
  `docker compose build --quiet backend && docker compose up -d --no-deps --force-recreate backend`
- `docker compose ps`
  - backend: running on `0.0.0.0:8080->8080`
  - frontend: running on `0.0.0.0:8081->80`
  - mysql: healthy on `127.0.0.1:3307->3306`
  - redis: healthy on `127.0.0.1:6380->6379`
- DB proof:
  - `SW_SYS_VIEW_OPERATION.SysId=7002`
  - `SW_VIEW_OPERATION_MODELOPERATION=8002`
  - `SW_SYS_OPERATIONVIEW.SW_SYS_OPVIEW_OPREATION=7002`
  - `SW_SYS_OPERATIONVIEW_ITEM.SysId=8101`
  - owner operation view: `8002`
  - item name: `审批意见`
  - joined param name: `remark`
  - param view: `100`
- `POST /api/v1/view/getlistview` with `{"viewId":100}`
  - Save operation `7002` returned `params[0].id=8101`,
    `paramId=7201`, `paramName=remark`, `viewId=100`,
    `filter=state=0`.
- `POST /api/v1/data/querydatadetail` with `{"viewId":100,"objId":"1001"}`
  - Detail operation DTO returned the same `params[0]` metadata.

## Skipped Checks

- Full backend `mvn test` was not run; this slice touched `fool-view` View
  metadata hydration and DTO adapters, so focused backend tests plus live
  Docker API proof were used.
- Frontend container was not rebuilt because the frontend change was a
  TypeScript interface-only API shape update; `npm run build` proved the Vue
  app still type-checks.
- Live `runoperation(7002)` was not used as success evidence. A quick probe
  still fails in the existing dynamic-object update serialization path; this
  slice only proves operation metadata hydration and DTO exposure.

## Risks

- This slice only exposes operation parameter metadata. It does not implement
  a UI for prompting operation parameters before `runoperation`; that should be
  driven from `operations[].params[]` in a separate workflow slice.

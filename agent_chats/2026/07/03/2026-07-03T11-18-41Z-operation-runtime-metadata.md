# Operation Runtime Metadata Hydration

## Prompt

- Continue the Docker/Vue FoolFrame migration and keep commits atomic.

## Scope

- Added runtime `Operation` fields for legacy `SW_SYS_OPERATION` metadata:
  filter, argument model/filter, invoke DLL/class/method, and return model.
- Hydrated those columns through view-operation loading.
- Kept execution unchanged; assembly invocation and external-model operation
  dispatch remain future work.

## Changed Files

- `fool-model/src/main/java/org/fool/framework/model/model/Operation.java`
- `fool-view/src/main/java/org/fool/framework/view/model/PersistedViewOperation.java`
- `fool-view/src/main/java/org/fool/framework/view/service/ViewDataService.java`
- `fool-view/src/test/java/org/fool/framework/view/service/ViewDataServiceTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/03/2026-07-03T11-18-41Z-operation-runtime-metadata.md`

## Validation

- Red check:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=ViewDataServiceTest test`
  - Failed as expected at
    `ViewDataServiceTest.getViewDataHydratesLegacyOperationInvokeColumns`
    because the persisted/runtime operation metadata fields were missing.
- Green focused check:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=ViewDataServiceTest test`
  - Tests run: 4, failures: 0, errors: 0.
- Green module check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false test`
  - Tests run: 92, failures: 0, errors: 0.
- Harness:
  `python scripts/check_repo_harness.py`
  - Passed: `Repository harness validation passed.`
- Docker runtime:
  `docker compose up -d --build backend`
  - Backend image built and backend container restarted.

## Runtime Evidence

- Temporarily updated operation `7002` in Docker MySQL:
  - `SW_MODEL_OPERATION_FILTER = "order_state='0'"`
  - `SW_MODEL_OPERATION_ARGMODEL = 100`
  - `SW_MODEL_OPERATION_ARGFILTER = "arg_id=.orderId"`
  - `SW_MODEL_OPERATION_INVOKEDLL = "Legacy.dll"`
  - `SW_MODEL_OPERATION_INVOKECLASS = "Legacy.Worker"`
  - `SW_MODEL_OPERATION_INVOKEMETHOD = "Run"`
  - `SW_MODEL_OPERATION_RETURNMODEL = 100`
- Verified the temporary DB metadata:
  `order_state='0'  100  arg_id=.orderId  Legacy.dll  Legacy.Worker  Run  100`
- Verified view-operation loading still works through backend:
  `POST http://localhost:8080/api/v1/view/getlistview` with `{"viewId":100}`
  returned operation `7002`.
- Verified the same path through the Vue proxy:
  `POST http://localhost:8081/api/v1/view/getlistview` with `{"viewId":100}`
  returned operation `7002`.
- Cleanup verification:
  operation `7002` metadata columns were reset to `NULL`; cleanup count was
  `1`.

## Skipped Checks

- Frontend unit/build checks were not rerun because this slice does not change
  Vue code; the Vue proxy runtime route was smoke tested.
- No assembly invocation behavior was implemented in this slice.

## Remaining Risk

- `BaseOperationType.Assebmly`, external-model execution, and
  `SetParamValue`/`SetConStrValue` command use still need executable parity.
- Operation-level `SW_MODEL_OPERATION_FILTER` is hydrated but not yet enforced
  as a precondition.

## Follow-Ups

- Continue with the smallest executable `runoperation` gap once a seedable
  legacy behavior can be proved end to end.

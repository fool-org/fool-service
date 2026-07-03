# QueryDataDetail Response Alias Compatibility

## Prompt

- Continue the Docker/Vue FoolFrame migration.
- Keep the migrated detail workflow View-first and avoid binding to concrete
  business DTOs.
- Keep changes scoped and commit atomically.

## Scope

- Audited FoolFrame detail result DTOs:
  `ResultDataDetail`, `DataDetail`, `PropertyDataItems`, `DataItem`, and
  `ObjValuePair`.
- Added legacy read-only Pascal JSON aliases to the migrated detail response
  DTOs while preserving the Vue camel-case fields.
- Added focused serialization coverage in the existing `ViewDataAdapterTest`.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/dto/ListDataValue.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/QueryDataDetailResult.java`
- `fool-view/src/test/java/org/fool/framework/view/adapter/ViewDataAdapterTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/04/2026-07-03T21-37-02Z-querydatadetail-response-alias.md`

## Validation

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -DfailIfNoTests=false -Dtest=ViewDataAdapterTest,DataControllerLegacyQueryDataDetailTest test`
  - Passed.
- `python3 scripts/check_repo_harness.py`
  - Passed: `Repository harness validation passed.`
- `git diff --check`
  - Passed.
- `docker compose up -d --build backend`
  - Passed: Maven reactor built successfully and `fool-service-backend-1`
    was recreated.

## Runtime Evidence

- `python3 scripts/runtime_doctor.py`
  - Passed backend `/test`, `view/getlistview`, `data/querydata`,
    `data/inputquery`, and `report/getmkqview`.
- `docker compose ps`
  - `backend` up on `8080`, `frontend` up on `8081`, MySQL and Redis healthy.
- `POST http://localhost:8080/api/v1/data/querydatadetail`
  with `{"viewId":100,"objId":"1001"}`
  - Passed alias assertions for top-level `Data` / `data`, `Operations` /
    `operations`, detail `ObjId` / `objId`, `SimpleData` / `simpleData`,
    collection `Items` / `items`, and value `PrpId` / `prpId`, `FmtValue` /
    `fmtValue`.
  - Runtime sample: `legacy detail aliases ok 1001 orderId`.

## Skipped Checks And Risks

- Full root `mvn test` was not rerun for this slice because the change is
  limited to read-only JSON alias accessors and focused `fool-view` coverage.

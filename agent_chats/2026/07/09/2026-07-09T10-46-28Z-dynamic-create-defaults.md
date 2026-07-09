# 2026-07-09 Dynamic Create Defaults

## Prompt

- Continue the Docker/FoolFrame/Vue migration goal.
- Keep the migration View-first and avoid binding saves to concrete business
  DTOs.
- Control file size and reuse shared paths instead of adding Order-specific
  fixes.

## Scope

- Fixed dynamic create/save column generation to skip scalar properties that
  are absent from the legacy save DTO.
- Kept explicit submitted null values writable; only missing DTO fields are
  omitted.
- Added focused coverage for a missing BusinessObject foreign key using the
  table default during create.
- Rebuilt the backend Docker service and proved legacy `savenewobj` through
  the Vue proxy with a missing `customer` BusinessObject field.

## Changed Files

- `fool-model/src/main/java/org/fool/framework/model/service/ModelDataService.java`
- `fool-model/src/test/java/org/fool/framework/model/service/ModelDataServiceBusinessObjectSaveTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T10-46-28Z-dynamic-create-defaults.md`

## Red Test

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=ModelDataServiceBusinessObjectSaveTest test`
  failed first because `createData` wrote a missing BusinessObject foreign-key
  column as null.

## Runtime Proof

- `docker compose up -d --build backend` rebuilt and restarted the backend.
- `POST http://localhost:8081/api/v1/data/savenewobj` with `ViewID=102`,
  `Id=989901`, `symbol=RUNTIME-DOCTOR`, `state=0`, and no `customer` field
  returned `code=0`.
- `POST http://localhost:8081/api/v1/data/querydatadetail` for
  `ViewId=102`, `ObjId=989901` returned `code=0` and the saved symbol.
- Direct SQL confirmed `market_order.order_customer_id = 0` for `989901`
  before the proof row was cleaned up.

## Validation

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=ModelDataServiceBusinessObjectSaveTest test` passed.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am test` passed with 82 tests.
- `python scripts/runtime_doctor.py` passed.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.

## Skipped Checks

- Frontend unit/build checks were not run because this slice changed only the
  backend dynamic write path and migration evidence.

## Risks

- Maven still warns about the existing duplicate `spring-jdbc` dependency in
  `fool-dao/pom.xml`.

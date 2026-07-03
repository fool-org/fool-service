# Prompt

Continue the FoolFrame migration while keeping file size and code reuse under
control. The user also called out that work should stay on the migration goal
instead of drifting into business-specific sample data.

# Scope

Migrate the narrow model-trigger write side effect needed next: legacy model
SAVE triggers with `SET_VALUE` commands should update dynamic data through
`ModelDataService`.

# Changes

- `ModelDataService` now runs hydrated model triggers after CREATE/SAVE and
  before DELETE, with recursive trigger execution disabled for trigger-persisted
  writes.
- Trigger command execution currently supports `CommandsType.SET_VALUE`, sorted
  by legacy command index, then persists through `OperationBaseType`.
- Added `OperationCommandValueResolver` in `fool-model` so model triggers and
  `DataQueryService` runoperation share the same `$`, `.`, `#.`, `@`, math, and
  BusinessObject value parsing.
- Added a MySQL-backed `ModelDataServiceTest` case proving a legacy SAVE trigger
  changes the stored dynamic row.
- Updated migration parity and task state.

# Validation

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-model -am -DfailIfNoTests=false -Dtest=ModelDataServiceTest test`
  - Passed.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -DfailIfNoTests=false -Dtest=DataQueryServiceRunOperationTest test`
  - Passed.
- `python3 scripts/check_repo_harness.py`
  - Passed.
- `docker compose up -d --build backend`
  - Backend image rebuilt and backend container restarted successfully.
- `python3 scripts/runtime_doctor.py`
  - Passed compose, backend `/test`, `getlistview(ViewId=100)`,
    `querydata(ViewId=100)`, `inputquery(ViewId=100)`, and
    `getmkqview(ViewId=100)`.

# Runtime Evidence

The focused `ModelDataServiceTest` uses the Docker MySQL network and creates
legacy `SW_SYS_MODEL_TRIGGER` / `SW_SYS_MODEL_TRIGGER_COMMANDS` rows, then
verifies the persisted dynamic table value changes from the trigger command.
After rebuild, `docker compose ps` showed backend, frontend, MySQL, and Redis
running, with MySQL and Redis healthy.

# Risks

Only `SET_VALUE` trigger commands are executed in this slice. Full model trigger
filter semantics, assembly invocation, and broader command types remain open
until a migrated seed or workflow needs them.

# Follow-ups

Continue comparing FoolFrame write-time behavior for property triggers and
non-SET_VALUE model trigger commands before claiming complete write parity.

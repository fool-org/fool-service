# Runoperation JSON WCF Noop

## Prompt

Continue the Docker/Vue FoolFrame migration, maximizing reuse and avoiding
unnecessary new infrastructure.

## Scope

- Compared FoolFrame `HandlerRunOperation` and `ModelMethodContext` behavior
  for operation base types not handled by the execution switch.
- Reused the existing `DataQueryService.runLegacyOperation` success path so
  WCF / JSONPOST / JSONGET operations return success messages without
  create/save/delete side effects.
- Did not add HTTP, JSON, or WCF client code; FoolFrame's current behavior for
  these operation types is a successful no-op unless a real handler surface is
  implemented separately.
- Updated migration source-of-truth text that still claimed invoke paths were
  not executed.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceRunOperationTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-08T20-08-13Z-runoperation-json-wcf-noop.md`

## Validation

- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -Dtest=DataQueryServiceRunOperationTest#runLegacyOperationTreatsJsonWcfOperationTypesAsSuccessfulNoops test`
  - Failed because JSONPOST returned the default unsuccessful result.
- GREEN: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -Dtest=DataQueryServiceRunOperationTest#runLegacyOperationTreatsJsonWcfOperationTypesAsSuccessfulNoops test`
  - Focused legacy no-op success regression passed.
- GREEN: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am test`
  - `fool-view` and required upstream module tests passed.
- GREEN: `python scripts/check_repo_harness.py`
  - Repository harness validation passed.
- GREEN: `git diff --check`
  - No whitespace errors.
- GREEN: `docker compose up -d --build backend`
  - Backend image rebuilt and backend container recreated.
- GREEN: `python scripts/runtime_doctor.py`
  - Compose, auth shell, View/data, inputquery, report, message, notify, and
    logout probes passed.
- GREEN: `docker compose ps`
  - Backend and frontend were running; MySQL and Redis were healthy.
- GREEN: `curl http://localhost:8080/test`
  - Returned `200 465`.
- GREEN: `curl http://localhost:8081/`
  - Returned `200 399`.

## Risks

- WCF / JSONPOST / JSONGET still do not perform external calls. This matches
  the observed FoolFrame operation switch; add client execution only when a
  migrated handler requires it.

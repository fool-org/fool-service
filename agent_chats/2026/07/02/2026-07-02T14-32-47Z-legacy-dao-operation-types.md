# Legacy DAO Operation Types

## Prompt

- Continue migration against `../FoolFrame`, keep Docker running, keep Vue
  frontend, and make timely atomic commits.

## Scope

- Added `OperationType` with legacy `Create`/`Save`/`Delete`/`DynamicUpdate`
  ordinals.
- Added package-scope `SqlOperation` with legacy queued SQL operation names,
  including the original `excute` spelling.
- Added package-scope `SqlTransAutoMic` command carrier matching the old
  transaction queue item shape.

## Changed Files

- `fool-dao/src/main/java/org/fool/framework/dao/OperationType.java`
- `fool-dao/src/main/java/org/fool/framework/dao/SqlOperation.java`
- `fool-dao/src/main/java/org/fool/framework/dao/SqlTransAutoMic.java`
- `fool-dao/src/test/java/org/fool/framework/dao/LegacyOperationTypeTest.java`
- `docs/migration/foolframe-parity.md`

## Validation

- Red check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-dao -am -Dtest=org.fool.framework.dao.LegacyOperationTypeTest test`
  failed because `OperationType`, `SqlOperation`, and `SqlTransAutoMic` did not
  exist.
- Green check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-dao -am -Dtest=org.fool.framework.dao.LegacyOperationTypeTest test`
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-dao -am test`
- `python scripts/check_repo_harness.py`
- `git diff --check`
- `curl -sS -m 5 http://localhost:8080/test`
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list`
- `curl -sS -m 5 http://localhost:8081/ | head -5`
- `docker compose ps`

## Skipped Checks

- Full root `mvn test`; this slice only adds DAO type-surface parity.

## Risks And Follow-Ups

- These types preserve the legacy queue surface only. Full `DBContext.Attatch`
  transaction execution parity remains outside this slice.

# Legacy View Operation Location

## Prompt

- Continue the Docker/Vue FoolFrame migration with timely atomic commits.

## Scope

- Exposed legacy `ViewOperation.Location` through backend `OperationInfo`.
- Synced the Vue API type with the new `location` field.
- Kept the existing integer location values, matching legacy
  `SW_VIEW_OPERATION_INDEX`.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/dto/OperationInfo.java`
- `fool-view/src/main/java/org/fool/framework/view/adapter/ViewAdapter.java`
- `fool-view/src/test/java/org/fool/framework/view/adapter/ViewAdapterTest.java`
- `frontend/src/api.ts`
- `docs/migration/foolframe-parity.md`

## Validation

- Red check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=ViewAdapterTest#viewOperationsAreMappedToLegacyOperationInfo test`
  failed because `OperationInfo.getLocation()` did not exist.
- Green check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=ViewAdapterTest#viewOperationsAreMappedToLegacyOperationInfo test`
  ran `ViewAdapterTest`: 1 test, 0 failures, 0 errors.
- Module check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am test`
  passed: 22 `fool-view` tests plus dependency module tests.
- Frontend check:
  `cd frontend && npm test && npm run build`
- Harness check:
  `python scripts/check_repo_harness.py`
- Whitespace check:
  `git diff --check`
- Runtime smoke:
  `curl -sS -m 5 http://localhost:8080/test`
  `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
  `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list`
  `curl -sS -m 5 http://localhost:8081/`
  `docker compose ps`

## Risks And Follow-Ups

- This slice only exposes the existing operation location metadata. It does
  not add a new Java enum for legacy `VewOperationLocation`; add that only if
  callers need symbolic values instead of the stored integer.

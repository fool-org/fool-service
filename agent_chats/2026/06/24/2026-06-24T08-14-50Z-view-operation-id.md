# View Operation ID Compatibility

## Prompt

- Continue the `/goal`: run the Docker environment, migrate against `../FoolFrame`,
  keep the frontend on Vue, and make timely atomic commits.

## Scope

- Compared `../FoolFrame/src/Server/Soway.Server/ListView/HandlerGetListView.cs`,
  `Detail/HandlerQueryDataDetail.cs`, `ListView/ViewOperation.cs`, and
  `../FoolFrame/src/Server/SCPB05-Soway.Model/Operation/Operation.cs`.
- Legacy view operation metadata returns an operation `ID`, defaulting to `0`
  when no backing operation exists.
- Added the matching Java `Operation.id` carrier field and mapped it to
  `OperationInfo.id`; synced Vue API types.

## Changed Files

- `docs/migration/foolframe-parity.md`
- `fool-model/src/main/java/org/fool/framework/model/model/Operation.java`
- `fool-view/src/main/java/org/fool/framework/view/adapter/ViewAdapter.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/OperationInfo.java`
- `fool-view/src/test/java/org/fool/framework/view/adapter/ViewAdapterTest.java`
- `frontend/src/api.ts`
- `agent_chats/2026/06/24/2026-06-24T08-14-50Z-view-operation-id.md`

## Validation

- Red test first:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -Dtest=ViewAdapterTest#viewOperationsAreMappedToLegacyOperationInfo -DfailIfNoTests=false test`
  - Failed as expected with `Operation should expose legacy id`.
- Focused green:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -Dtest=ViewAdapterTest#viewOperationsAreMappedToLegacyOperationInfo -DfailIfNoTests=false test`
- Refactor check:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -Dtest=ViewAdapterTest,LegacyAutoViewFactoryTest -DfailIfNoTests=false test`
- Frontend checks:
  - `cd frontend && npm test`
  - `cd frontend && npm run build`
- Harness/checks:
  - `python scripts/check_repo_harness.py`
  - `git diff --check`
- Docker rebuild:
  - `docker compose up -d --build`

## Runtime Evidence

- Docker status after rebuild:
  - backend and frontend containers are running.
  - MySQL and Redis containers are healthy.
- View definition smoke:
  - `curl ... /api/v1/view/get-view`
  - Returned `{"code":0,"viewName":"OrderList","autoFreshTime":0,"operations":0}`.
- List-query smoke:
  - `curl ... /api/v1/data/query-list`
  - Returned `{"code":0,"cols":["Order ID","Symbol","State"],"rows":2}`.
- Frontend smoke:
  - `curl http://localhost:8081/`
  - Returned the Vue HTML shell.

## Skipped Checks

- Full root `mvn test` without datasource override was not run; the affected
  adapter tests passed, frontend checks passed, and the Docker rebuild compiled
  the full backend reactor with tests skipped as configured by the Dockerfile.
- Runtime seed data has no view operations, so operation ID behavior is covered
  by `ViewAdapterTest`.

## Risks And Follow-Ups

- This migrates the operation ID metadata surface only. Legacy operation
  execution (`RunOperation`) still needs a separate service/runtime slice.

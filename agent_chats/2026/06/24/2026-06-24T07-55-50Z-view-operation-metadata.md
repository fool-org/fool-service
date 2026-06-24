# View Operation Metadata Compatibility Surface

## Prompt

- Continue the `/goal`: run the Docker environment, migrate against `../FoolFrame`,
  keep the frontend on Vue, and make timely atomic commits.

## Scope

- Compared `../FoolFrame/src/Server/Soway.Server/ListView/HandlerGetListView.cs`.
- Legacy view definitions include `Operations` with operation name, require-select
  flag, and result view id.
- Added operation metadata to Java `ListViewInfo` via existing `OperationInfo`
  and mapped `ViewOperation` from `ViewAdapter`.
- Added `viewId` and `requireSelect` fields to `OperationInfo` while preserving
  existing `text`, `type`, and `viewName` fields.

## Changed Files

- `docs/migration/foolframe-parity.md`
- `fool-view/src/main/java/org/fool/framework/view/adapter/ViewAdapter.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/ListViewInfo.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/OperationInfo.java`
- `fool-view/src/test/java/org/fool/framework/view/adapter/ViewAdapterTest.java`
- `agent_chats/2026/06/24/2026-06-24T07-55-50Z-view-operation-metadata.md`

## Validation

- Red test first:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -Dtest=ViewAdapterTest#viewOperationsAreMappedToLegacyOperationInfo -DfailIfNoTests=false test`
  - Failed compiling because `ListViewInfo.getOperations()` did not exist.
- Focused green:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -Dtest=ViewAdapterTest -DfailIfNoTests=false test`
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -Dtest=ViewDataAdapterTest,ViewAdapterTest,LegacyAutoViewFactoryTest -DfailIfNoTests=false test`
- Compose-network view regression:
  - `docker run --rm --network fool-service_default -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -Dtest='org.fool.framework.view.adapter.*Test,org.fool.framework.view.service.*Test' -DfailIfNoTests=false -Dspring.datasource.url='jdbc:mysql://mysql:3306/car_wash?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC' -Dspring.datasource.username=root -Dspring.datasource.password=Pa88word test`
- Rebuilt backend Docker image:
  - `docker compose up -d --build backend`
- Runtime smoke:
  - `curl -fsS --retry 10 --retry-delay 3 --retry-connrefused -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
  - Response included `"operations":[]` for the current seed view, proving the
    runtime DTO shape is present. Operation item contents are covered by the
    adapter unit test because the current Docker seed has no persisted operation
    table.

## Skipped Checks

- Full backend `mvn test` was not run; this slice is isolated to `fool-view`
  DTO/adapter behavior and the focused view tests passed.

## Risks And Follow-Ups

- Persisted view operation loading is still absent from the Docker seed schema;
  this change exposes the DTO/adapter surface for in-memory/default views and
  future persisted operation rows.

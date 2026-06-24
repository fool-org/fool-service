# View Row Format Compatibility Surface

## Prompt

- Continue the `/goal`: run the Docker environment, migrate against `../FoolFrame`,
  keep the frontend on Vue, and make timely atomic commits.

## Scope

- Compared `../FoolFrame/src/Server/Soway.Server/Querylist/HandlerQueryData.cs`.
- Legacy list query excludes `ItemEditType.Format` view items from returned
  columns and uses the first format item property value as `RowFmt`.
- Compared `../FoolFrame/src/Server/SCPB05-Soway.Model/ItemEditType.cs` for
  enum ordinal parity.
- Added Java `ItemEditType` with legacy ordinals, view-item `editType`
  metadata, list DTO `rowFmt`, adapter filtering, and Docker MySQL `edit_type`
  schema/seed support.

## Changed Files

- `docker/mysql/init/006-view.sql`
- `docs/migration/foolframe-parity.md`
- `fool-view/src/main/java/org/fool/framework/view/model/ItemEditType.java`
- `fool-view/src/main/java/org/fool/framework/view/model/ViewItem.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/ListDataItem.java`
- `fool-view/src/main/java/org/fool/framework/view/adapter/ViewAdapter.java`
- `fool-view/src/main/java/org/fool/framework/view/adapter/ViewDataAdapter.java`
- `fool-view/src/test/java/org/fool/framework/view/adapter/ViewAdapterTest.java`
- `fool-view/src/test/java/org/fool/framework/view/adapter/ViewDataAdapterTest.java`
- `agent_chats/2026/06/24/2026-06-24T07-42-31Z-view-row-format.md`

## Validation

- Red test first:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -Dtest=ViewDataAdapterTest -DfailIfNoTests=false test`
  - Failed compiling because `ItemEditType` did not exist yet.
- Focused green:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -Dtest=ViewDataAdapterTest,ViewAdapterTest -DfailIfNoTests=false test`
- View package regression on Compose network:
  - `docker run --rm --network fool-service_default -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -Dtest='org.fool.framework.view.adapter.*Test,org.fool.framework.view.api.*Test,org.fool.framework.view.service.*Test' -DfailIfNoTests=false -Dspring.datasource.url='jdbc:mysql://mysql:3306/car_wash?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC' -Dspring.datasource.username=root -Dspring.datasource.password=Pa88word test`
- Current Docker volume schema patched and checked:
  - `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e 'SHOW COLUMNS FROM fool_sys_view_item LIKE "edit_type";'`
- Rebuilt and restarted the Docker stack:
  - `docker compose up -d --build`
- Runtime smoke:
  - `curl -fsS --retry 10 --retry-delay 3 --retry-connrefused http://localhost:8081/`
  - `curl -fsS --retry 10 --retry-delay 3 --retry-connrefused http://localhost:8080/test`
  - `curl -fsS --retry 10 --retry-delay 3 --retry-connrefused -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
  - `curl -fsS --retry 10 --retry-delay 3 --retry-connrefused -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list`
- Temporary runtime row-format proof:
  - Inserted a temporary `fool_sys_view_item` row with `edit_type = 10` for
    `state`, called `/api/v1/data/query-list`, observed `rowFmt` values
    `OPEN` and `FILLED`, then deleted the temporary row and rechecked the seed
    rows.

## Skipped Checks

- Full backend `mvn test` was not run; the focused `fool-view` regression ran
  against Compose MySQL, and the Docker image build still uses `-DskipTests`.

## Risks And Follow-Ups

- Existing MySQL volumes do not rerun Docker init scripts automatically; this
  local volume was patched manually for `edit_type`. A future general migration
  runner would reduce manual schema drift for long-lived local volumes.

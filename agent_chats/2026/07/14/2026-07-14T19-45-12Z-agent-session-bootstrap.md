# Agent Chat Evidence: ordered agent session bootstrap

## Prompt

按这个顺序接入agent 以及相应的会话机制。

The referenced order is:

1. report/query agent
2. form/view agent
3. model agent
4. data-source agent
5. event/automation agent

## Scope

- Added the first backend `fool-agent` module.
- Registered `fool-agent` in the Maven reactor and `business-application`.
- Added an ordered agent capability catalog and session mechanism.
- Added REST endpoints for capability discovery, session creation, message
  recording, session lookup, and ordered advancement.
- Exposed stable JSON capability ids such as `report-query` while still
  accepting enum names such as `REPORT_QUERY`.
- Added a JDBC-backed session store plus Docker/MySQL schema, with in-memory
  storage retained only as a non-database fallback.
- Ordered auto-configuration after Spring's `JdbcTemplate` auto-configuration
  so the Docker runtime selects the JDBC-backed store when MySQL is available.
- Added controlled draft output for each turn. The `report-query` stage now
  returns a low-risk read-only draft with report/query endpoints,
  View-scoped payload, default `ReportCols`, hydrated View/Model candidate
  columns, and validation steps.
- Wired the `form-view` stage to the same View/Model metadata source and added
  a medium-risk draft-only payload with View endpoints, fields, child
  collections, operation buttons, and save/run-operation approval gates.
- Wired the `model` stage to concrete model metadata hydration with
  properties, relations, operations, and a read-only DDL diff plan.
- Wired the `data-source` stage to concrete connection catalog,
  credential-reference, and routing validation drafts over `WorkDataBase`,
  `DB_App`, `DB_AppDB`, and `DS_DataSourceSet`.
- Wired the `event-automation` stage to concrete event definition, recipient,
  idempotency, and audit dry-run drafts over `SW_EVT_DEF`, `SW_EVT_EVENT`,
  `SW_SYS_MSG`, recipient relation tables, model metadata, and View metadata.
- Fixed the Docker runtime metadata read by avoiding a cross-column collation
  comparison between `fool_sys_view.view_model` and `fool_sys_model.name`;
  the JDBC provider now reads View first and then resolves Model by parameter.
- Fixed event metadata runtime reads across mixed MySQL collations by applying
  explicit collation to event definition joins and subquery comparisons.
- Documented the current API and follow-up risks.

## Changes

- `pom.xml`
- `business-application/pom.xml`
- `fool-agent/pom.xml`
- `fool-agent/src/main/java/org/fool/framework/agent/**`
- `fool-agent/src/main/resources/META-INF/spring.factories`
- `fool-agent/src/test/java/org/fool/framework/agent/service/*Test.java`
- `docker/mysql/init/011-agent.sql`
- `docs/agent-sessions.md`
- `README.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T19-45-12Z-agent-session-bootstrap.md`

## Validation

- Local host Maven failed before code compilation because the active Java is
  OpenJDK 8 and the repository targets Java 17:
  `mvn -q -pl fool-agent -am test`
- Green validation with the repository's documented Java 17 Docker path:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-agent -am test`
- Repository harness:
  `python scripts/check_repo_harness.py`
- Application assembly compile/package with tests skipped after focused module
  tests:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl business-application -am -DskipTests package`
- Backend Docker runtime rebuild:
  `DOCKER_CONFIG=/tmp/fool-service-docker-config docker-compose up -d --build backend`
- Runtime health:
  `curl --retry 10 --retry-delay 2 --retry-connrefused -fsS http://localhost:8080/test`
- Compose state:
  `DOCKER_CONFIG=/tmp/fool-service-docker-config docker-compose ps -a`
  showed `backend` up, `mysql` healthy, `redis` healthy, `frontend` up, and
  `db-migrate` `Exited (0)`.
- Capability catalog smoke:
  `curl -fsS http://localhost:8080/api/v1/agent/capabilities`
  returned 5 ordered ids: `report-query`, `form-view`, `model`,
  `data-source`, `event-automation`.
- Focused report/query metadata hydration unit coverage is included in:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-agent -am test`
- Rebuilt the backend after the hydration fix:
  `DOCKER_CONFIG=/tmp/fool-service-docker-config docker-compose up -d --build backend`
- Runtime health after rebuild:
  `curl --retry 10 --retry-delay 2 --retry-connrefused -fsS http://localhost:8080/test`
- Compose state after rebuild:
  `DOCKER_CONFIG=/tmp/fool-service-docker-config docker-compose ps -a`
  showed `backend` up, `mysql` healthy, `redis` healthy, `frontend` up, and
  `db-migrate` `Exited (0)`.
- Repository harness after hydration:
  `python scripts/check_repo_harness.py`
- Diff whitespace check after hydration:
  `git diff --check`
- Focused form/view metadata hydration unit coverage:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-agent -am test`
- Application assembly compile/package after form/view hydration:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl business-application -am -DskipTests package`
- Backend Docker runtime rebuild after form/view hydration:
  `DOCKER_CONFIG=/tmp/fool-service-docker-config docker-compose up -d --build backend`
- Runtime health after form/view rebuild:
  `curl --retry 10 --retry-delay 2 --retry-connrefused -fsS http://localhost:8080/test`
- Compose state after form/view rebuild:
  `DOCKER_CONFIG=/tmp/fool-service-docker-config docker-compose ps -a`
  showed `backend` up, `mysql` healthy, `redis` healthy, `frontend` up, and
  `db-migrate` `Exited (0)`.
- Repository harness after form/view hydration:
  `python scripts/check_repo_harness.py`
- Diff whitespace check after form/view hydration:
  `git diff --check`
- Focused model metadata hydration unit coverage:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-agent -am test`
- Application assembly compile/package after model hydration:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl business-application -am -DskipTests package`
- Backend Docker runtime rebuild after model hydration:
  `DOCKER_CONFIG=/tmp/fool-service-docker-config docker-compose up -d --build backend`
- Runtime health after model rebuild:
  `curl --retry 10 --retry-delay 2 --retry-connrefused -fsS http://localhost:8080/test`
- Compose state after model rebuild:
  `DOCKER_CONFIG=/tmp/fool-service-docker-config docker-compose ps -a`
  showed `backend` up, `mysql` healthy, `redis` healthy, `frontend` up, and
  `db-migrate` `Exited (0)`.
- Repository harness after model hydration:
  `python scripts/check_repo_harness.py`
- Diff whitespace check after model hydration:
  `git diff --check`
- Focused data-source and event/automation metadata hydration unit coverage:
  `docker run --rm -v "$PWD":/workspace -w /workspace maven:3.9.8-eclipse-temurin-17 mvn -q -pl fool-agent -am test`
- Application assembly compile/package after data-source and event hydration:
  `docker run --rm -v "$PWD":/workspace -w /workspace maven:3.9.8-eclipse-temurin-17 mvn -q clean package -Dmaven.test.skip=true`
- Backend Docker image rebuild after event collation fix:
  `DOCKER_API_VERSION=1.45 DOCKER_CONFIG=/tmp/fool-service-docker-config docker-compose build backend`
- Normal backend startup with initialization enabled is currently blocked by
  unrelated initialization idempotency drift:
  `Duplicate column name 'FOOL_SYS_VIEW_LISTITEMSVIEW_NAME'`.
- Agent runtime smoke used a one-off backend container with initialization
  disabled:
  `DOCKER_API_VERSION=1.45 DOCKER_CONFIG=/tmp/fool-service-docker-config docker-compose run -d --service-ports --name fool-service-backend-agent-smoke -e FOOL_APP_INITIALIZATION_ENABLED=false backend`
- Runtime health in the one-off backend:
  `curl -fsS http://localhost:8080/test`
- Final repository harness after documentation/task/evidence updates:
  `python scripts/check_repo_harness.py`
- Final diff whitespace check:
  `git diff --check`
- Final compose state:
  `DOCKER_API_VERSION=1.45 DOCKER_CONFIG=/tmp/fool-service-docker-config docker-compose ps -a`
  showed `fool-service-backend-agent-smoke` up on port 8080, MySQL healthy,
  Redis healthy, `db-migrate` `Exited (0)`, normal `backend` exited with the
  unrelated initialization issue, and `frontend` exited.

## Runtime Evidence

- Docker build required `DOCKER_CONFIG=/tmp/fool-service-docker-config`
  because the sandbox prevented Docker from writing buildx activity under the
  default home directory.
- Report/query session smoke used token `agent-smoke-token-jdbc-1784059306`.
  The message response returned:
  - `code`: `0`
  - `sessionId`: `723fda51-d65d-4956-a2fe-7f166a269ab7`
  - `currentCapability`: `report-query`
  - `draftRisk`: `low-read-only`
  - `reportModelEndpoint`: `/api/v1/report/getmkqview`
  - `draftViewId`: `100`
  - `messageCount`: `3`
- MySQL persistence check:
  `SELECT COUNT(*) AS sessions FROM FOOL_AGENT_SESSION WHERE SESSION_TOKEN = 'agent-smoke-token-jdbc-1784059306';`
  returned `1`.
- MySQL persistence check:
  `SELECT COUNT(*) AS messages FROM FOOL_AGENT_MESSAGE WHERE SESSION_ID IN (SELECT SESSION_ID FROM FOOL_AGENT_SESSION WHERE SESSION_TOKEN = 'agent-smoke-token-jdbc-1784059306');`
  returned `3`.
- Hydrated report/query session smoke used token
  `agent-hydration-ok-1784060060`. The message response returned:
  - `code`: `0`
  - `sessionId`: `389ba48f-0a1a-4762-a200-393189bd94b3`
  - `currentCapability`: `report-query`
  - `summary`: `已基于 View/Model 元数据生成报表/查询草案：候选列和默认 ReportCols 已绑定当前 View。`
  - `metadataStatus`: `hydrated`
  - `viewName`: `OrderList`
  - `modelName`: `Order`
  - `candidateCount`: `7`
  - `reportColsCount`: `6`
  - first default report col: `ColName=Order ID`, `ColId=orderId`,
    `Index=1`, `OrderType=2`
  - collection field `items` was present in `candidateColumns` with
    `Reportable=false` and `ListViewId=101`, so it was excluded from default
    `ReportCols`.
- Hydrated smoke MySQL persistence check for token
  `agent-hydration-ok-1784060060` returned `sessions=1` and `messages=3`.
- Ordered report/query -> form/view runtime smoke used token
  `agent-formview-ok-1784060829`. The form/view message response returned:
  - `code`: `0`
  - `sessionId`: `f71c715e-92f0-4285-b2a9-e24d153cf5d8`
  - `currentCapability`: `form-view`
  - `messageCount`: `6`
  - `summary`: `已基于 View 元数据生成表单/视图草案：字段、子项和操作按钮已绑定当前 View。`
  - `draftRisk`: `medium-draft-only`
  - `metadataStatus`: `hydrated`
  - `viewName`: `OrderList`
  - `detailViewId`: `102`
  - `fieldsCount`: `7`
  - `childCollectionCount`: `1`
  - `operationCount`: `2`
  - first field: `Order ID`, `Readonly=true`
  - first child collection `ListViewId`: `101`
  - first operation: `Name=删除`, `OperationId=7001`, `Type=COMMAND`,
    `ConfirmMessage=确定要删除？该操作不可撤消`
- Ordered form/view smoke MySQL persistence check for token
  `agent-formview-ok-1784060829` returned `sessions=1` and `messages=6`.
- Direct `/api/v1/view/getlistview` comparison for `ViewId=100` returned
  `name=OrderList`, `items=6`, `operations=2`, `detailViewId=102`,
  `firstItem=Order ID`, and `firstOperation=删除:op=7001`. The agent draft
  intentionally exposes the collection field separately in `childCollections`,
  so `fieldsCount=7` equals the 6 list fields plus the `items` child
  collection.
- Ordered report/query -> form/view -> model runtime smoke used token
  `agent-model-ok-1784061462`. The model message response returned:
  - `code`: `0`
  - `sessionId`: `f8f95f66-004a-4773-be2f-7af6aab3cf96`
  - `currentCapability`: `model`
  - `messageCount`: `9`
  - `summary`: `已基于模型元数据生成模型草案：字段、关系、默认值和操作定义已绑定当前 Model。`
  - `draftRisk`: `high-dry-run-required`
  - `metadataStatus`: `hydrated`
  - `modelName`: `Order`
  - `tableName`: `market_order`
  - `propertyCount`: `7`
  - `relationCount`: `1`
  - `operationCount`: `2`
  - `ddlMode`: `read-only-ddl-diff`
  - `ddlColumnChecks`: `6`
  - first property: `orderId`, `DbColumn=order_id`
  - first relation: `items->itemId`, `TableName=market_order_item`
  - first operation: `删除`, `OperationId=7001`, `CommandCount=0`
- Ordered model smoke MySQL persistence check for token
  `agent-model-ok-1784061462` returned `sessions=1` and `messages=9`.
- Direct SQL comparison for `ModelId=100` returned
  `model=Order:table=market_order`, `properties=7`, `relations=1`, and
  `operations=2`.
- Ordered report/query -> form/view -> model -> data-source runtime smoke used
  token `agent-datasource-ok-1784062200`. The data-source message response
  returned:
  - `code`: `0`
  - `sessionId`: `df0ac8a2-e969-4301-8208-e07d66def5f4`
  - `currentCapability`: `data-source`
  - `messageCount`: `12`
  - `draftRisk`: `high-credential-boundary`
  - `metadataStatus`: `hydrated`
  - `workingDatabases`: `1`
  - `applicationRoutes`: `1`
  - `dataSourceRoutes`: `1`
  - `selectedType`: `dataSourceKey`
  - `selectedDataSourceKey`: `car_wash`
  - `selectedDbNo`: `01`
  - `checkMode`: `read-only-connectivity-check`
  - `checkQuery`: `SELECT 1`
  - `credentialReference`: `WorkDataBase.DBNo=01/pwd*`
  - `plaintextSecretsExposed`: `false`
  - `rawConnectionStringExposed`: `false`
- Direct SQL comparison for data-source metadata returned
  `WorkDataBase DBNo=01 DBName=car_wash DBSysName=car_wash IsActive=1`,
  `UserName=root CompanyName=Docker ServerIp=mysql:3306 IsLocal=1`,
  `credential_configured=1`, and `DS_DataSourceSet car_wash -> 01`.
- Ordered report/query -> form/view -> model -> data-source ->
  event/automation runtime smoke used token `agent-event-ok-1784063717`. The
  event message response returned:
  - `code`: `0`
  - `sessionId`: `e103ffb2-f0db-482f-9b4a-7dce0b32c89f`
  - `currentCapability`: `event-automation`
  - `messageCount`: `15`
  - `draftRisk`: `high-dry-run-required`
  - `metadataStatus`: `hydrated`
  - `definitionCount`: `1`
  - `selectedDefinitionId`: `00000000-0000-0000-0000-000000000100`
  - `stateName`: `IsRunning`
  - `modelName`: `Order`
  - `tableName`: `market_order`
  - `objectIdColumn`: `order_id`
  - `queryPreview`: ``SELECT * FROM market_order WHERE `order_state` = 0``
  - `directUsers`: `1`
  - `fallbackWhenEmpty`: `All authorized users`
  - `existingEventCount`: `1`
  - `dryRunMode`: `read-only-matched-object-dry-run`
  - `schedulerMutation`: `false`
  - `idempotencyCheck`: `SW_EVT_EVENT.EVT_Defination + SW_EVT_EVENT.EVT_DEF`
  - `auditEventTable`: `SW_EVT_EVENT`
  - `auditMessageTable`: `SW_SYS_MSG`
- Direct SQL comparison for event metadata returned
  `EVTDEF_ID=00000000-0000-0000-0000-000000000100`,
  `EVTDEF_FILTER=[order_state] = 0`, `EVTDEF_VIEW=100`,
  `EVTDEF_OPERATION=read-order`, `EVTDEF_MSGFMT=Order state matched`,
  `EVTDEF_TIMEOUTSECS=60`, `EVTDEF_MODEL=100`, `EVTDEF_MODELREF=0`,
  `EVTDEF_STATE=0`, `notify_users=1`, and `existing_events=1`.
- Agent persistence check for token `agent-event-ok-1784063717` returned
  `sessions=1` and `messages=15`.
- No frontend entry point was added in this slice.

## Risks

- The agent reply is deterministic guidance; it does not yet call an external
  model provider.
- The report/query stage hydrates metadata but does not yet run an automatic
  preview comparison against `/api/v1/report/getmkqview` in the agent turn.
- The form/view stage hydrates fields, child collections, and operation button
  metadata, but it does not yet write View metadata, save business rows, or run
  operations.
- The model stage hydrates metadata and creates a DDL dry-run plan, but it does
  not yet generate table diff SQL or write model metadata.
- The data-source stage hydrates connection catalog and route metadata, but it
  does not expose plaintext credentials or change stored routes/credentials.
- The event/automation stage hydrates event definitions and dry-run/audit
  metadata, but it does not create `SW_EVT_EVENT`, send `SW_SYS_MSG`, or enable
  scheduler mutations.
- Normal `docker-compose up backend` is currently blocked by unrelated
  initialization idempotency drift unless
  `FOOL_APP_INITIALIZATION_ENABLED=false` is used for agent smoke validation.

## Follow-Ups

- Add a runtime read-only preview gate that compares hydrated `ReportCols`
  against `/api/v1/report/getmkqview` before allowing saved report
  definitions.
- Add a runtime read-only preview gate that compares hydrated form/view drafts
  against `/api/v1/view/getlistview` before allowing View metadata writes or
  operation execution.
- Add a runtime DDL dry-run preview gate that compares hydrated model drafts
  against target table schema before allowing model metadata writes.
- Add data-source route mutation gates, credential owner approval, rollback,
  and audit evidence before enabling route or credential writes.
- Add event matched-object preview, recipient preview, approval, rollback, and
  audit evidence before creating events, sending messages, or enabling
  scheduler mutations.
- Add frontend workspace entry points once the backend session contract is
  stable enough for browser smoke automation.

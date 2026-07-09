# RunOperation Token Context

## Prompt

Continue the Docker/Vue FoolFrame migration, keep the goal focused, avoid
business DTO binding, and control file size/reuse.

## Scope

- Rechecked FoolFrame `ModelMethodContext.GetValue` and
  `Expressions.GetValueExpression`.
- Confirmed legacy `#` owner expressions require an owner object carrier that
  Java `IDynamicData` / `DbMysqlDynamic` does not currently expose; no new
  owner protocol was added in this slice.
- Wired `runoperation` operation-command evaluation to pass the request
  `Token` into the existing `LegacyContextValueService` path.
- Reused the existing shared `OperationCommandValueResolver`; no new parser or
  concrete business DTO shortcut was added.
- Covered normal `SET_VALUE`, assembly parameter/constructor values, and
  nested external-model operation commands through the same token-aware path.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceRunOperationTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-08T22-51-36Z-runoperation-token-context.md`

## Validation

- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -Dtest=DataQueryServiceRunOperationTest#runLegacyUpdateOperationResolvesContextValueFromRequestToken test`
  failed with `expected:<[admin]> but was:<[]>`.
- GREEN: same command passed after carrying `request.getToken()` through the
  operation-command evaluation path.
- GREEN: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am test`
- GREEN: `python scripts/check_repo_harness.py`
- GREEN: `git diff --check`
- GREEN: `docker compose build backend`
- GREEN: `docker compose up -d backend frontend`
- GREEN: `python scripts/runtime_doctor.py`

Local host Maven was not used for the Java checks because the shell `JAVA_HOME`
currently points to Java 8 and fails Spring Boot 2.7 / Java 17 compilation with
`invalid target release: 17`; Docker Maven/JDK17 is the repo validation path.

## Risks

- `@userid` / `@username` are backed by the existing auth context service.
  FoolFrame connection expressions such as `@appcon` and `@datacon` still need
  real app/session connection state.
- Full `#` owner-expression parity remains open because migrated dynamic data
  does not yet carry an owner object like FoolFrame `IObjectProxy.Owner`.

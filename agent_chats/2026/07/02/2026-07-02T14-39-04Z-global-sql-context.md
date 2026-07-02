# Legacy Global SQL Context

## Prompt

- Continue the Docker/Vue FoolFrame migration and make timely atomic commits.

## Scope

- Migrated the legacy `SCPB02-Soway.DB/Global.cs` connection context surface.
- Added default connection string get/set behavior.
- Added type-scoped connection registration and lookup.
- Preserved the legacy behavior where `KeyName` is accepted but the registered
  connection is keyed by type only.

## Changed Files

- `fool-dao/src/main/java/org/fool/framework/dao/GlobalSqlContext.java`
- `fool-dao/src/test/java/org/fool/framework/dao/GlobalSqlContextTest.java`
- `docs/migration/foolframe-parity.md`

## Validation

- Red check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-dao -am -Dtest=org.fool.framework.dao.GlobalSqlContextTest test`
  failed because `GlobalSqlContext` did not exist.
- Green check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-dao -am -Dtest=org.fool.framework.dao.GlobalSqlContextTest test`
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-dao -am test`
- `python scripts/check_repo_harness.py`
- `git diff --check`
- `curl -sS -m 5 http://localhost:8080/test`
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list`
- `curl -sS -m 5 http://localhost:8081/ | head -5`
- `docker compose ps`

## Risks And Follow-Ups

- This slice preserves the static connection registry only. Full legacy
  `DBContext`/multi-DB execution routing remains outside this slice.

# View Item PropertyName Fallback

## Prompt

- Continue the active migration goal:
  1. run the environment through Docker
  2. finish migration against `../FoolFrame`
  3. keep the frontend on Vue
  4. commit atomically

## Scope

- Compared `../FoolFrame/src/Server/Soway.Server/ListView/HandlerGetListView.cs`.
- Migrated the legacy `PropertyName` behavior for missing item properties:
  legacy returns `""` when `item.Property == null`.
- Kept the modern `property` DTO field as `modelProperty`.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/adapter/ViewAdapter.java`
- `fool-view/src/test/java/org/fool/framework/view/adapter/ViewAdapterTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/02/2026-07-02T19-41-05Z-view-item-property-name-fallback.md`

## Validation

- RED:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=ViewAdapterTest#viewInfoUsesEmptyLegacyColumnPropertyNameWhenPropertyIsMissing test`
  failed as expected with `expected:<[]> but was:<[orderId]>`.
- GREEN:
  same focused Maven command passed.
- Adapter:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=ViewAdapterTest test`
  passed 15 tests.
- Module:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am test`
  passed 34 tests.
- Docker:
  `docker compose up -d --build backend` rebuilt and started the backend.
- Harness:
  `python scripts/check_repo_harness.py` passed.
- Whitespace:
  `git diff --check` passed.

## Runtime Evidence

- `docker compose ps` showed backend, frontend, mysql, and redis running.
- `curl -fsS http://localhost:8080/test` returned two seeded order rows.
- `curl -fsS -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view` returned success with `propertyName` values for seeded columns.

## Risks

- No frontend files changed in this slice.

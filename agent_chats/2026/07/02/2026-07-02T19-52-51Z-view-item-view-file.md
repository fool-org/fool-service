# View Item ViewFile

## Prompt

- Continue the active migration goal:
  1. run the environment through Docker
  2. finish migration against `../FoolFrame`
  3. keep the frontend on Vue
  4. commit atomically

## Scope

- Compared `../FoolFrame/src/Server/Soway.Server/ListView/HandlerGetListView.cs`.
- Migrated the legacy list-view `ViewItem.ViewFile` response surface.
- Kept the new carrier field transient because this slice only exposes the DTO
  behavior and does not add new database schema.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/model/ViewItem.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/TableColumnInfo.java`
- `fool-view/src/main/java/org/fool/framework/view/adapter/ViewAdapter.java`
- `fool-view/src/test/java/org/fool/framework/view/adapter/ViewAdapterTest.java`
- `frontend/src/api.ts`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/02/2026-07-02T19-52-51Z-view-item-view-file.md`

## Validation

- RED:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=ViewAdapterTest#viewInfoIncludesLegacyColumnViewFile test`
  failed as expected with `ViewItem should expose legacy item-file metadata`.
- GREEN:
  same focused Maven command passed.
- Adapter:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=ViewAdapterTest test`
  passed 16 tests.
- Module:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am test`
  passed 35 tests.
- Frontend:
  `cd frontend && npm test && npm run build` passed.
- Docker:
  `docker compose up -d --build backend` rebuilt and restarted the backend.
- Harness:
  `python scripts/check_repo_harness.py` passed.
- Whitespace:
  `git diff --check` passed.

## Runtime Evidence

- `docker compose ps` showed backend, frontend, mysql, and redis running.
- `curl -fsS http://localhost:8080/test` returned two seeded order rows.
- `curl -fsS -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view` returned success and included `viewFile` in seeded table columns.

## Risks

- Seeded Docker data has no item template file, so runtime smoke proves the
  field is present as `null`; the focused adapter test proves non-null mapping.

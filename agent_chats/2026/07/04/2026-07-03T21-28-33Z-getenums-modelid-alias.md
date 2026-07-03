# GetEnums ModelId Alias Compatibility

## Prompt

- Continue the Docker/Vue FoolFrame migration.
- Keep request handling at the generic View/data protocol boundary instead of
  binding to concrete business DTOs.
- Keep the slice small and commit atomically.

## Scope

- Audited `../FoolFrame/src/Web/Cloud-Social/soway.js` and
  `../FoolFrame/src/Web/routes/index.js` for `getenums` request shape.
- Added legacy aliases to `GetEnumRequest.modelId`: `ModelId`, `ModelID`, and
  `modelid`.
- Added focused DTO deserialization coverage in the existing enum controller
  test.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/dto/GetEnumRequest.java`
- `fool-view/src/test/java/org/fool/framework/view/api/DataControllerEnumTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/04/2026-07-03T21-28-33Z-getenums-modelid-alias.md`

## Validation

- Passed:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -DfailIfNoTests=false -Dtest=DataControllerEnumTest test`
- Passed:
  `python3 scripts/check_repo_harness.py`
- Passed:
  `git diff --check`
- Passed:
  `docker compose up -d --build backend`
- Passed:
  `python3 scripts/runtime_doctor.py`

## Runtime Evidence

- `POST /api/v1/data/getenums` with `{"Token":"token-1","ModelId":"102"}`
  returned `Open` and `Filled`.
- `POST /api/v1/data/getenums` with `{"Token":"token-1","modelid":"102"}`
  returned `Open` and `Filled`.
- `docker compose ps` showed backend, frontend, MySQL, and Redis running after
  the backend rebuild.

## Skipped Checks And Risks

- Full root Maven tests were not run; the change is isolated to request
  deserialization and enum controller behavior, covered by the focused
  `fool-view` test plus Docker read smoke.

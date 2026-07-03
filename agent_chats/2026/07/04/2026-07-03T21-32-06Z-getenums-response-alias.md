# GetEnums Response Alias Compatibility

## Prompt

- Continue the Docker/Vue FoolFrame migration.
- Keep View/data protocol parity moving without binding enum handling to a
  concrete business DTO.
- Keep changes small and commit atomically.

## Scope

- Audited `../FoolFrame/src/Server/Soway.Server/Enum/GetEnumResult.cs` and
  `../FoolFrame/src/Server/Soway.Server/EnumValues.cs`.
- Added legacy read-only JSON aliases to `GetEnumResult`:
  `EnumValues`, `Name`, and `Value`.
- Kept the existing Vue-facing camel-case fields:
  `enumValues`, `name`, and `value`.
- Added focused serialization coverage to the existing enum controller test.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/dto/GetEnumResult.java`
- `fool-view/src/test/java/org/fool/framework/view/api/DataControllerEnumTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/04/2026-07-03T21-32-06Z-getenums-response-alias.md`

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
  returned both `data.enumValues` and `data.EnumValues`.
- Each returned enum row included both camel-case `name` / `value` and legacy
  `Name` / `Value` fields.
- `docker compose ps` showed backend, frontend, MySQL, and Redis running after
  the backend rebuild.

## Skipped Checks And Risks

- Full root Maven tests were not run; the change is isolated to enum response
  serialization and covered by focused `fool-view` tests plus Docker read
  smoke.

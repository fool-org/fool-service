# Inputquery response aliases

## Prompt

Continue the Docker/Vue FoolFrame migration, keeping View-driven rendering and
protocol compatibility ahead of concrete business DTO binding.

## Scope

- Added FoolFrame response aliases to `InputQueryResult`: `Items`, `Id`, and
  `Text`.
- Added shared Vue helpers for input-query candidate lists and item labels.
- Updated the API-tool input-query table and metadata lookup editor to consume
  candidate rows through those helpers.
- Tightened the Docker runtime doctor so the loaded-View `inputquery` path must
  expose both camel `items` and Pascal `Items`.
- Updated `tasks.md` and `docs/migration/foolframe-parity.md`.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/dto/InputQueryResult.java`
- `fool-view/src/test/java/org/fool/framework/view/api/DataControllerInputQueryTest.java`
- `frontend/src/App.vue`
- `frontend/src/MetadataFieldEditor.vue`
- `frontend/src/api.ts`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `scripts/runtime_doctor.py`
- `tasks.md`
- `docs/migration/foolframe-parity.md`

## Validation

- `cd frontend && npm test`: passed, 71 tests.
- `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=DataControllerInputQueryTest -DfailIfNoTests=false test`: passed, 3 focused tests.
- `cd frontend && npm run build`: passed.
- `python scripts/check_repo_harness.py`: passed.
- `git diff --check`: clean.
- `docker compose up -d --build frontend`: frontend and backend images built
  successfully; compose recreated backend.
- `docker compose up -d --force-recreate --no-deps frontend`: frontend
  container recreated from the rebuilt image.
- `python scripts/runtime_doctor.py`: passed all compose, auth, View/data,
  detail, `inputquery`, report, message, notify, and logout checks.

## Runtime Evidence

- Backend container running on `http://localhost:8080`.
- Frontend container recreated and running on `http://localhost:8081`.

## Risks

- Local host `mvn -pl fool-view -am ...` still depends on a Java 17 runtime;
  this slice used the repo's Docker Java 17 validation path.

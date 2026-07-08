# Report model aliases

## Prompt

Continue the Docker/Vue FoolFrame migration. Keep the frontend View-first:
render the page from View/report metadata first, then bind data, without
coupling Vue panels to concrete business DTO fields.

## Scope

- Added FoolFrame Pascal aliases to `ReportModelResult` for `getmkqview`
  report model metadata.
- Added shared Vue helpers for report model columns, option labels, state
  labels, and generic read-item field type/edit display.
- Updated the Vue report-column tool table and default `ReportCols` generation
  to consume report model metadata through those helpers.
- Tightened the Docker runtime doctor so the loaded-View `getmkqview` path must
  expose the legacy `Cols` alias before report smoke can pass.
- Updated `tasks.md` and `docs/migration/foolframe-parity.md`.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/dto/ReportModelResult.java`
- `fool-view/src/test/java/org/fool/framework/view/api/ReportControllerTest.java`
- `frontend/src/App.vue`
- `frontend/src/api.ts`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `scripts/runtime_doctor.py`
- `tasks.md`
- `docs/migration/foolframe-parity.md`

## Validation

- `cd frontend && npm test`: passed, 71 tests.
- `cd frontend && npm run build`: passed.
- `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=ReportControllerTest -DfailIfNoTests=false test`: passed, 17 focused tests.
- `python scripts/check_repo_harness.py`: passed.
- `git diff --check`: clean.
- `docker compose up -d --build frontend`: backend and frontend images built,
  backend container recreated and started.
- `docker compose up -d --force-recreate --no-deps frontend`: frontend
  container recreated from the rebuilt image.
- `python scripts/runtime_doctor.py`: passed all compose, auth, View/data,
  detail, `inputquery`, report, message, notify, and logout checks.
- `docker compose ps`: backend, frontend, MySQL, and Redis running; MySQL and
  Redis healthy.

## Runtime Evidence

- Backend container running on `http://localhost:8080`.
- Frontend container recreated and running on `http://localhost:8081`.
- `runtime_doctor.py` passed `report:getmkqview` and `report:getrpt` using the
  loaded View id and report model columns.

## Risks

- Local host Maven still depends on Java 17; this slice used the repo's Docker
  Java 17 validation path for backend tests.

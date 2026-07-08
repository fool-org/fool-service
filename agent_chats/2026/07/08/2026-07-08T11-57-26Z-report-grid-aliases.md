# Report grid aliases

## Prompt

Continue the FoolFrame migration with View-first rendering: render from View
and report metadata first, then bind data, without coupling Vue page code to
concrete business DTO fields.

## Scope

- Added FoolFrame Pascal aliases to report grid DTOs: `ViewId`,
  `CurrentPage`, `PageSize`, `TotalRecords`, `TotalPages`, `Cells`, and cell
  fields `Col`, `Row`, `ColSpan`, `RowSpan`, `FmtValue`.
- Added Vue report-grid helpers that read `cells` / `Cells` and camel/Pascal
  cell fields before matrix rendering.
- Updated the Vue report grid panel to consume cells through the shared helper
  instead of direct `reportResponse.data.cells` access.
- Tightened the Docker runtime doctor so the loaded-View `getrpt` smoke must
  expose `Cells`.
- Updated `tasks.md` and `docs/migration/foolframe-parity.md`.

## Changed Files

- `fool-report/src/main/java/org/fool/framework/report/ReportCell.java`
- `fool-report/src/main/java/org/fool/framework/report/ReportGridResult.java`
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
- `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=ReportControllerTest -DfailIfNoTests=false test`: passed, 18 focused tests.
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
- `runtime_doctor.py` passed `report:getrpt` while requiring `Cells` on the
  report grid payload.

## Risks

- Local host Maven still depends on Java 17; this slice used the repo's Docker
  Java 17 validation path for backend tests.

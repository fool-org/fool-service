# Legacy Web getenum Alias

## Prompt

Continue the FoolFrame migration with Docker, Vue, atomic commits, maximum
reuse, and View-first data flow.

## Scope

- Compared `../FoolFrame/src/Web/app.js`,
  `../FoolFrame/src/Web/routes/index.js`, and
  `../FoolFrame/src/Web/Cloud-Social/soway.js`.
- Reused the existing `getenums` DTO and enum lookup service.
- Added `/api/v1/data/getenum` as a migrated alias for the old Web
  `/model/getenum` wrapper.
- Kept the existing lowercase `modelid` DTO alias.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/api/DataController.java`
- `fool-view/src/test/java/org/fool/framework/view/api/DataControllerEnumTest.java`
- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Focused Docker Maven test:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=DataControllerEnumTest test`
- `python scripts/runtime_doctor_test.py`
- `python scripts/check_repo_harness.py`
- `git diff --check`
- `docker compose up -d --build backend`
- `python scripts/runtime_doctor.py`

## Skipped Checks

- Full root `mvn test` not rerun for this alias-only route slice.

## Risks

- The raw Express `/model/getenum` path was not mounted. Compatibility stays
  under the migrated `/api/v1/data/getenum` boundary.

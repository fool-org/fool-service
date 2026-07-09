# Legacy Web Save/New Aliases

## Prompt

Continue the FoolFrame migration with Docker, Vue, atomic commits, maximum
reuse, and View-first data flow.

## Scope

- Compared `../FoolFrame/src/Web/public/javascripts/app/detailview.js`,
  `../FoolFrame/src/Web/routes/index.js`, and
  `../FoolFrame/src/Web/Cloud-Social/soway.js`.
- Reused the existing `saveobj` and `savenewobj` service paths.
- Added migrated route aliases for the old Web `/data/save` and `/data/new`
  wrappers as `/api/v1/data/save` and `/api/v1/data/new`.
- Accepted the old Web `obj`, `ownerviewid`, `ownerid`, and `prpid` request
  fields at the DTO boundary.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/api/DataController.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/SaveObjRequest.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/LegacySaveNewObjRequest.java`
- `fool-view/src/test/java/org/fool/framework/view/api/DataControllerSaveObjTest.java`
- `fool-view/src/test/java/org/fool/framework/view/api/DataControllerSaveNewObjTest.java`
- `fool-view/src/test/java/org/fool/framework/view/dto/SaveObjRequestTest.java`
- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Focused Docker Maven test:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=DataControllerSaveObjTest,DataControllerSaveNewObjTest,SaveObjRequestTest test`
- `python scripts/runtime_doctor_test.py`
- `git diff --check`
- `python scripts/check_repo_harness.py`
- `docker compose up -d --build backend`
- `python scripts/runtime_doctor.py`

## Skipped Checks

- Full root `mvn test` not rerun for this alias-only data boundary slice.

## Risks

- The raw Express paths are not mounted directly. Compatibility stays under
  the migrated `/api/v1/data/*` boundary used by the Vue/Nginx stack.

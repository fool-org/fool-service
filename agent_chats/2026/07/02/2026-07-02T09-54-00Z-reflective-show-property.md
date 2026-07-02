# Reflective Show Property Parity

## Prompt

- Continue the active migration goal against `../FoolFrame`.
- Current slice: migrate the legacy reflective model `ShowProperty` selection
  rule from `AssemblyModelFactory.LoadModel`.

## Scope

- Added runtime `showProperty` metadata to `Model`.
- Set reflective model `showProperty` using the legacy rule:
  first property whose name contains `name`, then the model ID property, then
  the first property.
- Added focused assertions for both fallback cases.
- Updated `docs/migration/foolframe-parity.md`.

## Changed Files

- `fool-model/src/main/java/org/fool/framework/model/model/Model.java`
- `fool-app-manage/src/main/java/org/fool/framework/app/ReflectiveAppModuleSource.java`
- `fool-app-manage/src/test/java/org/fool/framework/app/AppManageMigrationTest.java`
- `docs/migration/foolframe-parity.md`

## Validation

- Red:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -DfailIfNoTests=false test`
  failed at `fool-app-manage` test compilation because `Model#getShowProperty()`
  did not exist.
- Green:
  the same command passed with `BUILD SUCCESS`; `AppManageMigrationTest` ran
  35 tests with 0 failures and 0 errors.
- Full backend:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false test`
  passed with `BUILD SUCCESS` across the 15-module reactor.
- Repository harness:
  `python scripts/check_repo_harness.py` passed.
- Diff hygiene:
  `git diff --check` passed with no output.
- Runtime stack:
  `docker compose ps` showed backend and frontend up, with MySQL and Redis
  healthy.

## Risks

- This only adds the shared runtime metadata. Business-object list join/display
  behavior can now build on `showProperty`, but is not part of this slice.

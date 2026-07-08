# Focused Maven test config

## Prompt

Continue the Docker/FoolFrame/Vue migration with atomic commits and maximum
reuse.

## Scope

- Replaced the pending "focused Maven profile" backlog with the smaller actual
  fix: root Surefire config ignores upstream modules with no matching tests.
- Kept module scoping on Maven's existing `-pl <module> -am -Dtest=<TestClass>`
  path instead of adding profile scaffolding.
- Updated validation and migration docs to show the simplified Docker Maven
  commands.

## Changed Files

- `pom.xml`
- `docs/validation.md`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Red check: `docker run --rm --network fool-service_default -v "$PWD":/workspace
  -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl
  fool-view -am -Dtest=DataQueryServiceInputQueryTest test` failed in
  `fool-common` with `No tests were executed!`.
- Green check: the same command passed after the Surefire config change,
  running `DataQueryServiceInputQueryTest` with 7 tests.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v
  "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn test`:
  passed, 15 reactor modules.
- `python scripts/check_repo_harness.py`: passed.
- `git diff --check`: passed.

## Risks

- This does not add named Maven profiles. The existing `-pl/-am` mechanism is
  the focused module boundary; add profiles only if a real repeated module set
  appears.

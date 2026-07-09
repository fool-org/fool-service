# Reflective Dependency Packages

## Prompt

- Continue the Docker/FoolFrame/Vue migration with maximum reuse and controlled
  file size.

## Scope

- Added a `ReflectiveAppModuleSource` overload that accepts declared dependency
  package names.
- Reused the existing recursive package scanner and module dependency wiring.
- Added a focused test proving an annotated model in a declared dependency
  package becomes its own module and is wired as a root-module dependency.
- Updated migration/task state.

## Changed Files

- `fool-app-manage/src/main/java/org/fool/framework/app/ReflectiveAppModuleSource.java`
- `fool-app-manage/src/test/java/org/fool/framework/app/ReflectiveAppModuleSourceDependencyTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T11-21-21Z-reflective-dependency-packages.md`

## Validation

- `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=ReflectiveAppModuleSourceDependencyTest test` - passed.
- `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest,ReflectiveAppModuleSourceDependencyTest test` - passed, 42 tests in the targeted app-manage test set.
- `python scripts/check_repo_harness.py` - passed.
- `git diff --check` - passed.

## Skipped Checks

- Local host Maven was not used because the host JDK fails Java 17 compilation
  with `invalid target release: 17`; Docker Maven is the repository validation
  baseline for Java 17.

## Risks

- Low. This deliberately avoids scanning the entire Java classpath; callers
  declare the dependency package names that replace FoolFrame assembly
  references.

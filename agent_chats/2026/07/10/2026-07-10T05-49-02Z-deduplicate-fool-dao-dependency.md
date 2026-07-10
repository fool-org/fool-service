# Deduplicate fool-dao Dependency

## Prompt

Continue the migration while controlling file size and maximizing reuse.

## Scope

- Remove the second identical `spring-jdbc` declaration from
  `fool-dao/pom.xml`.
- Keep the original compile-scoped dependency and all unrelated POM settings
  unchanged.

## Changed Files

- `fool-dao/pom.xml`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/10/2026-07-10T05-49-02Z-deduplicate-fool-dao-dependency.md`

## Validation

- `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-dao -am -DskipTests package`
  passed the five-module reactor without the duplicate dependency warning.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-dao -am -DfailIfNoTests=false test`
  passed 39 `fool-common` and 18 `fool-dao` tests.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.

## Risk

No dependency is removed from the resolved classpath because the original
compile-scoped `spring-jdbc` declaration remains. Existing DAO test code logs
a caught null dereference during one test, but Surefire reports all 18 DAO
tests passing; that test-quality issue predates and is outside this POM-only
change.

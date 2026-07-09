# Recursive Owner Expression

## Prompt

Continue the Docker/Vue FoolFrame migration with atomic commits and maximum
reuse.

## Scope

- Rechecked FoolFrame `GetValueExpression`: `#` recursively evaluates the
  remaining expression against `ob.Owner`, so `##.field` reads the grandparent.
- Updated the shared Java `OperationCommandValueResolver` to treat `#` as a
  recursive owner prefix instead of a one-level `#.` special case.
- Left `@modelcon`, `@clientaddress`, and `@context` untouched because the
  current Java token/session state has no reliable source for those values.

## Changed Files

- `fool-model/src/main/java/org/fool/framework/model/service/OperationCommandValueResolver.java`
- `fool-model/src/test/java/org/fool/framework/model/service/OperationCommandValueResolverTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T11-31-42Z-recursive-owner-expression.md`

## Validation

- GREEN: `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=OperationCommandValueResolverTest test`
- GREEN: `python scripts/check_repo_harness.py`
- GREEN: `git diff --check`

## Skipped Checks

- Docker runtime doctor/backend rebuild were not rerun for this resolver-only
  change; the running backend container would not include the new class until a
  rebuild.

## Risks

- Broader FoolFrame context expressions still need real Java state before they
  should be migrated.

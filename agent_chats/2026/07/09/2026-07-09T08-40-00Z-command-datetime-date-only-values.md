# Command DateTime Date-Only Static Values

## Prompt

- Continue the FoolFrame migration while keeping the work focused on the
  migration goal, avoiding DTO-bound shortcuts, and keeping file size/code
  reuse under control.

## Scope

- Align shared operation-command static DateTime parsing with FoolFrame's
  `Convert.ToDateTime` behavior for date-only values.
- Keep the fix in `OperationCommandValueResolver` so both `runoperation` and
  trigger command paths reuse the same behavior.

## Changed Files

- `fool-model/src/main/java/org/fool/framework/model/service/OperationCommandValueResolver.java`
- `fool-model/src/test/java/org/fool/framework/model/service/OperationCommandValueResolverTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Red:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=OperationCommandValueResolverTest#resolvesLegacyDateTimeStaticDateOnlyValues -DfailIfNoTests=false test`
  failed with:
  `date-only DateTime static values should parse: Text '2026-07-03' could not be parsed at index 10`.
- Green:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=OperationCommandValueResolverTest#resolvesLegacyDateTimeStaticDateOnlyValues -DfailIfNoTests=false test`
  passed.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -DfailIfNoTests=false test`
  passed: 79 tests, 0 failures, 0 errors.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.
- `python scripts/runtime_doctor.py` passed against the running Docker stack.

## Runtime Evidence

- Docker stack was running with backend, frontend, MySQL, and Redis.
- Runtime doctor covered auth first-hop, app shell menu, `getlistview`,
  `querydata`, `querydatadetail`, `inputquery`, report routes, message routes,
  and logout through the current Compose stack.

## Risks And Follow-Ups

- Full backend reactor tests were not rerun for this narrow parser slice; run
  the full backend matrix before broader model-command changes.
- Remaining migration work stays in `docs/migration/foolframe-parity.md`.

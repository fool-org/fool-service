# Inputquery selected View filter

## Prompt

Continue the FoolFrame migration with Docker/Vue parity, atomic commits,
maximum reuse, and no concrete business DTO binding.

## Scope

- Compared legacy `HandlerInputQuery` and `InputContext` with the current
  backend `inputquery` implementation.
- Made backend `inputquery` combine a configured `ViewItem.SelectedView` raw
  filter with the text `LIKE` filter on the target-model lookup path.
- Kept existing source-list branches first, so `Property.Source` and
  `ViewItem.sourceExpression` behavior is unchanged.
- Added a focused service test for selected View filter composition.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceInputQueryTest.java`
- `tasks.md`
- `docs/migration/foolframe-parity.md`

## Validation

- `mvn -pl fool-view -Dtest=DataQueryServiceInputQueryTest test`: failed on
  local JDK with `invalid target release: 17`.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataQueryServiceInputQueryTest test`: passed, 7 tests.

## Runtime Evidence

- `docker compose up -d --build backend`: rebuilt and recreated backend.
- `python scripts/runtime_doctor.py`: passed compose health plus backend
  `/test`, auth, view, data, inputquery, report, message, and logout probes.

## Risks

- This only applies selected View filters to normal target-model lookup.
  Broader input-query expression evaluation remains in the migration backlog.

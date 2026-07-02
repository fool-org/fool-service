# Business Object List Join Parity

## Prompt

- Continue the active FoolFrame migration goal.
- Current slice: migrate legacy list-query BusinessObject display behavior from
  `ListViewQueryContext`, where non-multi-map object properties join the target
  model and select its `ShowProperty`.

## Scope

- `SqlGenerator` now emits a `LEFT OUTER JOIN` for non-collection
  BusinessObject properties when the target model has a distinct
  `showProperty`.
- Joined target ID/show columns are selected with the legacy
  `<property>_<column>` alias shape.
- `Mapper` now reconstructs a partial nested `IDynamicData` object from those
  aliases.
- Existing simple-column SQL output stays unchanged when no join is needed.
- Missing multi-map DBMaps metadata is skipped instead of failing select SQL
  generation.
- Updated `docs/migration/foolframe-parity.md`.

## Changed Files

- `fool-model/src/main/java/org/fool/framework/model/sqlscript/SqlGenerator.java`
- `fool-model/src/main/java/org/fool/framework/model/service/Mapper.java`
- `fool-model/src/test/java/org/fool/framework/model/sqlscript/SqlGeneratorTest.java`
- `fool-model/src/test/java/org/fool/framework/model/service/MapperDbMapsTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/02/2026-07-02T10-00-10Z-business-object-list-join.md`

## Validation

- Red:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -DfailIfNoTests=false test`
  failed with 2 expected failures:
  `generateSelectJoinsLegacyBusinessObjectShowProperty` and
  `mapsLegacyJoinedBusinessObjectFromListQueryAliases`.
- Green focused:
  the same command passed with `BUILD SUCCESS`; `fool-model` ran 18 tests with
  0 failures and 0 errors.
- Full backend:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false test`
  passed with `BUILD SUCCESS` across the 15-module reactor.
- Repository harness:
  `python scripts/check_repo_harness.py` passed.
- Whitespace:
  `git diff --check` passed with no output.
- Runtime stack:
  `docker compose ps` showed backend and frontend up, with MySQL and Redis
  healthy.

## Risks

- Ordering and keyword search over joined BusinessObject show columns are still
  separate parity gaps.

# Audit Model Runtime Remaining Work

## Prompt

Continue the Docker/FoolFrame/Vue migration and prove completion from current
source and runtime evidence rather than relying on the existing percentage.

## Scope

- Recheck the `SCPB05-Soway.Model` remaining runtime mutation wording against
  FoolFrame source and current Java behavior.
- Verify the active Docker model connection state.
- Remove speculative collection/external-model/routed-transaction wording only
  where current evidence closes it.

## Evidence

- FoolFrame `ModelBindingList` runs collection `ItemsAdd` and `ItemsDelete`;
  current `ModelDataService` covers add, set, and delete collection triggers.
- FoolFrame `ModelMethodContext` external-model branches cover operation lookup,
  detail fallback, create/update/delete, command execution, and result mapping;
  the current trigger and runoperation paths cover the same observable slices.
- FoolFrame `SqlServer.dbContext` commits Create/Save row transactions before
  executing model triggers, so it does not promise cross-connection atomicity.
- Live Docker query returned four `SW_SYS_MODEL` rows, all with null
  `MODEL_CON`; current migrated models therefore use the active `car_wash`
  datasource.

## Validation

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=ModelDataServiceTest -Dsurefire.failIfNoSpecifiedTests=false test`
  passed 37 `ModelDataServiceTest` cases on Java 17.
- `docker compose exec -T mysql mysql -uroot -pPa88word -N -e "SELECT COALESCE(MODEL_CON,'<null>'), COUNT(*) FROM car_wash.SW_SYS_MODEL GROUP BY MODEL_CON ORDER BY COUNT(*) DESC;"`
  returned `<null> 4`.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.

## Result

The active-datasource Model runtime mutation surface is no longer listed as
open migration work. Model-specific connection routing remains conditional on
a future concrete migrated model with non-empty `MODEL_CON`; no abstraction or
unused routing factory was added speculatively.

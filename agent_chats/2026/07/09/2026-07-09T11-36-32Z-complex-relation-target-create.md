# Complex Relation Target Create

## Prompt

Continue the Docker/Vue FoolFrame migration with atomic commits and maximum
reuse.

## Scope

- Rechecked FoolFrame `dbContext.CreateComplexRelationBuild`: Many2Many and
  Recurve relation writes create the target object first when it does not
  exist, then insert the relation row.
- Updated `ModelDataService.writeCollectionRelations` to reuse the existing
  dynamic `createData(..., writeCollections=false, runTriggers=false)` path
  for missing complex-relation targets before inserting the relation.
- Added a focused runtime model test for creating a missing Many2Many target
  row and relation row together.

## Changed Files

- `fool-model/src/main/java/org/fool/framework/model/service/ModelDataService.java`
- `fool-model/src/test/java/org/fool/framework/model/service/ModelDataServiceTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T11-36-32Z-complex-relation-target-create.md`

## Validation

- INFRA FAIL: `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=ModelDataServiceTest#createDataCreatesMissingLegacyManyToManyTargetRows,ModelDataServiceTest#createDataWritesLegacyManyToManyRelationRows,ModelDataServiceTest#saveDataWritesLegacyManyToManyRelationRows test`
  failed because the container was not on `fool-service_default` and could not
  resolve `mysql`.
- GREEN: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=ModelDataServiceTest#createDataCreatesMissingLegacyManyToManyTargetRows,ModelDataServiceTest#createDataWritesLegacyManyToManyRelationRows,ModelDataServiceTest#saveDataWritesLegacyManyToManyRelationRows test`
- GREEN: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=ModelDataServiceTest test`
- GREEN: `python scripts/check_repo_harness.py`
- GREEN: `git diff --check`

## Skipped Checks

- Docker runtime doctor/backend rebuild were not rerun for this model-service
  internal parity slice; focused Docker Maven tests covered the changed path.

## Risks

- Broader collection-state parity remains open for cases not represented by
  the current relation model tests.

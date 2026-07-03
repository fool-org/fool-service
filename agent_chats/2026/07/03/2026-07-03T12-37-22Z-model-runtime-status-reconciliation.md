# Model Runtime Mutation Status Reconciliation

## Prompt

- Continue the Docker/Vue FoolFrame migration and keep commits atomic.

## Scope

- Reconciled `docs/migration/foolframe-parity.md` against current test coverage
  instead of reimplementing completed behavior.
- Confirmed existing tests cover simple batch saves, old-id dynamic save
  lookup, DBMaps create/update writes, One2Many child-row
  create/update/delete-list sync, Many2Many/Recurve relation-table
  insert/delete-list sync, legacy `saveobj` `Itemproperties`, and legacy
  `savenewobj` owner-relation creation.
- Narrowed the remaining runtime mutation list to unproven richer collection
  state, remaining command types, WCF/JSON/external-model edge cases, trigger
  side effects, and routed-connection transaction behavior.

## Changed Files

- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/03/2026-07-03T12-37-22Z-model-runtime-status-reconciliation.md`

## Validation

- Focused status proof:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -v /tmp:/host-tmp -w /workspace maven:3.9-eclipse-temurin-17 sh -lc 'mvn -pl fool-model,fool-view -am -DfailIfNoTests=false -Dtest=ModelDataServiceTest,DataQueryServiceSaveObjTest test > /host-tmp/fool-runtime-status-tests.log 2>&1; status=$?; tail -n 80 /host-tmp/fool-runtime-status-tests.log; exit $status'`
  - `ModelDataServiceTest`: tests run 21, failures 0, errors 0, skipped 0.
  - `DataQueryServiceSaveObjTest`: tests run 5, failures 0, errors 0,
    skipped 0.
- `python scripts/check_repo_harness.py`
  - Repository harness validation passed.
- `git diff --check`
  - Passed.

## Runtime Evidence

- `docker compose ps`
  - Backend and frontend were running.
  - MySQL and Redis were running and healthy.

## Skipped Checks

- Frontend unit/build checks are not required for this docs/evidence-only
  status reconciliation.

## Remaining Risk

- The reconciled status does not complete the remaining runtime mutation work;
  it removes already-proven items from the remaining list so future work can
  target the real gaps.

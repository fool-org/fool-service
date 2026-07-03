# Model Trigger Hydration

## Prompt

- Continue the FoolFrame migration with Docker available.
- Compare against `../FoolFrame`.
- Keep the Vue frontend as the replacement UI surface.
- Report overall migration progress.

## Scope

Implemented the next small runtime parity slice for legacy
`SCPB05-Soway.Model` model triggers. This slice hydrates metadata only; it does
not execute trigger side effects during create/save/delete.

## Changes

- Added a mapped `Trigger` runtime model for `SW_SYS_MODEL_TRIGGER`.
- Added transient `Model.triggers`.
- Extended `ModelDataService.getModel` to load model triggers and indexed
  trigger commands from `SW_SYS_MODEL_TRIGGER_COMMANDS`.
- Added a focused regression test proving trigger and command hydration from
  legacy tables.
- Updated `docs/migration/foolframe-parity.md` to move trigger metadata
  hydration out of the remaining gap list while keeping trigger side-effect
  execution open.

## Validation

- Red:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -v /tmp:/host-tmp -w /workspace maven:3.9-eclipse-temurin-17 sh -lc 'mvn -pl fool-model -am -DfailIfNoTests=false -Dtest=ModelDataServiceTest test > /host-tmp/fool-trigger-red.log 2>&1; status=$?; tail -n 80 /host-tmp/fool-trigger-red.log; exit $status'`
  failed before implementation because `Model.getTriggers()` and `Trigger`
  accessors did not exist.
- Green:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -v /tmp:/host-tmp -w /workspace maven:3.9-eclipse-temurin-17 sh -lc 'mvn -pl fool-model -am -DfailIfNoTests=false -Dtest=ModelDataServiceTest test > /host-tmp/fool-trigger-green.log 2>&1; status=$?; tail -n 80 /host-tmp/fool-trigger-green.log; exit $status'`
  passed with `Tests run: 22, Failures: 0, Errors: 0, Skipped: 0` and
  `BUILD SUCCESS`.
- `git diff --check` passed.
- `python scripts/check_repo_harness.py` passed.
- `COMPOSE_PROGRESS=plain docker compose up -d --build backend` passed; the
  Docker Maven reactor finished with all 15 modules `SUCCESS` and rebuilt the
  backend image.

## Runtime Evidence

- `curl --retry 10 --retry-delay 2 --retry-connrefused -fsS http://localhost:8080/test`
  returned the Docker seed rows.
- Docker stack is up with backend on `8080`, frontend on `8081`, MySQL on
  `3307`, and Redis on `6380`.

## Risks

- Trigger side-effect execution still remains. FoolFrame runs save/create
  triggers after DB writes and delete triggers before DB deletion.
- This slice reuses `OperationCommand` with SQL aliases for trigger command
  owner/index columns; future execution should verify all trigger command
  types, not only metadata hydration.

## Follow-ups

- Execute model triggers in the migrated create/save/delete flow.
- Add matching property-trigger hydration/execution once the model-trigger path
  is proven.

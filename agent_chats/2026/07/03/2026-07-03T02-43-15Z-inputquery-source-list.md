# InputQuery Source List

## Prompt

- Continue the FoolFrame migration with Docker runtime, Vue frontend, and timely atomic commits.

## Scope

- Migrated the legacy `inputquery` existing-object source-list branch for `Property.Source`.
- Kept owner-context and view-item source-expression branches open.

## Changes

- `DataQueryService.inputQuery` now checks `ObjID` plus selected property `source`.
- When the current object exposes that source collection, candidates are filtered from the collection with legacy case-insensitive contains matching.
- The existing target-model paged SQL lookup remains the fallback when no usable source collection is present.
- Migration parity docs now distinguish this completed source-list branch from the remaining owner-context/view-item source-expression work.

## Validation

- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=DataQueryServiceInputQueryTest#inputQueryFiltersLegacySourceListForExistingObject test`
  - Failed with `expected:<2> but was:<0>` before the source-list branch existed.
- GREEN: same focused command passed after adding the branch.
- Module regression: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am test`
  - Passed with `BUILD SUCCESS`; reactor through `fool-view` ran 56 tests.
- Repo harness: `python scripts/check_repo_harness.py`
  - Passed with `Repository harness validation passed.`
- Whitespace: `git diff --check`
  - Passed with no output.
- Docker runtime: `docker compose up -d --build backend`
  - Rebuilt the backend image and started `fool-service-backend-1`.
- HTTP smoke: `curl --retry 20 --retry-delay 2 --retry-all-errors -fsS http://localhost:8080/test`
  - Passed and returned seeded order rows.
- HTTP smoke: `curl -fsS -H 'Content-Type: application/json' -d '{"viewName":"OrderList","viewItemId":"symbol","text":"BTC"}' http://localhost:8080/api/v1/data/inputquery`
  - Passed with `{"code":0,"message":"success","data":{"items":[]}}`.
- Runtime status: `docker compose ps`
  - Backend, frontend, MySQL, and Redis were up; MySQL and Redis were healthy.

## Skipped Checks

- Frontend tests were not rerun because this change did not touch `frontend/`.

## Risks

- Owner-context handling for added child objects remains open.
- `VIEW_ITEM_SOURCEEXP` runtime metadata is still not wired into `DataQueryService.inputQuery`.

## Follow-ups

- Add `ViewItem` source-expression metadata and use it as the first source expression, matching `HandlerInputQuery`.
- Add the added-item owner-context branch once parent object loading and new child source evaluation are available.

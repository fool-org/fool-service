# saveobj Updated Subitems

## Prompt

Continue the Docker/FoolFrame migration goal, with emphasis on View-first data
flow, reuse, small files, and no speculative code.

## Scope

Closed one concrete `SCPB05-Soway.Model` collection-state parity slice:
legacy `saveobj.Itemproperties[].Items[]` means "update existing child item",
not "add child item".

## Changes

- `DataQueryService` now keeps `Items[]` child rows in the iterable payload for
  persistence while moving their state from `SubItemList.AddedList` to
  `SubItemList.UpdatedList`.
- `DataQueryServiceSaveObjTest` now proves `Items[]`, `AddedItems[]`, and
  `DelteItems[]` land in updated, added, and deleted state respectively.
- Updated `tasks.md` and the FoolFrame parity log.

## Validation

- Failing proof before implementation:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=DataQueryServiceSaveObjTest#saveLegacyObjectWritesItemPropertiesToDynamicSubItems test`
  failed with `expected:<1> but was:<0>` for `UpdatedList`.
- Passing focused proof after implementation:
  same command passed with `Tests run: 1, Failures: 0, Errors: 0`.
- Passing save-object test class:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=DataQueryServiceSaveObjTest test`
  passed with `Tests run: 6, Failures: 0, Errors: 0`.
- Runtime smoke:
  `python scripts/runtime_doctor.py` passed all Docker auth/View/data/save/report/message checks.

## Runtime Evidence

- Docker Compose services were already up and healthy.
- Runtime doctor passed `data:saveobj-items-delete`, proving the current Docker
  save path still updates and deletes child rows through the Vue proxy.

## Risks

- This preserves the current persistence behavior; it only tightens the
  `SubItemList` state surface for existing child updates.

## Follow-ups

- Remaining collection work should target a newly identified legacy edge case,
  not another DTO-state rewrite.

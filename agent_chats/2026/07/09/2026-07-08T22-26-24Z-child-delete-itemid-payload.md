# Child Delete ItemId Payload

## Prompt

Continue the View-first Vue migration and avoid binding child payloads to raw
data DTO fields.

## Scope

- Keep child delete payloads to the legacy fields that FoolFrame actually uses.
- Do not change backend delete semantics in this slice.

## Legacy Source

- `../FoolFrame/src/Server/Soway.Server/DataFormator.cs` removes deleted child
  rows by matching `item.ItemId`; the `DelteItems` branch does not read
  `Propertyies`.

## Changes

- Added a regression test proving delete payloads do not include child data DTO
  values.
- Changed `buildDeletedItemProperty` to emit an empty `propertyies` list for
  deleted items.
- Updated migration parity docs and task state.

## Validation

- RED: `npm test -- --run src/viewWorkflow.test.ts` failed because `dtoOnly`
  leaked into `DelteItems[].propertyies`.
- GREEN: `npm test -- --run src/viewWorkflow.test.ts`.
- PASS: `npm test`.
- PASS: `npm run build`.
- PASS: `python scripts/check_repo_harness.py`.
- PASS: `git diff --check`.
- PASS: `docker compose build frontend`.
- PASS: `docker compose up -d frontend`.
- PASS: `python scripts/runtime_doctor.py`.

## Runtime Evidence

- `curl -fsS http://localhost:8081/` loaded frontend bundle
  `index-BRasaseJ.js`.
- `docker compose ps` showed backend, rebuilt frontend, MySQL, and Redis
  running; MySQL and Redis were healthy.

## Risks

- This relies on the migrated backend keeping the same delete-by-item-id
  behavior as FoolFrame.

## Follow-ups

- Continue the remaining migration work from `docs/migration/foolframe-parity.md`.

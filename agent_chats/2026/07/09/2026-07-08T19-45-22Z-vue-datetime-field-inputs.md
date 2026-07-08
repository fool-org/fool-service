# Vue DateTime Field Inputs

## Prompt

Continue the Docker/Vue FoolFrame migration. Re-check the rendered View page
first, then its data, because binding frontend editors to concrete business DTO
fields is the wrong direction.

## Scope

- Confirmed the active runtime shape stays View-first: `getlistview(ViewId)`
  defines the rendered fields and `querydata(ViewId)` / `querydatadetail`
  provide values through `ListDataValue` metadata.
- Added `DateTime` / `PrpType=14` support to the shared Vue metadata input
  helper, mapping it to native `datetime-local`.
- Normalized legacy DateTime display strings only at the editor input boundary,
  converting space-separated values and trimming fractional seconds for browser
  compatibility.
- Kept widget selection independent from business DTO field names such as
  `createdAt` or `orderTime`.

## Changed Files

- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/MetadataFieldEditor.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-08T19-45-22Z-vue-datetime-field-inputs.md`

## Validation

- RED: `npm test -- viewWorkflow.test.ts`
  - Failed because `fieldInputType` still returned `text` for `PrpType=14`
    and `fieldInputValue` did not exist.
- RED: `npm test -- payload.test.ts`
  - Failed because `MetadataFieldEditor` did not use `fieldInputValue`.
- GREEN: `npm test -- viewWorkflow.test.ts`
  - `34` focused View workflow helper tests passed.
- GREEN: `npm test -- payload.test.ts`
  - `59` focused payload/source tests passed.
- GREEN: `npm test`
  - `96` frontend tests passed.
- GREEN: `npm run build`
  - `vue-tsc --noEmit && vite build` succeeded.
- GREEN: `python scripts/check_repo_harness.py`
  - Repository harness validation passed.
- GREEN: `git diff --check`

## Risks

- This does not add a custom date-time picker or timezone conversion. Values
  continue to save as strings through the existing metadata-driven payload
  path.

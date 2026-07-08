# Vue Native Field Inputs

## Prompt

Continue the Docker/Vue FoolFrame migration and improve frontend usability
while keeping rendering driven by View metadata instead of concrete business
DTO fields.

## Scope

- Added a shared `fieldInputType` helper for low-risk scalar field metadata.
- Rendered non-enum, non-lookup, editable fields through native HTML input
  types: `date`, `time`, and `number` where the View field type says so.
- Kept enum selects, BusinessObject lookup, readonly controls, and save
  payloads on the existing shared View workflow path.
- Skipped custom picker widgets and datetime value conversion.

## Changed Files

- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/MetadataFieldEditor.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-08T19-38-04Z-vue-native-field-inputs.md`

## Validation

- RED: `npm test -- viewWorkflow.test.ts`
  - Failed because `fieldInputType` did not exist.
- RED: `npm test -- payload.test.ts`
  - Failed because `MetadataFieldEditor` still rendered generic text inputs.
- GREEN: `npm test -- viewWorkflow.test.ts`
  - `33` focused View workflow helper tests passed.
- GREEN: `npm test -- payload.test.ts`
  - `59` focused payload/source tests passed.
- RED: `npm run build`
  - Failed because the new test used numeric `PrpType: 1` while the Vue API
    type declares `PrpType` as a legacy string.
- GREEN: `npm test`
  - `95` frontend tests passed.
- GREEN: `npm run build`
  - `vue-tsc --noEmit && vite build` succeeded.
- GREEN: `python scripts/check_repo_harness.py`
  - Repository harness validation passed.
- GREEN: `git diff --check`

## Risks

- `DateTime` is intentionally left as text until the stored/display value
  shape is normalized for `datetime-local`; native `date`, `time`, and numeric
  inputs do not change the existing string save payload contract.

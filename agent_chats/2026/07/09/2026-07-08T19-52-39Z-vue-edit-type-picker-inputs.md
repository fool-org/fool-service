# Vue EditType Picker Inputs

## Prompt

Continue the Docker/Vue FoolFrame migration, keeping the rendered View page
ahead of data and avoiding concrete business DTO bindings.

## Scope

- Compared FoolFrame `ItemEditType` picker values with the migrated Java enum.
- Reused the existing Vue `fieldInputType` helper so `DatePicker`,
  `TimePicker`, and `DateTimePicker` select native inputs from View metadata.
- Kept field names out of widget selection and left save payloads on the
  existing metadata-driven string path.
- Rebuilt the Docker frontend/backend services so the running Compose stack
  serves the current frontend bundle.

## Changed Files

- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-08T19-52-39Z-vue-edit-type-picker-inputs.md`

## Validation

- RED: `npm test -- viewWorkflow.test.ts`
  - Failed because `EditType=DatePicker` still mapped to `text`, and
    `EditType=DateTimePicker` did not normalize DateTime input values.
- GREEN: `npm test -- viewWorkflow.test.ts`
  - `34` focused View workflow helper tests passed.
- GREEN: `npm test`
  - `96` frontend tests passed.
- GREEN: `npm run build`
  - `vue-tsc --noEmit && vite build` succeeded.
- GREEN: `python scripts/check_repo_harness.py`
  - Repository harness validation passed.
- GREEN: `docker compose up -d --build frontend`
  - Rebuilt frontend and backend images and recreated both containers.
- GREEN: `curl http://localhost:8081/`
  - Returned `200 399`.
- GREEN: `curl http://localhost:8080/test`
  - Returned `200 465`.
- GREEN: `python scripts/runtime_doctor.py`
  - Compose, auth shell, View/data, inputquery, report, message, notify, and
    logout checks passed.

## Risks

- This does not add custom picker widgets. Native browser inputs cover the
  migrated picker metadata until a real legacy-only widget needs more.

# Vue Numeric EditType

## Prompt

Continue the Docker/Vue FoolFrame migration, maximize reuse, and avoid
building a custom widget layer unless the legacy protocol requires it.

## Scope

- Added Vue helper coverage for FoolFrame numeric `ItemEditType` enum values.
- Mapped `ReadOnly=0`, `CheckBox=2`, `DatePicker=6`, `TimePicker=7`, and
  `DateTimePicker=8` to the same native controls already used for string enum
  names.
- Updated frontend API types so legacy `editType` / `EditType` may be a
  string or number.

## Changed Files

- `frontend/src/api.ts`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-08T21-22-52Z-vue-numeric-edit-type.md`

## Validation

- RED: `npm test -- viewWorkflow.test.ts` failed because numeric
  `EditType=6` still mapped to `text`.
- GREEN: `npm test -- viewWorkflow.test.ts`
- GREEN: `npm test`
- GREEN: `npm run build`
- GREEN: `docker compose build frontend >/tmp/fool_frontend_build.log 2>&1 && docker compose up -d frontend >/tmp/fool_frontend_up.log 2>&1`
- Browser GREEN: `http://localhost:8081/?qa=edit-type-numeric` loaded rebuilt
  bundle `index-CdsgFW_d.js`, rendered Order List / Detail, and current-bundle
  warning/error logs were empty.

## Risks

- This covers numeric aliases for existing native controls only. Rich custom
  widgets such as `RichTextBox`, `ComboBox`, `SelectLable`, and `DropTextBox`
  remain future slices.

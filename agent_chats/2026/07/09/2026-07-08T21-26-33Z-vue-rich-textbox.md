# Vue RichTextBox

## Prompt

Continue the Docker/Vue FoolFrame migration, maximize reuse, and keep the
frontend metadata-driven.

## Scope

- Added a shared Vue helper for legacy `RichTextBox` / numeric
  `ItemEditType=5` metadata.
- Rendered those fields with a native `<textarea>` in `MetadataFieldEditor`.
- Reused the existing value update path by allowing `HTMLTextAreaElement`.

## Changed Files

- `frontend/src/MetadataFieldEditor.vue`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-08T21-26-33Z-vue-rich-textbox.md`

## Validation

- RED: `npm test -- viewWorkflow.test.ts` failed because
  `isMultilineField` did not exist.
- RED: `npm test -- payload.test.ts` failed because
  `MetadataFieldEditor` did not import/use `isMultilineField`, render
  `<textarea>`, or handle `HTMLTextAreaElement`.
- GREEN: `npm test -- viewWorkflow.test.ts`
- GREEN: `npm test -- payload.test.ts`
- GREEN: `npm test`
- GREEN: `npm run build`
- GREEN: `docker compose build frontend >/tmp/fool_frontend_build.log 2>&1 && docker compose up -d frontend >/tmp/fool_frontend_up.log 2>&1`
- Browser GREEN: `http://localhost:8081/?qa=rich-textbox` loaded rebuilt
  bundle `index-C4zu0XJH.js` and current-bundle warning/error logs were empty.

## Risks

- This covers the multiline native editor only. Legacy `ComboBox`,
  `SelectLable`, and `DropTextBox` still need separate View metadata behavior
  before migration can call those widget paths complete.

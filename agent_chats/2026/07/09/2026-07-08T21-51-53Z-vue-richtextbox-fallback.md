# Vue RichTextBox Fallback

## Prompt

Continue the FoolFrame migration by following the legacy View render path
before adding frontend widget behavior.

## Scope

- Rechecked FoolFrame detail rendering: `detailView.jade` emits generic field
  metadata and `detailview.js` passes `data-propertyType` to `setextype`.
- Changed `isMultilineField` so `RichTextBox` / numeric `ItemEditType=5`
  selects a `<textarea>` only when `PrpType` / `PropertyType` metadata is
  absent.
- Kept typed string fields on the normal scalar input path even when stale
  `EditType=RichTextBox` metadata is present.
- Corrected migration docs and task state to stop overstating RichTextBox
  widget parity.

## Changed Files

- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-08T21-51-53Z-vue-richtextbox-fallback.md`

## Validation

- RED: `npm test -- viewWorkflow.test.ts` failed because `PrpType=String`
  with `EditType=RichTextBox` still returned multiline.
- GREEN: `npm test -- viewWorkflow.test.ts`
- GREEN: `npm test`
- GREEN: `npm run build`
- GREEN: `git diff --check`
- GREEN: `python scripts/check_repo_harness.py`
- GREEN: `docker compose build frontend && docker compose up -d frontend`
- GREEN: `curl -fsS http://localhost:8081/` returned bundle
  `index-nn0-Wy3G.js`, and `docker compose ps` showed backend, frontend,
  MySQL, and Redis running.

## Risks

- This deliberately does not implement `ComboBox`, `SelectLable`, or
  `DropTextBox`; those still need View/data-source proof before a frontend
  widget is added.

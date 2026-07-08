# Vue PrpType First Controls

## Prompt

Keep the migration focused on the legacy View render path: inspect the
rendered View page first, then data, and avoid binding frontend controls to a
concrete business DTO.

## Scope

- Verified FoolFrame detail rendering through `detailView.jade`,
  `detailview.js`, `setextype.js`, and `savetext.js`.
- Changed Vue scalar input selection to prefer `PrpType` / `PropertyType`
  metadata before considering `EditType` picker hints.
- Kept `EditType` picker hints as compatibility fallback only when property
  type metadata is absent, while preserving view-item-only states such as
  readonly and rich text.
- Accepted numeric `PrpType` / `PropertyType` codes for imported legacy
  configurations without adding a business-specific DTO.

## Changed Files

- `frontend/src/api.ts`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-08T21-35-07Z-vue-prptype-first-controls.md`

## Validation

- RED: `npm test -- viewWorkflow.test.ts` failed because `PrpType=String`
  with `EditType=DatePicker` still rendered as `date`.
- GREEN: `npm test -- viewWorkflow.test.ts`
- GREEN: `npm test`
- GREEN: `npm run build`
- GREEN: `docker compose build frontend`
- GREEN: `docker compose up -d frontend`
- GREEN: `docker compose ps`
- GREEN: `curl -fsS http://localhost:8081/` returned bundle
  `index-C6Jsw2Pw.js`
- GREEN: `git diff --check`
- GREEN: `python scripts/check_repo_harness.py`

## Risks

- This does not complete custom legacy widgets such as `ComboBox`,
  `SelectLable`, or `DropTextBox`; those still need separate View metadata and
  data-source analysis before implementation.

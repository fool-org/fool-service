# Vue ReadOnly Priority

## Prompt

Continue the FoolFrame-to-Vue migration while keeping rendering bound to
generic View metadata instead of concrete business DTOs.

## Scope

- Kept Vue readonly detection aligned with the FoolFrame detail page path:
  `detailView.jade` emits `data-readonly`, `detailview.js` edits only when it
  is false, and `savetext.js` saves only non-readonly fields.
- Changed `isReadonlyField` so explicit `readOnly` / `ReadOnly` wins before
  `EditType=ReadOnly` compatibility fallback.
- Added regression coverage for imported metadata where `ReadOnly=false` is
  paired with stale readonly `EditType`.

## Changed Files

- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-08T21-40-37Z-vue-readonly-priority.md`

## Validation

- RED: `npm test -- viewWorkflow.test.ts` failed because `readOnly=false`
  with `EditType=ReadOnly` still returned readonly.
- GREEN: `npm test -- viewWorkflow.test.ts`
- GREEN: `npm test`
- GREEN: `npm run build`
- GREEN: `docker compose build frontend`
- GREEN after startup retry: `docker compose up -d frontend` and
  `curl -fsS http://localhost:8081/` returned bundle `index-OfvGlpYn.js`
- GREEN: `git diff --check`
- GREEN: `python scripts/check_repo_harness.py`

## Risks

- This only fixes readonly priority. Custom widgets such as `ComboBox`,
  `SelectLable`, and `DropTextBox` still need separate View/data-source proof
  before implementation.

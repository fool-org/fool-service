# Legacy Detail Command Availability

## Prompt

Continue aligning the old page layout, style, and interactions in atomic
commits without coupling Vue components to concrete business DTOs.

## Scope

- Compared parent detail commands and field lookup controls in
  `detailView.jade`, `detailview.js`, and `setextype.js` with the Vue detail
  workflow.
- Removed the Vue-only global request lock from Edit, Save, View operations,
  and metadata lookup editors.
- Kept edit-session guards, the save request's own loading protection, and
  lookup-local loading state.
- Removed the unused detail `pending` prop and lookup context flag; added no
  state, request path, DTO binding, or duplicate component.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/MetadataFieldEditor.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T16-48-35Z-legacy-detail-command-availability.md`

## Validation

- `cd frontend && npm test` passed: 15 files, 181 tests.
- `cd frontend && npm run build` passed.
- `python scripts/check_repo_harness.py` passed.
- `ViewDetailPanel.vue` shrank from 463 to 461 lines and
  `MetadataFieldEditor.vue` shrank from 194 to 191 lines.
- Pending Docker rebuild, runtime doctor, and authorized browser acceptance.

## Risks And Follow-ups

- Browser-verify Edit, Save, and a lookup editor during an unrelated real
  request without submitting a detail save.
- Current Docker metadata may not expose a parent View operation; retain source
  contract coverage when no runtime fixture exists.
- `docs/superpowers/` is unrelated untracked work and remains untouched.

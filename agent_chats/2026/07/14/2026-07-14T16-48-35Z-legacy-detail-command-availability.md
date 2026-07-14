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
- `docker compose build frontend` passed.
- `docker compose up -d --no-deps --force-recreate frontend` passed.
- Deployed frontend image:
  `sha256:0ada59f116bf5c9bd971f597651ef6e902f589bae705ed04e4339445ab225e82`.
- `docker compose ps -a` showed backend/frontend/MySQL/Redis running and
  `db-migrate` at `Exited (0)`.
- `python scripts/runtime_doctor.py` passed all 67 checks before and after
  browser acceptance; backend `/test` passed through the doctor.
- Authorized browser acceptance logged in with `admin/admin`, opened
  `/view102/1001`, paused the backend, and started a real `getsubmenu` request
  by selecting Views.
- During that unrelated pending request, Edit remained enabled and entered the
  edit session. Save became enabled, and the Customer AutoComplete retained
  `disabled=false`; no detail save was submitted.
- Unpausing and reloading restored read-only `BTC-USDT / Ada Capital / Open`
  with Save disabled. MySQL still held order `1001 / BTC-USDT / 3001 / 0` and
  the same three child rows: `2001`, `2004`, and `1783093814663`.

## Risks And Follow-ups

- This parity slice is closed; the broader old-page migration remains active.
- Current Docker metadata does not expose a parent View operation, so that
  no-global-lock branch is covered by the source contract rather than a visible
  runtime fixture.
- No screenshot artifact was retained because the acceptance depended on DOM
  enabled state and a temporary paused request; data and runtime restoration
  were verified directly.
- `docs/superpowers/` is unrelated untracked work and remains untouched.

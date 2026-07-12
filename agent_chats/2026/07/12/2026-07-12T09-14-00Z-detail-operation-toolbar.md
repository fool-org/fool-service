# Detail Operation Toolbar

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with the old
  FoolFrame Web pages, using an atomic commit for each change.

## Scope

- Keep Edit, Save, and metadata-defined View operation buttons in one detail
  toolbar, matching `detailView.jade` action order.
- Preserve operation disabled state while the detail is editing.
- Remove visible operation parameter-name labels after verifying old Web
  `operation.js` only posts View/object/operation ids and has no parameter UI.
- Retain hydrated operation parameter metadata and shared protocol helpers for
  backend compatibility.

## Changed Files

- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T09-14-00Z-detail-operation-toolbar.md`

## Validation

- `cd frontend && npm test && npm run build`
  - 139 tests passed.
  - Vue TypeScript checking and Vite production build passed.
- `docker compose build frontend && docker compose up -d --no-deps frontend`
  - Rebuilt frontend deployed on port 8081.
- `python scripts/runtime_doctor.py`
  - Full runtime checks passed, including non-mutating runoperation aliases and
    View/detail operation metadata.
- `python scripts/check_repo_harness.py`
  - Repository harness passed.
- `git diff --check` passed.

## Runtime Evidence

- Docker serves the rebuilt detail toolbar at `http://localhost:8081`.
- Runtime doctor proved the legacy operation metadata and execution-result
  paths remain healthy after the toolbar change.

## Skipped Or Downgraded Checks

- Authenticated browser interaction remains gated by a fresh CAPTCHA without
  current solve permission.

## Risks And Follow-Ups

- Final authenticated desktop and 390x844 browser acceptance must prove the
  toolbar order, wrapping, edit-disable state, and operation result message.

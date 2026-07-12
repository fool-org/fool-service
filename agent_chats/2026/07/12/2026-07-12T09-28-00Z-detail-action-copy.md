# Detail Action Copy

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with the old
  FoolFrame Web pages, using an atomic commit for each change.

## Scope

- Restore old Web Chinese Edit/Save labels in the standalone detail toolbar.
- Restore Chinese child add, select dialog, search/page, edit/save/delete,
  close, empty-state, and confirmation copy.
- Restore Chinese local validation and fallback operation-result messages in
  the parent workflow.
- Preserve loaded metadata labels and backend operation `ReturnMsg` content.
- Localize the shared metadata lookup Search button used by detail fields.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/MetadataFieldEditor.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T09-28-00Z-detail-action-copy.md`

## Validation

- `cd frontend && npm test && npm run build`
  - 139 tests passed.
  - Vue TypeScript checking and Vite production build passed.
- `docker compose build frontend && docker compose up -d --no-deps frontend`
  - Rebuilt frontend deployed on port 8081.
- `python scripts/runtime_doctor.py`
  - Full runtime checks passed, including detail, lookup, operation, and child
    persistence routes.
- `python scripts/check_repo_harness.py`
  - Repository harness passed.
- `git diff --check` passed.

## Runtime Evidence

- Docker serves the rebuilt detail action copy at `http://localhost:8081`.
- Runtime doctor proved detail and child behavior remains healthy after the
  static presentation change.

## Skipped Or Downgraded Checks

- Authenticated browser interaction remains gated by a fresh CAPTCHA without
  current solve permission.

## Risks And Follow-Ups

- Final authenticated desktop and 390x844 browser acceptance must prove all
  visible detail labels, confirmation, operation feedback, and lookup actions.

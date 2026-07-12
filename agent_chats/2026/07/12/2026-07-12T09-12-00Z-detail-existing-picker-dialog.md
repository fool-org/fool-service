# Detail Existing Picker Dialog

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with the old
  FoolFrame Web pages, using an atomic commit for each change.

## Scope

- Make child creation mode follow `SelectFromExists`: manual metadata editors
  for false, modal candidate selection for true.
- Move candidate search, paging, and shared metadata table into a responsive
  dialog matching the old `#selectdialog` interaction.
- Load candidates through the existing parent workflow when the dialog opens.
- Close the dialog after emitting the existing add-selected-row event without
  copying candidate DTO state into the presentation component.

## Changed Files

- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T09-12-00Z-detail-existing-picker-dialog.md`

## Validation

- `cd frontend && npm test && npm run build`
  - 139 tests passed.
  - Vue TypeScript checking and Vite production build passed.
- `docker compose build frontend && docker compose up -d --no-deps frontend`
  - Rebuilt frontend deployed on port 8081.
- `python scripts/runtime_doctor.py`
  - Full runtime checks passed, including candidate View metadata/data and
    selected-row `AddedItems` persistence.
- `python scripts/check_repo_harness.py`
  - Repository harness passed.
- `git diff --check` passed.

## Runtime Evidence

- Docker serves the rebuilt candidate dialog at `http://localhost:8081`.
- Runtime doctor proved the select-from-existing View and child persistence
  chains remain healthy after the modal interaction change.

## Skipped Or Downgraded Checks

- Authenticated browser interaction remains gated by a fresh CAPTCHA without
  current solve permission.

## Risks And Follow-Ups

- Final authenticated desktop and 390x844 browser acceptance must prove modal
  open/search/page/select/close and the resulting child row refresh.

# Child Id Column

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with the old
  FoolFrame Web pages, using an atomic commit for each change.

## Scope

- Compare the Vue detail child table with old `detailView.jade`.
- Remove the hard-coded visible `ID` column because the old table renders only
  child `Properties[]` metadata and operation columns.
- Retain `itemDataId(item)` for stable row keys, lookup context, inline save,
  delete, and detail navigation.
- Correct the empty-row colspan after removing the visible column.

## Changed Files

- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T09-47-20Z-child-id-column.md`

## Validation

- `cd frontend && npm test -- --run`
  - 140 tests passed across 8 files.
- `cd frontend && npm run build`
  - Vue TypeScript checking and Vite production build passed.
- `python scripts/check_repo_harness.py` passed.

## Runtime Evidence

- `docker compose build frontend && docker compose up -d --no-deps frontend`
  rebuilt and restarted the Vue frontend successfully.
- `python scripts/runtime_doctor.py` passed all Compose, auth, View, data,
  detail, child, report, message, chart, and Sudoku checks.

## Skipped Or Downgraded Checks

- Authenticated browser interaction remains gated by the current captcha until
  fresh user authorization is received.

## Risks And Follow-Ups

- Browser acceptance must still prove child-row save/delete/detail actions and
  confirm that column headers and empty rows remain aligned without overflow.

# Metadata Field Display

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with the old
  FoolFrame Web pages, using an atomic commit for each change.

## Scope

- Compare `MetadataFieldEditor.vue` with old `detailView.jade` and
  `setextype.js` before editing.
- Restore old Boolean display text (`是` / `否`) while retaining the existing
  legacy string save contract (`true` / `false`).
- Stop rendering the selected BusinessObject id as extra user-facing text;
  keep it as the protocol value behind the lookup display label.
- Use Chinese fallback text for lookup failures without replacing backend error
  messages.

## Changed Files

- `frontend/src/MetadataFieldEditor.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T09-45-37Z-metadata-field-display.md`

## Validation

- `cd frontend && npm test -- --run`
  - 139 tests passed across 8 files.
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

- Browser acceptance must still prove Boolean toggling, BusinessObject search
  selection/display, and saved detail values in desktop and mobile layouts.

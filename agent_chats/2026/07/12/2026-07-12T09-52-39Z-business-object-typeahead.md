# Business Object Typeahead

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with the old
  FoolFrame Web pages, using an atomic commit for each change.

## Scope

- Compare `MetadataFieldEditor.vue` with old `setextype.js` typeahead behavior.
- Replace the manual InputGroup/search-button/Listbox interaction with the
  already-installed PrimeVue AutoComplete.
- Query after one typed character with the component's 300ms delay, show
  View-derived candidate label/id, and restore the old Chinese empty message.
- Keep the selected id as the only emitted save value and retain ViewId,
  object, owner, and added-row request context.
- Remove CSS used only by the deleted manual lookup controls.

## Changed Files

- `frontend/src/MetadataFieldEditor.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T09-52-39Z-business-object-typeahead.md`

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

- Browser acceptance must prove delayed querying, candidate overlay/empty
  state, selection display, saved id, and mobile overlay positioning.

# Map Fallback Copy

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with the old
  FoolFrame Web pages, using an atomic commit for each change.

## Scope

- Compare `LegacyMapPanel.vue` with old `mapview.js`.
- When title metadata is absent, use the first View-derived information item as
  the marker title before falling back to a generic location label.
- Replace fixed English location, map-error, and map-region text with Chinese
  copy while preserving OpenStreetMap attribution and the existing lazy Leaflet
  implementation.

## Changed Files

- `frontend/src/LegacyMapPanel.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T09-49-11Z-map-fallback-copy.md`

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

- Browser acceptance must still prove map rendering, popup titles, empty/error
  states, and responsive framing in desktop and mobile Sudoku layouts.

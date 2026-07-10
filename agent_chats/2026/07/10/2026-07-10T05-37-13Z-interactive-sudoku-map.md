# Interactive Sudoku Map

## Prompt
- Continue the FoolFrame-to-Vue migration, prioritize a genuinely usable
  frontend, maximize existing metadata reuse, and control file size.

## Scope
- Compared old `mapview.js` / `baidumaputil.js` behavior with the current Vue
  Sudoku Map coordinate table.
- Kept the existing `legacyMapMarkers` projection from rendered View row
  `Items` and map `EditType` values `16`, `17`, and `18`.
- Added a focused `LegacyMapPanel.vue` using stable Leaflet 1.9.4 with
  OpenStreetMap tiles, circle markers, information popups, valid-coordinate
  filtering, viewport fitting, and cleanup on unmount.
- Built popup content with DOM `textContent` instead of interpolated HTML.
- Lazy-loaded Leaflet JavaScript and CSS only when a Map panel is rendered.
- Replaced the map table in `SudokuPanels.vue` with the shared map component
  and retained a compact textual location list.

## Changed Files
- `frontend/package.json`
- `frontend/package-lock.json`
- `frontend/src/LegacyMapPanel.vue`
- `frontend/src/SudokuPanels.vue`
- `frontend/src/payload.test.ts`
- `frontend/src/style.css`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/10/2026-07-10T05-37-13Z-interactive-sudoku-map.md`

## Validation
- `cd frontend && npm test -- --run` passed: 7 files, 130 tests.
- `cd frontend && npm run build` passed.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.
- `docker compose build frontend` passed.
- `docker compose up -d --force-recreate --no-deps frontend` replaced the
  running frontend.
- `python scripts/runtime_doctor.py` passed all checks, including the seeded
  Map child View and map-item metadata path through the Vue proxy.

## Runtime Evidence
- Vite emitted the map implementation as separate assets:
  `leaflet-src-7ah4FPQk.js` at 150.12 kB and `leaflet-CIGW-MKW.css` at 15.61 kB.
- The main JavaScript is 138.03 kB; Leaflet is not in the initial chunk.
- `LegacyMapPanel.vue` is 90 lines and `SudokuPanels.vue` dropped from 140 to
  126 lines.

## Risks
- Tile rendering requires network access to the configured OpenStreetMap tile
  server; the component retains coordinates and names when map loading fails.
- Signed-in pixel and marker-click browser inspection still requires explicit
  approval to submit the local captcha-backed Docker admin login.

## Follow-ups
- Complete desktop/mobile map rendering and marker-popup inspection after
  captcha approval.
- Recheck the remaining legacy chart visualization against its current meter
  renderer after the map workflow is visually accepted.

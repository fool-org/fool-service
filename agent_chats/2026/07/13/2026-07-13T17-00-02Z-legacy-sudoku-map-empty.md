# Legacy Sudoku Empty Map Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Inspect the old Sudoku Map partial and controller
before changing the Vue empty-marker rendering.

## Scope

- Mount the Sudoku Map before marker data exists.
- Restore the old empty-map center, zoom, and scroll-wheel interaction.
- Preserve View-first marker projection and the shared map renderer.

## Changed Files

- `frontend/src/LegacyMapPanel.vue`
- `frontend/src/SudokuPanels.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T17-00-02Z-legacy-sudoku-map-empty.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/includes/Map.jade` always renders the Map panel,
  update time, and Refresh command.
- `../FoolFrame/src/Web/public/javascripts/app/mapview.js` initializes the map
  before querying rows, uses `116.32,39.94917` at zoom 18, and calls
  `enableScrollWheelZoom()` before adding valid markers.

## Implementation

- Removed only the marker-count condition from the existing Sudoku Map branch.
- Reused `LegacyMapPanel`; its zero-point path now sets the legacy center and
  zoom after mounting the tile layer instead of returning an error sentence.
- Enabled Leaflet wheel zoom through the existing map option.
- Added no DTO, API, component, dependency, or CSS changes.

## Validation

- Full frontend suite: 155 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Docker frontend production build and forced no-dependency recreation passed.
  Deployed image id:
  `sha256:524efba15289a8d0a85986df17efd5fdd71f4881702010fd6f21bf272b3f5335`.
- Authenticated `/view103` rendered one `Customer Map` region with one Zoom in
  and one Zoom out command at both 1440x1000 and 390x844. A real wheel gesture
  changed OpenStreetMap tile zoom from level 19 to 18.
- Focused screenshots show the map, attribution, update time, and Refresh
  command without clipping or overlap.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-sudoku-map-empty/sudoku-map-desktop.png`
- `artifacts/runs/20260714-legacy-sudoku-map-empty/sudoku-map-mobile.png`

## Risks And Follow-Ups

- The seeded Map panel currently returns two valid positions; the zero-marker
  branch is covered by the source contract and frontend tests.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.

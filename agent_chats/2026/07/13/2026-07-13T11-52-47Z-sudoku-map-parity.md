# Sudoku Map Presentation Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically.

## Scope

- Compare old `Map.jade`, `mapview.js`, and `soway.css` against the deployed
  Vue Sudoku map panel.
- Remove the Vue-only visible coordinate list while preserving marker popups.
- Restore the old fixed 200px map height at desktop and mobile widths.

## Changed Files

- `frontend/src/LegacyMapPanel.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T11-52-47Z-sudoku-map-parity.md`

## Legacy Evidence

- `views/includes/Map.jade` renders only `.sw-map` plus the update/refresh
  footer; it has no visible coordinate list.
- `mapview.js` binds title and metadata details to each marker info window.
- `public/stylesheets/soway.css` fixes `.sw-map` at `height:200px`.

## Validation

- Focused `payload.test.ts`: 82 tests passed.
- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Compose frontend build passed with image manifest
  `sha256:3eeef1d2cb57e86a535457e61e4ad897ddf080b82b44b925f33da19a279756f4`.
- Compose frontend/backend smoke requests passed; MySQL and Redis remained
  healthy and `db-migrate` remained `Exited (0)`.
- Authenticated desktop `/view103`: the map measured 200px high, no coordinate
  list rendered, two marker layers remained present, and clicking a marker
  opened `Customer Name Grace Trading / Customer ID: 3002`.
- At 390x844, the map measured 328x200px, `scrollWidth` equaled 390, no
  coordinate list rendered, and browser logs were empty.

## Runtime Evidence

- `artifacts/runs/20260713-sudoku-map-parity/desktop.jpg`
- `artifacts/runs/20260713-sudoku-map-parity/mobile.jpg`

## Risks And Follow-Ups

- Sudoku's chart still uses the shared responsive chart minimum height instead
  of old `.sw-partialchart`'s fixed 200px height; handle that separately so the
  top-level `viewWithChart` page is not regressed.
- Old `Sudoku.js` also equalizes all flow-control heights. Restore that after
  the map/chart intrinsic sizes are aligned, then verify the deployed geometry.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.

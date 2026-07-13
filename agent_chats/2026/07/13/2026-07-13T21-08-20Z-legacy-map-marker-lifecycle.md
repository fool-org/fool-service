# Legacy Map Marker Lifecycle

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically and retaining View-first rendering and code reuse.

## Scope

- Restore markers after asynchronous child View data arrives.
- Restore the old popup boundary for coordinate-only rows.
- Preserve the empty-map Beijing fallback and shared map component.

## Changed Files

- `frontend/src/LegacyMapPanel.vue`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T21-08-20Z-legacy-map-marker-lifecycle.md`

## Legacy Evidence

- `../FoolFrame/src/Web/public/javascripts/app/mapview.js` creates the map at
  Beijing `116.32, 39.94917` and zoom 18 before posting `/data/querylist`.
- Its query callback adds one marker for every row containing longitude and
  latitude, after the asynchronous response arrives.
- It attaches an info window only when `title != undefined || info.length > 0`;
  a row containing only coordinates still receives a marker but no popup.

## Implementation

- `LegacyMapPanel.vue` now initializes one Leaflet marker layer and watches the
  View-derived marker projection. Each update clears and rebuilds that layer,
  so initial empty props no longer suppress later query results.
- Popup binding now follows the old title/information predicate. The Vue-only
  `位置` fallback was removed, while invalid coordinates and empty-map fallback
  behavior remain unchanged. The component is 99 lines.

## Validation

- Full frontend suite: 164 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Frontend Docker build and recreation passed. Deployed image id:
  `sha256:af152899df985589cec0c29a2901af6fc05ad4e0d02c7b377a8dc183db8f50fa`.
- Before this fix, authenticated `/view103` exposed `2 个地图位置` but contained
  zero `.leaflet-interactive` paths. After recreation it contained two paths.
- Clicking the two paths produced exactly one popup each: `Customer Name Grace
  Trading / Customer ID: 3002` and `Customer Name Ada Capital / Customer ID:
  3001`.
- At 390x844 the page retained two paths in a 328x200 map, and document scroll
  width remained 390px with no horizontal overflow.
- Coordinate-only no-popup behavior is covered by the View projection unit and
  the component predicate assertion; both seeded runtime rows contain popup
  metadata, so that boundary was not claimed as live seed coverage.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-map-marker-lifecycle/map-marker-lifecycle-desktop.png`
- `artifacts/runs/20260714-legacy-map-marker-lifecycle/map-marker-lifecycle-mobile.png`

## Risks And Follow-Ups

- Leaflet replaces old Web's Baidu map provider, but the View-driven marker,
  viewport, and popup interaction boundaries now match this legacy slice.
- Complete page parity remains subject to further View-by-View comparison.
- The existing authenticated browser session was reused, so no fresh CAPTCHA
  was generated or read for this frontend-only slice.

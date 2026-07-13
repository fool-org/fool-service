# Legacy Map Popup Layout

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically and retaining View-first rendering and code reuse.

## Scope

- Restore the old map info-window content dimensions.
- Restore two-items-per-row detail grouping.
- Keep the shared safe DOM popup builder and responsive map behavior.

## Changed Files

- `frontend/src/LegacyMapPanel.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T21-15-14Z-legacy-map-popup-layout.md`

## Legacy Evidence

- `../FoolFrame/src/Web/public/javascripts/app/mapview.js` configures each
  `BMap.InfoWindow` with `width: 240` and `height: 100`.
- The same builder starts one paragraph for every even metadata index and
  closes it after every odd index, grouping at most two details per row.
- Title selection remains separate: `EditType=18` supplies the title, or the
  first information item supplies the fallback.

## Implementation

- The existing Leaflet popup now fixes both `minWidth` and `maxWidth` to 240px,
  and its safe DOM content receives a scrollable fixed 100px height.
- Information metadata is sliced in pairs into paragraph rows. Each value still
  enters through `textContent`; no HTML interpolation, DTO branch, dependency,
  or second map component was added. `LegacyMapPanel.vue` is 106 lines.

## Validation

- Full frontend suite: 164 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Frontend Docker build and forced recreation passed. Deployed image id:
  `sha256:21082a67ba68c8c16f9f49e764246e980362a89902152fdb894d9a5f0f58a0cf`.
- Authenticated `/view103` retained two marker paths and one popup after click.
  Browser measurement showed a 241x100 content box after pixel rounding, a
  287px outer popup, one paragraph row, and one detail cell for the seeded
  single-info marker.
- At 390x844 the same popup remained 287px wide within `66..353`, while the map
  remained within `31..359` and document horizontal overflow stayed false.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-map-popup-layout/map-popup-layout-desktop.png`
- `artifacts/runs/20260714-legacy-map-popup-layout/map-popup-layout-mobile.png`

## Risks And Follow-Ups

- The current seeded markers expose one information item each. Pair grouping
  is source-guarded; future seed coverage with three or more info fields would
  strengthen runtime evidence without changing the View-first renderer.
- Complete page parity remains subject to further View-by-View comparison.
- The existing authenticated browser session was reused, so no fresh CAPTCHA
  was generated or read for this frontend-only slice.

# Legacy Static Submenu

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Keep navigation driven by loaded auth/View
metadata rather than hard-coded business routes.

## Scope

- Restore old non-navigable child-menu semantics for `ViewId=0` entries.
- Preserve the shared desktop/mobile navigation path for View-backed children.

## Changed Files

- `frontend/src/LegacyMenuNav.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T18-25-18Z-legacy-static-submenu.md`

## Legacy Evidence

- `../FoolFrame/src/Web/public/javascripts/app/menuinfo.js` wraps a submenu
  label in an anchor only when its `ViewId` is nonzero.
- A zero-View child is appended as plain `li.list-group-item` text and has no
  click handler or disabled command state.

## Implementation

- View-backed children remain buttons that emit the existing shared selection
  event.
- Zero-View children now render as `nav-static-item` text without button
  semantics or an event handler.
- Reused the existing navigation layout selectors for static items instead of
  adding a second menu component or duplicated style block.
- Added source-contract coverage for both branches and removal of the Vue-only
  disabled-child predicate. `LegacyMenuNav.vue` remains 55 lines.

## Validation

- Focused payload contract: 82 tests passed.
- Full frontend suite: 156 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Frontend-only Docker build and forced recreation passed. Deployed image id:
  `sha256:1fa8cdada3061be96e77fe7045e9453be3c211cd0e09aec48cadc2eb2fa16cb2`.
- Authenticated desktop navigation expanded `Views`, exposed an enabled
  `OrderList` child, and routed it to `/view100` with the View page intact.
- Mobile 390x844 navigation retained the same enabled child and had no
  document-level horizontal overflow.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-static-submenu/menu-desktop.png`
- `artifacts/runs/20260714-legacy-static-submenu/menu-mobile.png`

## Risks And Follow-Ups

- Docker currently seeds only a View-backed submenu child, so the exact static
  branch is protected by the old-source contract rather than invented menu
  fixture data. Normal desktop/mobile navigation has runtime coverage.
- The CAPTCHA-backed runtime doctor was not run because the existing
  authenticated browser session covered this frontend-only slice without
  generating a fresh CAPTCHA.

# Legacy Read-Item Empty Copy Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Inspect the old detail/read-item templates before
changing Vue detail initialization copy.

## Scope

- Remove Vue-only status copy before read-item View fields.
- Keep `/itemview:id` View-only and avoid querying an empty business object.
- Preserve detail fields, collection tabs, and existing detail data flows.

## Changed Files

- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T17-06-47Z-legacy-itemview-empty-copy.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/item.jade` renders the read-item View heading,
  fields, and detail tabs directly without an initialization sentence.
- `../FoolFrame/src/Web/views/detailView.jade` is rendered only after the route
  obtains detail data and contains no select-record placeholder.
- `../FoolFrame/src/Web/routes/index.js` keeps `/itemview:id` on
  `getreaditemview`, while `/view:id/:objid` obtains data before rendering
  `detailView`.

## Implementation

- Removed the single detail empty-state block containing
  `已加载视图定义。` / `请从列表选择记录。`.
- Kept schema-only field and collection rendering unchanged.
- Added no query, DTO binding, API, component, dependency, composable, or CSS.

## Validation

- Full frontend suite: 155 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Docker frontend production build and forced no-dependency recreation passed.
  Deployed image id:
  `sha256:1b5159e9b3ed2a17b737a91f0554ab4bde58e4e08f7cb413bf813a641ea0b121`.
- Authenticated `/itemview100` rendered one `OrderList` heading, the View-derived
  `Order ID` field, and one `Items` tab. Both removed sentences had zero matches.
- Focused screenshots at 1440x1000 and 390x844 show the metadata fields and
  collection heading without clipping or overlap.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-itemview-empty-copy/itemview-desktop.png`
- `artifacts/runs/20260714-legacy-itemview-empty-copy/itemview-mobile.png`

## Risks And Follow-Ups

- This slice intentionally keeps `/itemview:id` schema-only instead of adding a
  business-data request; that matches the migrated View-first boundary.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.

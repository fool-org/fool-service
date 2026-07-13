# View With Chart Layout Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically.

## Scope

- Compare old `viewWithChart.jade` and `view.jade` toolbar placement against
  the shared Vue list panel.
- Keep chart-template search controls left aligned while preserving the normal
  list toolbar's right alignment.
- Restore the old full-width Data/Chart tab rule.
- Verify the deployed layout at desktop and 390px widths.

## Changed Files

- `frontend/src/ViewListPanel.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T11-41-33Z-view-with-chart-layout.md`

## Legacy Evidence

- `views/viewWithChart.jade` uses `form.navbar-form` with no right-alignment
  class and follows it with a full `ul.nav.nav-tabs` rule.
- `views/view.jade` alone adds `navbar-right` to the list query form.
- The shared Vue toolbar was always `justify-content: flex-end`, and
  `.view-template-tabs` imposed a Vue-only 320px maximum width.

## Validation

- Focused `payload.test.ts`: 82 tests passed.
- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Compose frontend build passed with image manifest
  `sha256:40b3aa039da727f8a567b08c876eabe1289e84fc789a0b17819704a338a7a29e`.
- Compose frontend/backend smoke requests passed.
- Authenticated desktop `/view100`: the query input moved from `left=936` to
  `left=40`, computed toolbar alignment became `flex-start`, and the tab rule
  expanded from 320px to the 1200px content width.
- Authenticated desktop `/view101`: no chart tabs rendered and the normal list
  toolbar remained `flex-end`, with its query input at the right edge.
- At 390x844, the chart toolbar and tab rule both measured 330px,
  `scrollWidth` equaled 390, switching to `图表` rendered the SVG chart, and
  browser logs were empty.

## Runtime Evidence

- `artifacts/runs/20260713-view-with-chart-layout/desktop.png`
- `artifacts/runs/20260713-view-with-chart-layout/mobile.png`

## Risks And Follow-Ups

- The first mobile full-page capture timed out on the long data table. Geometry
  checks completed, and the final mobile viewport screenshot succeeded after
  switching to the shorter Chart pane.
- The CSS import assertion was removed because this Vitest SSR setup returns an
  empty string for global CSS imports; the Vue modifier class remains covered
  by the source contract, and computed style is covered by browser acceptance.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.

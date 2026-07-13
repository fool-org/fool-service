# Legacy Navigation Tab Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Keep files small and reuse shared code.

## Scope

- Restore Bootstrap `nav-tabs` styling across every migrated legacy tab set.
- Preserve chart, detail, report, and Sudoku tab interactions.

## Changed Files

- `frontend/src/ViewListPanel.vue`
- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/ViewReportPanel.vue`
- `frontend/src/SudokuPanels.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T13-43-12Z-legacy-nav-tabs.md`

## Legacy Evidence

- `viewWithChart.jade`, `detailView.jade`, the report dialog in `view.jade`,
  and `views/includes/Group.jade` all use Bootstrap `.nav-tabs`.
- Bootstrap renders inactive links blue, active tabs white with a `#ddd`
  border and transparent bottom, and uses 10x15px link padding.
- PrimeVue previously rendered a solid blue active block with white text,
  no tab border, 15x18.75px padding, and its own active bar.

## Implementation

- Added one `legacy-tabs` class to the four existing Prime Tabs call sites.
- Added one shared style block for Bootstrap colors, borders, radius, margin,
  type scale, padding, hover state, and active-bar removal.
- No tab component, state, event, or request path was duplicated or changed.

## Validation

- Focused `payload.test.ts`: 82 tests passed.
- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Compose frontend build passed with image id
  `sha256:4258fbb7932f51411ca7de6edf17be158216c526ccbf6523f46bafd93b60c9fb`.
- Compose frontend/backend smoke requests returned HTTP 200; MySQL and Redis
  remained healthy and `db-migrate` remained `Exited (0)`.
- Authenticated `/view100`: active Data was white/`#555`, inactive Chart was
  transparent/`#337ab7`, padding was 10x15px, and the Prime active bar was
  hidden. Clicking Chart switched selected state, hid the table, and rendered
  the chart.
- Authenticated `/view102/1001`, `/view103`, and `/view101` report UI exposed
  the shared class with selected `Items`, `Group Orders`, and `Output` tabs.
- At 390px the report dialog was 358px wide, all three labels fit, and page
  `scrollWidth` stayed 390.

## Runtime Evidence

- `artifacts/runs/20260713-legacy-nav-tabs/data-tab.jpg`
- `artifacts/runs/20260713-legacy-nav-tabs/chart-tab.jpg`
- `artifacts/runs/20260713-legacy-nav-tabs/report-tabs.jpg`
- `artifacts/runs/20260713-legacy-nav-tabs/report-mobile.jpg`

## Risks And Follow-Ups

- PrimeVue focus-visible behavior remains intact for keyboard accessibility;
  only the legacy resting, hover, and selected presentation was restored.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.

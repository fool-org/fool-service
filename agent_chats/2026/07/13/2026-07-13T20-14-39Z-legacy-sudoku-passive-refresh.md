# Legacy Sudoku Passive Refresh

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically and retaining View-first rendering and code reuse.

## Scope

- Align Sudoku List, linechart, and Map footer command availability.
- Preserve linechart timer behavior while restoring Map's one-shot lifecycle.

## Changed Files

- `frontend/src/SudokuPanels.vue`
- `frontend/src/useSudokuPanels.ts`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T20-14-39Z-legacy-sudoku-passive-refresh.md`

## Legacy Evidence

- `includes/List.jade` renders `刷新` with `ng-click="query()"` and its
  controller updates `querytime` from list `FreshTime`.
- `includes/linechart.jade` and `includes/Map.jade` render the same text as
  anchors without `ng-click` or `href`.
- Neither linechart nor Map assigns `querytime`, so both retain the literal
  `更新时间` label with a blank value.
- `LineChartController` registers its detail refresh through `timer`; the Map
  controller performs one list request during map initialization and does not
  register a timer.

## Implementation

- Kept one shared Sudoku footer but split footer presence from manual refresh
  availability. List panels retain the existing command; linechart and Map use
  one styled passive anchor without an event or `href`.
- Always rendered the old update-time label. Only List/Group List resolve a
  list `FreshTime`, preventing shared ViewId state from leaking a list time into
  linechart or Map.
- Excluded Map from composable refresh scheduling while retaining list timers
  and linechart's detail `AutoFreshTime` path.

## Validation

- Full frontend suite: 157 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Frontend Docker build and forced recreation passed. Deployed image id:
  `sha256:7ac789f2980deda737fafd63c74e1b4b0abf5a766942e764c58455a07d8628d1`.
- Authenticated `/view103` exposed exactly two real Refresh buttons for Orders
  List and Group Orders. Price Chart and Customer Map each exposed one passive
  `A` element without `href` or a link/button accessibility role.
- Clicking Price Chart's passive text left its rolling chart tail unchanged at
  one `0.25 / 62500` sample.
- At 390x844 the passive footer and panel were 328px and 330px wide,
  respectively, and the document scroll width remained exactly 390px.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-sudoku-passive-refresh/sudoku-passive-refresh-desktop.png`
- `artifacts/runs/20260714-legacy-sudoku-passive-refresh/sudoku-passive-refresh-mobile.png`

## Risks And Follow-Ups

- Automatic timer registration is covered by the workflow source contract;
  the browser slice verifies the rendered command boundary and passive click.
- Complete page parity remains subject to further View-by-View comparison.
- The existing authenticated browser session was reused, so no fresh CAPTCHA
  was generated or read for this frontend-only slice.

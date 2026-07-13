# Legacy Chart Line Labels

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically while retaining View-first rendering and code reuse.

## Scope

- Resolve the old `symbol: 'none'` plus `label.normal.show` behavior from
  ECharts 3.1.7 source rather than assuming all configured labels are visible.
- Remove only invented line value labels while retaining bar/scatter labels,
  axis tooltip values, shared geometry, and both chart entrypoints.
- Correct the migration/task wording that previously treated line labels as an
  old visible behavior.

## Changed Files

- `frontend/src/LegacyChartPanel.vue`
- `frontend/src/style.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T23-04-31Z-legacy-chart-line-labels.md`

## Legacy Evidence

- Both old `swchartLine.js` chart paths configure line series with
  `symbol: 'none'` and `label.normal.show: true`.
- ECharts 3.1.7 `visual/symbol.js` propagates the configured `none` symbol to
  series data.
- `SymbolDraw.js` refuses to create a Symbol when that visual is `none`.
- `Symbol.js` is the line path that attaches label text, while `LineView.js`
  contains no independent label rendering. Therefore those configured line
  labels never become visible graphics.
- Deployed Vue baselines were eight bar plus eight line labels in `/view100`
  and 100 bar plus 100 line labels in compact `/view103`.

## Implementation

- Added one template guard so shared value text renders only when the series is
  not line. Bar and scatter use the existing label geometry unchanged.
- Corrected the earlier task/parity wording from all metadata-series labels to
  bar/scatter labels. No state, helper, component, dependency, DTO branch,
  request, geometry path, or duplicate renderer was added.

## Validation

- Focused `npm test -- style.test.ts payload.test.ts`: 89 tests passed.
- Full `npm test`: 13 files and 171 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- `python scripts/check_repo_harness.py` and `git diff --check` passed.
- Frontend Docker build and forced recreation passed. Deployed image id:
  `sha256:de48b7558b78fcc68f638500480fcc1fdcd98ebb94b1d9d17cf4d09a57d86455`.
- Backend `/test` and Compose checks passed; MySQL/Redis were healthy and
  `db-migrate` remained `Exited (0)`.
- Authenticated desktop `/view100` changed from 16 to eight value labels: all
  eight bar labels remained and all eight line labels disappeared. Its chart
  stayed 414px with no overflow and area opacity remained 0.7.
- Real pointer movement still exposed `1002 / Amount: 1.5 / Price: 3,450` in
  the axis tooltip, proving line data remained interactive.
- At 390x844, `/view100` retained eight bar labels, zero line labels, a 470px
  chart, two category labels, and no overflow.
- Mobile `/view103` retained 100 bar labels, zero line labels, two category
  labels, and a contained 328x200 compact chart. Browser warning/error logs
  were empty.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-chart-line-labels/order-chart-line-labels-desktop.png`
- `artifacts/runs/20260714-legacy-chart-line-labels/order-chart-line-labels-mobile.png`
- `artifacts/runs/20260714-legacy-chart-line-labels/sudoku-line-labels-mobile.png`

## Skipped Checks And Risks

- The old FoolFrame app was not separately booted; behavior was resolved from
  the exact bundled ECharts 3.1.7 symbol/label creation chain and verified
  against the migrated runtime counts.
- The existing authenticated session was reused, so no fresh CAPTCHA was
  generated or read for this frontend-only slice.

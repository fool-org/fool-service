# Legacy Chart Axis Style

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically and retaining View-first rendering and code reuse.

## Scope

- Compare the shared Vue chart axes with the exact defaults used by the old
  ECharts dependency.
- Restore the old axis line, label, and split-line presentation across the
  top-level chart tab and Sudoku compact chart.
- Make the CSS contract test inspect real stylesheet content without adding a
  production dependency or renderer branch.

## Changed Files

- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `frontend/src/style.test.ts`
- `frontend/vite.config.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T22-02-09Z-legacy-chart-axis-style.md`

## Legacy Evidence

- `../FoolFrame/src/Web/package.json` declares ECharts `^3.1.7`.
- ECharts 3.1.7 `src/coord/axisDefault.js` defines axis lines as `#333` at
  width 1, axis labels as `#333` at 12px, and split lines as `#ccc` at width 1.
- Neither old `swchartLine.js` option branch overrides those defaults.

## Implementation

- Replaced only the shared chart axis selectors: grid lines now use `#ccc`,
  while axis lines and text use `#333`; labels use the legacy 12px size.
- Moved the existing chart-surface assertions out of the oversized payload
  contract and added the exact axis assertions in focused `style.test.ts`.
- Enabled Vitest CSS processing only for `style.css`; the prior raw import was
  an empty string under Vitest defaults, so earlier frame assertions were
  vacuous. No component, DTO, API request, or chart geometry was changed.

## Validation

- Full frontend suite: 165 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- `python scripts/check_repo_harness.py` and `git diff --check` passed.
- Frontend Docker build and forced recreation passed. Deployed image id:
  `sha256:b9a5e840160bacffb814517a3638aa71afbbf84da013aef1de767fbe4a1bb736`.
- Backend `/test` and Compose checks passed; MySQL/Redis were healthy and
  `db-migrate` remained `Exited (0)`.
- Authenticated `/view100` computed split lines as `rgb(204, 204, 204)`/1px,
  axis lines as `rgb(51, 51, 51)`/1px, and labels as
  `rgb(51, 51, 51)`/12px. Its chart remained 414px on desktop and 470px at
  390x844; eight desktop and two mobile labels had no overlap.
- Authenticated `/view103` computed the same axis styles at 390x844 and
  retained its 328x200 compact chart with two non-overlapping labels. Browser
  warnings/errors were empty and document overflow was false on all checks.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-chart-axis-style/order-chart-desktop.png`
- `artifacts/runs/20260714-legacy-chart-axis-style/order-chart-mobile.png`

## Risks And Follow-Ups

- This slice restores ECharts defaults only; it does not claim parity for all
  remaining chart or non-chart interactions.
- The existing authenticated browser session was reused, so no fresh CAPTCHA
  was generated or read for this frontend-only slice.

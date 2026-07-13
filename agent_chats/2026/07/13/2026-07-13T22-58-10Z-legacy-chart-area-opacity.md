# Legacy Chart Area Opacity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically while retaining View-first rendering and code reuse.

## Scope

- Trace the old line-area opacity through both `swchartLine.js` branches and
  ECharts 3.1.7 rendering defaults.
- Restore that exact shared presentation without changing series data,
  geometry, legend behavior, components, or dependencies.
- Verify the deployed computed style and responsive chart contracts.

## Changed Files

- `frontend/src/style.css`
- `frontend/src/style.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T22-58-10Z-legacy-chart-area-opacity.md`

## Legacy Evidence

- Both old `swchartLine.js` chart paths set `areaStyle.normal` to an empty
  object for line metadata, so the area is enabled without an opacity override.
- ECharts 3.1.7 `LineView.js` assigns the enabled polygon `style.opacity = 0.7`
  before applying the empty area style and View-series visual color.
- The deployed Vue baseline had one Price `.chart-line-area` with the correct
  `#2f4554` fill but computed opacity 0.2.

## Implementation

- Changed the existing shared `.chart-line-area` opacity from 0.2 to 0.7.
- Added one focused CSS contract test. No component, state, helper, dependency,
  DTO branch, request, geometry path, or duplicate renderer was added.

## Validation

- Focused `npm test -- style.test.ts`: 6 tests passed.
- Full `npm test`: 13 files and 170 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- `python scripts/check_repo_harness.py` and `git diff --check` passed.
- Frontend Docker build and forced recreation passed. Deployed image id:
  `sha256:e789b97a63c1d92a54e686b4684562d6f1645f3af57d38580404a3293d799945`.
- Backend `/test` and Compose checks passed; MySQL/Redis were healthy and
  `db-migrate` remained `Exited (0)`.
- Container inspection found `.chart-line-area{opacity:.7}` in
  `index-Ct6fVG-u.css`. After a cache-busting navigation, the browser loaded
  that same asset and computed the Price area as opacity 0.7 with fill
  `#2f4554`.
- Desktop `/view100` retained a 414px chart, eight category labels, and no
  overflow. At 390x844 it retained 470px, two labels, and no overflow.
- Price legend hide/show still removed and restored the area, with the Amount
  scale switching to 2.5 while Price was hidden. Browser warning/error logs
  were empty.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-chart-area-opacity/order-chart-area-desktop.png`
- `artifacts/runs/20260714-legacy-chart-area-opacity/order-chart-area-mobile.png`

## Skipped Checks And Risks

- The in-app browser initially reused the previous hashed stylesheet on the
  same URL. Runtime verification therefore used a query-string cache miss and
  explicitly confirmed the loaded CSS hash and computed style; the deployed
  container itself already contained only the new hashed main stylesheet.
- The existing authenticated session was reused, so no fresh CAPTCHA was
  generated or read for this frontend-only slice.

# Legacy Sudoku Unknown Partial Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Inspect the old Sudoku ViewFile dispatch before
changing the final Vue panel fallback.

## Scope

- Keep unknown Sudoku child partial content empty.
- Preserve all five explicit partial renderers and View-first loading.

## Changed Files

- `frontend/src/SudokuPanels.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T17-12-56Z-legacy-sudoku-unknown-empty.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/Sudoku.jade` dispatches child `ViewFile` through
  a five-case `List`, `Group`, `Map`, `Item`, and `linechart` switch.
- The mixin has no default case, so an unmatched child keeps only its outer
  column and appends no data or status copy.

## Implementation

- Removed the final generic `暂无数据。` branch from `SudokuPanels`.
- Kept the existing unknown-kind panel boundary and all explicit renderers.
- Added no query, DTO binding, API, component, dependency, composable, or CSS.

## Validation

- Full frontend suite: 155 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Docker frontend production build and forced no-dependency recreation passed.
  Deployed image id:
  `sha256:ec9bebad32ac1367f6f7932c09568361f73ef680a4a34451fac9ce684d7d316e`.
- Authenticated `/view103` rendered exactly one title for each supported root
  panel: `Orders List`, `Price Chart`, `Customer Map`, `Order Item`, and
  `Order Group`; `暂无数据。` had zero matches.
- Focused screenshots at 1440x1000 and 390x844 show the supported panel flow
  without clipping or overlap.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-sudoku-unknown-empty/sudoku-supported-desktop.png`
- `artifacts/runs/20260714-legacy-sudoku-unknown-empty/sudoku-supported-mobile.png`

## Risks And Follow-Ups

- Docker metadata contains only supported partial names. The unknown-partial
  branch is covered by the old template contract and frontend source test; the
  browser run proves only supported-panel non-regression.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.

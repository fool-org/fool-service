# Sudoku Equal Panel Height Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically.

## Scope

- Compare old `Sudoku.js` flow-control sizing against the deployed Vue grid.
- Apply one maximum height to every panel only after root and Group-list View
  data is ready.
- Keep the locked height stable across Group tab changes and panel refreshes.

## Changed Files

- `frontend/src/SudokuPanels.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T12-14-05Z-sudoku-equal-panels.md`

## Legacy Evidence

- `public/javascripts/app/Sudoku.js` reads every `.sw-flowcontrol` height,
  finds one global maximum, and writes that height back to every control.
- The old code runs this sizing pass once through `domReady`; it does not
  remeasure when Group tabs change or child data refreshes.
- A CSS-only `1fr` prototype equalized the Vue panels but changed all heights
  from 458px to 375px when `Group Detail` was selected, so it was discarded.

## Validation

- Focused `payload.test.ts`: 82 tests passed.
- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Compose frontend build passed with image manifest
  `sha256:790964d8377c4a796066790b22fd6c5e25703403f685b26c87a685b286422a75`.
- Compose frontend/backend smoke requests returned HTTP 200; MySQL and Redis
  remained healthy and `db-migrate` remained `Exited (0)`.
- Authenticated desktop `/view103`: the five natural panel heights were
  375/295/295/339/458px before locking; all five then measured 458.148px.
  Switching to `Group Detail` kept the same inline `grid-auto-rows` value and
  all five heights unchanged.
- A fresh 390x844 page entry locked all five rows at 642.242px, retained that
  value after the same Group tab switch, and kept page `scrollWidth` equal to
  390px.
- Browser logs were empty.

## Runtime Evidence

- `artifacts/runs/20260713-sudoku-equal-panels/desktop.jpg`
- `artifacts/runs/20260713-sudoku-equal-panels/mobile.jpg`

## Risks And Follow-Ups

- The Vue pass waits for the View-derived panel results instead of measuring
  empty Angular shells at DOM ready. This preserves the old one-time equalizing
  interaction without reproducing its late-data overlap bug.
- Like the old page, the height is not recomputed for in-page viewport resize;
  re-entering or reloading the Sudoku View computes a new viewport-specific
  maximum.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.

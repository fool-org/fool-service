# Legacy Sudoku Simple Item

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically and retaining View-first rendering and code reuse.

## Scope

- Restore the Group simple-child text presentation.
- Preserve Group tab selection and the View-derived legacy string.
- Remove inappropriate Vue empty-state geometry from this branch.

## Changed Files

- `frontend/src/SudokuPanels.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T21-31-21Z-legacy-sudoku-simple-item.md`

## Legacy Evidence

- `../FoolFrame/src/Web/public/javascripts/app/groupview.js` creates an empty
  child `.panel` for `ListViewType=1`, then directly appends `这是简单项`.
- The old branch adds no empty-state container, centering, fixed minimum height,
  muted color, request, or command.

## Implementation

- Replaced `empty-state compact` with `sudoku-simple-item` on the existing
  simple-child branch. The focused class only restores Bootstrap body color,
  14px type, and 20px line height.
- Group metadata, tab state, child loading, and the exact legacy text remain
  unchanged. `SudokuPanels.vue` remains 189 lines.

## Validation

- Full frontend suite: 164 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Frontend Docker build and forced recreation passed. Deployed image id:
  `sha256:ab76779f511250c7cc80e7152beb4204c591f7f5c5298f4eef9693c619427946`.
- Authenticated `/view103` switched from Group Orders to Group Detail and kept
  `Group Detail` selected. `这是简单项` rendered as a 20px-high block at the
  tab-panel's x=58 origin, using 14px/20px and `rgb(51, 51, 51)`. The Group
  contained zero `.empty-state` elements and the page had no overflow.
- At 390x844 the same selected tab rendered the line within x=48..342 at 20px
  height, with no document horizontal overflow.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-sudoku-simple-item/sudoku-simple-item-desktop.png`
- `artifacts/runs/20260714-legacy-sudoku-simple-item/sudoku-simple-item-mobile.png`

## Risks And Follow-Ups

- This intentionally preserves the old placeholder string because it is the
  explicit `ListViewType=1` branch, not a generic empty-data message.
- Complete page parity remains subject to further View-by-View comparison.
- The existing authenticated browser session was reused, so no fresh CAPTCHA
  was generated or read for this frontend-only slice.

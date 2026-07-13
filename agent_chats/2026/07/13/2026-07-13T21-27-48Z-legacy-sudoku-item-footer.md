# Legacy Sudoku Item Footer

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically and retaining View-first rendering and code reuse.

## Scope

- Restore the Item partial's update/refresh footer.
- Keep its Refresh text passive and preserve existing active refresh commands.
- Reuse the shared Sudoku footer without new state or markup branches.

## Changed Files

- `frontend/src/SudokuPanels.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T21-27-48Z-legacy-sudoku-item-footer.md`

## Legacy Evidence

- `../FoolFrame/src/Web/public/javascripts/app/subitem.js` appends
  `更新时间`, an empty `querytime` span, and a `刷新` anchor after the Item table.
- That anchor has no href or click handler. `GetItemController.refresh` is also
  an empty function, so Item does not expose an executable refresh command.

## Implementation

- Added `item` to `sudokuPanelHasFooter`. The existing footer already leaves
  non-List time blank and renders non-manual refresh as an inert anchor.
- No data workflow, timer, event, CSS, component, or DTO branch was added.
  `SudokuPanels.vue` remains 189 lines.

## Validation

- Full frontend suite: 164 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Frontend Docker build and forced recreation passed. Deployed image id:
  `sha256:cfc99d89792c7a83bfca891d24cc2651c5a2c952d7bafab9cfa2cdb662768b4c`.
- Authenticated `/view103` rendered one Item footer with text
  `更新时间 刷新`, zero buttons, and one anchor without an href. The page retained
  exactly two executable `刷新` buttons for List and Group List and now exposed
  three passive refresh anchors for linechart, Map, and Item.
- At 390x844 the Item panel remained within x=30..360, its footer ended at
  x=359, and document horizontal overflow was false.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-sudoku-item-footer/sudoku-item-footer-desktop.png`
- `artifacts/runs/20260714-legacy-sudoku-item-footer/sudoku-item-footer-mobile.png`

## Risks And Follow-Ups

- The blank update time matches old `subitem.js`, which never assigns
  `querytime` in this controller.
- Complete page parity remains subject to further View-by-View comparison.
- The existing authenticated browser session was reused, so no fresh CAPTCHA
  was generated or read for this frontend-only slice.

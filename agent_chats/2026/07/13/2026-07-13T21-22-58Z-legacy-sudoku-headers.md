# Legacy Sudoku Headers

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically and retaining View-first rendering and code reuse.

## Scope

- Restore the header contract for each old Sudoku include.
- Remove the Vue-only Group parent title while retaining child tab state.
- Restore inert `详细` text for Item and Group List presentations.

## Changed Files

- `frontend/src/SudokuPanels.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T21-22-58Z-legacy-sudoku-headers.md`

## Legacy Evidence

- `views/includes/List.jade`, `Map.jade`, and `linechart.jade` wrap each View
  name in one active Bootstrap `nav-tabs` entry.
- `views/includes/Item.jade` uses a gray `panel-heading`, a normal panel title,
  and an inert `详细` anchor at the right.
- `views/includes/Group.jade` has no parent View-name heading. `groupview.js`
  builds child tabs and adds an inert `详细` anchor to each List child's footer.

## Implementation

- The existing `Panel` loop now adds one class derived from `ViewFile` kind.
  CSS uses that class to render static active tabs, the Item heading, or hide
  the Group parent header without branching data or duplicating components.
- One shared inert-detail class serves Item and Group List. The panel shell now
  uses Bootstrap's 4px radius, `#ddd` border, and light panel shadow.
  `SudokuPanels.vue` remains 189 lines.

## Validation

- Full frontend suite: 164 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Frontend Docker build and forced recreation passed. Deployed image id:
  `sha256:f92062a57025f34ac9852b0caf1f5952cc2660e4322dad608d0f700f643ff733`.
- Authenticated `/view103` rendered List, linechart, and Map headers at 14px,
  normal weight, 10x15px padding, and 4px top radii. Item rendered a 16px gray
  header with `详细`; Group's parent header had `display:none` while both child
  tabs remained visible. Exactly two inert `详细` texts were visible.
- At 390x844 all five panel shells measured 330px within `30..360`; the same
  four outer headers and two Detail texts remained visible, the Group parent
  stayed hidden, and document horizontal overflow was false.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-sudoku-headers/sudoku-headers-desktop.png`
- `artifacts/runs/20260714-legacy-sudoku-headers/sudoku-headers-mobile-top.png`
- `artifacts/runs/20260714-legacy-sudoku-headers/sudoku-headers-mobile-item.png`
- `artifacts/runs/20260714-legacy-sudoku-headers/sudoku-headers-mobile-group.png`

## Risks And Follow-Ups

- The Detail text remains intentionally inert because neither old template
  attaches an href or click handler.
- Complete page parity remains subject to further View-by-View comparison.
- The existing authenticated browser session was reused, so no fresh CAPTCHA
  was generated or read for this frontend-only slice.

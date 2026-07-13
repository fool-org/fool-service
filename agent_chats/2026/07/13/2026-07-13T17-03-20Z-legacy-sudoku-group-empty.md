# Legacy Sudoku Empty Group Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Inspect the old Sudoku Group partial and controller
before changing the Vue empty-child rendering.

## Scope

- Keep a Group body present when the loaded child View has no Items.
- Keep unknown child-type tab panels empty instead of adding Vue copy.
- Preserve list/simple child rendering and View-first child loading.

## Changed Files

- `frontend/src/SudokuPanels.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T17-03-20Z-legacy-sudoku-group-empty.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/includes/Group.jade` always renders empty nav and
  tab-content containers before Group data loads.
- `../FoolFrame/src/Web/public/javascripts/app/groupview.js` appends child tabs
  from View `Items`; zero Items append no message, `ListViewType=1` appends
  `这是简单项`, and unknown child types append no tab-panel content.

## Implementation

- Matched the Group branch by ViewFile kind without requiring child Items.
- Mounted the existing Tabs component only when metadata children exist, so an
  empty Group retains a blank body without indexing a missing first child.
- Removed only the unknown-child Vue empty-state sentence.
- Added no DTO, API, component, dependency, composable, or CSS changes.

## Validation

- Full frontend suite: 155 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Docker frontend production build and forced no-dependency recreation passed.
  Deployed image id:
  `sha256:0288b077dd776ff88e68676dea576577b608e85b8b2f2e612203caf5dc06987d`.
- Authenticated `/view103` rendered one `Group Orders` and one `Group Detail`
  tab. Selecting `Group Detail` showed the legacy `这是简单项` content, and no
  `暂无数据。` text existed on the page.
- Focused screenshots at 1440x1000 and 390x844 show the selected tab and simple
  content without clipping or overlap.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-sudoku-group-empty/sudoku-group-desktop.png`
- `artifacts/runs/20260714-legacy-sudoku-group-empty/sudoku-group-mobile.png`

## Risks And Follow-Ups

- The seeded Group currently exposes one list and one simple child; zero-child
  and unknown-child branches are covered by the old-source contract and
  frontend tests.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.

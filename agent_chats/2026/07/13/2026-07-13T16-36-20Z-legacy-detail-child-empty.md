# Legacy Empty Detail Collection Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Inspect the old detail View first and keep child
rows bound through the existing metadata-derived group helpers.

## Scope

- Restore the old heading-only child table after all visible rows are removed.
- Do not render an invented empty collection panel when the View defines no
  child groups.

## Changed Files

- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T16-36-20Z-legacy-detail-child-empty.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/detailView.jade` renders child tabs and each
  collection table directly from `view.Data.Items`.
- The table body iterates child rows and defines no `暂无子项。` row or fallback
  panel; an empty defined collection therefore leaves its tab and heading.

## Implementation

- Guarded the child collection panel with the presence of View-defined child
  groups before checking the selected/schema-only detail state.
- Removed the redundant inner tab guard and both Vue-only empty-state paths.
- Kept row rendering on `groupItems(group)` and retained staged deletion,
  metadata columns, child operations, and parent-save behavior unchanged.
- Added source contracts for the panel guard, heading-only table, and removed
  copy. Added no DTO, API, state, component, or CSS changes.

## Validation

- Focused source-contract suite: 82 tests passed.
- Full frontend suite: 155 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Docker frontend production build and forced no-dependency recreation passed.
  Deployed image id:
  `sha256:0b13c702e827bf5f1b1dc119089387d2c2894ad44ba9e4c59cc75070072f77db`.
- Authenticated `/view102/1001` loaded three child rows. Staging each Delete
  reduced the visible button count from 3 to 2 to 1; after the third action the
  child area retained one table with one heading row, no dialog, and no
  `暂无子项。` copy. The parent Save command was not used, so the staged runtime
  check did not persist child deletion.
- Desktop and 390x844 screenshots show the heading-only child table without
  overlap. Repository harness validation passed; frontend `/` and backend
  `/test` returned HTTP 200. MySQL and Redis remained healthy and `db-migrate`
  remained `Exited (0)`.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-detail-child-empty/child-empty-desktop.png`
- `artifacts/runs/20260714-legacy-detail-child-empty/child-empty-mobile.png`

## Risks And Follow-Ups

- Runtime deletion was intentionally left as unsaved client state; persistence
  behavior was outside this rendering-only slice.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.

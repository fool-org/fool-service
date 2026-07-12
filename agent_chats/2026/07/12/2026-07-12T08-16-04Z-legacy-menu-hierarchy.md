# Legacy Menu Hierarchy

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with the old
  FoolFrame Web pages, using an atomic commit for each change.

## Scope

- Keep top-level `getmain` menu entries visible after loading a submenu.
- Expand `getsubmenu` entries beneath their selected parent on desktop and
  mobile.
- Keep the mobile navigation Drawer open while expanding a parent and close it
  only when opening a concrete View.
- Reuse one menu renderer for desktop and mobile navigation.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/LegacyMenuNav.vue`
- `frontend/src/payload.test.ts`
- `frontend/src/style.css`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T08-16-04Z-legacy-menu-hierarchy.md`

## Validation

- `cd frontend && npm test && npm run build`
  - 130 tests passed.
  - Vue TypeScript checking passed.
  - Vite production build passed.
- `git diff --check` passed.

## Runtime Evidence

- No browser artifact was claimed for nested-menu behavior because the default
  Docker seed may not expose a parent menu without a direct View.

## Risks And Follow-Ups

- The final authenticated browser acceptance must exercise a real parent and
  child menu on desktop and 390x844 mobile layouts.
- Detail view/edit/save interaction parity is the next independent slice.

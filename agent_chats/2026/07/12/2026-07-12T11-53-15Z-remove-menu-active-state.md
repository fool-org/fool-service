# Remove Invented Menu Active State

## Prompt

Continue aligning the Vue page with FoolFrame layout, style, and interaction,
with every behavior change committed atomically.

## Scope

- Trace old top and child menu state from `tbar.jade` through `menuinfo.js` and
  Bootstrap dropdown behavior.
- Remove Vue's route-derived top/child `active` state.
- Preserve the dropdown-open equivalent and all View navigation behavior.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/LegacyMenuNav.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T11-53-15Z-remove-menu-active-state.md`

## Validation

- `cd frontend && npm test -- --run`: 9 files, 147 tests passed.
- `cd frontend && npm run build`: TypeScript and Vite production build passed;
  the main application chunk decreased from 257.54 kB to 257.36 kB.
- Source scan confirms `LegacyMenuNav.vue` contains neither `currentViewId` nor
  an `active` class binding.
- `docker compose build frontend`: passed.
- `docker compose up -d --no-deps frontend`: rebuilt frontend deployed on port
  8081.
- `python scripts/runtime_doctor.py`: all Compose, auth, menu, View/data,
  report, message, and logout checks passed.
- `python scripts/check_repo_harness.py`: passed.
- `docker compose ps -a`: backend/frontend are up, MySQL/Redis are healthy, and
  `db-migrate` is `Exited (0)`.

## Source Evidence

- `../FoolFrame/src/Web/views/includes/tbar.jade` does not mark a menu item
  active from the current View.
- `../FoolFrame/src/Web/public/javascripts/app/menuinfo.js` either navigates to
  a View or loads children; it does not derive active state from the route.
- Bootstrap applies `.open` only while the dropdown is expanded.

## Risks And Follow-Ups

- Bootstrap colors and spacing remain a separate atomic parity slice.

# Vue Auto Load View

## Prompt

- Continue the Docker/FoolFrame/Vue migration.
- Make concrete progress toward a usable Vue frontend without binding the page
  to business DTOs.

## Scope

- Reused the existing metadata-driven `loadViewWorkflow()` path.
- Added a Vue `onMounted` call so the default `OrderList` view loads when the
  page opens.
- Left manual `Load View` in place for changing or refreshing the selected
  View.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/04/2026-07-03T17-39-06Z-vue-auto-load-view.md`

## Validation

- `cd frontend && npm test`
  - Passed: 2 test files, 41 tests.
- `cd frontend && npm run build`
  - Passed: `vue-tsc --noEmit` and Vite production build.
- `docker compose build --quiet frontend`
  - Passed.
- `docker compose up -d --no-deps frontend`
  - Passed; frontend container was recreated.

## Runtime Evidence

- Browser target: `http://localhost:8081/`.
- Browser plugin was available. `domSnapshot()` failed with
  `incrementalAriaSnapshot is not a function`, so verification used the same
  Browser session through read-only DOM `evaluate`, console logs, screenshots,
  and scoped Playwright interactions.
- After reload, the first screen rendered an `OrderList` table immediately
  without pressing `Load View`.
- Detail panel selected the first row and rendered metadata fields.
- Customer lookup still worked after the change:
  - search term `Grace`
  - candidate `Grace Trading`
  - selected draft value `3002`
- Browser console warnings/errors:
  - none reported by `tab.dev.logs({ levels: ["error", "warn"], limit: 50 })`.

## Skipped Checks

- Backend tests were not rerun; this slice only changes the Vue mount behavior
  and a frontend source assertion.

## Risks

- Auto-loading means the app makes backend requests on page open. This is
  intended for the current default View workflow; defer lazy loading again only
  if the first screen becomes user-selectable across many apps/databases.

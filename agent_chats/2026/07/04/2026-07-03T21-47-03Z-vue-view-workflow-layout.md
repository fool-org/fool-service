# Vue View Workflow Layout

## Prompt

- Continue the Docker/Vue FoolFrame migration.
- Frontend should be usable, not just a collection of components.
- Keep the workflow View-first: render the page from loaded View metadata,
  query data from that View, and avoid binding to concrete business DTOs.
- Keep the change small and commit atomically.

## Scope

- Checked the Vue main workflow in the browser at `http://localhost:8081`.
- Fixed the CSS grid stretch that pushed the View toolbar and table down the
  page.
- Added a minimum width for the main View table so narrow screens scroll the
  table instead of crushing action buttons.

## Changed Files

- `frontend/src/style.css`
- `tasks.md`
- `agent_chats/2026/07/04/2026-07-03T21-47-03Z-vue-view-workflow-layout.md`

## Validation

- `cd frontend && npm test`
  - Passed: 47 tests.
- `cd frontend && npm run build`
  - Passed: `vue-tsc --noEmit` and Vite build.
- `docker compose up -d --no-deps --build frontend`
  - Passed and recreated `fool-service-frontend-1`.
- `python3 scripts/runtime_doctor.py`
  - Passed backend `/test`, `view/getlistview`, `data/querydata`,
    `data/inputquery`, and `report/getmkqview`.

## Browser Evidence

- Browser path: in-app Browser was available.
- Browser limitation: `domSnapshot()` failed with an
  `incrementalAriaSnapshot` compatibility error, so rendered checks used
  Browser evaluate, console logs, locator clicks, viewport override, and
  screenshots.
- Desktop `1280x720` at `http://localhost:8081/`:
  - Page title `Fool Service`, H1 `Order List`.
  - 8 View rows rendered, 7 detail fields rendered, no app error message, no
    Vite/framework overlay, no console error or warning.
  - Clicking the second `Open` button changed selected detail object from
    `1783093577687` to `9851343`.
- Mobile `390x844`:
  - 8 View rows rendered, no app error message, no Vite/framework overlay, no
    console error or warning.
  - Main table scrolls horizontally with `scrollWidth=640` and
    `clientWidth=326`; the `Open` button remains a normal button instead of
    vertical wrapped text.
- Screenshots saved outside the repo:
  - `/tmp/fool-service-vue-desktop.png`
  - `/tmp/fool-service-vue-mobile.png`

## Skipped Checks And Risks

- Full backend `mvn test` was not rerun because this slice only changes Vue
  CSS and task/evidence docs.

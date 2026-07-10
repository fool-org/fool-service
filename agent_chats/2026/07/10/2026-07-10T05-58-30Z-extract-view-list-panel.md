# Extract View List Panel

## Prompt

Continue the Docker/FoolFrame/Vue migration while controlling file size and
maximizing reuse.

## Scope

- Move list View presentation from `App.vue` into `ViewListPanel.vue`.
- Reuse the existing `viewWorkflow` metadata projections for columns, rows,
  operations, paging, chart data, and Sudoku panel metadata.
- Keep API requests, selected object state, detail state, and mutations in the
  existing `App.vue` / `useViewDataWorkflow` path.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/ViewListPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/10/2026-07-10T05-58-30Z-extract-view-list-panel.md`

## Result

- `App.vue`: 1108 to 1048 lines.
- `ViewListPanel.vue`: 134 lines.
- The new component receives loaded View/Data metadata and emits user actions;
  it does not own or duplicate backend requests or business DTO state.
- Production JS remains 142.43 kB gzip 48.84 kB, with Leaflet still isolated
  in its lazy 150.12 kB chunk.

## Validation

- `cd frontend && npm test`: 130 tests passed.
- `cd frontend && npm run build`: TypeScript and Vite production build passed.
- `docker compose up -d --build frontend`: frontend and Java 17 backend images
  built successfully; the deployed frontend was force-recreated from the new
  image.
- `python scripts/runtime_doctor.py`: all Docker, auth, View, data, report,
  message, and legacy route checks passed.
- In-app browser desktop login page rendered without console warnings/errors.
- In-app browser 390x844 login page had `scrollWidth=390`; the form stayed
  within x=14..376 at width 362.
- Browser artifacts:
  `artifacts/browser/2026-07-10-view-list-panel/login-desktop.png`
  and
  `artifacts/browser/2026-07-10-view-list-panel/login-mobile-390x844.png`.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.

## Residual Risk

The signed-in View list/chart/Sudoku interaction was not browser-replayed in
this slice because the visible login requires a CAPTCHA. Browser policy
requires explicit user confirmation before solving it; API-level runtime
coverage and the production Vue build remain green.

# Reset Chart Tab On Navigation

## Prompt

Continue aligning old page layout and interaction, commit each behavior
atomically, and use the newly authorized current local CAPTCHA with
`admin/admin` for authenticated browser acceptance.

## Scope

- Reproduce Home, menu, and browser-history navigation from the Chart tab.
- Reset a chart View to its legacy default Data tab whenever shell navigation
  re-enters the page, including when the View id does not change.
- Preserve the current tab during search, paging, and automatic data refresh.
- Revalidate desktop, 390x844, detail, child-detail, and browser-history flows.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/ViewListPanel.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/13/2026-07-12T18-29-20Z-reset-chart-tab-on-navigation.md`

## Validation

- `cd frontend && npm test -- --run src/payload.test.ts`: 82 tests passed.
- `cd frontend && npm test -- --run && npm run build`: 153 tests passed and
  the production build completed; `ViewListPanel` is 18.07 kB.
- `python scripts/check_repo_harness.py`: passed.
- `docker compose ps -a`: frontend/backend/MySQL/Redis running; `db-migrate`
  exited successfully with code 0.
- Authenticated browser acceptance passed at 1440x900 and 390x844 using the
  user-authorized current CAPTCHA and local `admin/admin` login.
- Home, `Views > OrderList`, Back, and Forward reset the same chart View to
  Data; search while Chart is active keeps Chart selected.
- The mobile Drawer closes after selection, the document stays 390 pixels
  wide, `/view100/1001` opens its first Items tab, `/view101/2001` opens from
  the child link, and Back restores the parent detail in read mode.
- Browser artifacts: `artifacts/runs/20260713-authenticated-view-parity/`.
- `git diff --check`: passed before delivery record creation.

## Source Evidence

- Old `viewWithChart.jade` marks Data active on every page entry.
- The Vue component previously reset only when View id or template kind
  changed, so Home and history navigation resolving to the same View retained
  Chart.
- A shell-owned navigation revision now distinguishes page entry from data
  refresh without adding another workflow implementation.

## Skipped Checks And Risks

- Standard `docker compose build frontend` stalled while resolving Docker Hub
  base-image metadata. BuildKit-off Compose and direct legacy Docker builds
  also stalled before compilation; all sessions were stopped.
- The already-built `frontend/dist` was copied into the running Compose
  frontend container and Nginx was reloaded for browser acceptance. Recreating
  that container before a successful image build would restore the prior UI.
- `python scripts/runtime_doctor.py` was not rerun because its auth checks read
  and consume additional CAPTCHA values beyond the single browser CAPTCHA
  authorized for this acceptance run.

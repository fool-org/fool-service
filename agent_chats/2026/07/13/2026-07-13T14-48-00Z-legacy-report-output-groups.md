# Legacy Report Output Groups Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Keep files small and reuse shared code.

## Scope

- Restore the two legacy report-output command groups.
- Preserve output selection, movement, removal, and ordering state.

## Changed Files

- `frontend/src/ReportOutputSelector.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T14-48-00Z-legacy-report-output-groups.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/view.jade` places Up, Down, and Delete in one
  `.btn-group.btn-group-xs`, then Ascending, Descending, and Clear Sort in a
  second extra-small group inside a toolbar.
- The Vue selector retained all six commands but rendered them as one undivided
  wrapping sequence.

## Implementation

- Wrapped the existing movement/removal and ordering buttons in two labelled
  `legacy-button-group-xs` containers.
- Reused the shared compact group utility introduced for report paging.
- Set only the old 5px toolbar gap between groups; no new component, helper, or
  report state was added.
- Added accessible group names without changing individual icon tooltips or
  command labels.

## Validation

- Focused frontend suite: 82 tests passed.
- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Docker frontend production build and forced Compose recreation passed.
  Deployed image id:
  `sha256:853bdffe99a9e5cc4e8eda3be88041e50e5e01caa66b37d0df50569297bacf94`.
- Authenticated `/view101` runtime exposed exactly one Adjust group and one Sort
  group. Adding Item ID enabled the controls; Ascending added `[升序]` and Clear
  Sort removed it.
- Desktop and 390x844 screenshots show stable group spacing and no action
  overlap; the mobile action screenshot covers the scrolled selected-column
  section.
- Repository harness validation passed; frontend `/` and frontend-proxied
  `/test` returned HTTP 200. MySQL and Redis remained healthy and `db-migrate`
  remained `Exited (0)`.

## Runtime Evidence

- `artifacts/runs/20260713-legacy-report-output-groups/output-groups-desktop.png`
- `artifacts/runs/20260713-legacy-report-output-groups/output-groups-mobile.png`
- `artifacts/runs/20260713-legacy-report-output-groups/output-groups-mobile-actions.png`

## Risks And Follow-Ups

- Icon buttons remain the Vue usability improvement over glyphicons while the
  old command grouping and state transitions are preserved.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.

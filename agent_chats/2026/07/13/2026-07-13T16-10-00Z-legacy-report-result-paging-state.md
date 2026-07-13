# Legacy Report Result Paging State Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Inspect the old View and controller first.

## Scope

- Restore the old result paging buttons' available boundary interaction.
- Preserve pending-request protection and existing report request/state flow.

## Changed Files

- `frontend/src/ViewReportPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T16-10-00Z-legacy-report-result-paging-state.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/view.jade` renders Previous and Next without
  `disabled` attributes.
- `../FoolFrame/src/Web/public/javascripts/app/mkreport.js` keeps both commands
  available and checks `pageindex > 1` / `pageindex < totalpages` inside `pre`
  and `next` before requesting another page.
- The Vue migration moved those checks into button disabled expressions.

## Implementation

- Added one local `changeReportPage(offset)` handler that returns for pages
  outside `1..max(1, resultPages)` and otherwise reuses `runReport`.
- Changed both paging buttons to disable only while a parent request is pending.
- Added source contracts for the handler, bounds check, and both button events.
- Added no new page state, component, dependency, or shared abstraction.

## Validation

- Focused source-contract suite: 82 tests passed.
- Full frontend suite: 154 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Docker frontend production build and forced Compose recreation passed.
  Deployed image id:
  `sha256:5ae9a0e27edfe8274aa82ea85cd87e3a795a9cfda24228389f091269803f4711`.
- Authenticated `/view101` generated a one-page Item ID report. Previous and
  Next were both enabled at page 1 of 1.
- Clicking each boundary command preserved `报表结果 共1页 当前第1页` and the
  same five report rows, confirming both paths were no-ops.
- Desktop and 390x844 screenshots show the compact paging group without
  overflow. Repository harness validation passed; frontend `/` and
  frontend-proxied `/test` returned HTTP 200. MySQL and Redis remained healthy
  and `db-migrate` remained `Exited (0)`.

## Runtime Evidence

- `artifacts/runs/20260713-legacy-report-result-paging-state/result-paging-desktop.png`
- `artifacts/runs/20260713-legacy-report-result-paging-state/result-paging-mobile.png`

## Risks And Follow-Ups

- This runtime dataset has one report page, so both boundary paths were covered;
  the existing `runReport` path remains responsible for in-range requests.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.

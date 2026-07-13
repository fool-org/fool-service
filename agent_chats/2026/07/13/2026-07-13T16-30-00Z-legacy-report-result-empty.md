# Legacy Empty Report Result Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Inspect the old View first and preserve the
existing View-derived report request.

## Scope

- Restore the old fixed report-result table when no data rows match.
- Remove only the Vue-specific empty-result sentence.

## Changed Files

- `frontend/src/ViewReportPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T16-30-00Z-legacy-report-result-empty.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/view.jade` always renders `table#rptTable` in the
  report-result modal and defines no empty-state copy.
- `../FoolFrame/src/Web/public/javascripts/app/mkreport.js` removes existing
  table rows before appending returned cells; zero cells therefore leave the
  same empty table mounted.

## Implementation

- Removed the row-count condition from the existing report table.
- Removed the Vue-only `暂无报表数据。` fallback so an empty result leaves an
  empty `tbody` inside the same table and scroll container.
- Added source contracts for the fixed table and removed copy.
- Added no state, component, DTO, API, pagination, or CSS changes.

## Validation

- Focused source-contract suite: 82 tests passed.
- Full frontend suite: 155 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Docker frontend production build and forced no-dependency recreation passed.
  Deployed image id:
  `sha256:7f96944288ba299c5c115bfcc2c8a0cf54b079a363998a99ec6a76b9ce4d6eef`.
- Authenticated `/view101` generated an Item ID report filtered to the absent
  value `999999999999999999`. The dialog contained one fixed result table, no
  `暂无报表数据。` copy, and one protocol-supplied heading row with no data rows.
- Desktop and 390x844 screenshots show the heading-only table without overlap.
  Repository harness validation passed; frontend `/` and backend `/test`
  returned HTTP 200. MySQL and Redis remained healthy and `db-migrate` remained
  `Exited (0)`.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-report-result-empty/result-empty-desktop.png`
- `artifacts/runs/20260714-legacy-report-result-empty/result-empty-mobile.png`

## Risks And Follow-Ups

- The current backend retains the selected output heading for a zero-match
  result, so the runtime table has one heading row and no data rows.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.

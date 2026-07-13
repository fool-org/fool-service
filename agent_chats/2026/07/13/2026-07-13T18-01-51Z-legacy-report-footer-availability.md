# Legacy Report Footer Availability

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Keep report requests driven by loaded View
metadata rather than concrete business DTOs.

## Scope

- Restore old report-footer command availability at empty and partial states.
- Preserve request-pending protection and existing report request builders.

## Changed Files

- `frontend/src/ViewReportPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T18-01-51Z-legacy-report-footer-availability.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/view.jade` renders `确定` and `保存报表定义`
  without disabled predicates tied to outputs, conditions, or report name.
- `../FoolFrame/src/Web/public/javascripts/app/mkreport.js` dispatches report
  generation without a preflight selected-column or complete-condition gate.
- The old `/report/saverpt` route forwards the current columns, expression, and
  report name without client-side non-empty validation.

## Implementation

- Removed the local `conditionsComplete` and `canRun` computed gates.
- Both footer commands now disable only while a request is pending, matching
  the old availability while preventing duplicate Vue requests.
- Kept report columns, conditions, name, API routes, and View ids on their
  existing shared metadata-driven paths. `ViewReportPanel.vue` is 369 lines.

## Validation

- Focused payload contract: 82 tests passed.
- Full frontend suite: 156 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Docker frontend image build and forced frontend recreation passed. Deployed
  image id:
  `sha256:1cc765e25c18125957ab5ec4c3f6c1d9d960091bd16b058e329ab8dbdbd0c8dd`.
- Authenticated `/view101` report setup had zero selected output options while
  both footer commands remained enabled at 1440x1000 and 390x844; mobile had no
  document-level horizontal overflow.
- Clicking empty-output `确定` successfully opened a one-page result containing
  the View-derived Item ID and Item Name columns. Empty-name save availability
  was verified without clicking it, avoiding a persistent report-definition
  side effect.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-report-footer-availability/report-footer-empty-desktop.png`
- `artifacts/runs/20260714-legacy-report-footer-availability/report-footer-empty-mobile.png`

## Risks And Follow-Ups

- Invalid backend combinations still use the existing report error surface;
  this slice restores old dispatch availability rather than inventing defaults.
- The CAPTCHA-backed runtime doctor was not run because the existing
  authenticated browser session covered this slice without generating a new
  CAPTCHA.

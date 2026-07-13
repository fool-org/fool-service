# Legacy Report Condition Add Availability

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Keep report fields derived from loaded View
metadata rather than concrete business DTOs.

## Scope

- Restore both old report-condition Add commands when the loaded View has no
  report fields.
- Preserve request-pending protection and the existing condition draft model.

## Changed Files

- `frontend/src/ViewReportPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T18-16-53Z-legacy-report-condition-add-availability.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/view.jade` renders unconditional Add glyphs in
  both the condition header and trailing row.
- `../FoolFrame/src/Web/public/javascripts/app/mkreport.js` initializes
  `candidatecols` to an array before opening the report dialog, including when
  `/report/mkqview` returns no fields.
- Old `addcondition()` inserts an empty condition row and builds its field
  options from the current `candidatecols` array.

## Implementation

- Removed the Vue-only `!modelColumns.length` disabled predicate from both
  condition Add buttons.
- Both commands still disable while a request is pending and call the shared
  `addCondition()` draft builder.
- Added source-contract coverage for both buttons and the removed field-count
  gate.
- Added no state, helper, API, DTO binding, abstraction, or dependency.
  `ViewReportPanel.vue` remains 369 lines.

## Validation

- Focused payload contract: 82 tests passed.
- Full frontend suite: 156 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Docker rebuild and forced frontend recreation passed. Deployed frontend image
  id:
  `sha256:e64693a254755cc21ee7533d33f383f96aef5d7b1a232d3dec27f53d7e09e50c`.
- Authenticated `/view101` condition setup exposed two enabled Add commands.
  Clicking the header command added exactly one empty condition row whose field
  options remained sourced from loaded View metadata.
- Desktop 1280x720 and mobile 390x844 checks kept both Add commands visible and
  enabled. Mobile document width remained 390 pixels with no page overflow.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-report-condition-add-availability/condition-add-desktop.png`
- `artifacts/runs/20260714-legacy-report-condition-add-availability/condition-add-mobile.png`

## Risks And Follow-Ups

- Seeded `/view101` metadata supplies report fields, so the exact zero-field
  branch is protected by the source contract rather than a fabricated runtime
  DTO fixture.
- The CAPTCHA-backed runtime doctor was not run because the existing
  authenticated browser session covered this slice without generating a new
  CAPTCHA.

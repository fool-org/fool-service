# Legacy Report Output Availability

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Keep report rendering driven by loaded View
metadata rather than concrete business DTOs.

## Scope

- Restore old report-output method and Add-command availability when no
  candidate column is selected.
- Preserve request-pending protection and existing output-selection behavior.

## Changed Files

- `frontend/src/ReportOutputSelector.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T18-12-02Z-legacy-report-output-availability.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/view.jade` renders the output-method `select` and
  right-arrow Add command without a disabled predicate tied to candidate
  selection.
- `../FoolFrame/src/Web/public/javascripts/app/mkreport.js` keeps the Add command
  available and returns without changing output state when candidate or output
  selections are missing.

## Implementation

- Removed the Vue-only `!selectedCandidate` disabled predicates from the
  output-method list and Add arrow.
- Retained the existing `addOutput` no-op guard, duplicate-output handling, and
  request-pending disabled state.
- Added source-contract coverage for command availability and the no-op guard.
- Added no state, helper, API, DTO binding, abstraction, or dependency.
  `ReportOutputSelector.vue` remains 185 lines.

## Validation

- Focused payload contract: 82 tests passed.
- Full frontend suite: 156 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Docker frontend image build and forced frontend recreation passed. Deployed
  image id:
  `sha256:31891ee5e03887067977e97a4f98266fbf4eb19354e5ae8b155e782124177e6a`.
- Authenticated `/view101` report setup kept the output-method list and Add
  arrow enabled. Clicking Add selected `Item ID[原值]` through the existing
  View-derived metadata flow.
- Desktop 1280x720 and mobile 390x844 checks had no document-level horizontal
  overflow. The report dialog and commands remained visible at both sizes.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-report-output-availability/report-output-desktop.png`
- `artifacts/runs/20260714-legacy-report-output-availability/report-output-mobile.png`
- `artifacts/runs/20260714-legacy-report-output-availability/report-output-desktop-state.json`

## Risks And Follow-Ups

- Seeded `/view101` metadata always supplies candidate columns, so the exact
  zero-candidate branch is protected by the source contract rather than a
  fabricated runtime DTO fixture.
- The CAPTCHA-backed runtime doctor was not run because the existing
  authenticated browser session covered this slice without generating a new
  CAPTCHA.

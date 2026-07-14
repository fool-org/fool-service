# Report Output Type Parity

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced the complete candidate-change and Add handlers in FoolFrame
  `mkreport.js` before changing Vue.
- Removed the Vue-only fallback that synthesized an `原值` output type when a
  report model column has no `QueryTypes` metadata.
- Kept the old single-output-type candidate-change shortcut and the explicit
  Add command for columns with multiple real output types.
- Added no business DTO, request shape, abstraction, or dependency; the
  component remains driven only by loaded report/View metadata.

## Changed Files

- `frontend/src/ReportOutputSelector.vue`
- `frontend/src/ViewReportPanel.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T21-20-14Z-report-output-types.md`

## Validation

- `cd frontend && npm test` (19 files, 197 tests passed)
- `cd frontend && npm run build`
- Built exact implementation commit `9259f616` from a clean archive as image
  `sha256:ce79cf223147e2662d4297c92440dcdd508df0bf1e4464e727d11e11c470a555`.
  The image contains entry bundle `index-B5Hvze8R.js` and report chunk
  `ViewReportPanel-DOTjGmsi.js`.
- Authorized isolated-browser acceptance read the local CAPTCHA and logged in
  with `admin/admin` through HTTP 200. Real `getmkqview` metadata returned six
  output types for `Item ID`; explicit Add produced `Item ID[原值]`.
- A browser interception changed that same loaded column to empty lower- and
  Pascal-case `QueryTypes`. The output-method list rendered zero options and
  clicking Add left the selected list at zero.
- A second interception retained exactly one real output type. Changing the
  candidate away and back automatically added `Item ID[原值]`, preserving the
  old shortcut. Logout returned HTTP 200 and removed the local token.
- Runtime screenshots are ignored artifacts at
  `artifacts/runs/20260715-report-output-type-parity/no-query-types.png` and
  `artifacts/runs/20260715-report-output-type-parity/single-type-shortcut.png`.

## Risks And Follow-ups

- Seeded report columns all currently have output types, so the no-type branch
  uses interception of the real HTTP 200 metadata response plus a source-level
  regression test instead of changing database metadata.
- Concurrent Agent Session, installation, app-manage, Maven, and item-route
  work remains unrelated and unstaged.

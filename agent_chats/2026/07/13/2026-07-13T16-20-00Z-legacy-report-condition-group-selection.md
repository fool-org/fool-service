# Legacy Report Condition Group Selection Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Inspect the old View first, keep files small, and
avoid concrete business DTO coupling.

## Scope

- Restore one representative checkbox for an existing condition group.
- Apply representative selection and clearing to the complete group.
- Preserve nested grouping, legacy feedback, and View-derived filter state.

## Changed Files

- `frontend/src/reportConditions.ts`
- `frontend/src/reportConditions.test.ts`
- `frontend/src/ViewReportPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T16-20-00Z-legacy-report-condition-group-selection.md`

## Legacy Evidence

- `../FoolFrame/src/Web/public/javascripts/app/mkreport.js` removes every
  selected `chkbox` except the first when a group is created.
- The same script restores missing child-row checkboxes when that group is
  split, so the remaining first checkbox represents the grouped unit.

## Implementation

- Added one pure `reportConditionSelectionIds` helper beside the existing
  condition grouping functions. Ungrouped rows resolve to their own id; grouped
  rows resolve to every id sharing their top-level group.
- The condition editor renders a binary checkbox only for an ungrouped row or
  group-start row. Checking or clearing it updates the existing selected-id
  collection for the full unit; hidden rows retain a grid placeholder.
- Merge feedback now counts unique logical units, so one selected existing
  group reports `不能合并单个`, while that group plus an adjacent row remains a
  valid outer-group selection.
- Added no business DTO, API payload field, dependency, or duplicate group
  state.

## Validation

- Focused condition and source-contract suites: 88 tests passed.
- Full frontend suite: 155 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Docker frontend and backend production builds and forced Compose recreation
  passed. Deployed frontend image id:
  `sha256:8d82ff7a225f4c72eb61d61c6a89a18ee1b10d2a7b804d811af3e8589c455930`.
- Authenticated `/view101` created three conditions and grouped the first two.
  The second checkbox disappeared while the first and third remained.
- Selecting only the group representative reported `不能合并单个`; adding the
  third checkbox created an outer group, cleared feedback, and left exactly one
  representative checkbox for all three rows.
- Desktop and 390x844 screenshots show stable condition columns without
  overlap. Repository harness validation passed; frontend `/` and backend
  `/test` returned HTTP 200. MySQL and Redis remained healthy and `db-migrate`
  remained `Exited (0)`.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-report-condition-group-selection/group-selection-desktop.png`
- `artifacts/runs/20260714-legacy-report-condition-group-selection/group-selection-mobile.png`

## Risks And Follow-Ups

- Nested split controls still use the existing innermost-group behavior; this
  slice changes only the old grouped checkbox representation and selection.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.

# Legacy Report Condition Group Feedback Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Inspect the old View before its data flow.

## Scope

- Restore the old merge command's available interaction.
- Restore single and non-contiguous selection feedback.
- Preserve valid grouping, nested-group protection, and pending-request safety.

## Changed Files

- `frontend/src/reportConditions.ts`
- `frontend/src/reportConditions.test.ts`
- `frontend/src/ViewReportPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T16-00-00Z-legacy-report-condition-group-feedback.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/view.jade` renders the merge glyph without a
  selection-dependent `disabled` attribute.
- `../FoolFrame/src/Web/public/javascripts/app/mkreport.js` validates inside
  `mergecondition()`: one selected row alerts `不能合并单个`, and a gap alerts
  `不连续不能合并`.
- The old zero-selection path falls through to invalid array access; this crash
  is not a useful interaction contract to preserve.

## Implementation

- Added one exported `reportConditionGroupError` function beside the existing
  group validator. It reuses `selectedGroupContext` for contiguous and complete
  group checks.
- Kept the merge command enabled unless the parent request is pending.
- Routed zero, single, and non-contiguous feedback through the dialog's existing
  status `Message`; no alert component or duplicate validation state was added.
- Successful grouping still clears selected condition ids and the message.

## Validation

- Focused condition and source-contract suites: 87 tests passed.
- Full frontend suite: 154 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Docker frontend production build and forced Compose recreation passed.
  Deployed image id:
  `sha256:04c9b8b14efc59c03a944e4aee7c6a4ac39c5fa1b7e6d6b57a4ede06f8cdc0cd`.
- Authenticated `/view101` condition setup showed the merge command enabled at
  zero selection and reported `请选择要合并的条件` when clicked.
- One selected row reported `不能合并单个`; rows one and three reported
  `不连续不能合并`.
- Selecting all three contiguous rows grouped them, cleared the alert and all
  checks, and exposed one `拆分分组` command.
- Desktop and 390x844 screenshots show the valid group and mobile feedback
  without overlap. Repository harness validation passed; frontend `/` and
  frontend-proxied `/test` returned HTTP 200. MySQL and Redis remained healthy
  and `db-migrate` remained `Exited (0)`.

## Runtime Evidence

- `artifacts/runs/20260713-legacy-report-condition-group-feedback/group-feedback-desktop.png`
- `artifacts/runs/20260713-legacy-report-condition-group-feedback/group-feedback-mobile.png`

## Risks And Follow-Ups

- Zero selection uses `请选择要合并的条件` instead of reproducing the old
  JavaScript exception; single and non-contiguous legacy messages remain exact.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.

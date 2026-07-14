# Typed Report Condition Values

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced `view.jade`'s empty report value cell, `mkreport.js`'s selected-column
  handler, and `setextype.makeinput` before changing Vue.
- Replaced the report-only enum/text split with the existing generic
  `MetadataFieldEditor`, driven by the selected report/View column.
- Restored enum, Boolean, date, time, date-time, constrained numeric, and
  BusinessObject condition controls.
- Kept BusinessObject ids separate from their suggestion text so report filters
  emit the old `ValueExp` / `ValueFmt` pair.
- Added a 43-line report-metadata adapter and 58-line focused test file. The
  existing report panel remains 396 lines and the shared editor 198 lines.
- Added no concrete business DTO, duplicate field editor, dependency, or new
  request route.

## Changed Files

- `frontend/src/MetadataFieldEditor.vue`
- `frontend/src/ViewReportPanel.vue`
- `frontend/src/ViewReportPanel.test.ts`
- `frontend/src/payload.test.ts`
- `frontend/src/reportConditions.ts`
- `frontend/src/reportConditionValue.ts`
- `frontend/src/reportConditionValue.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T21-51-28Z-report-condition-values.md`

## Validation

- `cd frontend && npx vitest run src/reportConditionValue.test.ts
  src/ViewReportPanel.test.ts src/payload.test.ts` (3 files, 95 tests passed)
- `cd frontend && npx vitest run src/reportConditionValue.test.ts
  src/ViewReportPanel.test.ts` (2 files, 11 tests passed after the final source
  guard update)
- `cd frontend && npm test` (20 files, 203 tests passed)
- `cd frontend && npm run build`
- `git diff --check`
- `python scripts/check_repo_harness.py` was attempted but failed on unrelated
  concurrent work: `AppManageMigrationTest.java` has 2140 lines against the
  repository limit of 2100.
- Built exact implementation commit `1c974a72` from a clean archive with
  Buildx state redirected to `/private/tmp`. The running image is
  `sha256:a1e2acccbcc5cd36ae49a7ae78bbfa3174bd391cbdee891e21e2f2a374fa9fb7`,
  with entry `index-B2ru2Sla.js`, report chunk `ViewReportPanel-B_JpIU8t.js`,
  and editor chunk `MetadataFieldEditor-CQsNfjqP.js`.
- Authorized isolated-browser acceptance read a fresh local CAPTCHA, logged in
  with `admin/admin`, entered `/view101`, and used an HTTP-200 report metadata
  response containing seven property types. Logout returned HTTP 200.
- Enum rendered `Open` and changed to `Closed`; Boolean rendered an unchecked
  checkbox and emitted `false` / `否`; date, time, and date-time rendered their
  native input types and retained `2026-07-15`, `09:30`, and
  `2026-07-15T09:30`.
- Numeric type 1 used the old four-character text-input boundary and filtering;
  entering `12a345` produced `123` in the browser. The BusinessObject editor
  requested `viewId=101`, `viewItemId=Customer`, `text=Ali`, selected
  `3001 / Alice`, and emitted `ValueExp=3001` with `ValueFmt=Alice`.
- The intercepted `mkrpt` request contained all seven View-derived conditions;
  Return, Cancel, and logout completed with no browser console/page errors.
- Post-acceptance MySQL checks retained 8 `market_order` rows and 4
  `market_order_item` rows. Order 1001 remained `BTC-USDT`, state `0`, customer
  `3001`, amount `0.2500000000`, and price `62500.0000000000`.

## Risks And Follow-ups

- The seeded Market report does not contain every legacy property type, so the
  seven-type matrix used a synthetic HTTP-200 `getmkqview` response while auth,
  View routing, controls, lookup, report submission, and logout stayed live.
- The concurrent backend smoke container exited after browser acceptance; this
  slice did not restart or modify that unrelated initialization work.
- Repository harness status remains blocked only by the concurrent oversized
  Java test noted above; frontend tests, type checking, Vite build, clean-image
  build, runtime interaction, and database invariants passed.

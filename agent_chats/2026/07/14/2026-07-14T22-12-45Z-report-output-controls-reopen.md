# Report Output Controls Reopen

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced `mkreport.initquery` and confirmed it rebuilds only
  `#rpt-candidate`; the output-method and selected-output controls remain in the
  static `view.jade` modal.
- Browser-tested commit `751af12c` and found its source-only guards incomplete:
  PrimeVue destroyed `ReportOutputSelector` while the metadata-loading `v-if`
  hid the dialog, so the retained local options still disappeared.
- Moved only output-method options, selected method, and selected-output index
  to the still-mounted `ViewReportPanel`; the selector still owns all control
  mutations.
- Added no App-level business state, DTO, store, route, dependency, or new
  abstraction. The parent/selector remain 406/183 lines.

## Changed Files

- `frontend/src/ReportOutputSelector.vue`
- `frontend/src/ViewReportPanel.vue`
- `frontend/src/ViewReportPanel.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T22-12-45Z-report-output-controls-reopen.md`

## Validation

- `cd frontend && npm test -- --run ViewReportPanel.test.ts payload.test.ts
  reportOutputs.test.ts` (3 files, 97 tests passed)
- `cd frontend && npm test` (20 files, 208 tests passed)
- `cd frontend && npm run build`
- `python scripts/runtime_doctor.py` (68 checks passed)
- `python scripts/check_repo_harness.py` was attempted but failed on unrelated
  concurrent work: `AppManageMigrationTest.java` has 2106 lines against the
  repository limit of 2100.
- Exact implementation commit `2b7d66fa` produced deployed image
  `sha256:fdac83a762b1f6a1d796b9936a32b073e8430ed2a7f9ff960abf41cdae3945a9`
  with report chunk `ViewReportPanel-Bqy-CSPJ.js`.
- Authorized browser acceptance read a fresh local CAPTCHA, logged in with
  `admin/admin`, opened `/view101`, and logged out; login/logout returned HTTP
  200.
- First open selected `Item Name / 计数`, added `Item Name[计数]`, and retained
  selected output index zero. After Cancel/reopen, a second HTTP-200
  `getmkqview` reset the candidate to `Item ID` while preserving output options
  `原值/计数`, selected method `计数`, output index zero, and the first output.
  A second Add produced `Item ID[计数]`.
- Visible evidence:
  `artifacts/runs/20260715-report-draft-reopen/output-controls-reopened.png`
  (`1440x900`, SHA-256
  `201c9934d29c8b0b5fffeef187840a581b3f92cb8ec8231705d5f286dc717366`).
- Compose retained backend/frontend/mysql/redis up, MySQL/Redis healthy, and
  `db-migrate` at `Exited (0)`.

## Risks And Follow-ups

- The screenshot artifact is intentionally ignored by Git; its exact path and
  hash are recorded above.
- Repository harness status is blocked only by the concurrent oversized Java
  test noted above. This slice did not edit or stage that file.

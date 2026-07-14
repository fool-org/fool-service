# Report Candidate Result Return

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced `view.jade`'s separate static setup/result modals and `mkreport.js`
  before changing Vue. Report generation hides setup and successful completion
  shows results; Return hides results, resets page index, and shows the same
  setup DOM without reloading metadata.
- Kept the selected candidate in the mounted View-level report UI state so
  result Return does not reset it.
- Reset the candidate to the first View column only after a successful
  `getmkqview` response, preserving the old `initquery` candidate rebuild on a
  fresh report open.
- Resolved the native candidate change from its event value before the parent
  `v-model` round trip, including the old single-method automatic Add path.
- Reused the existing output constructor. Added no App state, business DTO,
  store, route, dependency, or new abstraction. `ReportOutputSelector.vue` and
  `ViewReportPanel.vue` remain 181 and 409 lines.

## Changed Files

- `frontend/src/ReportOutputSelector.vue`
- `frontend/src/ViewReportPanel.vue`
- `frontend/src/ViewReportPanel.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T22-22-03Z-report-candidate-result-return.md`

## Validation

- `cd frontend && npx vitest run src/ViewReportPanel.test.ts
  src/payload.test.ts src/reportOutputs.test.ts` (3 files, 100 tests passed)
- `cd frontend && npm test` (20 files, 211 tests passed)
- `cd frontend && npm run build`
- `git diff --check`
- `python scripts/runtime_doctor.py` (68 checks passed)
- `python scripts/check_repo_harness.py` was attempted but failed on unrelated
  concurrent work: `AppManageMigrationTest.java` has 2106 lines against the
  repository limit of 2100.
- Implementation commits `e6d22c12` and `ef18d38d` produced final exact image
  `sha256:c9767bf5e725f2918934de8aed2628d3efb289bb72964885417ecfae9e9be2b3`
  with report chunk `ViewReportPanel-D-lrTGwB.js`. The image returned HTTP 200
  for the frontend and proxied backend on port 8086.
- Authorized isolated-browser acceptance read a fresh local CAPTCHA, logged in
  with `admin/admin`, opened `/view101`, and used two-column HTTP-200 report
  metadata plus a one-page HTTP-200 report response.
- The initial candidate was `第一列`. Selecting `第二列 / 第二计数` and adding it
  produced `第二列[第二计数]`. Confirm emitted one report request.
- Return left the metadata-request count at one and retained candidate
  `第二列`, both methods, selected `第二计数`, and the selected output. Cancel and
  reopening raised the metadata count to two, reset only the candidate to
  `第一列`, and retained the other output controls.
- Logout returned HTTP 200 with no browser console or page errors.
- Visible evidence:
  `artifacts/runs/20260715-report-result-return/candidate-retained-after-return.png`
  (`1440x900`, SHA-256
  `4176e8fedf2810672ad0de7d87397044eef12ebceeeb87f80acb6c14924a6aac`).
- Compose retained backend/frontend/mysql/redis up, MySQL/Redis healthy, and
  `db-migrate` at `Exited (0)`.
- Post-acceptance MySQL checks retained 8 `market_order` rows and 4
  `market_order_item` rows. Order 1001 remained `BTC-USDT`, state `0`, customer
  `3001`, amount `0.2500000000`, and price `62500.0000000000`.

## Risks And Follow-ups

- Seeded metadata does not provide distinct candidate method labels for this
  lifecycle edge, so report metadata/result responses were synthetic HTTP 200
  while authentication, View routing, controls, report transitions, and logout
  remained live.
- The screenshot artifact is intentionally ignored by Git; its exact path and
  hash are recorded above.
- Repository harness status remains blocked only by the concurrent oversized
  Java test noted above; frontend, exact-image, browser, runtime-doctor, Compose,
  and database checks passed.

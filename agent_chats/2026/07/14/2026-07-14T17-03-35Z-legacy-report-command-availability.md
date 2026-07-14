# Legacy Report Command Availability

## Prompt

Continue aligning the old page layout, style, and interactions in atomic
commits without coupling Vue components to concrete business DTOs.

## Scope

- Compared report setup and result commands in `view.jade` and `mkreport.js`
  with `ViewReportPanel.vue` and `ReportOutputSelector.vue`.
- Removed the Vue-only global request lock from output and condition editors,
  setup footer commands, result paging/Return, and mask dismissal.
- Preserved initial-generation setup hiding and the result header's old
  no-close-button layout, but kept results visible during paging requests like
  `ShowReportController`.
- Kept Return authoritative during an in-flight page request so its late
  response updates data without reopening results.
- Limited the existing `reportRunning` flag to initial generation, allowing
  Return to restore setup immediately during page requests without adding
  another state flag.
- Preserved request paths, local state, page guards, and View metadata adapters.
- Removed the now-unused report `pending` prop chain and output-selector
  disabled prop; added no state, route, request, DTO binding, or duplicate
  component.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/ViewReportPanel.vue`
- `frontend/src/ReportOutputSelector.vue`
- `frontend/src/ViewReportPanel.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T17-03-35Z-legacy-report-command-availability.md`

## Validation

- `cd frontend && npm test` passed: 17 files, 183 tests.
- `cd frontend && npm run build` passed.
- `python scripts/check_repo_harness.py` passed.
- The focused `ViewReportPanel.test.ts` contract is 16 lines and
  `ViewReportPanel.vue` shrank from 379 to 376 lines.
- After separating initial-generation hiding from result-page visibility, the
  same 17-file/183-test suite, production build, and harness passed again.
- After protecting Return from a late paging response, those same three checks
  passed once more.
- After narrowing `reportRunning` to initial generation, the 17-file/183-test
  suite, production build, and harness passed again.
- `docker compose build frontend` and final frontend replacement passed.
- Deployed frontend image:
  `sha256:8a5e0ea34a7e23e67589b9b50c97f2d5ebd1426d41ce9070619be8b97872ba76`.
- `docker compose ps -a` showed backend/frontend/MySQL/Redis running and
  `db-migrate` at `Exited (0)`.
- `python scripts/runtime_doctor.py` passed all 67 checks before and after
  browser acceptance; backend `/test` passed through the doctor.
- Authorized browser acceptance used `view101` and temporarily inserted items
  `990101` through `990107`, producing eleven rows and two report pages.
- During a paused-backend `saverpt` request, all setup buttons and selects had
  `disabled=false`; switching to Conditions and clicking Add Condition created
  one local condition row. The backend's existing no-op save route returned
  `报表定义已提交。` after resume.
- Selecting Item ID and Confirm preserved initial-generation hiding and opened
  `报表结果 共2页 当前第1页` with no header Close button.
- During a paused-backend next-page `mkrpt`, the result dialog remained visible
  and Previous, Next, export placeholders, and Return all had `disabled=false`.
  Return immediately restored setup before the request settled. The delayed
  request eventually failed after the pause, but rendered `无法生成报表。` in
  setup and did not reopen results.
- Setup Close remained active and closed the dialog. Physical mask clicks could
  not be proven because the browser locator centered clicks on dialog content;
  `dismissable-mask` remains covered by the focused source contract.
- All seven temporary items were deleted. MySQL returned to eight orders and
  four order items; reloaded `/view101` showed four records, no Page 2, no
  temporary text, and no dialog. Order `1001` remained
  `BTC-USDT / customer 3001 / state 0`.

## Risks And Follow-ups

- This parity slice is closed; the broader old-page migration remains active.
- Mask dismissal retains a small browser-evidence gap described above; its
  component contract and the shared PrimeVue pattern are unchanged.
- No screenshot artifact was retained because the acceptance depended on DOM
  enabled state and temporary request/database state; final runtime and data
  restoration were verified directly.
- `docs/superpowers/` is unrelated untracked work and remains untouched.

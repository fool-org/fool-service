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
- Pending Docker rebuild, runtime doctor, and authorized browser acceptance.

## Risks And Follow-ups

- Browser-verify setup tabs/commands during an unrelated request, then result
  paging and Return during a page `mkrpt`, without saving a report definition.
- Remove any temporary local acceptance rows and verify original data afterward.
- `docs/superpowers/` is unrelated untracked work and remains untouched.

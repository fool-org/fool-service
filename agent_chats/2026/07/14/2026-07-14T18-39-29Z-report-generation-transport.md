# Report Generation Transport Handling

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced initial generation and result paging through the old
  `MakeReportController` / `ShowReportController` before changing Vue.
- Restored success-only `mkrpt` transport handling: initial failure leaves both
  report dialogs closed; paging failure retains the current result.
- Extracted one component-local success-only action helper and reused it for
  report metadata and generation instead of duplicating transport handling.
- Kept response-backed errors, successful generation, paging boundaries,
  View-derived state, routes, payloads, and DTO bindings unchanged.

## Changed Files

- `frontend/src/ViewReportPanel.vue`
- `frontend/src/ViewReportPanel.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T18-39-29Z-report-generation-transport.md`

## Validation

- `cd frontend && npm test -- --run ViewReportPanel.test.ts payload.test.ts`
  (2 files, 86 tests passed)
- `cd frontend && npm test` (19 files, 193 tests passed)
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py`
- Pending Compose build and authorized stopped-backend browser acceptance.

## Risks And Follow-ups

- Browser acceptance should load `/view101`, open report setup, stop the
  backend, run the report, prove setup/results/errors remain absent, restore
  the backend, and prove successful result generation.
- Paging transport failure should retain existing results; proving it requires
  a temporary second report page and remains optional if initial failure and
  source-contract coverage pass.
- Candidate View metadata-load transport behavior remains a separate decision
  because old `initQueryView` leaves its loading dialog open indefinitely.
- `docs/superpowers/` is unrelated untracked work and remains untouched.

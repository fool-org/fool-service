# Report Metadata Transport Handling

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced the report entry from `view.jade` into `mkreport.initquery` before
  comparing the current Vue report setup.
- Restored the old success-only transport path: a report-column network or
  non-2xx failure leaves the main View unchanged and the report setup closed.
- Reused the existing transport classifier and `silentTransport` action option.
- Kept response-backed errors, successful report setup, View-derived columns,
  routes, payloads, and DTO bindings unchanged.

## Changed Files

- `frontend/src/ViewReportPanel.vue`
- `frontend/src/ViewReportPanel.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T18-34-03Z-report-metadata-transport.md`

## Validation

- `cd frontend && npm test -- --run ViewReportPanel.test.ts payload.test.ts`
  (2 files, 85 tests passed)
- `cd frontend && npm test` (19 files, 192 tests passed)
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py`
- Pending Compose build and authorized stopped-backend browser acceptance.

## Risks And Follow-ups

- Browser acceptance should keep `/view100` rows visible, stop the backend,
  click `统计`, prove the report/error dialogs remain absent, restore the
  backend, and prove the report setup opens normally.
- Candidate View metadata-load transport behavior remains a separate decision
  because old `initQueryView` leaves its loading dialog open indefinitely.
- `docs/superpowers/` is unrelated untracked work and remains untouched.

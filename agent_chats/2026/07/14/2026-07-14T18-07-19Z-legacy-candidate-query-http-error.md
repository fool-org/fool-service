# Legacy Candidate Query HTTP Error

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced `detailView.jade`'s `#selectdialog` through its mounted
  `QuerylistdataController` before comparing the candidate data path.
- Reused the existing optional `silentTransport` action policy only for the
  candidate `child-select-data` request.
- Kept candidate View metadata loading and response-backed business-error
  presentation unchanged.
- Added no component, API route, DTO binding, or abstraction.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T18-07-19Z-legacy-candidate-query-http-error.md`

## Validation

- `cd frontend && npm test -- --run payload.test.ts` passed: 1 file, 83 tests.
- `cd frontend && npm test` passed: 19 files, 187 tests.
- `cd frontend && npm run build` passed, including `vue-tsc --noEmit`.
- `python scripts/check_repo_harness.py` passed.
- Pending Compose rebuild and authorized candidate-picker browser acceptance.

## Risks And Follow-ups

- Browser acceptance must first load candidate results, stop the backend, issue
  another Find, and prove rows/record count/paging remain without shared error;
  then restart the backend and prove the picker recovers in the same session.
- Candidate View metadata-load transport behavior is intentionally outside this
  slice because old `initQueryView` owns a separate loading-dialog lifecycle.
- `docs/superpowers/` is unrelated untracked work and remains untouched.

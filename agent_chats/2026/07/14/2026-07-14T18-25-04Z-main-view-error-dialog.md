# Main View Error Dialog

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced `querylistdata.js` response errors into `showerror.showerrormsg` before
  comparing the current main View outlet.
- Replaced the main View's inline error Message with the old modal interaction.
- Extracted the existing detail error markup into `LegacyErrorDialog` and reused
  it in both main View and detail surfaces.
- Kept login's error-code/CAPTCHA dialog separate and left transport handling,
  View/Data projection, routes, payloads, and DTO bindings unchanged.

## Changed Files

- `frontend/src/LegacyErrorDialog.vue`
- `frontend/src/ViewListPanel.vue`
- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/App.vue`
- `frontend/src/ViewListPanel.test.ts`
- `frontend/src/ViewDetailPanel.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T18-25-04Z-main-view-error-dialog.md`

## Validation

- `cd frontend && npm test -- --run ViewListPanel.test.ts ViewDetailPanel.test.ts payload.test.ts`
  (3 files, 88 tests passed)
- `cd frontend && npm test` (19 files, 191 tests passed)
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py`
- Pending Compose build and authorized invalid-View browser acceptance.

## Risks And Follow-ups

- Browser acceptance should navigate to `/view999999`, prove the HTTP-200
  nonzero-code response opens `发生错误`, close it, and return to a valid View.
- Candidate View metadata-load transport behavior remains a separate decision
  because old `initQueryView` leaves its loading dialog open indefinitely.
- `docs/superpowers/` is unrelated untracked work and remains untouched.

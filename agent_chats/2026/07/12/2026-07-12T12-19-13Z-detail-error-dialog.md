# Detail Error Dialog Parity

## Prompt

Keep legacy interaction logic aligned while allowing visual modernization, and
commit every behavior change atomically.

## Scope

- Verify the visible destination of `App.errorMessage` on detail routes.
- Restore a modal error outlet for edit guards, validation, and detail API
  failures.
- Reuse the existing detail Dialog/Button dependencies and shared error state.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T12-19-13Z-detail-error-dialog.md`

## Validation

- `cd frontend && npm test -- --run`: 9 files, 148 tests passed.
- `cd frontend && npm run build`: TypeScript and Vite production build passed;
  `ViewDetailPanel` is 70.76 kB after reusing its existing Dialog dependency.
- Regression coverage proves App passes and clears the shared error while the
  detail panel renders and dismisses the `发生错误` modal.
- `docker compose build frontend`: passed.
- `docker compose up -d --no-deps frontend`: rebuilt frontend deployed on port
  8081.
- `python scripts/runtime_doctor.py`: all Compose, auth, View/data, detail,
  operation, report, message, and logout checks passed.
- `python scripts/check_repo_harness.py`: passed.
- `docker compose ps -a`: backend/frontend are up, MySQL/Redis are healthy, and
  `db-migrate` is `Exited (0)`.

## Source Evidence

- `default.jade` defines the global `#error-dialog` titled `发生错误`.
- `detailview.js` routes edit-state operation blocking through `showerror`.
- Before this slice, detail actions wrote `App.errorMessage`, but
  `ViewDetailPanel` had no error prop or visible error component.

## Risks And Follow-Ups

- List and login surfaces retain their current inline modernized feedback;
  this slice fixes the missing detail interaction without duplicating those
  surfaces.
- Authenticated browser acceptance needs a fresh CAPTCHA confirmation.

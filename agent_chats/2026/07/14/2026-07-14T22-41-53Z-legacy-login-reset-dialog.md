# Legacy Login Reset Dialog

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically while retaining View-first rendering and code reuse.

## Scope

- Recheck the signed-out View after report and shell parity were exhausted.
- Restore Reset's old modal-close timing instead of treating the blank modal as
  removable presentation.
- Keep credentials, database binding, CAPTCHA ownership, and business errors
  on their existing boundaries.

## Legacy Evidence

- `../FoolFrame/src/Web/views/index.jade` binds Reset to `showerror()`.
- `../FoolFrame/src/Web/public/javascripts/app/login.js` opens the shared error
  modal immediately and registers `$scope.refresh()` on `hidden.bs.modal`.
- The click itself does not request a CAPTCHA or clear a field. Closing the
  modal refreshes only the CAPTCHA key/image; username, password, and hidden
  database remain untouched.

## Implementation

- Added one component-local `resetDialogVisible` flag to `LoginPanel`.
- Reused the existing error dialog presentation for Reset and routed its Close
  through `dismissLoginDialog()`.
- Reset Close emits the existing `refresh` event. A response-backed login error
  still emits `dismissError`, so App remains the owner of business-error and
  check-code requests.
- No DTO, request helper, route, store, dependency, or duplicate dialog
  component was added. `LoginPanel.vue` remains 199 lines.

## Changed Files

- `frontend/src/LoginPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T22-41-53Z-legacy-login-reset-dialog.md`

## Commits

- Implementation: `5d04def0 fix(frontend): defer login reset refresh until dialog close`

## Validation

- `cd frontend && npm test -- --run payload.test.ts`: 85/85 passed.
- `cd frontend && npm test`: 20 files, 212/212 passed.
- `cd frontend && npm run build`: passed.
- `python scripts/check_repo_harness.py`: passed.
- `python scripts/runtime_doctor.py`: 68/68 passed.
- `git diff --check`: passed for the scoped implementation and evidence.

## Runtime Evidence

- Exact clean-archive frontend image:
  `sha256:f089382d2eef340138511a803e0b7ed7253570432c964bc54be7f24bb56e786b`
  with revision label `5d04def0` and entry bundle `index-DR8HOYTi.js`.
- Exact-image container: `fool-frontend-login-reset-5d04def0` on
  `http://localhost:8089` in `fool-service_default`.
- Browser acceptance filled `admin/admin` and `stale-code`, clicked Reset, and
  observed no `getcheckcode` request for 500 ms. The dialog was visible and all
  three fields plus the CAPTCHA image were unchanged.
- Closing the dialog emitted exactly one HTTP-200 `getcheckcode`, retained
  username/password, cleared the CAPTCHA input, and replaced its image.
- The newly read local CAPTCHA completed login to `/main`; login/logout both
  returned HTTP 200, and browser console/page errors were empty.
- `artifacts/runs/20260715-login-reset-close/reset-dialog-before-refresh.png`
  (`1280x800`, SHA-256
  `9167f3ca672d4e06f9e995e83066ce691aef7b43b7e3ded030bb3e34874fd6bc`).
- `artifacts/runs/20260715-login-reset-close/reset-closed-after-refresh.png`
  (`1280x800`, SHA-256
  `323b2f6a32af9a8adcbd9d335caab39e5b62ff2c3158ce14da9667be864472ec`).
- Compose services were healthy, `db-migrate` was `Exited (0)`, backend
  `/test` passed, and MySQL retained 8 orders, 4 child rows, and unchanged order
  `1001 BTC-USDT / state 0 / customer 3001 / amount 0.25 / price 62500`.

## Skipped Checks And Risks

- The full legacy Node/Angular application was not started. Checked-in Jade,
  JavaScript, Bootstrap event semantics, and exact-image Vue browser behavior
  supplied the comparison.

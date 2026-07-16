# Phase 4 Browser Authorization Matrix Closure

## Prompt

Follow `docs/authorization-and-agent-risk-control.md` in order, complete the
Agent-related development, locate the browser validation failure, update the
README, and commit the finished slice.

## Scope

- Replaced the blocked in-app-browser dependency with a deterministic local
  system-Chrome/CDP validation path.
- Closed the remaining four-role UI/network acceptance gate for ordinary,
  department-administrator, independent-approver, and system-administrator
  identities.
- Fixed security-denial HTTP status handling, missing denial audit events,
  CAPTCHA log exposure, and the remaining frontend body-token compatibility
  call.
- Preserved exact fixture, token, Action Request, approval, and Chrome-profile
  cleanup.

## Changes

- Added a Python orchestrator and Playwright CDP driver that launch the installed
  Google Chrome with a dedicated temporary profile and private loopback debug
  port. The harness refuses occupied ports and never attaches to a developer's
  existing browser.
- Added four isolated fixture identities, real Vue CAPTCHA login, row-scope and
  effective-action assertions, true HTTP-status assertions, immutable HIGH
  preview/approval validation, masked screenshots, sanitized JSON evidence,
  artifact secret scanning, checksums, and `finally` cleanup.
- Added fixture-identifier preflight checks and exact source-to-clone comparison
  for the isolated system administrator's authorization bindings.
- Changed the Vue `getmain` call to an empty JSON body so the bearer header is
  the only authentication input.
- Changed `ErrorHandleAspect` to rethrow authorization and controlled-action
  exceptions to the controller advice. The previous aspect converted a genuine
  `NO_MATCHING_ALLOW` denial into an HTTP 200 `CommonResponse`, which was the
  application root cause exposed by the browser matrix.
- Added audit events for `SELF_APPROVAL_FORBIDDEN` and HIGH-action execution
  denials such as `STEP_UP_REQUIRED`.
- Extended recursive log sanitization to redact CAPTCHA code, key, and image
  fields while preserving ordinary business response `code` fields.
- Updated the English and Chinese READMEs, validation guide, authorization
  operations guide, and Phase 4 task state.

## Changed Files

- `README.md`
- `README.zh-CN.md`
- `docs/authorization-operations.md`
- `docs/validation.md`
- `tasks.md`
- `frontend/package.json`
- `frontend/package-lock.json`
- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `scripts/browser_role_matrix_test.py`
- `scripts/harness/__init__.py`
- `scripts/harness/browser_role_matrix.py`
- `scripts/harness/browser_driver.cjs`
- `fool-error-handler/src/main/java/org/fool/framework/error/aspect/ErrorHandleAspect.java`
- `fool-error-handler/src/test/java/org/fool/framework/error/aspect/ErrorHandleAspectTest.java`
- `fool-agent/src/main/java/org/fool/framework/agent/action/ActionRequestService.java`
- `fool-agent/src/test/java/org/fool/framework/agent/action/ActionRequestServiceTest.java`
- `fool-log/src/main/java/org/fool/framework/log/SensitiveLogSanitizer.java`
- `fool-log/src/test/java/org/fool/framework/log/SensitiveLogSanitizerTest.java`

## Validation

- `node --check scripts/harness/browser_driver.cjs`: passed.
- `node scripts/harness/browser_driver.cjs --self-test`: passed.
- `PYTHONPATH=scripts python -m unittest scripts/browser_role_matrix_test.py -v`:
  14 tests passed.
- Focused Java 17 Maven validation for `ActionRequestServiceTest`,
  `ErrorHandleAspectTest`, and `SensitiveLogSanitizerTest`: 10 tests passed.
- Full Java 17 Maven reactor: all 16 modules passed.
- `cd frontend && npm test && npm run build`: 23 files / 234 tests passed;
  production build passed.
- `python scripts/check_repo_harness.py`: passed.
- `python scripts/runtime_doctor_test.py`: 50 tests passed.
- `python scripts/runtime_doctor.py`: all Compose, schema, frontend, backend,
  authentication, View, data, report, message, audit, and controlled-action
  checks passed.
- `PYTHONPATH=scripts python -m unittest scripts/authorization_review_test.py -v`:
  passed.
- `python scripts/authorization_review.py --strict`: passed with no orphaned,
  disabled, or wildcard permission bindings.
- `python scripts/authorization_security_regression.py --report-json
  artifacts/runs/20260716-phase4-final/security-regression.json`: policy
  invalidation, audit tamper detection, and 16-way audit concurrency passed.
- `python scripts/legacy_migration_contract.py --require-legacy`: passed.
- `python scripts/harness/browser_role_matrix.py --run-id
  20260716-browser-role-matrix-07`: all four roles and all eight acceptance
  checks passed.
- Independent database cleanup query returned zero across all 11 fixture
  categories: users, credentials, roles, role assignments, legacy users,
  legacy role assignments, departments, data policies, bindings, Action
  Requests, and approvals.
- Post-run log scan returned `rawCaptchaFields=0`, `base64Images=0`, and
  `redactedMarkers=26`.
- `git diff --check`: passed.

## Runtime Evidence

- `artifacts/runs/20260716-browser-role-matrix-07/summary.json`
- `artifacts/runs/20260716-browser-role-matrix-07/browser-role-matrix.json`
- `artifacts/runs/20260716-browser-role-matrix-07/audit.json`
- `artifacts/runs/20260716-browser-role-matrix-07/manifest.json`
- `artifacts/runs/20260716-browser-role-matrix-07/screenshots/ordinary-view100.png`
- `artifacts/runs/20260716-browser-role-matrix-07/screenshots/departmentAdmin-view100.png`
- `artifacts/runs/20260716-browser-role-matrix-07/screenshots/approver-before-approval.png`
- `artifacts/runs/20260716-browser-role-matrix-07/screenshots/approver-action-approved.png`
- `artifacts/runs/20260716-browser-role-matrix-07/screenshots/systemAdmin-awaiting-independent-approval.png`
- `artifacts/runs/20260716-browser-role-matrix-07/screenshots/systemAdmin-execute-step-up-denied.png`
- `artifacts/runs/20260716-phase4-final/security-regression.json`
- `artifacts/runs/20260716-phase4-final/log-security.json`

The final Action Request was
`c9219456-0eec-485e-bdf8-fbd3a6bac374`. It remained `APPROVED` and unexecuted
before fixture cleanup. Self-approval returned
`403 SELF_APPROVAL_FORBIDDEN`; execution without current step-up returned
`403 STEP_UP_REQUIRED`. Both reasons are present in the selected audit evidence.

## Skipped or Downgraded Checks

- A standard Docker image rebuild was not repeated because Docker Hub metadata
  access had stalled during the earlier attempt. The current Java 17 reactor JAR
  and verified frontend `dist` were copied into the existing local backend and
  frontend containers. The backend was restarted, and the full runtime doctor,
  browser matrix, and security regression then passed against those exact
  artifacts.

## Risks

- The browser proof covers the current local Compose stack and installed system
  Chrome; a production deployment still needs its normal release validation.
- Backend startup retains the known legacy missing-column mapper warnings, then
  completes successfully and passes the health/runtime probes.
- The evidence bundle is intentionally sanitized: full bodies, DOM snapshots,
  HAR, trace, video, credentials, CAPTCHA material, and bearer tokens are not
  retained.

## Follow-Ups

- Optionally add this Chrome/CDP matrix to a Chrome-capable protected CI runner.
- Retry the normal image-build path when registry metadata access is reliable.

## Task-State Update

- Checked the Phase 4 item in `tasks.md`; all acceptance gates are now complete.

## Linked Commit

- Included in `feat(agent): complete browser authorization matrix`.

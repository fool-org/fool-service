# Phase 4 Compatibility Removal and Hardening

## Prompt

Follow `docs/authorization-and-agent-risk-control.md` in order and complete the
Agent-related development.

## Scope

- Removed body-token authentication and raw Agent session-token storage.
- Added immediate policy freshness, tamper-evident audit verification and
  alerts, Action Catalog/runtime drift validation, permission review, and
  reversible security regressions.
- Added authenticated effective-action discovery for server-owned command
  visibility and completed legacy department-role inheritance across request
  subjects, approval rechecks, and menu discovery.
- Closed final audit gaps in Service-layer write enforcement, field-level deny
  precedence, confidential model-outbound masking/provider approval, and
  persisted Agent-message redaction.
- Ran the complete non-browser Phase 4 validation surface.

## Changes

- Protected code now derives identity only from `EffectiveSubjectContext`; old
  DTO token fields cannot authenticate or influence protected View operations.
- Removed the unused raw-token argument from `ViewDataService`; direct legacy
  mutation Services require an active approved Action Request execution scope.
- Agent sessions persist owner and fixed scope only; migration 016 removes
  `SESSION_TOKEN` from existing databases.
- Audit events use a transactional hash chain with scheduled/on-demand
  verification and deduplicated CRITICAL alerts.
- Permission/binding/data-policy timestamps feed a direct policy fingerprint;
  stale previews reset with `POLICY_CHANGED`.
- Startup compares executable catalog entries with registered handlers.
- `/api/v1/authz/effective-actions` evaluates the code-owned catalog against the
  current subject/resource and returns only allowed actions with the effective
  risk floor. The frontend uses those decisions to hide unavailable Agent,
  report, create, and edit commands.
- Legacy department role assignments now contribute namespaced roles to both
  request-time subjects and approval-time subject lookup. Legacy menus include
  direct-user and department-assigned roles before `view.discover` filtering.
- Fixed the generic DAO mapper's single-`@Id` promotion so identity selects and
  updates use the primary-key predicate instead of an always-true fallback.
- Excluded null company assignments from both effective-subject lookup paths so
  departments without a company no longer invalidate authentication.
- Multi-role data policies apply field DENY before read/filter/sort/export/write
  and LLM visibility, including wildcard/alias combinations. CONFIDENTIAL model
  fields are masked and require an explicit provider allowlist; RESTRICTED
  fields are removed. User/provider chat text is redacted before session
  persistence.
- Added strict authorization review and reversible policy/audit/concurrency
  runtime regression scripts plus an operations runbook.

## Validation

- Full Maven reactor: passed all 16 modules.
- Frontend: the latest run passed 23 files / 234 tests; production build passed.
- Repository harness, migration contract, script unit tests, migration replay,
  runtime doctor, authorization review, and security regression passed.
- Migration `016` passed both existing-volume replay and disposable fresh-db
  initialization. A final live HIGH bulk-update recheck passed step-up,
  self-approval rejection, independent target-scope approval, execution guard,
  persistence, audit trace, and fixture cleanup.
- Repackaged all 16 modules after the department-role compatibility fix,
  injected the resulting JAR into
  `fool-service-backend:subject-role-20260716`, and force-recreated only the
  Compose backend. `db-migrate` exited `0`; backend `/test`, frontend `/`, and
  the unauthenticated effective-actions `401 AUTHENTICATION_REQUIRED` boundary
  all passed.
- After the mapper and nullable-company fixes, focused DAO and subject tests
  passed, the final 16-module reactor passed with 81 `fool-auth` tests, and
  `fool-service-backend:subject-null-company-20260716` was deployed.
- A fresh runtime doctor passed against that final image. Supplemental API
  preflight passed for ordinary, department-administrator,
  independent-approver, and system-administrator identities, including
  identity resolution, bearer use, menu discovery, action visibility, row
  scope, and direct-write rejection.
- Pending HIGH request `bc372af3-63b6-478e-811b-430159f820ba` is an unexecuted
  same-value two-row preview. Owner self-approval is rejected, the owner sees
  the independent-approval notice, and the independent approver is marked
  approvable.

## Runtime Evidence

- `artifacts/runs/20260715-phase4-hardening/runtime-evidence.md`
- `artifacts/runs/20260715-phase4-hardening/runtime-doctor.txt`
- `artifacts/runs/20260715-phase4-hardening/security-regression.json`
- `artifacts/runs/20260715-phase4-hardening/authorization-review.json`
- `artifacts/runs/20260715-phase4-hardening/role-api-preflight.json`
- `artifacts/runs/20260715-phase4-hardening/browser-role-matrix.md`
- `artifacts/runs/20260715-phase4-hardening/high-action-recheck.json`
- `artifacts/runs/20260715-phase4-hardening/acceptance-matrix.md`

## Risks

- The browser role matrix is blocked outside the application and is explicitly
  not claimed as passed. The current official Browser client reaches the
  registered IAB route, then every native-pipe connection is rejected with
  `failed to read peer grandparent code signing identity`. Strict macOS
  verification reports `invalid signature (code or signature have been
  modified)` for `ChatGPT.app`, its `codex` and `node_repl` helpers, and the
  Chrome extension host. Two simultaneously running ChatGPT profiles and an
  update-time mixed process state are the likely trigger; the confirmed direct
  blocker is the invalid application/helper signing state.
- Fresh image construction remains dependent on Docker Hub metadata; the local
  artifact deployment and live Compose runtime passed, but the standard build
  retry did not progress past registry metadata.
- Backend initialization retains the known legacy
  `MODEL_DEFAULTITEMVIEW not found` warning, then completes and serves the
  health probe. It is recorded as residual migration noise rather than hidden
  or counted as authorization acceptance.
- Added an authenticated Action Center and backend-owned `owned`, `approvable`,
  `executable`, and `cancellable` response flags. Approval and execution
  commands are rendered only from those server decisions; the owner receives
  the independent-approval notice instead of approval controls.
- Installed reversible `phase4-*` ordinary, department-administrator, and
  independent-approver identities. The ordinary user is explicitly limited to
  Customer 3001, while the department administrator uses the real
  `DEPARTMENT` row rule for department 3002. Fixture scope and cleanup are
  recorded in `browser-role-fixtures.md` without repository-stored credentials.
- Re-ran the complete Maven reactor after the Action Center response change:
  all 16 modules passed. Frontend validation later passed 23 files / 234 tests
  after effective-action command gating, and the production build, repository
  harness, and `git diff --check` also passed.
- Focused compatibility validation passed 10 tests covering request/approval
  department-role resolution and legacy direct/department menu SQL.

## Follow-Ups

- Fully quit both ChatGPT profiles, restore a valid official application and
  Chrome extension-host signature, and relaunch a single consistent profile.
  Then execute the four-identity matrix, save screenshots/network evidence,
  remove the reversible fixtures and pending Action Request, check the Phase 4
  task, and complete the active goal.
- Retry the standard Compose build when Docker Hub metadata access is healthy.

## Linked Commits or PRs

- Included in the `feat(agent): enforce authorization and controlled actions`
  delivery commit that contains this record.

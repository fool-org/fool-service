# Authorization and Agent Control Delivery

## Prompt

Commit the completed Agent development and update the README.

## Scope

- Consolidate the ordered Phase 0-4 implementation from
  `docs/authorization-and-agent-risk-control.md` into one reviewable commit.
- Document the stable `/agent` and `/actions` entrypoints, authorization model,
  risk boundaries, and validation surface in the root README.
- Keep Phase 4 open because the four-identity browser UI/network matrix is not
  yet available.
- Exclude the unrelated modern visual-skin work from this commit.

## Changes

- Added bearer-only authentication, effective subjects, resource/row/field
  authorization, policy freshness, audit integrity, masking, and model-outbound
  controls.
- Added the code-owned Action Catalog and immutable Action Request lifecycle for
  bounded MEDIUM and HIGH domain actions, including step-up, independent
  approval, execution rechecks, idempotency, and recovery boundaries.
- Added the Action Center and server-owned effective-action flags so frontend
  visibility and commands follow current authorization decisions.
- Added migration, permission-review, security-regression, runtime-doctor, and
  focused backend/frontend validation coverage.
- Updated README, validation guidance, task state, and Phase 4 delivery evidence
  without claiming the blocked browser gate as passed.

## Changed Files

- `README.md`, `docs/agent-sessions.md`, `docs/authorization-operations.md`,
  `docs/validation.md`, and `tasks.md`.
- `docker/mysql/init/016-authorization-and-agent-control.sql`.
- `fool-common/src/{main,test}/java/org/fool/framework/common/authz/`.
- Authentication, authorization, security, audit, credential, and test changes
  under `fool-auth/`.
- Action catalog, request state machine, Agent session/outbound policy, API, and
  test changes under `fool-agent/`.
- Dedicated controlled-action handlers and authorization enforcement under
  `fool-view/`, `fool-model/`, `fool-db-manage/`, `fool-event/`,
  `fool-error-handler/`, `fool-log/`, and `business-application/`.
- `frontend/src/ActionCenterPage*`, `frontend/src/actionWorkflow*`, and the
  bearer/effective-action changes in the existing Agent and View workflows.
- `scripts/authorization_review*`, `scripts/authorization_security_regression.py`,
  and authorization-aware `scripts/runtime_doctor*` changes.
- Phase 0-4 records under `agent_chats/2026/07/15/` and this delivery record.

## Validation

- Java 17 Docker Maven reactor: all 16 modules passed, `BUILD SUCCESS`.
- `cd frontend && npm test`: 23 files / 234 tests passed.
- `cd frontend && npm run build`: typecheck and production build passed; 301
  modules transformed.
- `python scripts/check_repo_harness.py`: passed.
- `python scripts/legacy_migration_contract.py --require-legacy`: passed with
  469 C# sources, 25 Web routes, 25 IDataService operations, and 118 Views.
- `python scripts/runtime_doctor_test.py`: 50 tests passed.
- `PYTHONPATH=scripts python -m unittest scripts/authorization_review_test.py`:
  passed.
- `docker compose ps -a`: backend/frontend up, MySQL/Redis healthy, and
  `db-migrate` exited 0.
- `python scripts/runtime_doctor.py`: complete runtime matrix passed, including
  body-token rejection, audit integrity, controlled report save, and cleanup.
- `git diff --check`: passed.

## Runtime Evidence

- `artifacts/runs/20260715-phase0-authorization/runtime-evidence.md`
- `artifacts/runs/20260715-phase1-read-authorization/runtime-evidence.md`
- `artifacts/runs/20260715-phase2-controlled-actions/runtime-evidence.md`
- `artifacts/runs/20260715-phase3-high-risk-actions/runtime-evidence.md`
- `artifacts/runs/20260715-phase4-hardening/acceptance-matrix.md`

The `artifacts/` directory is intentionally gitignored; durable summaries and
the current blocker are retained in tracked `agent_chats` and `tasks.md`.

## Skipped or Downgraded Checks

- The live `python scripts/authorization_review.py --strict` rerun was not
  started because two read-only elevation requests timed out in the automatic
  approval reviewer. Its unit test passed in this delivery, and the latest
  Phase 4 strict live review remains recorded as passed.
- The reversible live security regression was not repeated after that elevation
  boundary. The latest Phase 4 run remains recorded as passed, and the current
  runtime doctor independently rechecked policy/audit/action health.
- The four-identity browser matrix remains blocked and is not counted as passed.

## Risks

- Browser Use native-pipe authorization rejects the official client with
  `failed to read peer grandparent code signing identity`. macOS strict
  verification reports invalid signatures for the application, bundled
  helpers, and Chrome extension host.
- Reversible browser identities and one unexecuted HIGH preview remain until the
  visual/network matrix can run and clean them up.
- The known legacy `MODEL_DEFAULTITEMVIEW not found` initialization warning
  remains non-fatal and outside this authorization acceptance boundary.

## Follow-Ups

- Restore a consistently signed official ChatGPT application/plugin and run the
  ordinary-user, department-administrator, independent-approver, and
  system-administrator browser matrix.
- Save UI and network evidence, remove the reversible fixtures and pending HIGH
  request, then close the Phase 4 task and active goal.
- Commit the excluded modern visual-skin work separately after its own scope
  review.

## Task-State Link or Update

- `tasks.md` keeps Phase 4 unchecked and records the exact external Browser
  signing blocker and remaining acceptance gate.

## Linked Commits or PRs

- Included in the `feat(agent): enforce authorization and controlled actions`
  commit that contains this record.

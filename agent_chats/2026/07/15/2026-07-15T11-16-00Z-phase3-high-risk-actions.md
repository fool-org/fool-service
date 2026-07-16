# Phase 3 High-Risk Actions

## Prompt

Follow `docs/authorization-and-agent-risk-control.md` in order and complete the
Agent-related development.

## Scope

- Added recent step-up, independent approval, and two-person approval rules.
- Onboarded the design's bounded HIGH actions with one handler per domain
  action and no generic SQL/code executor.
- Added message outbox/idempotency and explicit recovery evidence.

## Changes

- Added Redis-backed step-up proof, BCrypt-only reauthentication, current-subject
  approval rechecks, hash-bound approval records, self-approval rejection, and
  one-/two-person policies.
- Added dedicated delete, bounded bulk update, View Operation, additive DDL,
  data-source route, credential-reference, event-enable, and message-send
  handlers.
- Added a data-source action auto-configuration boundary, credential-reference
  storage, message outbox, unique approval constraints, HIGH permissions, and
  an unassigned approver role.
- Added explicit frontend helpers for step-up, approval, and owner execution;
  Chat displays an action request ID but never approves or executes it.

## Validation

- Java compile/package and focused HIGH approval/step-up tests passed.
- Focused frontend tests passed.
- Eight distinct HIGH runtime workflows succeeded with the expected approval
  counts, persisted results, audit traces, and post-test recovery.

## Runtime Evidence

- `artifacts/runs/20260715-phase3-high-risk-actions/runtime-evidence.md`

## Risks

- DDL is intentionally additive only and documents manual recovery because
  MySQL may implicitly commit DDL.
- Message recovery can cancel generated messages; already processed messages
  require human follow-up.

## Follow-Ups

- Remove body-token compatibility, add tamper-evident audit verification and
  automatic catalog/runtime drift gates, then run the full Phase 4 matrix.

## Linked Commits or PRs

- None; the active goal continues into Phase 4 in the current worktree.

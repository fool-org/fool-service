# Phase 1 Unified Read Authorization

## Prompt

Follow `docs/authorization-and-agent-risk-control.md` in order and complete the
Agent-related development.

## Scope

- Completed Phase 1 resource, row, field, quota, masking, menu, report, and
  Agent read-policy enforcement.
- Completed the report, form/View, and target-table schema preview gates.
- Completed outbound classification/provider policy and strict ActionIntent
  parsing while keeping all mutations behind the Phase 2 hard gate.

## Changes

- Added structured `DataPolicy` loading and row-policy compilation into
  parameterized filters; enforced readable/filterable/sortable fields, query
  caps, detail scope, and result masks across View/query/report paths.
- Filtered legacy menus and View operations through `view.discover` and
  `operation.execute`, with fail-closed security audit records.
- Applied the same authorization decision and policy to Agent metadata and its
  bounded runtime query preview.
- Added deterministic report, form/View, and DDL schema gates with evidence
  hashes and explicit `PASSED`/`BLOCKED` status.
- Added outbound minimization, forced restricted-field handling, provider
  allowlists/local fallback, and an allowlisted ActionIntent parser.
- Kept direct legacy writes blocked with HTTP 403 until they are moved onto the
  controlled Action Request state machine.

## Validation

- Focused Java 17 authorization/security/Agent matrix: 40 passed, 0 failed.
- Java 17 business application package: passed.
- Migration `016` replay: passed; `db-migrate-1` exited 0.
- Docker `/test`: HTTP 200.
- Query, report, and Agent preview each returned total 8 and rows 8 for View
  `100`; reportable fields matched across report model and Agent.
- Direct View and Agent form field IDs matched exactly; operations were denied
  in both paths; all three preview gates passed.

## Runtime Evidence

- `artifacts/runs/20260715-phase1-read-authorization/runtime-evidence.md`

## Risks

- Body-token compatibility remains intentionally active through Phase 3 and is
  removed in Phase 4.
- Legacy direct writes are intentionally unavailable until Phase 2 handlers
  preserve their domain semantics behind immutable previews and confirmation.

## Follow-Ups

- Implement the Action Request state machine and onboard the bounded MEDIUM
  actions before any write endpoint is reopened.

## Linked Commits or PRs

- None; the active goal continues into Phase 2 in the current worktree.

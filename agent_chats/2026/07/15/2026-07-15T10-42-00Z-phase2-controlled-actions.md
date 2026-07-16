# Phase 2 Controlled Actions

## Prompt

Follow `docs/authorization-and-agent-risk-control.md` in order and complete the
Agent-related development.

## Scope

- Implemented the immutable Action Request protocol and first bounded MEDIUM
  actions.
- Moved direct Vue mutations and Chat-created intents onto one state machine.
- Preserved legacy write blocks until each operation has a dedicated handler.

## Changes

- Added the code-owned Action Catalog, deterministic risk engine, canonical JSON
  hashing, preflight, immutable preview, confirmation TTL, conditional state
  transitions, idempotency, execution recheck, and fail-closed audit boundary.
- Added dedicated `report.save`, bounded `report.export`, and single-object
  `data.create` / `data.update` handlers with row, field, object-version, and
  policy-version revalidation.
- Added neutral `/api/v1/actions` APIs and explicit Vue confirmation while
  keeping compatibility body tokens out of new action payloads.
- Allowed Chat to propose a strict `ActionIntent` and create a request while
  preventing the model from confirming or executing it.

## Validation

- Focused Java tests: 8 passed.
- Frontend tests: 229 passed; frontend build passed.
- Java 17 business application package, migration replay, Docker health, four
  successful action flows, conflict/replay/bypass checks, audit queries, and
  cleanup checks passed.

## Runtime Evidence

- `artifacts/runs/20260715-phase2-controlled-actions/runtime-evidence.md`

## Risks

- HIGH actions remain unavailable until recent step-up and independent approval
  are enforced by Phase 3.
- Compatibility body-token handling remains temporary and is removed in Phase 4.

## Follow-Ups

- Add HIGH action approval rules and onboard only dedicated bounded handlers.

## Linked Commits or PRs

- None; the active goal continues into Phase 3 in the current worktree.

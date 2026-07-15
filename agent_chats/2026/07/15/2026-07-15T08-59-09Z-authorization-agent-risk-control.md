# Authorization And Agent Risk Control Design Baseline

## Prompt

Commit the remaining repository changes after completing the FoolFrame
migration closeout.

## Scope

- Established `docs/authorization-and-agent-risk-control.md` as the design
  source for authentication, authorization, data scope, model outbound policy,
  risk classification, approval, execution, and audit controls.
- Linked the existing Agent session boundary to the new security design without
  claiming that the planned controls are already implemented.
- Added phased implementation tasks and concrete acceptance criteria to the
  repository task board.

## Changed Files

- `docs/authorization-and-agent-risk-control.md`
- `docs/agent-sessions.md`
- `tasks.md`
- `agent_chats/2026/07/15/2026-07-15T08-59-09Z-authorization-agent-risk-control.md`

## Validation

- `python scripts/check_repo_harness.py` — passed.
- `git diff --check` — passed.

## Runtime Evidence

- Not applicable. This slice defines architecture, implementation phases, and
  acceptance criteria; it does not enable or alter runtime authorization.

## Risks

- The documented Phase 0 through Phase 4 controls remain open work. Existing
  Agent preview and draft language must not be treated as enforceable approval
  or authorization until the corresponding tasks pass their acceptance gates.
- Direct API, UI, report, Agent, and scheduler paths still require the planned
  shared deny-by-default enforcement before medium- or high-risk actions are
  enabled.

## Follow-Ups

- Start with the Phase 0 identity and deny-by-default authorization foundation
  tracked in `tasks.md`.
- Commit future implementation phases with focused tests and runtime evidence
  for each newly enabled action.

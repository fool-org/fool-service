# Candidate View Transport State

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced the collection picker from the rendered child View into
  `detailview.js initQueryView()` before changing Vue.
- Reused the shared action options to preserve pending state only for a silent
  candidate View transport failure.
- Retained View-before-data ordering, linked View ids, candidate columns,
  candidate query state, payloads, routes, DTOs, and components.
- Added no component, helper, state owner, request type, or dependency.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/useViewDataWorkflow.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T20-07-00Z-candidate-view-transport.md`

## Validation

- `cd frontend && npm test -- --run payload.test.ts` (84/84 passed)
- `cd frontend && npm test` (19 files, 195 tests passed)
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py`
- `git diff --check`
- Pending Docker deployment and authorized browser acceptance.

## Risks And Follow-ups

- Browser acceptance should force only the child candidate `getlistview`
  request to HTTP 502, prove the loader remains without shared feedback, then
  reload without the failure and prove the linked View opens before data query.
- Unrelated README/POM, agent-session, `docs/superpowers/`, and `fool-agent/`
  work remains untouched and excluded from this delivery.

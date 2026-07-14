# Operation Transport Handling

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced the old `operation.js` request and `showdetailinfo` success callback
  before changing Vue.
- Reused `runAction`'s existing `silentTransport` option for the one
  View-derived operation request.
- Retained response-backed results, edit-state guards, operation metadata,
  payloads, routes, DTO bindings, and result presentation.
- Added no component, helper, request type, or dependency.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T19-04-27Z-operation-transport.md`

## Validation

- `cd frontend && npm test -- --run payload.test.ts` (1 file, 83 tests passed)
- `cd frontend && npm test` (19 files, 193 tests passed)
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py`
- `git diff --check`
- Pending Compose deployment and authorized stopped-backend browser acceptance.

## Risks And Follow-ups

- Browser acceptance should load a real detail View, stop the backend, invoke
  its seeded operation, prove no shared error/result dialog appears, restore
  the backend, and prove the existing result dialog still opens on success.
- Response-backed business errors remain visible through the legacy result
  path; this slice suppresses only transport failures.
- `docs/superpowers/` is unrelated untracked work and remains untouched.

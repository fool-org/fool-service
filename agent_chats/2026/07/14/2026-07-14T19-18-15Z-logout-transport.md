# Logout Transport Handling

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced old `menuinfo.js` logout success callback before changing Vue.
- Reused `runAction`'s existing `silentTransport` option for logout.
- Retained successful root-route replacement, session cleanup, login metadata
  refresh, shell controls, payloads, routes, and DTOs.
- Added no component, helper, request type, or dependency.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T19-18-15Z-logout-transport.md`

## Validation

- `cd frontend && npm test -- --run payload.test.ts` (1 file, 83 tests passed)
- `cd frontend && npm test` (19 files, 193 tests passed)
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py`
- `git diff --check`
- Pending Compose deployment and authorized stopped-backend browser acceptance.

## Risks And Follow-ups

- Browser acceptance should stop the backend, attempt safe logout, prove the
  authenticated View/token/URL remain without a shared error, restore the
  backend, and prove a successful retry returns to fresh login.
- Response-backed logout errors are outside this transport-only slice.
- `docs/superpowers/` is unrelated untracked work and remains untouched.

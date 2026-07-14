# Legacy Message Poll Concurrency

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced the old `timer.js` registration loop and `message.js` callback before
  changing Vue.
- Removed the Vue-only in-flight gate so every 15-second tick may start
  `getmsg`, matching the old timer during slow requests.
- Retained the token guard, silent transport handling, session timer cleanup,
  first-message behavior, protocol adapters, routes, and DTO bindings.
- Added no component, helper, request type, or dependency.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T18-55-51Z-legacy-message-poll-concurrency.md`

## Validation

- `cd frontend && npm test -- --run payload.test.ts` (1 file, 83 tests passed)
- `cd frontend && npm test` (19 files, 193 tests passed)
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py`
- `git diff --check`
- Pending Compose deployment and authorized paused-backend browser acceptance.

## Risks And Follow-ups

- Browser acceptance should authenticate with the authorized local CAPTCHA,
  pause the backend for more than two polling periods, prove multiple pending
  `getmsg` requests, resume it, and confirm the shell remains usable.
- Overlapping message requests can complete out of order, which matches the old
  timer and `$http` behavior rather than adding a new ordering guarantee.
- `docs/superpowers/` is unrelated untracked work and remains untouched.

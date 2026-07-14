# Getmain Transport Handling

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced the authenticated old `index` route through `soway.getmain` and the
  shared server-side `postandget` error handler before changing Vue.
- Reused `runAction`'s existing transport classification and error state.
- Preserved the authenticated shell and token only for `getmain` transport
  failures while retaining the existing stale-token business-error recovery.
- Retained successful main metadata, View-first loading, payload, route, DTO,
  components, and login behavior.
- Added no ref, component, helper, request type, state owner, or dependency.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T19-48-26Z-getmain-transport.md`

## Validation

- `cd frontend && npm test -- --run src/payload.test.ts` (1 file, 83 tests passed)
- `cd frontend && npm test` (19 files, 193 tests passed)
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py`
- `git diff --check`
- Built `c4941f64` from a clean `git archive` snapshot and deployed frontend
  image `sha256:34e3ed81d17b883b95ce24070d5639cc64e43b3f95777f49e16392f0d8124da8`.
- Authorized isolated-browser acceptance read the local CAPTCHA and logged in
  with `admin/admin`. The deployed `index-BWT3CjNo.js` bundle received a
  deterministic HTTP 502 from `getmain`; `/main`, local token, and `.app-shell`
  remained while the login form, `发生错误`, and transport text stayed absent.
- Removing the failure returned HTTP 200 from `getmain` and restored Admin and
  Order List. After safe logout, a fake stored token produced an HTTP-200
  business error, cleared local storage, and rendered the login form.
- `python scripts/runtime_doctor.py` passed all 67 checks with Compose healthy
  and `db-migrate` at `Exited (0)`.

## Risks And Follow-ups

- The browser used a deterministic HTTP 502 so it could verify this branch
  without competing with the enum slice for the shared Compose backend. The
  deployed fetch/non-2xx path and the live success/business-error paths were
  all exercised.
- Vue keeps its existing stale-token return to login even though FoolFrame's
  root route passes a response-backed `getmain` error to Express `next()`.
- Concurrent agent-session work in Maven files, `fool-agent/`, README, and
  `docs/agent-sessions.md` remains unrelated and untouched.

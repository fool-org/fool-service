# Initapp Transport Handling

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced the old signed-out `index` route through `soway.initapp` and its shared
  server-side `postandget` error handler before accepting the Vue change.
- Reused `runAction`'s existing `silentTransport` option for only `initapp`.
- Retained the static fallback login shell, successful app/CAPTCHA/database
  adapters, login behavior, request payload, route, and DTO.
- Added no component, helper, request type, state owner, or dependency.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T19-37-45Z-initapp-transport.md`

## Validation

- `cd frontend && npm test -- --run payload.test.ts` (1 file, 83 tests passed)
- `cd frontend && npm test` (19 files, 193 tests passed)
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py`
- `git diff --check`
- Pending Docker deployment and authorized stopped-backend browser acceptance.

## Risks And Follow-ups

- Browser acceptance should stop the backend while signed out, reload the root,
  prove `initapp` settles without a browser error dialog, restore the backend,
  and prove reload recovers application metadata and CAPTCHA.
- Vue intentionally keeps a static retryable login shell instead of reproducing
  FoolFrame's server-render navigation that never receives an error callback.
- `docs/superpowers/` is unrelated untracked work and remains untouched.

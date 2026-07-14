# Detail Save Transport Handling

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced `detailView.jade` into `detailview.js beginsave()` before changing the
  Vue save state.
- Preserved the old non-dismissible `保存中` dialog on `saveobj` and
  `savenewobj` transport failures without shared error feedback.
- Retained response-backed business errors, successful save behavior, staged
  child changes, View-derived fields and identity, and post-save back
  navigation.
- Reused `runAction`'s existing transport classification and error state; added
  no state owner, helper, abstraction, component, request type, or dependency.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T20-02-23Z-detail-save-transport.md`

## Validation

- `cd frontend && npm test -- --run src/payload.test.ts` (1 file, 83 tests
  passed)
- `cd frontend && npm test` (19 files, 193 tests passed)
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py`
- `git diff --check`
- Built implementation commit `ecb8592e` from a clean archive and deployed it
  as tagged frontend image
  `sha256:67ce4a79d9e15cb8de295ac221b0a0479c47a734325915fab5c5eee61baa8f1c`.
  The running HTML referenced entry bundle `index-CYqoDW3m.js`.
- Authorized isolated-browser acceptance read the local CAPTCHA and logged in
  with `admin/admin`. On `/view102/1001`, a deterministic `saveobj` HTTP 502
  kept the route and one `保存中` dialog visible with zero `发生错误` dialogs and
  zero transport-error text. Removing the interception made the same unchanged
  save return HTTP 200 and complete the legacy back navigation to `/main`.
- A second isolated context forced `savenewobj` HTTP 502 on `/new102`; the same
  non-dismissible save dialog remained without shared/transport feedback, and
  the intercepted request did not reach the backend. Both sessions logged out.
- MySQL held eight orders and four order items before and after acceptance and
  runtime verification. Order 1001 remained `BTC-USDT`, state `0`, customer
  `3001`, amount `0.2500000000`, and price `62500.0000000000`; its three child
  rows were unchanged.
- `python scripts/runtime_doctor.py` passed all 67 checks with Compose healthy
  and `db-migrate` at `Exited (0)`.

## Risks And Follow-ups

- Failure injection covered `saveobj` and `savenewobj` independently without
  stopping the shared backend used by concurrent work. Live login, detail
  loading, existing save success, navigation, and persistence were exercised
  through Nginx; new-save recovery remains covered by runtime-doctor checks to
  avoid adding a browser-created order.
- The deliberately pending transport dialog requires a reload before retry;
  this is retained FoolFrame behavior, not a new recovery affordance.
- Concurrent runtime-doctor runs briefly exposed their temporary rows; both
  cleanup paths completed and final counts returned to the baseline.
- Concurrent agent-session work in Maven files, `fool-agent/`, README,
  `docs/agent-sessions.md`, and its own delivery evidence remains unrelated and
  untouched.

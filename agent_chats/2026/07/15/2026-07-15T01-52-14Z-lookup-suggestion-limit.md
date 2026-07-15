# Lookup Suggestion Limit

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Rechecked the BusinessObject editor in
  `../FoolFrame/src/Web/public/javascripts/app/setextype.js` before following
  its `inputquery` data path.
- Restored the old typeahead dataset `limit: 5` in the shared Vue metadata
  editor. No protocol DTO, business model, state module, or component was
  added.
- Confirmed the normal backend candidate query already uses page size five,
  while owner `Source` collections can return more and therefore still need
  the old client-side display limit.
- Browser-verified exact implementation commit `c43a8354` on Docker.

## Changed Files

- `frontend/src/MetadataFieldEditor.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/15/2026-07-15T01-52-14Z-lookup-suggestion-limit.md`

## Validation

- `cd frontend && npm test -- src/payload.test.ts` (85 tests passed)
- `cd frontend && npm test` (20 files, 214 tests passed)
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py`
- `python scripts/runtime_doctor.py` (68 checks passed)
- Frontend `/` and backend `/test` returned HTTP 200. Compose kept
  backend/frontend/MySQL/Redis up, MySQL/Redis healthy, and `db-migrate` at
  `Exited (0)`.
- Authorized `admin/admin` acceptance loaded `/view100/1001`, with
  `getreaditemview` before `querydatadetail`, then entered edit mode through
  the rendered detail View.
- A one-request CDP interception returned seven generic `inputquery`
  candidates without changing the database. The deployed editor rendered
  exactly Candidate One through Candidate Five plus `查找更多`; candidates six
  and seven were absent.
- At 1280x720 the suggestion menu aligned directly below the Customer input.
  On a fresh 390x844 load it flipped above the input because of available
  space: overlay bottom 612, input top 614, and document width equaled the
  390px viewport.
- After disabling interception, a real `Ada` query returned and rendered
  `Ada Capital - 3001`. Browser warnings and page errors were empty. The login
  token was logged out and the isolated test tab was closed.
- MySQL retained 8 `market_order` rows and 4 `market_order_item` rows. Order
  1001 remained `BTC-USDT`, state `0`, customer `3001`, amount `0.25`, and
  price `62500`.
- Runtime image:
  `sha256:242a1315cae58d1ed78599b27cc8cf540b300b55cd79bc8ca24ae91636a3493a`.
- Visible evidence:
  - `artifacts/runs/20260715-lookup-limit/desktop-five-suggestions.png`
    (`1280x720`, SHA-256
    `44978a763b9278a58571b679174d9030e5c269150ee6ca3c1255c2105e096700`).
  - `artifacts/runs/20260715-lookup-limit/mobile-five-suggestions.png`
    (`390x844`, SHA-256
    `65de5f2f248f6d42c42e0e2498e3c377eadbae0b67311bef5888b6f3bd4f53e2`).

## Risks And Follow-ups

- The standard Compose build remained unavailable because local Buildx cannot
  update its activity file (`operation not permitted`). The host-validated
  `dist` was copied into a clean temporary container from the existing Nginx
  image, committed, and used to recreate the frontend.
- Screenshot files are intentionally ignored by Git; their paths and hashes are
  recorded above.
- `docs/superpowers/` remains unrelated and untracked; this slice did not edit
  or stage it.

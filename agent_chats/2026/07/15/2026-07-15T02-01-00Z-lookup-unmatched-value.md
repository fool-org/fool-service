# Lookup Unmatched Value

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced `detailView.jade` into `setextype.js` and `savetext.js` before
  comparing Vue draft/save behavior.
- Confirmed the old page changes a BusinessObject id only on
  `typeahead:select`; non-empty unmatched text retains the prior id at save,
  while an empty input clears it.
- Removed PrimeVue `force-selection` from the existing shared metadata editor.
  No save builder, protocol DTO, business model, state module, or component was
  added.
- Browser-verified exact implementation commit `d8658f70` on Docker.

## Changed Files

- `frontend/src/MetadataFieldEditor.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/15/2026-07-15T02-01-00Z-lookup-unmatched-value.md`

## Validation

- `cd frontend && npm test -- src/payload.test.ts` (85 tests passed)
- `cd frontend && npm test` (20 files, 214 tests passed)
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py`
- `python scripts/runtime_doctor.py` (68 checks passed)
- Compose kept backend/frontend/MySQL/Redis up, MySQL/Redis healthy, and
  `db-migrate` at `Exited (0)`.
- Authorized `admin/admin` acceptance loaded `/view100/1001` View-first,
  entered edit mode, typed `No Such Customer`, waited for the empty lookup
  result, and blurred the input. The unmatched text remained visible.
- The real Vue save path was captured before transport. Its unmatched payload
  retained `{ "key": "customer", "value": "3001" }`; a fresh explicit
  clear produced `{ "key": "customer", "value": "" }`.
- The final two save responses were supplied by a one-page `window.fetch`
  proxy, so neither acceptance request reached the backend. Both flows returned
  through the existing save dialog lifecycle to `/`.
- Browser warnings and page errors were empty. The login token was logged out
  and the isolated tab was closed.
- MySQL retained 8 `market_order` rows and 4 `market_order_item` rows. Order
  1001 remained `BTC-USDT`, state `0`, customer `3001`, amount `0.25`, and
  price `62500`.
- Runtime image:
  `sha256:0beb64454641fbd7e9f86c44c4592be81f822f08970e6585b50bffd9ae7e92da`.
- Visible evidence:
  - `artifacts/runs/20260715-lookup-blur/unmatched-after-blur.png`
    (`1280x720`, SHA-256
    `072628724671f3e5260749c00a5210db2dfc45d4ce81f58298d6e3d8fc744be6`).

## Risks And Follow-ups

- Two preliminary CDP `Fetch` interception attempts did not pause `saveobj`;
  each reached the backend with the unchanged customer id `3001`. Database
  snapshots after those attempts and after final acceptance showed no drift.
- The standard Compose build remained unavailable because local Buildx cannot
  update its activity file (`operation not permitted`). The host-validated
  `dist` was injected into the existing Nginx image for runtime acceptance.
- Screenshot files are intentionally ignored by Git; the valid artifact path
  and hash are recorded above.
- `docs/superpowers/` remains unrelated and untracked; this slice did not edit
  or stage it.

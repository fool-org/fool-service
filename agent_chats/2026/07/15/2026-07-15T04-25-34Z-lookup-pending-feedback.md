# Lookup Pending Feedback

## Prompt

Continue aligning old layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Rechecked both BusinessObject typeahead construction paths in old
  `setextype.js` before following their `inputquery` data path.
- Restored the old visible `正在查询....` state in the shared metadata editor.
- Cleared prior candidates when a new query starts so waiting feedback cannot
  be mixed with stale results.

## Commits

- `d8c5c099 fix(frontend): restore lookup pending feedback`
- This delivery-evidence commit.

## Changed Files

- `frontend/src/MetadataFieldEditor.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/15/2026-07-15T04-25-34Z-lookup-pending-feedback.md`

## Validation

- `cd frontend && npm test` (20 files, 219 tests passed).
- `cd frontend && npm run build` (passed).
- `python scripts/check_repo_harness.py` (passed).
- `python scripts/runtime_doctor.py` (69 checks passed).
- Frontend `/` and backend `/test` returned HTTP 200. Compose kept
  backend/frontend/MySQL/Redis up, MySQL/Redis healthy, and `db-migrate` at
  `Exited (0)`.

## Runtime Evidence

- Authorized `admin/admin` acceptance loaded `/view100/1001` through
  `getreaditemview` before `querydatadetail`, then entered View-derived edit
  mode and issued real `inputquery` requests.
- CDP paused each request while the deployed Vue editor rendered
  `正在查询....`; the pending state contained zero stale candidate elements.
- Releasing a seven-row protocol response restored exactly five visible
  candidates through the already-migrated old client-side limit.
- Desktop input/overlay bounds both measured x=40..634 at 1280px. Mobile
  input/overlay bounds both measured x=30..360 at 390px.
- Desktop and mobile document widths matched their 1280px / 390px viewports.
  Browser runtime/log event count was zero.
- MySQL retained 8 `market_order` rows and 4 `market_order_item` rows. Order
  1001 remained `BTC-USDT`, state `0`, customer `3001`, amount `0.25`, and
  price `62500`.
- Runtime image:
  `sha256:70d0de41d699b7e0159f34deee3aa60a6f1356e04faa1207336020bcc8eb67b9`.
- Host and container `index.html` SHA-256:
  `9a6c002d56a9b05df6526cf0534170384cd0e773bfd341ff851ceec928d14b3c`.
- Visible evidence:
  - `artifacts/runs/20260715-lookup-pending/desktop-pending.png`
    (`6fb1c69ab9ccbdabad3d3f99c85cfa50cbe0ad608c46d9ad9b60e3848a9697e1`).
  - `artifacts/runs/20260715-lookup-pending/mobile-pending.png`
    (`452c9de36dfbe7b2069d888cc89fe199f0f676b1443cc5116b7611dfc7b19592`).

## Risks And Follow-ups

- `docker compose build frontend` still fails before context transfer because
  Buildx cannot update its activity file (`operation not permitted`). The
  host-validated clean `dist` was injected into a temporary Nginx container,
  committed, and used to recreate the frontend.
- Screenshot files are ignored by Git; paths and hashes are recorded above.
- `docs/superpowers/` remains unrelated and untracked; it was not edited or
  staged.

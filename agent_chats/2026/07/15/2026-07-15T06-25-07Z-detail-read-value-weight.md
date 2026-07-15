# Detail Read Value Weight

## Prompt

Continue aligning old layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Compared `detailView.jade` read-value markup with the deployed shared detail
  renderer before changing component or data behavior.
- Restored normal paragraph weight for read values while retaining emphasized
  View-derived labels and the existing responsive field geometry.
- Added explicit shared label/value classes; no page-specific View id, concrete
  business DTO, new component, or request branch was introduced.

## Changed Files

- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/ViewDetailPanel.test.ts`
- `frontend/src/style.css`
- `frontend/src/style.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/15/2026-07-15T06-25-07Z-detail-read-value-weight.md`

## Validation

- `cd frontend && npm test` (21 files, 225 tests passed).
- `cd frontend && npm run build` (passed).
- `PYTHONPATH=scripts python scripts/legacy_view_matrix.py` (118/118 passed).
- `python scripts/runtime_doctor.py` (69 checks passed after baseline replay).
- `python scripts/check_repo_harness.py` (passed).
- `git diff --check` (passed).

## Runtime Evidence

- Old `detailView.jade` places formatted values in ordinary `<p>` elements;
  Vue previously rendered every read value in `<strong>` at weight 700.
- On deployed `/view112/1`, the first value changed from `STRONG/700` to
  `SPAN/400`; its label remained at weight 750 on desktop and mobile.
- The desktop field tracks remained 192.7px / 385.3px, and the 390px layout
  retained one 326px stacked track.
- Both runs loaded `getreaditemview(112)` before
  `querydatadetail(112,1)` and `getenums`; they emitted zero write requests.
- Document/viewport widths matched at 1280/1280 and 390/390; browser
  runtime/log events were zero.
- MySQL retained admin id 1, 8 orders, and 4 order items after acceptance.
- Host and container `index.html` SHA-256 matched at
  `3f8ff4bbd0fec0ebc922343ca743878d11c17517a3c640a75f07ba2c65112417`.
- Runtime frontend image:
  `sha256:02741803f9cc964b7bbfca31ae2759c1a3923d52871b6c4c9ba1768afa14097f`.
- Visible evidence:
  - `artifacts/runs/20260715-detail-read-value/desktop-aligned.png`
    (`9e3845b4bd8ab9eeed50ef6d5a604d4d512a70336656cf3c9aed7ce6d4b8fdc3`).
  - `artifacts/runs/20260715-detail-read-value/mobile-aligned.png`
    (`e73514b1ef44e1e7aebe5a1ca9d572bbecd14f08d37255ded589f3f9fade73ce`).

## Skipped Or Downgraded Checks

- `docker compose build frontend` stopped before context transfer with
  `failed to update builder last activity time` and `operation not permitted`
  under `.docker/buildx/activity`. The validated clean `dist` was injected
  into a fresh Nginx base image and the Compose frontend was force-recreated.

## Risks And Follow-ups

- This change only removes invented value emphasis; it intentionally retains
  the existing accessible inline markup instead of restoring paragraph margins.
- Screenshot files are ignored by Git; paths and hashes are recorded above.
- `docs/superpowers/` remains unrelated and untracked; it was not edited or
  staged.

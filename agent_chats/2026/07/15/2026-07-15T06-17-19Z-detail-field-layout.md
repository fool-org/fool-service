# Detail Field Pair Layout

## Prompt

Continue aligning old layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Compared the rendered detail editor with `detailView.jade` before changing
  data or component behavior.
- Restored the old desktop label/control proportions for all shared detail
  fields while retaining the existing responsive mobile stack.
- Kept the change in the shared stylesheet and added no page-specific View id,
  concrete business DTO, component, or request branch.

## Changed Files

- `frontend/src/style.css`
- `frontend/src/style.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/15/2026-07-15T06-17-19Z-detail-field-layout.md`

## Validation

- `cd frontend && npm test` (21 files, 223 tests passed).
- `cd frontend && npm run build` (passed).
- `PYTHONPATH=scripts python scripts/legacy_view_matrix.py` (118/118 passed).
- `python scripts/runtime_doctor.py` (69 checks passed after baseline replay).
- `python scripts/check_repo_harness.py` (passed).
- `git diff --check` (passed).

## Runtime Evidence

- Old `detailView.jade` emits `.col-md-2` then `.col-md-4` for every simple
  field, which creates two 1:2 label/control pairs per Bootstrap desktop row.
- Before alignment, `/view112/1` put the label and editor in one 594px column,
  with the input below the label even at 1280px.
- After alignment, the first desktop edit pair used measured grid tracks of
  192.7px and 385.3px. At 390px it retained one 326px stacked track.
- Both runs loaded `getreaditemview(112)` before
  `querydatadetail(112,1)` and `getenums`; they emitted zero write requests.
- Document/viewport widths matched at 1280/1280 and 390/390; browser
  runtime/log events were zero.
- MySQL retained admin id 1, 8 orders, and 4 order items after acceptance.
- Host and container `index.html` SHA-256 matched at
  `167c2a2e433859bb095248909f3762b8f1c53347d2c187444fa8f2d2cf359823`.
- Runtime frontend image:
  `sha256:3360869fcdc4dcb20234fa0835e39c622a205018940fb90763b9901e986cba6f`.
- Visible evidence:
  - `artifacts/runs/20260715-detail-field-layout/desktop-aligned.png`
    (`ed53d4883f948be1ebf68ea3d79adc2e1ec2e4d992ca8ac19e2d0f76e7a67733`).
  - `artifacts/runs/20260715-detail-field-layout/mobile-aligned.png`
    (`93136e0bdb26303b3629218557e63b9dec0a049ebe1ff8e587b0b57196d5a139`).

## Skipped Or Downgraded Checks

- `docker compose build frontend` stopped before context transfer with
  `failed to update builder last activity time` and `operation not permitted`
  under `.docker/buildx/activity`. The validated clean `dist` was injected
  into the existing Nginx image and the Compose frontend was force-recreated.

## Risks And Follow-ups

- The responsive rule intentionally keeps each field stacked below 640px; it
  does not attempt to reproduce Bootstrap's historical 992px breakpoint.
- Screenshot files are ignored by Git; paths and hashes are recorded above.
- `docs/superpowers/` remains unrelated and untracked; it was not edited or
  staged.

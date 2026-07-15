# Detail Field Breakpoint

## Prompt

Continue aligning old layout, style, and interactions in atomic commits. Start
from the rendered View and avoid business DTO bindings.

## Scope

- Compared `../FoolFrame/src/Web/views/detailView.jade` with the deployed
  `/view112/1` detail route at desktop, tablet, and mobile widths.
- Restored Bootstrap's md boundary for the old `.col-md-2 + .col-md-4` field
  layout by stacking outer fields and inner label/value tracks below 992px.
- Kept the shared View-derived component, metadata/data merge, and request
  sequence unchanged.

## Changed Files

- `frontend/src/style.css`
- `frontend/src/style.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/15/2026-07-15T06-44-35Z-detail-field-breakpoint.md`

## Validation

- `cd frontend && npm test` (21 files, 226 tests passed).
- `cd frontend && npm run build` (passed).
- `PYTHONPATH=scripts python scripts/legacy_view_matrix.py` (118/118 passed).
- `python scripts/runtime_doctor.py` (69 checks passed).
- `docker compose run --rm db-migrate` (passed).
- `python scripts/check_repo_harness.py` (passed).
- MySQL baseline: admin id 1, 8 orders, and 4 order items.

## Runtime Evidence

- At 1280px, the first outer field pair remained 594px and its inner tracks
  remained 192.7px / 385.3px.
- At 768px, the first outer field changed from 338px to 688px and its value
  track changed from 212px beside the label to a 684px stacked row.
- At 390px, the existing 330px outer and 326px inner stack was unchanged.
- Each run loaded `getmain`, `getreaditemview`, `querydatadetail`, and
  `getenums` in the expected View-first order and emitted zero writes.
- Document widths matched all three viewports; browser runtime/log events were
  zero.
- Host and container `index.html` SHA-256 matched at
  `aa5177d26d2da5c2398ad1d628c800ccda9a577da3f971c556f2e638ef05b4d6`.
- Runtime frontend image:
  `sha256:0ef28b0bee175d22b56f60481a4d55034619a204494609103c4c51752fe1280d`.
- Visible evidence:
  - `artifacts/runs/20260715-detail-breakpoint/desktop-aligned.png`
    (`9e3845b4bd8ab9eeed50ef6d5a604d4d512a70336656cf3c9aed7ce6d4b8fdc3`).
  - `artifacts/runs/20260715-detail-breakpoint/tablet-aligned.png`
    (`2ddc7bbe70b7daa35e66470b975daad743e00fffe5e6ad4a1108aea3088c53a9`).
  - `artifacts/runs/20260715-detail-breakpoint/mobile-aligned.png`
    (`e73514b1ef44e1e7aebe5a1ca9d572bbecd14f08d37255ded589f3f9fade73ce`).

## Skipped Or Downgraded Checks

- `docker compose build frontend` stopped before context transfer with
  `failed to update builder last activity time` and `operation not permitted`
  under `.docker/buildx/activity`. The validated clean `dist` was injected
  into a fresh Nginx base image and the Compose frontend was force-recreated.

## Risks And Follow-ups

- This restores the old Bootstrap md behavior globally for imported detail
  Views; no View-specific override was added.
- Screenshot files are ignored by Git; paths and hashes are recorded above.
- Unrelated authorization design/task edits and `docs/superpowers/` were not
  edited or staged.

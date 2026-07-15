# Detail Edit Label Style

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits.
Keep the implementation View-first, reusable, and free of business DTO
bindings.

## Scope

- Compared `detailView.jade`'s shared label element with the deployed
  `/view112/1` edit surface.
- Reused the aligned neutral 14px detail label rule for edit labels instead of
  adding a duplicate style block.
- Changed no component, metadata, editor geometry, request, or data binding.

## Changed Files

- `frontend/src/style.css`
- `frontend/src/style.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/15/2026-07-15T07-22-29Z-detail-edit-label-style.md`

## Validation

- `cd frontend && npm test` (21 files, 227 tests passed).
- `cd frontend && npm run build` (passed).
- `docker compose run --rm db-migrate` (passed).
- `PYTHONPATH=scripts python scripts/legacy_view_matrix.py` (118/118 passed).
- `python scripts/runtime_doctor.py` (69 checks passed).
- `python scripts/check_repo_harness.py` (passed).
- MySQL baseline: admin id 1, 8 orders, and 4 order items.

## Runtime Evidence

- At 1280px, 768px, and 390px, the first edit label changed from
  `12.6px / rgb(71, 85, 105) / 700` to
  `14px / rgb(51, 51, 51) / 700`.
- The outer/editor widths remained 594px / 385.3px, 688px / 684px, and
  330px / 326px at the same three widths.
- Each run loaded `getmain`, `getreaditemview`, `querydatadetail`, and
  `getenums` in View-first order and emitted zero writes.
- Document widths matched all viewports; browser runtime/log events were zero.
- Host and container `index.html` SHA-256 matched at
  `7bf79b8bd27bd03d44fd2d95b90b312e91552dcdddc7df4db465e9302d42e24e`.
- Runtime frontend image:
  `sha256:1a9d132662e3663b4c3fdb56f814bc1d821f0d472f5e8f097527a8e688e85b10`.
- Visible evidence:
  - `artifacts/runs/20260715-detail-edit-label-style/desktop-aligned.png`
    (`f190dec72f54a648d2486c8d97989bd0c3ac4e9e00b0ea298a74159b9dc64af7`).
  - `artifacts/runs/20260715-detail-edit-label-style/tablet-aligned.png`
    (`92d6116ebc12d79a1118157822115738f3e60d0db920a16e1fe31e00e0a170fa`).
  - `artifacts/runs/20260715-detail-edit-label-style/mobile-aligned.png`
    (`9e0637a54df7e598095088d1486167eb179f18e2b10dc00ed17a157789172bf1`).

## Skipped Or Downgraded Checks

- `docker compose build frontend` stopped before context transfer with
  `failed to update builder last activity time` and `operation not permitted`
  under `.docker/buildx/activity`. The validated clean `dist` was injected
  into a fresh Nginx base image and the Compose frontend was force-recreated.
- The macOS browser process is denied its Mach bootstrap handshake in the
  managed sandbox. A disposable Alpine Chromium container with CJK fonts ran
  the real frontend, login, API, computed-style, viewport, and screenshot
  acceptance.

## Risks And Follow-ups

- The 14px scale remains an intentional readability improvement over the old
  10px body size; edit-state color and weight now match the aligned read state.
- Screenshot files are ignored by Git; paths and hashes are recorded above.
- Unrelated authorization design/task edits and `docs/superpowers/` were not
  edited or staged.

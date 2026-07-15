# Detail Label Style

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits.
Keep the implementation View-first and avoid business DTO bindings.

## Scope

- Compared `detailView.jade`, the old Bootstrap/page label rules, and the
  deployed `/view112/1` read surface.
- Replaced the invented slate, 750-weight, 12px read labels with the old
  neutral color and standard bold treatment.
- Retained a readable 14px scale instead of reproducing the old page's 10px
  body size, as permitted by the visual optimization requirement.
- Changed no component, metadata, layout geometry, request, or data binding.

## Changed Files

- `frontend/src/style.css`
- `frontend/src/style.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/15/2026-07-15T07-05-45Z-detail-label-style.md`

## Validation

- `cd frontend && npm test` (21 files, 227 tests passed).
- `cd frontend && npm run build` (passed).
- `docker compose run --rm db-migrate` (passed).
- `PYTHONPATH=scripts python scripts/legacy_view_matrix.py` (118/118 passed).
- `python scripts/runtime_doctor.py` (69 checks passed after message-seed
  replay; an earlier run raced the consumable runtime message seed).
- `python scripts/check_repo_harness.py` (passed).
- MySQL baseline: admin id 1, 8 orders, and 4 order items.

## Runtime Evidence

- At 1280px, 768px, and 390px, the first label changed from
  `12px / rgb(100, 116, 139) / 750` to `14px / rgb(51, 51, 51) / 700`.
- The outer/value tracks remained 594px / 385.3px, 688px / 684px, and
  330px / 326px at the same three widths.
- Each run loaded `getmain`, `getreaditemview`, `querydatadetail`, and
  `getenums` in View-first order and emitted zero writes.
- Document widths matched all viewports; browser runtime/log events were zero.
- Host and container `index.html` SHA-256 matched at
  `f71d49f2a518a884a07675e367bb31a0dfe84ff36bf1b3e9e0171be4c5bdd19d`.
- Runtime frontend image:
  `sha256:84825d3afef3670900a7bedcc102a15f992f3384806a38f32416837346eebccc`.
- Visible evidence:
  - `artifacts/runs/20260715-detail-label-style/desktop-aligned.png`
    (`7ecced15f7e088061f3db8084210bc384c5400c0b3fb591921b85a638887937c`).
  - `artifacts/runs/20260715-detail-label-style/tablet-aligned.png`
    (`f00d7da998183f860afb280b76605e6790e975e2e796ed6f57f8b2953d79fa64`).
  - `artifacts/runs/20260715-detail-label-style/mobile-aligned.png`
    (`4fb03cce9ac553e6fb4b8c391b09b78a2bf51d2df32f986398b429068767a547`).

## Skipped Or Downgraded Checks

- `docker compose build frontend` stopped before context transfer with
  `failed to update builder last activity time` and `operation not permitted`
  under `.docker/buildx/activity`. The validated clean `dist` was injected
  into a fresh Nginx base image and the Compose frontend was force-recreated.
- The macOS browser process was denied its Mach bootstrap handshake in the
  managed sandbox. A disposable Alpine Chromium container ran the same real
  frontend, login, API, computed-style, viewport, and screenshot acceptance.

## Risks And Follow-ups

- The 14px scale is an intentional readability improvement over the old 10px
  body size; color and weight retain the old label hierarchy.
- Screenshot files are ignored by Git; paths and hashes are recorded above.
- Unrelated authorization design/task edits and `docs/superpowers/` were not
  edited or staged.

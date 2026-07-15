# Item View Placeholder

## Prompt

Continue aligning old layout, style, and interactions in atomic commits. Start
from the rendered View and avoid business DTO bindings.

## Scope

- Compared `item.jade` with the deployed `/itemview100` schema-only route.
- Restored the old literal `你好` value for each View-derived top-level field.
- Scoped the behavior to `schemaOnly`; real object detail values retain their
  metadata/data merge path.
- Added no component, View-id branch, request state, or business DTO.

## Changed Files

- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/ViewDetailPanel.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/15/2026-07-15T06-35-47Z-itemview-placeholder.md`

## Validation

- `cd frontend && npm test` (21 files, 226 tests passed).
- `cd frontend && npm run build` (passed).
- `PYTHONPATH=scripts python scripts/legacy_view_matrix.py` (118/118 passed).
- `python scripts/runtime_doctor.py` (69 checks passed after baseline replay).
- `python scripts/check_repo_harness.py` (passed).
- `git diff --check` (passed).

## Runtime Evidence

- Old `item.jade` renders every `view.Items` name beside literal `你好`.
- Deployed `/itemview100` initially rendered six metadata fields with empty
  values; after alignment, the first `Order ID` value was `你好` on desktop and
  mobile at normal weight 400.
- Item-view loading requested only `getmain` then `getreaditemview`; it emitted
  zero write requests.
- The `Items` tab remained `aria-selected=false` with zero visible child
  tables before selection.
- A same-session `/view112/1` replay retained real first value `1` and the
  View-first `getreaditemview`, `querydatadetail`, `getenums` sequence.
- Document/viewport widths matched at 1280/1280 and 390/390; browser
  runtime/log events were zero.
- MySQL retained admin id 1, 8 orders, and 4 order items after acceptance.
- Host and container `index.html` SHA-256 matched at
  `b1f4e0ecff1137e854fb80d7ab76c22404a9425e461cb20234af034b0b6efb43`.
- Runtime frontend image:
  `sha256:43800318f00ba54b508e42eb578bf358e63d20ec3206d0897f3fa5826dc0bf7c`.
- Visible evidence:
  - `artifacts/runs/20260715-itemview-placeholder/desktop-aligned.png`
    (`2202e252cf83751ebbe4b4a18dd2f59b7870ec6514c0b2ac05271299194ae51d`).
  - `artifacts/runs/20260715-itemview-placeholder/mobile-aligned.png`
    (`af976d2bd594035eec59172f57ac883f9cda9cc356dac7c280db019150cbfafc`).

## Skipped Or Downgraded Checks

- `docker compose build frontend` stopped before context transfer with
  `failed to update builder last activity time` and `operation not permitted`
  under `.docker/buildx/activity`. The validated clean `dist` was injected
  into a fresh Nginx base image and the Compose frontend was force-recreated.

## Risks And Follow-ups

- The literal is intentionally restricted to item metadata routes because it
  is template content, not object data.
- Screenshot files are ignored by Git; paths and hashes are recorded above.
- `docs/superpowers/` remains unrelated and untracked; it was not edited or
  staged.

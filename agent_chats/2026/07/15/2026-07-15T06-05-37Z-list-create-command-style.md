# List Create Command Style

## Prompt

Continue aligning old layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Audited all 114 current View operations before choosing the next gap.
- Confirmed 112 are generated create/edit navigation operations; the only two
  commands are the already-verified inert OrderList rows with no target View.
- Restored the old `view.jade` presentation for generated create commands.
- Kept the style scoped to the shared 162-line `ViewListPanel.vue` instead of
  extending the 1400-line global stylesheet or adding another component.

## Changed Files

- `frontend/src/ViewListPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/15/2026-07-15T06-05-37Z-list-create-command-style.md`

## Validation

- `cd frontend && npm test` (21 files, 222 tests passed).
- `cd frontend && npm run build` (passed).
- `PYTHONPATH=scripts python scripts/legacy_view_matrix.py` (118/118 passed).
- `python scripts/runtime_doctor.py` (69 checks passed after baseline replay).
- `python scripts/check_repo_harness.py` (passed).
- `git diff --check` (passed).

## Runtime Evidence

- Old `view.jade` uses only `.btn` for `RequireSelect=false` create commands;
  Bootstrap keeps a 1px solid transparent border, transparent background, and
  `#333` text on that surface.
- Deployed `/view123` initially rendered `新建` as
  `p-button-secondary p-button-outlined`, with a visible
  `rgb(71, 85, 105)` border and matching text.
- The aligned desktop and mobile command is
  `p-button-secondary p-button-text legacy-create-operation`, with transparent
  border/background and `rgb(51, 51, 51)` text. Its box remained 54x34px.
- Desktop loaded `getlistview(123)` before `querydata(123)`; clicking `新建`
  then loaded `getreaditemview(122)` before `initnew(122)` and opened
  `/new122`. The 390px run preserved the same sequence.
- Both runs emitted zero save or operation requests. Document/viewport widths
  matched at 1280/1280 and 390/390; browser runtime/log events were zero.
- `DB_AppDB` remained at its sole original `App_Id=1, DBNo=01, SysId=1` row.
- Host and container `index.html` SHA-256 matched at
  `1d8dd753db2d6fe3e7ed4d8e81bf4301bd3fc4b920b1a484adc483208500e0df`.
- Runtime frontend image:
  `sha256:f5f138e8a2bd187a550a13c1aa9fec13cfc0134b46a5f406b5c87ad79df78467`.
- Visible evidence:
  - `artifacts/runs/20260715-create-command/desktop-aligned.png`
    (`fde56320d8c66dda3e6f4a68f3972cd5a3ef0541af12cd2049c5e8e52a94aed8`).
  - `artifacts/runs/20260715-create-command/mobile-aligned.png`
    (`2df5982deb7b22c138622c1d70686937a6e1d5e5a5163c0081810739ed8322d3`).

## Skipped Or Downgraded Checks

- `docker compose build frontend` stopped before context transfer with
  `failed to update builder last activity time` and `operation not permitted`
  under `.docker/buildx/activity`. The validated clean `dist` was injected
  into the existing Nginx image and the Compose frontend was force-recreated.

## Risks And Follow-ups

- The first post-browser runtime-doctor replay found the one-shot message seed
  consumed. Replaying the idempotent `db-migrate` catalog restored the standard
  baseline, after which all 69 checks passed.
- Screenshot files are ignored by Git; paths and hashes are recorded above.
- `docs/superpowers/` remains unrelated and untracked; it was not edited or
  staged.

# Inline Child Edit Acceptance

## Prompt

Continue aligning old layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Closed the browser-proof gap for `detailview.js` inline child Edit/Save.
- Compared the old child-row event boundary before following runtime data.
- Reused Order detail View 102 and child list View 101 through a reversible
  metadata fixture rather than adding a page-specific Vue branch.
- Verified local row staging and parent-owned persistence on desktop/mobile.

## Changed Files

- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/15/2026-07-15T05-47-57Z-inline-child-edit-acceptance.md`

## Validation

- Authorized browser acceptance with local CAPTCHA and `admin/admin`.
- `python scripts/runtime_doctor.py` (69 checks passed after restoration).
- `python scripts/check_repo_harness.py` (passed).
- Application and test source were unchanged by this slice, so frontend and
  Maven suites were not rerun.

## Runtime Evidence

- Baseline metadata was `1102:0:NULL:NULL:NULL` and
  `1204:0:101:101:101` for item-name and collection View rows.
- Temporarily made item name editable, cleared only the collection edit/select
  pointers, and retained child list View 101 as the column owner.
- Created dedicated order `9915071502` and child `9915071521` with initial name
  `CDX-INLINE-BEFORE`.
- Desktop row Save staged `CDX-INLINE-DESKTOP` with zero API/write requests;
  MySQL still held the initial value until one parent `saveobj` persisted it.
- Mobile row Save staged `CDX-INLINE-MOBILE` with zero API/write requests;
  MySQL still held the desktop value until one parent `saveobj` persisted it.
- Both parent payloads contained child id `9915071521` and the corresponding
  staged name in the View-derived updated-item list.
- Both runs requested `getreaditemview` before `querydatadetail`; document and
  viewport widths matched at 1280/1280 and 390/390.
- Browser runtime/log event count was zero.
- Cleanup restored both metadata rows exactly and removed both dedicated data
  rows; the final dedicated-row count was zero.
- Visible evidence:
  - `artifacts/runs/20260715-inline-child-edit/desktop-staged-edit.png`
    (`94ad19ea435c1dd483c9ed5283c36b2fc79baf6059d26b8d64cf919856760256`).
  - `artifacts/runs/20260715-inline-child-edit/mobile-staged-edit.png`
    (`27bbc7b58fb2ecc4757ee99749c772f341b1e7b09db20a5c2d34d5b5ed564407`).

## Risks And Follow-ups

- Several preliminary harness attempts stopped on DOM timing/locator issues or
  the concurrent Compose rebuild. Cleanup was verified after each attempt; the
  final guarded run completed with zero browser errors and exact restoration.
- Screenshot files are ignored by Git; paths and hashes are recorded above.
- `docs/superpowers/` remains unrelated and untracked; it was not edited or
  staged.

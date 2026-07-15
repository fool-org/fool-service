# Menu Image Acceptance

## Prompt

Continue aligning old layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Closed the explicit browser follow-up for old `AUTH_MENU_IMAGE` metadata.
- Reused the existing Docker parent `Views` / child `OrderList` hierarchy.
- Used two original FoolFrame 32x32 PNG files as reversible runtime fixtures.
- Verified the same shared Vue menu component on desktop and mobile.

## Changed Files

- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/15/2026-07-15T04-34-59Z-menu-image-acceptance.md`

## Validation

- Authorized browser acceptance with local CAPTCHA and `admin/admin`.
- `python scripts/runtime_doctor.py` (69 checks passed after restoration).
- `python scripts/check_repo_harness.py` (passed).
- Application and test source were unchanged, so frontend and Maven suites
  were not rerun.

## Runtime Evidence

- Baseline menu ids 1/2 both had `AUTH_MENU_IMAGE=NULL`.
- Temporarily copied old `02.png` / `06.png` into the running Nginx document
  root and pointed only those two menu rows to the temporary URLs.
- Desktop showed `Views` and nested `OrderList` images at 30x30; both source
  assets loaded at their native 32x32 size.
- Mobile showed the same parent/child images in the open Drawer. The stable
  Drawer occupied x=0..300 and the document/viewport width was 390/390.
- Clicking mobile `OrderList` closed the Drawer, navigated to `/view100`, and
  requested `getlistview` before `querydata`.
- Browser runtime/log event count was zero.
- Cleanup restored menu ids 1/2 to `AUTH_MENU_IMAGE=NULL` and removed both
  temporary Nginx files; a final container search returned no matching files.
- Visible evidence:
  - `artifacts/runs/20260715-menu-images/desktop-parent-child-images.png`
    (`5e4a834bd0db0e9ad43ae9975b18546c2abae33a5756d49b68a31f22af466310`).
  - `artifacts/runs/20260715-menu-images/mobile-parent-child-images.png`
    (`cda6688b3114e4f1caba782ace62a26e5494141bb623aa25a7d0a152cf60aba5`).

## Risks And Follow-ups

- Three preliminary CDP attempts stopped on test-harness assertions: whole
  button text, a click return value, and a DOM-node wait result. Each attempt
  ran the same cleanup trap; the final baseline check proves no metadata or
  container-file drift remained.
- Screenshot files are ignored by Git; paths and hashes are recorded above.
- `docs/superpowers/` remains unrelated and untracked; it was not edited or
  staged.

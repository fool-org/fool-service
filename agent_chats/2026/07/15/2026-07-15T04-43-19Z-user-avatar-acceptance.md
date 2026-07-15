# User Avatar Acceptance

## Prompt

Continue aligning old layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Closed the deferred browser follow-up for a non-empty `UserAvtarUrl`.
- Compared FoolFrame Jade/CSS rendering before following runtime user data.
- Used one original FoolFrame 32x32 PNG as a reversible runtime fixture.
- Verified the existing shared Vue shell on desktop and mobile.

## Changed Files

- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/15/2026-07-15T04-43-19Z-user-avatar-acceptance.md`

## Validation

- Authorized browser acceptance with local CAPTCHA and `admin/admin`.
- `python scripts/runtime_doctor.py` (69 checks passed after restoration).
- `python scripts/check_repo_harness.py` (passed).
- Application and test source were unchanged, so frontend and Maven suites
  were not rerun.

## Runtime Evidence

- Admin id 1 started with `SW_AUTH_USER.USER_AVTAR=NULL`.
- Temporarily copied old `02.png` into the running Nginx document root and
  pointed only admin id 1 to `/legacy-user-avatar.png`.
- Desktop loaded the native 32x32 image at a 50x50 circular display box,
  retained `Admin`, and kept the header within a 1280px document/viewport.
- Mobile loaded the same image at 50x50 with `border-radius: 50%`, retained
  `Admin`, and kept the header within a 390px document/viewport.
- Browser runtime/log event count was zero.
- Cleanup restored `USER_AVTAR=NULL` and removed the temporary Nginx file.
- Visible evidence:
  - `artifacts/runs/20260715-user-avatar/desktop-avatar.png`
    (`62a3e350ba4a1761001102bf992a9a4e08a228c9f75de6f711085f2b53d8b696`).
  - `artifacts/runs/20260715-user-avatar/mobile-avatar.png`
    (`8f2e078fcdd40576b17c44c4060c3b3cb92d5f22a5a0c241b412f7575cb9cd95`).

## Risks And Follow-ups

- Screenshot files are ignored by Git; paths and hashes are recorded above.
- The temporary source image is a menu glyph rather than a portrait, but the
  browser exercised the identical non-empty URL, intrinsic-size, layout, and
  circular rendering path required by the old templates.
- `docs/superpowers/` remains unrelated and untracked; it was not edited or
  staged.

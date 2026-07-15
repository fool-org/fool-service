# Boolean Editor Acceptance

## Prompt

Continue aligning old layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Closed the real-browser proof gap for `setextype.js`'s type-8 editor.
- Followed imported Model detail View 146 to object 181 and its stored data.
- Verified the existing shared metadata editor without adding a fixture,
  page-specific Vue branch, or concrete business DTO.

## Changed Files

- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/15/2026-07-15T05-54-09Z-boolean-editor-acceptance.md`

## Validation

- Authorized browser acceptance with local CAPTCHA and `admin/admin`.
- `python scripts/runtime_doctor.py` (69 checks passed).
- `python scripts/check_repo_harness.py` (passed).
- Application and test source were unchanged by this slice, so frontend and
  Maven suites were not rerun.

## Runtime Evidence

- Old `setextype.js` renders one checkbox for type 8, and `savetext.js` reads
  its checked state rather than displaying a separate Boolean value.
- `/view146/181` requested `getreaditemview` before `querydatadetail` on both
  desktop and mobile.
- `autoSysId=true` rendered exactly one checked checkbox; its editor wrapper
  had no visible `是/否` or other text.
- Clicking the control changed only local state to unchecked and emitted zero
  API requests and zero write requests.
- Mobile reload restored the checked state from persisted data.
- `SW_SYS_MODEL.MODEL_AUTOID` remained `1` before and after acceptance.
- Document and viewport widths matched at 1280/1280 and 390/390. Browser
  runtime/log event count was zero.
- Visible evidence:
  - `artifacts/runs/20260715-boolean-editor/desktop-checked.png`
    (`8f327096828d631ca95d15c92e224bcd157ffecf079d90d6c3204e84b5ba4465`).
  - `artifacts/runs/20260715-boolean-editor/desktop-unchecked.png`
    (`a4060f26fd53db68ae961ed316c665ffd1d46ac27ed9d05ddfc5e08ecc8e1de2`).
  - `artifacts/runs/20260715-boolean-editor/mobile-checked.png`
    (`a377120b5d6a1fce6c02662f172b1314e51b869788407a0b5abb5e3b080e81d9`).
  - `artifacts/runs/20260715-boolean-editor/mobile-unchecked.png`
    (`d126316694d704bd629b2f92cfc2a2450cb34890f3feaa187b841a7d167f3a1b`).

## Risks And Follow-ups

- The mobile unchecked screenshot ends at the field label; DOM geometry and
  checked-state assertions still cover the same control after the click, and
  the mobile checked screenshot visibly covers its rendered presentation.
- Screenshot files are ignored by Git; paths and hashes are recorded above.
- `docs/superpowers/` remains unrelated and untracked; it was not edited or
  staged.

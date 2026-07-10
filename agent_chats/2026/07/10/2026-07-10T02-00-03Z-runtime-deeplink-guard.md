# Delivery Evidence: Runtime Deep-Link Guard

## Prompt

Continue the Docker/FoolFrame/Vue migration goal with atomic commits, reuse,
and View-first rendering.

## Scope

- Compared old FoolFrame Web GET routes in `../FoolFrame/src/Web/app.js`.
- Extended `scripts/runtime_doctor.py` to prove the frontend container serves
  the built Vue bundle for:
  - `/main`
  - `/view100`
  - `/view100/1001`
  - `/itemview100`
  - `/new100`
  - `/new100/1001&100&items`
- Added one focused runtime doctor helper test.

## Changed Files

- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/10/2026-07-10T02-00-03Z-runtime-deeplink-guard.md`

## Validation

- `python scripts/runtime_doctor_test.py`
  - 44 tests passed.
- `python scripts/check_repo_harness.py`
  - Repository harness validation passed.
- `git diff --check`
  - Passed with no whitespace errors.
- `python scripts/runtime_doctor.py`
  - All Docker/runtime smoke checks passed, including:
    - `frontend:main-path`
    - `frontend:view-path`
    - `frontend:view-detail-path`
    - `frontend:itemview-path`
    - `frontend:new-path`
    - `frontend:new-owner-path`

## Skipped Checks

- No checks skipped for this runtime-doctor slice.

## Risks

- This guard proves Docker/nginx serves the Vue bundle at the legacy GET
  paths. It does not replace browser-level interaction checks inside each
  loaded page.

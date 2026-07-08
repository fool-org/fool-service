# Runtime auth shell smoke

## Prompt

Continue the Docker/Vue/FoolFrame migration and keep commits atomic.

## Scope

- Extended `scripts/runtime_doctor.py` to carry the `loginv2` token through
  the logged-in legacy shell routes behind the Vue proxy:
  - `getapp`
  - raw-token `getmain`
  - top-menu-driven `getsubmenu`
  - `logout`
- Updated `tasks.md` and `docs/migration/foolframe-parity.md`.

## Validation

- `python scripts/runtime_doctor.py`
  - Passed, including `auth:getapp`, `auth:getmain`, `auth:getsubmenu`, and
    `auth:logout` through `http://localhost:8081`.
- `python scripts/runtime_doctor_test.py`
  - Passed: 9 tests.

## Risks

- This proves the Docker-seeded auth shell path. Full `loginv2` app-session
  selection remains tracked as broader future token-context work.

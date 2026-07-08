# Runtime auth smoke

## Prompt

Continue the Docker/Vue/FoolFrame migration and keep progress atomic.

## Scope

- Extended `scripts/runtime_doctor.py` to prove the legacy auth first-hop path
  through the Vue proxy:
  - `initapp`
  - `getcheckcode` / `checkcode`
  - `loginv2`
  - `getuserinfo`
- Added helper tests for boolean success responses and legacy login token
  extraction.
- Updated `tasks.md` and `docs/migration/foolframe-parity.md`.

## Validation

- `python scripts/runtime_doctor_test.py`
  - Passed: 9 tests.
- `python scripts/runtime_doctor.py`
  - Passed, including new `auth:initapp`, `auth:checkcode`, `auth:loginv2`,
    and `auth:getuserinfo` checks through `http://localhost:8081`.

## Risks

- This only proves the seeded Docker admin legacy auth chain. It does not claim
  full app-session selection parity beyond the existing migrated endpoints.

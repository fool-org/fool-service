# Runtime doctor report save check

## Prompt

Continue the FoolFrame migration, keep the Docker/Vue goal moving, and avoid
business DTO binding or oversized frontend edits.

## Scope

- Extended `scripts/runtime_doctor.py` with a `report:saverpt` check through
  the Vue frontend proxy.
- Added a small helper for legacy void success responses, because
  `/saverpt` intentionally returns the FoolFrame no-op success surface rather
  than a data object.
- Updated `tasks.md` and `docs/migration/foolframe-parity.md`.

## Validation

- `python scripts/runtime_doctor_test.py`
  - Passed: 6 tests.
- `python scripts/check_repo_harness.py`
  - Passed.
- `python scripts/runtime_doctor.py`
  - Passed, including `report:saverpt`.

## Risks

- This does not implement saved-report persistence. FoolFrame
  `HandlerSaveReport` is a no-op success surface; persistence/execution/export
  remains separate report migration work.

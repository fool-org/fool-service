# Delivery Evidence: Docker Seed Marker Guard

## Prompt

Continue the Docker/FoolFrame/Vue migration goal with atomic commits, reuse,
and maximum reuse.

## Scope

- Extended the existing Docker init schema harness to guard key seed markers
  needed by the current Vue/runtime smoke workflow.
- Covered app shell, admin/menu seed, Order list/Sudoku views, query catalogs,
  event definition, and BTC order row markers.
- Added one focused harness test for a missing required seed marker.

## Changed Files

- `scripts/check_repo_harness.py`
- `scripts/check_repo_harness_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/10/2026-07-10T02-07-19Z-docker-seed-marker-guard.md`

## Validation

- `python scripts/check_repo_harness_test.py`
  - 9 tests passed.
- `python scripts/check_repo_harness.py`
  - Repository harness validation passed.
- `git diff --check`
  - Passed with no whitespace errors.

## Skipped Checks

- Runtime Docker smoke was not rerun for this harness-only guard; the changed
  logic does not alter runtime code or SQL contents.

## Risks

- This guard protects selected smoke-critical seed markers. It does not claim
  the full `car_wash` production migration story is complete.

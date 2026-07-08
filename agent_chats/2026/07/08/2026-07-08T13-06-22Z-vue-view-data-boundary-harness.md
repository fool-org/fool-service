# Vue View Data Boundary Harness

## Prompt

The user pointed out that the migrated frontend must render from View metadata
first, query data by that View, and avoid binding page rendering to concrete
business DTOs.

## Scope

- Added a repository harness check for Vue render paths.
- Kept runtime Vue files unchanged; this is a regression guard for the existing
  metadata-driven render path.

## Changed Files

- `scripts/check_repo_harness.py`
- `scripts/check_repo_harness_test.py`
- `docs/standards/STD-HARNESS-001.md`
- `docs/validation.md`
- `tasks.md`
- `docs/migration/foolframe-parity.md`

## Validation

- RED: `python scripts/check_repo_harness_test.py`
  - Failed before implementation because `check_vue_view_data_boundaries` did
    not exist.
- PASS: `python scripts/check_repo_harness_test.py`
- PASS: `python scripts/check_repo_harness.py`

## Risks

- The guard is intentionally narrow: it blocks direct `row.values` /
  `Object.keys(first)` use in main Vue render files, while allowing shared
  helper tests and data-normalization helpers to keep fixture payloads.

## Follow-ups

- Replace the Docker smoke `Order` seed with imported target metadata when a
  stable FH_JAVA/FoolFrame source-of-truth export is selected.

# Prompt

Continue the Docker/FoolFrame/Vue migration goal and keep validation tied to
the real runtime.

# Scope

- Move the manual `getlistview -> getreaditemview` DetailViews probe into the
  Docker runtime doctor.
- Keep the check generic: validate child View metadata exists, not business
  DTO fields.

# Changes

- `scripts/runtime_doctor.py` now accepts Pascal `DetailViewId`.
- Added a runtime check that loads the detail View id from `getlistview(100)`,
  calls `getreaditemview(detailViewId)`, and fails unless `DetailViews` has
  nested child item metadata.
- Updated migration/task notes.

# Validation

- `python scripts/runtime_doctor.py`
  - Passed, including `view:getreaditemview-detailviews`.
- `python scripts/check_repo_harness.py`
  - Passed.
- `git diff --check`
  - Passed.

# Risks

- The doctor intentionally checks the seeded Docker `OrderList` workflow only.
  Broader View fixtures should add their own runtime probes when seeded.

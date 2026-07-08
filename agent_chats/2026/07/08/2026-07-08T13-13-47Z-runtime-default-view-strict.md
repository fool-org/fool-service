# Runtime Default View Strictness

## Prompt

The user called out that the migrated flow must render the page from View
metadata first, then load data from that View, and that binding the workflow to
a concrete business DTO is wrong.

## Scope

- Removed the Docker runtime doctor's fixed seeded list View fallback.
- Added a regression test proving runtime checks do not call
  `getlistview(100)` when `getapp/getmain` fail to provide `App.DefaultViewId`.

## Changed Files

- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `tasks.md`
- `docs/migration/foolframe-parity.md`

## Validation

- RED: `python scripts/runtime_doctor_test.py -k fallback`
  - Failed because `view:getlistview` still passed through the seeded
    `ViewId=100` fallback.
- PASS: `python scripts/runtime_doctor_test.py`
- PASS: `python scripts/check_repo_harness.py`

## Risks

- This tightens runtime evidence only. It does not replace the seeded order
  metadata itself with FH_JAVA/FoolFrame imported metadata.

## Follow-ups

- Continue replacing seed/demo assumptions with imported configuration or
  metadata-driven runtime paths.

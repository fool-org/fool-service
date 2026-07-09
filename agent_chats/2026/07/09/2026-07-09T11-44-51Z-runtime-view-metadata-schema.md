# Runtime View Metadata Schema

## Prompt

Continue the Docker/Vue FoolFrame migration with atomic commits and maximum
reuse.

## Scope

- Rechecked the apparent Java annotation/schema misses and kept them out of
  parent tables because they are collection child-table owner columns.
- Extended the existing runtime schema catalog to guard the modern
  `fool_sys_model`, `fool_sys_model_property`, `fool_sys_view`, and
  `fool_sys_view_item` columns used by the Vue View-first workflow.
- Reused the existing Docker init schema harness and runtime doctor instead of
  adding a new Java annotation parser.

## Changed Files

- `scripts/runtime_schema.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T11-44-51Z-runtime-view-metadata-schema.md`

## Validation

- GREEN: `python scripts/runtime_doctor_test.py`
- GREEN: `python scripts/check_repo_harness.py`
- GREEN: one-off Docker init coverage check for every
  `LEGACY_CORE_SCHEMA_COLUMNS` entry
- GREEN: `python scripts/runtime_doctor.py`

## Skipped Checks

- Full Maven and frontend test suites were not rerun; this change only extends
  Python schema catalogs and validation evidence.

## Risks

- This does not attempt complete Java annotation-to-SQL verification; it keeps
  the contract on runtime columns already used by Docker/Vue smoke paths.

# 2026-07-09 Runtime GetEnums View Model

## Prompt

- Continue the Docker/FoolFrame/Vue migration goal.
- Keep Vue and runtime proof tied to View metadata instead of seeded business
  DTO defaults.

## Scope

- Added a runtime-doctor helper that finds enum model ids from loaded View
  metadata, reusing the same property type/model extraction shape as the
  BusinessObject lookup helper.
- Extended Docker runtime smoke to call legacy `/api/v1/data/getenums` through
  the Vue proxy using the loaded View enum model id.
- No backend or Vue behavior changed; this is runtime proof coverage for an
  already migrated endpoint.

## Changed Files

- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T10-16-12Z-runtime-getenums-view-model.md`

## Red Tests

- `python scripts/runtime_doctor_test.py` failed first because
  `enum_view_model_id` did not exist.
- `python scripts/runtime_doctor_test.py` then failed because
  `api_checks` did not include `data:getenums`.

## Validation

- `python scripts/runtime_doctor_test.py` passed.
- `python scripts/runtime_doctor.py` passed, including Docker compose,
  FH_JAVA `market_symbols`, legacy core schema, auth shell, View/data,
  `getenums`, report, message, and logout checks.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.

## Skipped Checks

- No Maven or frontend test run: this slice only extends runtime doctor
  coverage and migration evidence.

## Risks

- The runtime check requires the default loaded View to expose at least one
  enum field with model metadata.

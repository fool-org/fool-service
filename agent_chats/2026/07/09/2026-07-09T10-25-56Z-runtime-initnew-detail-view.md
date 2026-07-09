# 2026-07-09 Runtime InitNew Detail View

## Prompt

- Continue the Docker/FoolFrame/Vue migration goal.
- Keep runtime proof tied to loaded View metadata instead of seeded View ids.

## Scope

- Extended the Docker runtime doctor to call legacy `/api/v1/data/initnew`
  through the Vue proxy using the loaded `DetailViewId`.
- Reused the existing detail-response check and made it accept both camel
  `data.simpleData` and FoolFrame Pascal `Data.SimpleData` aliases.
- No backend or Vue behavior changed; this is runtime proof coverage for an
  already migrated endpoint.

## Changed Files

- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T10-25-56Z-runtime-initnew-detail-view.md`

## Red Test

- `python scripts/runtime_doctor_test.py` failed first because `api_checks`
  did not include `data:initnew`.

## Validation

- `python scripts/runtime_doctor_test.py` passed.
- `python scripts/runtime_doctor.py` passed, including Docker compose,
  FH_JAVA `market_symbols`, legacy core schema, auth shell, View/data,
  `getenums`, `initnew`, report, message, and logout checks.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.

## Skipped Checks

- No Maven or frontend test run: this slice only extends runtime doctor
  coverage and migration evidence.

## Risks

- The runtime check depends on the default app shell resolving a detail View
  that can initialize empty detail data.

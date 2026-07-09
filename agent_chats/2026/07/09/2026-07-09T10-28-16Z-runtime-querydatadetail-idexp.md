# 2026-07-09 Runtime QueryDataDetail IdExp

## Prompt

- Continue the Docker/FoolFrame/Vue migration goal.
- Keep runtime proof tied to loaded View metadata and legacy protocol fields.

## Scope

- Extended the Docker runtime doctor to call legacy `/api/v1/data/querydatadetail`
  through the Vue proxy using loaded `DetailViewId`, blank `ObjId`, and a
  legacy `$<row id>` `IdExp` derived from the row returned by `querydata`.
- Reused the existing detail-response check.
- No backend or Vue behavior changed; this is runtime proof coverage for an
  already migrated protocol branch.

## Changed Files

- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T10-28-16Z-runtime-querydatadetail-idexp.md`

## Red Test

- `python scripts/runtime_doctor_test.py` failed first because `api_checks`
  did not include `data:querydatadetail-idexp`.

## Validation

- `python scripts/runtime_doctor_test.py` passed.
- `python scripts/runtime_doctor.py` passed, including Docker compose,
  FH_JAVA `market_symbols`, legacy core schema, auth shell, View/data,
  `querydatadetail.IdExp`, report, message, and logout checks.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.

## Skipped Checks

- No Maven or frontend test run: this slice only extends runtime doctor
  coverage and migration evidence.

## Risks

- The runtime check depends on `querydata` returning a row id before the
  IdExp detail request runs.

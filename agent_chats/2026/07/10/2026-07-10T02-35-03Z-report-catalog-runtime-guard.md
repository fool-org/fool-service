# Report Catalog Runtime Guard

## Prompt
- Continue the Docker/FoolFrame/Vue migration with atomic commits and maximum
  reuse.

## Scope
- Reused the existing runtime doctor report model path.
- Required `getmkqview` and legacy `mkqview` runtime checks to see at least one
  report column with both compare and query catalog options.
- Added focused helper coverage and updated migration/task evidence.

## Changed Files
- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/10/2026-07-10T02-35-03Z-report-catalog-runtime-guard.md`

## Validation
- `python scripts/runtime_doctor_test.py` passed: 46 tests.
- `python scripts/runtime_doctor.py` passed against the running Docker stack.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.

## Runtime Evidence
- Before this patch, `http://localhost:8081/api/v1/report/getmkqview` and
  `/api/v1/report/mkqview` returned non-empty `CompareTypes` / `QueryTypes`;
  the runtime doctor did not require that surface yet.

## Risks
- This is a runtime guard-only slice; it does not change report runtime
  behavior.

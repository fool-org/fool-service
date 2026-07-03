# Runtime Doctor

## Prompt

Continue the Docker/FoolFrame/Vue migration and keep commits atomic. The
current slice makes Docker runtime validation repeatable now that the View-first
Vue workflow is stable enough for smoke automation.

## Scope

- Added `scripts/runtime_doctor.py`, a stdlib Python smoke command for the
  running Docker stack.
- The doctor checks compose service state plus backend `/test` and
  frontend-proxied View workflow APIs:
  `getlistview(ViewId)`, `querydata(ViewId)`, `inputquery(ViewId)`, and
  `getmkqview(ViewId)`.
- Added focused helper tests in `scripts/runtime_doctor_test.py`.
- Updated validation, standards, task state, and migration parity docs to make
  the doctor the documented runtime smoke command.

## Changed Files

- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/validation.md`
- `docs/standards/README.md`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/04/2026-07-03T21-08-03Z-runtime-doctor.md`

## Validation

- `python3 scripts/runtime_doctor_test.py`
- `python3 scripts/check_repo_harness.py`
- `python3 scripts/runtime_doctor.py`

## Runtime Evidence

- `python3 scripts/runtime_doctor.py` returned `PASS` for:
  `compose:backend`, `compose:frontend`, `compose:mysql`, `compose:redis`,
  `backend:test`, `view:getlistview`, `data:querydata`, `data:inputquery`,
  and `report:getmkqview`.

## Risks

- The doctor assumes the Docker seed workflow with `ViewId=100` is present.
  It intentionally does not replace Maven, frontend unit/type checks, or
  broader browser QA.
